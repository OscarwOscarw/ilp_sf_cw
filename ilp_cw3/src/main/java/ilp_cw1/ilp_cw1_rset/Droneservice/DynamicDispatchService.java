package ilp_cw1.ilp_cw1_rset.Droneservice;

import data.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class DynamicDispatchService {

    private final Map<String, DroneSimulationState> droneSimulationStates = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduler;
    private volatile boolean isSimulationRunning = false;
    private List<RestrictedArea> restrictedAreas;
    private final Map<String, List<MedDispatchRec>> droneToOriginalTasks = new ConcurrentHashMap<>();
    private final Object simulationLock = new Object();
    private final droneService droneService;

    public DynamicDispatchService(droneService droneService) {
        this.droneService = droneService;
        this.restrictedAreas = Collections.unmodifiableList(
                Optional.ofNullable(droneService.getRestrictedAreas()).orElse(Collections.emptyList())
        );
        this.scheduler = Executors.newScheduledThreadPool(1);
        System.out.println("✅ DynamicDispatchService initialized, restricted areas count: " + restrictedAreas.size());
    }

    public void refreshRestrictedAreas() {
        try {
            List<RestrictedArea> areas = droneService.getRestrictedAreas();
            this.restrictedAreas = Collections.unmodifiableList(
                    areas != null ? areas : Collections.emptyList()
            );
            System.out.println("🔄 Restricted areas data refreshed, current count: " + restrictedAreas.size());
        } catch (Exception e) {
            System.err.println("❌ Failed to refresh restricted areas data: " + e.getMessage());
            this.restrictedAreas = Collections.unmodifiableList(Collections.emptyList());
        }
    }

    /**
     * Start simulation with idempotent calls
     */
    public void startSimulation(DeliveryPathResponse pathResponse) {
        if (pathResponse == null || pathResponse.getDronePaths().isEmpty()) {
            throw new IllegalArgumentException("Invalid path response: drone paths cannot be empty");
        }

        synchronized (simulationLock) {
            stopSimulation();

            droneSimulationStates.clear();
            droneToOriginalTasks.clear();

            for (DeliveryPathResponse.DronePath dronePath : pathResponse.getDronePaths()) {
                String droneId = dronePath.getDroneId();
                if (droneSimulationStates.containsKey(droneId)) {
                    continue;
                }

                DroneSimulationState state = new DroneSimulationState(dronePath);
                droneSimulationStates.put(droneId, state);

                List<MedDispatchRec> originalTasks = dronePath.getDeliveries().stream()
                        .filter(delivery -> delivery.getDeliveryId() != null)
                        .map(delivery -> {
                            MedDispatchRec task = new MedDispatchRec();
                            task.setId(Integer.parseInt(delivery.getDeliveryId().toString()));

                            MedDispatchRec.Requirements requirements = Optional.ofNullable(task.getRequirements())
                                    .orElse(new MedDispatchRec.Requirements());
                            task.setRequirements(requirements);

                            MedDispatchRec.Delivery taskDelivery = new MedDispatchRec.Delivery();
                            if (!delivery.getFlightPath().isEmpty()) {
                                PositionDto lastPoint = delivery.getFlightPath().get(delivery.getFlightPath().size() - 1);
                                taskDelivery.setLng(lastPoint.getLng());
                                taskDelivery.setLat(lastPoint.getLat());
                            }
                            task.setDelivery(taskDelivery);
                            return task;
                        })
                        .collect(Collectors.toList());

                droneToOriginalTasks.put(droneId, originalTasks);
                System.out.println("Initialized drone: " + droneId + ", task count: " + originalTasks.size());
            }

            isSimulationRunning = true;
            scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(this::updateAllDronePositions, 0, 200, TimeUnit.MILLISECONDS);
            System.out.println("=== Dynamic simulation started ===");
        }
    }

    /**
     * Stop simulation and ensure thread pool is properly closed
     */
    public void stopSimulation() {
        synchronized (simulationLock) {
            if (!isSimulationRunning) return;

            isSimulationRunning = false;
            if (scheduler != null) {
                scheduler.shutdown();
                try {
                    if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                        System.out.println("Thread pool failed to shutdown gracefully within 1 second, forcing shutdown");
                        scheduler.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    scheduler.shutdownNow();
                    Thread.currentThread().interrupt();
                }
                scheduler = null;
            }
            System.out.println("=== Dynamic simulation stopped ===");
        }
    }

    /**
     * Get current task status for all drones
     */
    public Map<Drone, List<MedDispatchRec>> getOngoingTasks() {
        Map<Drone, List<MedDispatchRec>> result = new HashMap<>();

        for (Map.Entry<String, DroneSimulationState> entry : droneSimulationStates.entrySet()) {
            String droneId = entry.getKey();
            DroneSimulationState state = entry.getValue();

            Drone drone = new Drone();
            drone.setId(droneId);

            List<MedDispatchRec> originalTasks = droneToOriginalTasks.getOrDefault(droneId, Collections.emptyList());
            List<MedDispatchRec> currentTasks = new ArrayList<>(originalTasks);

            if (state.isProcessingEmergency && state.currentEmergencyTaskId != null) {
                MedDispatchRec emergencyTask = new MedDispatchRec();
                emergencyTask.setId(state.currentEmergencyTaskId);
                emergencyTask.setRequirements(new MedDispatchRec.Requirements());

                MedDispatchRec.Delivery delivery = new MedDispatchRec.Delivery();
                if (state.getCurrentPosition() != null) {
                    delivery.setLng(state.getCurrentPosition().getLng());
                    delivery.setLat(state.getCurrentPosition().getLat());
                }
                emergencyTask.setDelivery(delivery);

                currentTasks.add(0, emergencyTask);
            }

            result.put(drone, currentTasks);
        }

        return result;
    }

    /**
     * Estimate interrupt cost for a drone
     */
    public double estimateInterruptCost(String droneId) {
        DroneSimulationState state = droneSimulationStates.get(droneId);
        return state != null ? state.calculateRemainingPathRatio() : 0.0;
    }

    /**
     * Get current status of a drone
     */
    public DroneStatusDto getCurrentDroneStatus(String droneId) {
        DroneSimulationState state = droneSimulationStates.get(droneId);
        if (state == null) return null;

        return new DroneStatusDto(
                droneId,
                state.getCurrentPosition(),
                state.getStatus(),
                state.getCurrentTaskId(),
                state.getTotalTasks(),
                state.getCompletedTasksCount(),
                state.isProcessingEmergency(),
                state.getCurrentEmergencyTaskId()
        );
    }

    /**
     * Get current status of all drones
     */
    public List<DroneStatusDto> getAllCurrentDroneStatuses() {
        return droneSimulationStates.values().stream()
                .map(state -> new DroneStatusDto(
                        state.getDroneId(),
                        state.getCurrentPosition(),
                        state.getStatus(),
                        state.getCurrentTaskId(),
                        state.getTotalTasks(),
                        state.getCompletedTasksCount(),
                        state.isProcessingEmergency(),
                        state.getCurrentEmergencyTaskId()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Insert emergency task to specified drone
     */
    public void insertEmergencyTask(String droneId, DeliveryPathResponse.Delivery emergencyTask) {
        if (emergencyTask == null || emergencyTask.getDeliveryId() == null) {
            throw new IllegalArgumentException("Emergency task or task ID cannot be empty");
        }

        DroneSimulationState state = droneSimulationStates.get(droneId);
        if (state == null) {
            throw new NoSuchElementException("Drone " + droneId + " does not exist");
        }

        System.out.println("⚠️ Emergency task skips restricted area check, allowed to execute");

        System.out.println("\n=============================================");
        System.out.println("=== Inserting emergency task " + emergencyTask.getDeliveryId() + " for drone " + droneId + " ===");
        System.out.println("=============================================");

        state.insertEmergencyTask(emergencyTask);
    }

    /**
     * Periodically update all drone positions
     */
    private void updateAllDronePositions() {
        if (!isSimulationRunning) return;

        boolean allCompleted = true;
        for (DroneSimulationState state : droneSimulationStates.values()) {
            if (state.getStatus() != DroneStatus.COMPLETED) {
                state.updatePosition();
                allCompleted = false;
            }
        }

        if (allCompleted) {
            System.out.println("\n=== All drone tasks completed, simulation will stop automatically ===");
            stopSimulation();
        }
    }

    /**
     * Check if position is within restricted area
     */
    private boolean isInRestrictedArea(PositionDto position) {
        return restrictedAreas.stream()
                .anyMatch(area -> isPointInPolygon(position, area.getVertices()));
    }

    /**
     * Point in polygon detection (ray casting algorithm)
     */
    private boolean isPointInPolygon(PositionDto point, List<PositionDto> vertices) {
        if (vertices == null || vertices.size() < 3) return false;

        int n = vertices.size();
        boolean inside = false;
        for (int i = 0, j = n - 1; i < n; j = i++) {
            PositionDto vi = vertices.get(i);
            PositionDto vj = vertices.get(j);

            if (isPointOnSegment(point, vi, vj)) {
                return true;
            }

            if ((vi.getLat() > point.getLat()) != (vj.getLat() > point.getLat())) {
                double slope = (vj.getLng() - vi.getLng()) / (vj.getLat() - vi.getLat());
                double xIntersect = vi.getLng() + (point.getLat() - vi.getLat()) * slope;
                if (point.getLng() < xIntersect) {
                    inside = !inside;
                }
            }
        }
        return inside;
    }

    /**
     * Check if point is on line segment
     */
    private boolean isPointOnSegment(PositionDto p, PositionDto a, PositionDto b) {
        if (Math.min(a.getLng(), b.getLng()) - 1e-9 <= p.getLng() && p.getLng() <= Math.max(a.getLng(), b.getLng()) + 1e-9
                && Math.min(a.getLat(), b.getLat()) - 1e-9 <= p.getLat() && p.getLat() <= Math.max(a.getLat(), b.getLat()) + 1e-9) {
            double crossProduct = (p.getLng() - a.getLng()) * (b.getLat() - a.getLat())
                    - (p.getLat() - a.getLat()) * (b.getLng() - a.getLng());
            return Math.abs(crossProduct) < 1e-9;
        }
        return false;
    }

    public boolean isSimulationRunning() {
        return isSimulationRunning;
    }

    /**
     * Add simulation state for idle drone
     */
    public void addDroneState(String droneId, DeliveryPathResponse.DronePath dronePath) {
        synchronized (simulationLock) {
            if (droneSimulationStates.containsKey(droneId)) {
                System.out.println("Drone " + droneId + " already exists in simulation system, no need to add again");
                return;
            }

            DroneSimulationState state = new DroneSimulationState(dronePath);
            droneSimulationStates.put(droneId, state);

            droneToOriginalTasks.put(droneId, new ArrayList<>());
            System.out.println("Dynamically added drone: " + droneId + " to simulation system");
        }
    }

    /**
     * Internal class for drone simulation state
     */
    private class DroneSimulationState {
        private final String droneId;
        private List<DeliveryPathResponse.Delivery> allFlightSegments;
        private final Queue<DeliveryPathResponse.Delivery> emergencyDeliveries = new LinkedList<>();
        private PositionDto currentPosition;
        private DroneStatus status = DroneStatus.READY;
        private int currentSegmentIndex = 0;
        private int currentPathPointIndex = 0;
        private boolean isProcessingEmergency = false;
        private Integer currentEmergencyTaskId;
        private int interruptedSegmentIndex = 0;
        private int interruptedPathIndex = 0;

        public DroneSimulationState(DeliveryPathResponse.DronePath dronePath) {
            this.droneId = dronePath.getDroneId();
            this.allFlightSegments = new ArrayList<>(dronePath.getDeliveries());

            if (!allFlightSegments.isEmpty() && !allFlightSegments.get(0).getFlightPath().isEmpty()) {
                this.currentPosition = allFlightSegments.get(0).getFlightPath().get(0);
            } else {
                PositionDto servicePoint = getServicePointForDrone();
                if (servicePoint != null) {
                    this.currentPosition = servicePoint;
                    System.out.println("Drone " + droneId + " uses service point as initial position: (" +
                            servicePoint.getLng() + ", " + servicePoint.getLat() + ")");
                } else {
                    this.currentPosition = new PositionDto(0.0, 0.0);
                    System.out.println("Warning: Drone " + droneId + " has empty initial path and cannot get service point, setting default position (0.0, 0.0)");
                }
            }
        }

        /**
         * Get service point position for drone
         */
        private PositionDto getServicePointForDrone() {
            try {
                List<DroneForServicePoint> availableDronesInfo = droneService.readAvailableDrones();
                Drone tempDrone = new Drone();
                tempDrone.setId(droneId);
                return droneService.getServicePointForDrone(tempDrone, availableDronesInfo);
            } catch (Exception e) {
                System.err.println("Failed to get service point for drone " + droneId + ": " + e.getMessage());
                return null;
            }
        }

        /**
         * Insert emergency task and pause current task
         */
        public void insertEmergencyTask(DeliveryPathResponse.Delivery emergencyTask) {
            if (isProcessingEmergency) {
                emergencyDeliveries.add(emergencyTask);
                System.out.println("Drone " + droneId + " is already processing emergency task, new emergency task added to queue");
                return;
            }

            this.interruptedSegmentIndex = currentSegmentIndex;
            this.interruptedPathIndex = currentPathPointIndex;
            this.isProcessingEmergency = true;
            this.currentEmergencyTaskId = emergencyTask.getDeliveryId();

            List<DeliveryPathResponse.Delivery> newTaskList = new ArrayList<>();
            newTaskList.add(emergencyTask);

            if (currentSegmentIndex < allFlightSegments.size()) {
                List<DeliveryPathResponse.Delivery> remainingTasks =
                        new ArrayList<>(allFlightSegments.subList(currentSegmentIndex, allFlightSegments.size()));
                newTaskList.addAll(remainingTasks);
                System.out.println("Drone " + droneId + " saved " + remainingTasks.size() + " remaining tasks");
            } else {
                System.out.println("Drone " + droneId + " has no remaining tasks");
            }

            this.allFlightSegments = newTaskList;
            this.currentSegmentIndex = 0;
            this.currentPathPointIndex = 0;
            this.status = DroneStatus.MOVING;

            System.out.println("Drone " + droneId + " paused current task, starting emergency task " + emergencyTask.getDeliveryId());
            System.out.println("New task sequence contains " + newTaskList.size() + " task segments");

            for (int i = 0; i < newTaskList.size(); i++) {
                DeliveryPathResponse.Delivery task = newTaskList.get(i);
                System.out.println("  Task segment " + i + ": deliveryId=" + task.getDeliveryId() +
                        ", path points=" + (task.getFlightPath() != null ? task.getFlightPath().size() : 0));
            }
        }

        /**
         * Update drone position - critical fix method
         */
        public void updatePosition() {
            if (status == DroneStatus.COMPLETED) return;

            DeliveryPathResponse.Delivery currentSegment = getCurrentSegment();
            if (currentSegment == null) {
                status = DroneStatus.COMPLETED;
                return;
            }

            List<PositionDto> flightPath = currentSegment.getFlightPath();
            if (flightPath.isEmpty()) {
                currentSegmentIndex++;
                currentPathPointIndex = 0;
                return;
            }

            if (currentPathPointIndex < flightPath.size() - 1) {
                currentPathPointIndex++;
                currentPosition = flightPath.get(currentPathPointIndex);
                status = DroneStatus.MOVING;
            } else {
                System.out.println("Drone " + droneId + " completed task segment: " +
                        (currentSegment.getDeliveryId() != null ? currentSegment.getDeliveryId() : "return to service point"));

                if (isProcessingEmergency && currentSegmentIndex == 0) {
                    System.out.println("Drone " + droneId + " emergency task completed, restoring original tasks");
                    isProcessingEmergency = false;
                    currentEmergencyTaskId = null;

                    allFlightSegments.remove(0);

                    currentSegmentIndex = interruptedSegmentIndex;
                    currentPathPointIndex = interruptedPathIndex;

                    if (currentSegmentIndex >= allFlightSegments.size() || getCurrentSegment() == null) {
                        System.out.println("Drone " + droneId + " is idle drone, adding return to service point path");
                        addReturnToServicePointSegment();
                    }

                    if (!emergencyDeliveries.isEmpty()) {
                        DeliveryPathResponse.Delivery nextEmergency = emergencyDeliveries.poll();
                        System.out.println("Executing next queued emergency task: " + nextEmergency.getDeliveryId());
                        insertEmergencyTask(nextEmergency);
                    } else {
                        System.out.println("Drone " + droneId + " starting remaining tasks, current segment index: " + currentSegmentIndex);
                    }
                } else {
                    currentSegmentIndex++;
                    currentPathPointIndex = 0;
                }

                if (currentSegmentIndex >= allFlightSegments.size()) {
                    status = DroneStatus.COMPLETED;
                    System.out.println("Drone " + droneId + " all tasks completed");
                } else {
                    status = DroneStatus.READY;
                }
            }
        }

        /**
         * Add return to service point task segment for drone - critical fix method
         */
        private void addReturnToServicePointSegment() {
            try {
                PositionDto currentPos = getCurrentPosition();
                if (currentPos == null) {
                    System.err.println("Error: Cannot get drone current position");
                    return;
                }

                PositionDto servicePoint = getServicePointForDrone();
                if (servicePoint == null) {
                    System.err.println("Error: Cannot get service point position for drone " + droneId);
                    return;
                }

                DeliveryPathResponse.Delivery returnSegment = new DeliveryPathResponse.Delivery();
                returnSegment.setDeliveryId(null);

                List<PositionDto> returnPath = new ArrayList<>();
                returnPath.add(currentPos);
                returnPath.add(servicePoint);

                returnSegment.setFlightPath(returnPath);

                allFlightSegments.add(returnSegment);
                System.out.println("Drone " + droneId + " added return to service point path: " +
                        currentPos.getLng() + "," + currentPos.getLat() + " -> " +
                        servicePoint.getLng() + "," + servicePoint.getLat());

            } catch (Exception e) {
                System.err.println("Failed to add return path for drone " + droneId + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        /**
         * Get current flight segment being processed
         */
        private DeliveryPathResponse.Delivery getCurrentSegment() {
            if (currentSegmentIndex < allFlightSegments.size()) {
                DeliveryPathResponse.Delivery segment = allFlightSegments.get(currentSegmentIndex);
                if (segment != null && segment.getFlightPath() != null && !segment.getFlightPath().isEmpty()) {
                    return segment;
                } else {
                    System.out.println("Drone " + droneId + " skipping invalid task segment, index: " + currentSegmentIndex);
                    currentSegmentIndex++;
                    return getCurrentSegment();
                }
            }
            return null;
        }

        /**
         * Calculate remaining path ratio (for interrupt cost estimation)
         */
        public double calculateRemainingPathRatio() {
            if (allFlightSegments.isEmpty()) return 0.0;

            int totalPoints = 0;
            int completedPoints = 0;

            for (int i = 0; i < allFlightSegments.size(); i++) {
                DeliveryPathResponse.Delivery segment = allFlightSegments.get(i);
                if (segment.getFlightPath() != null) {
                    int segmentPoints = segment.getFlightPath().size();
                    totalPoints += segmentPoints;

                    if (i < currentSegmentIndex) {
                        completedPoints += segmentPoints;
                    } else if (i == currentSegmentIndex) {
                        completedPoints += currentPathPointIndex;
                    }
                }
            }

            if (totalPoints == 0) return 0.0;

            double ratio = 1.0 - (double) completedPoints / totalPoints;
            System.out.println("Drone " + droneId + " remaining path ratio: " + completedPoints + "/" + totalPoints + " = " + ratio);

            return ratio;
        }

        // Getter methods
        public String getDroneId() { return droneId; }
        public PositionDto getCurrentPosition() { return currentPosition; }
        public DroneStatus getStatus() { return status; }
        public Integer getCurrentTaskId() {
            DeliveryPathResponse.Delivery current = getCurrentSegment();
            return current != null ? current.getDeliveryId() : null;
        }
        public int getTotalTasks() { return allFlightSegments.size(); }
        public int getCompletedTasksCount() {
            return isProcessingEmergency ? interruptedSegmentIndex : currentSegmentIndex;
        }
        public boolean isProcessingEmergency() { return isProcessingEmergency; }
        public Integer getCurrentEmergencyTaskId() { return currentEmergencyTaskId; }
    }

    /**
     * Drone status enum
     */
    public enum DroneStatus {
        READY, MOVING, COMPLETED
    }

    /**
     * Drone status DTO
     */
    public static class DroneStatusDto {
        private final String droneId;
        private final PositionDto currentPosition;
        private final DroneStatus status;
        private final Integer currentTaskId;
        private final int totalTasks;
        private final int completedTasksCount;
        private final boolean isProcessingEmergency;
        private final Integer currentEmergencyTaskId;

        public DroneStatusDto(String droneId, PositionDto currentPosition, DroneStatus status,
                              Integer currentTaskId, int totalTasks, int completedTasksCount,
                              boolean isProcessingEmergency, Integer currentEmergencyTaskId) {
            this.droneId = droneId;
            this.currentPosition = currentPosition;
            this.status = status;
            this.currentTaskId = currentTaskId;
            this.totalTasks = totalTasks;
            this.completedTasksCount = completedTasksCount;
            this.isProcessingEmergency = isProcessingEmergency;
            this.currentEmergencyTaskId = currentEmergencyTaskId;
        }

        // Getter methods
        public String getDroneId() { return droneId; }
        public PositionDto getCurrentPosition() { return currentPosition; }
        public DroneStatus getStatus() { return status; }
        public Integer getCurrentTaskId() { return currentTaskId; }
        public int getTotalTasks() { return totalTasks; }
        public int getCompletedTasksCount() { return completedTasksCount; }
        public boolean isProcessingEmergency() { return isProcessingEmergency; }
        public Integer getCurrentEmergencyTaskId() { return currentEmergencyTaskId; }
    }
}