package ilp_cw1.ilp_cw1_rset.Droneservice;

import data.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmergencyDispatchService {

    private final droneService droneService;
    private final DynamicDispatchService dynamicDispatchService;

    public EmergencyDispatchService(droneService droneService, DynamicDispatchService dynamicDispatchService) {
        this.droneService = droneService;
        this.dynamicDispatchService = dynamicDispatchService;
    }

    /**
     * Process emergency task request
     */
    public EmergencyHandleResult processEmergencyRequest(EmergencyDispatchRequest emergencyRequest,
                                                         Map<Drone, List<MedDispatchRec>> droneToTasksMap,
                                                         Map<Integer, PositionDto> taskLocations) {

        if (emergencyRequest.getEmergencyTasks() == null || emergencyRequest.getEmergencyTasks().isEmpty()) {
            return new EmergencyHandleResult(
                    null,
                    0.0,
                    0.0,
                    "No emergency tasks in request.",
                    false
            );
        }

        EmergencyMedDispatchRec emergencyTask = emergencyRequest.getEmergencyTasks().get(0);
        System.out.println("\n=============================================");
        System.out.println("=== Received emergency task request ID: " + emergencyTask.getId() + " ===");
        System.out.println("=============================================");

        List<Drone> allWorkingDrones = new ArrayList<>(droneToTasksMap.keySet());
        List<DroneForServicePoint> availableDronesInfo = droneService.readAvailableDrones();

        List<Drone> allDrones = droneService.getAllDrones();
        List<Drone> candidateDrones = findCandidateDronesForEmergency(emergencyTask, allDrones, availableDronesInfo);

        if (candidateDrones.isEmpty()) {
            return new EmergencyHandleResult(
                    null,
                    0.0,
                    0.0,
                    "No drones found that meet capability requirements and can handle emergency task.",
                    false
            );
        }
        System.out.println("Found " + candidateDrones.size() + " candidate drones meeting capability requirements: " +
                candidateDrones.stream().map(Drone::getId).collect(Collectors.toList()));

        Drone optimalDrone = findIdleDrone(candidateDrones, allWorkingDrones);
        if (optimalDrone != null) {
            System.out.println("✅ Strategy: Found idle drone " + optimalDrone.getId() + ", directly assigned.");
            return buildAndDispatchResult(optimalDrone, emergencyTask, true, 0.0);
        }
        System.out.println("⚠️ Strategy: No idle drones, entering busy drone cost screening process.");

        Map<Drone, Double> interruptCosts = calculateInterruptCostsForCandidates(candidateDrones, droneToTasksMap, taskLocations);
        if (interruptCosts.isEmpty()) {
            return new EmergencyHandleResult(
                    null,
                    0.0,
                    0.0,
                    "Unable to calculate interrupt cost for any busy candidate drone.",
                    false
            );
        }
        Map<Drone, Double> reassignCosts = calculateReassignCosts(interruptCosts);
        optimalDrone = selectOptimalDrone(candidateDrones, interruptCosts, reassignCosts);

        if (optimalDrone == null) {
            return new EmergencyHandleResult(
                    null,
                    0.0,
                    0.0,
                    "All busy drones do not meet cost constraints (reassign cost >= interrupt cost * 10), cannot interrupt.",
                    false
            );
        }

        double baseInterruptCost = interruptCosts.get(optimalDrone);
        return buildAndDispatchResult(optimalDrone, emergencyTask, false, baseInterruptCost);
    }

    /**
     * Filter drones that can handle emergency task (check capability match and availability)
     */
    private List<Drone> findCandidateDronesForEmergency(EmergencyMedDispatchRec emergencyTask,
                                                        List<Drone> allDrones,
                                                        List<DroneForServicePoint> availableDronesInfo) {
        return allDrones.stream()
                .filter(drone -> canDroneHandleEmergency(drone, emergencyTask, availableDronesInfo))
                .collect(Collectors.toList());
    }

    /**
     * Check if drone can handle emergency task (capability match + time availability)
     */
    private boolean canDroneHandleEmergency(Drone drone,
                                            EmergencyMedDispatchRec task,
                                            List<DroneForServicePoint> availableDronesInfo) {
        MedDispatchRec.Requirements req = task.getRequirements();
        Drone.DroneCapability capability = drone.getCapability();

        if (capability == null) {
            System.out.println("Drone " + drone.getId() + " has no capability information, cannot handle task");
            return false;
        }

        if (req.isCooling() && !capability.getCooling()) {
            System.out.println("Drone " + drone.getId() + " does not support cooling, cannot handle task");
            return false;
        }

        if (req.isHeating() && !capability.getHeating()) {
            System.out.println("Drone " + drone.getId() + " does not support heating, cannot handle task");
            return false;
        }

        if (req.getCapacity() > capability.getCapacity()) {
            System.out.println("Drone " + drone.getId() + " insufficient capacity (required: " + req.getCapacity() + ", actual: " + capability.getCapacity() + ")");
            return false;
        }

        return isDroneAvailable(drone.getId(), availableDronesInfo);
    }

    /**
     * Check if drone is available at current time
     */
    private boolean isDroneAvailable(String droneId, List<DroneForServicePoint> availableDronesInfo) {
        for (DroneForServicePoint sp : availableDronesInfo) {
            for (DroneForServicePoint.DroneAvailability da : sp.getDrones()) {
                if (droneId.equals(da.getId())) {
                    return !da.getAvailability().isEmpty();
                }
            }
        }
        System.out.println("Drone " + droneId + " not in available list");
        return false;
    }

    /**
     * Find idle drone among candidate drones
     */
    private Drone findIdleDrone(List<Drone> candidateDrones, List<Drone> workingDrones) {
        Set<String> workingDroneIds = workingDrones.stream()
                .map(Drone::getId)
                .collect(Collectors.toSet());

        return candidateDrones.stream()
                .filter(drone -> !workingDroneIds.contains(drone.getId()))
                .peek(drone -> System.out.println("Found idle drone: " + drone.getId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Calculate interrupt costs for candidate drones
     */
    private Map<Drone, Double> calculateInterruptCostsForCandidates(List<Drone> candidateDrones,
                                                                    Map<Drone, List<MedDispatchRec>> droneToTasksMap,
                                                                    Map<Integer, PositionDto> taskLocations) {
        Map<Drone, Double> costs = new HashMap<>();
        Set<String> workingDroneIds = droneToTasksMap.keySet().stream().map(Drone::getId).collect(Collectors.toSet());

        for (Drone drone : candidateDrones) {
            if (!workingDroneIds.contains(drone.getId())) {
                continue;
            }

            DynamicDispatchService.DroneStatusDto status = dynamicDispatchService.getCurrentDroneStatus(drone.getId());
            if (status != null && status.getStatus() == DynamicDispatchService.DroneStatus.MOVING) {
                List<MedDispatchRec> dronesTasks = droneToTasksMap.get(drone);
                if (dronesTasks == null) {
                    System.out.println("Warning: Drone " + drone.getId() + " task list is null, using empty list");
                    dronesTasks = Collections.emptyList();
                }

                try {
                    double cost = estimateInterruptCost(drone, status, dronesTasks, taskLocations);
                    if (Double.isNaN(cost) || Double.isInfinite(cost)) {
                        System.err.println("Warning: Invalid interrupt cost calculated for drone " + drone.getId() + ": " + cost);
                        cost = 1000.0;
                    }
                    costs.put(drone, cost);
                    System.out.println("Drone " + drone.getId() + " interrupt cost calculated as: " + cost);
                } catch (Exception e) {
                    System.err.println("Error: Exception occurred while calculating interrupt cost for drone " + drone.getId() + ": " + e.getMessage());
                    e.printStackTrace();
                    costs.put(drone, 1500.0);
                }
            } else {
                System.out.println("Drone " + drone.getId() + " not in moving state, skipping cost calculation.");
            }
        }
        return costs;
    }

    /**
     * Estimate interrupt cost (based on remaining path ratio and drone cost parameters)
     */
    private double estimateInterruptCost(Drone drone,
                                         DynamicDispatchService.DroneStatusDto status,
                                         List<MedDispatchRec> tasks,
                                         Map<Integer, PositionDto> taskLocations) {
        double remainingPathRatio = dynamicDispatchService.estimateInterruptCost(drone.getId());

        Drone.DroneCapability capability = drone.getCapability();
        double costPerMove = capability != null && capability.getCostPerMove() != null
                ? capability.getCostPerMove()
                : 1.0;

        return remainingPathRatio * costPerMove * (tasks.size() + 1);
    }

    /**
     * Calculate reassign costs (1.5 times interrupt cost)
     */
    private Map<Drone, Double> calculateReassignCosts(Map<Drone, Double> interruptCosts) {
        Map<Drone, Double> reassignCosts = new HashMap<>();
        interruptCosts.forEach((drone, cost) -> {
            double reassCost = cost * 1.5;
            reassignCosts.put(drone, reassCost);
            System.out.println("Drone " + drone.getId() + " reassign cost calculated as: " + reassCost);
        });
        return reassignCosts;
    }

    /**
     * Select optimal drone (meets cost constraints and has lowest reassign cost)
     */
    private Drone selectOptimalDrone(List<Drone> candidates,
                                     Map<Drone, Double> interruptCosts,
                                     Map<Drone, Double> reassignCosts) {
        System.out.println("\n--- Starting optimal drone selection (cost constraint: reassign cost < interrupt cost * 10) ---");

        List<Drone> viableDrones = candidates.stream()
                .filter(drone -> interruptCosts.containsKey(drone))
                .filter(drone -> {
                    double interruptCost = interruptCosts.get(drone);
                    double reassignCost = reassignCosts.getOrDefault(drone, Double.MAX_VALUE);
                    boolean viable = reassignCost < interruptCost * 10;
                    System.out.printf("Drone %s: interrupt cost=%.2f, reassign cost=%.2f, meets constraint=%s%n",
                            drone.getId(), interruptCost, reassignCost, viable ? "yes" : "no");
                    return viable;
                })
                .collect(Collectors.toList());

        if (viableDrones.isEmpty()) {
            return null;
        }

        return viableDrones.stream()
                .min(Comparator.comparingDouble(drone -> reassignCosts.get(drone)))
                .orElse(null);
    }

    /**
     * Build dispatch result and execute emergency task insertion
     */
    private EmergencyHandleResult buildAndDispatchResult(Drone drone, EmergencyMedDispatchRec emergencyTask, boolean isIdle, double baseCost) {
        DeliveryPathResponse.Delivery emergencyDelivery = new DeliveryPathResponse.Delivery();
        emergencyDelivery.setDeliveryId(emergencyTask.getId());

        PositionDto emergencyPos = emergencyTask.getDelivery().toPositionDto();
        List<PositionDto> emergencyPath = new ArrayList<>();

        DynamicDispatchService.DroneStatusDto status = dynamicDispatchService.getCurrentDroneStatus(drone.getId());

        try {
            PositionDto currentPosition;
            if (status == null) {
                if (isIdle) {
                    System.out.println("Initializing idle drone " + drone.getId() + " simulation state");
                    initializeIdleDroneState(drone);
                    List<DroneForServicePoint> availableDronesInfo = droneService.readAvailableDrones();
                    PositionDto servicePointPos = droneService.getServicePointForDrone(drone, availableDronesInfo);

                    if (servicePointPos == null) {
                        return new EmergencyHandleResult(
                                drone.getId(),
                                0.0,
                                baseCost,
                                "Unable to get service point position for drone " + drone.getId() + ", task assignment failed.",
                                false
                        );
                    }
                    currentPosition = servicePointPos;
                } else {
                    return new EmergencyHandleResult(
                            drone.getId(),
                            0.0,
                            baseCost,
                            "Drone " + drone.getId() + " does not exist in system, cannot assign task",
                            false
                    );
                }
            } else {
                currentPosition = status.getCurrentPosition();
            }

            DeliveryPathResponse.Delivery calculatedDelivery = calculateEmergencyDeliveryPath(
                    drone, emergencyTask, currentPosition, isIdle, false
            );

            emergencyDelivery.setFlightPath(calculatedDelivery.getFlightPath());

            try {
                dynamicDispatchService.insertEmergencyTask(drone.getId(), emergencyDelivery);
            } catch (NoSuchElementException e) {
                return new EmergencyHandleResult(
                        drone.getId(),
                        0.0,
                        baseCost,
                        "Task assignment failed: " + e.getMessage(),
                        false
                );
            } catch (SecurityException e) {
                return new EmergencyHandleResult(
                        drone.getId(),
                        0.0,
                        baseCost,
                        "Task path exception: " + e.getMessage(),
                        false
                );
            }

            double finalCost = isIdle ? baseCost + 0.00000000005 : baseCost + (baseCost * 10);

            return new EmergencyHandleResult(
                    drone.getId(),
                    finalCost,
                    baseCost,
                    "Emergency task successfully assigned to drone " + drone.getId() + ", processing cost: " + finalCost,
                    true
            );

        } catch (RestrictedAreaBlockageException e) {
            System.out.println("❌ Restricted area blockage exception: " + e.getMessage());
            return new EmergencyHandleResult(
                    drone.getId(),
                    0.0,
                    baseCost,
                    e.getMessage(),
                    false
            );
        } catch (Exception e) {
            System.err.println("❌ Emergency task assignment exception: " + e.getMessage());
            e.printStackTrace();
            return new EmergencyHandleResult(
                    drone.getId(),
                    0.0,
                    baseCost,
                    "Emergency task assignment failed: " + e.getMessage(),
                    false
            );
        }
    }

    private DeliveryPathResponse.Delivery calculateEmergencyDeliveryPath(Drone drone,
                                                                         EmergencyMedDispatchRec emergencyTask,
                                                                         PositionDto startPosition,
                                                                         boolean isIdle,
                                                                         boolean forceBypass) {
        System.out.println("=== Calculating emergency order path ===");
        System.out.println("Emergency level: " + emergencyTask.getEmergencyLevel());
        System.out.println("Force bypass restricted areas: " + forceBypass);

        PositionDto emergencyPos = emergencyTask.getDelivery().toPositionDto();
        List<PositionDto> emergencyPath = new ArrayList<>();

        List<RestrictedArea> restrictedAreas = droneService.getRestrictedAreas();
        List<DroneForServicePoint> availableDronesInfo = droneService.readAvailableDrones();
        PositionDto servicePointPos = droneService.getServicePointForDrone(drone, availableDronesInfo);

        if (servicePointPos == null) {
            throw new IllegalArgumentException("Unable to get drone service point position");
        }

        boolean bypassRestrictedAreas = forceBypass || shouldBypassRestrictedAreas(emergencyTask);

        if (bypassRestrictedAreas) {
            System.out.println("⚠️ Allowed to fly over restricted areas, calculating direct path");
            emergencyPath = calculateDirectPath(startPosition, emergencyPos);
        } else {
            emergencyPath = droneService.calculateAStarPath(startPosition, emergencyPos, restrictedAreas, drone);
            if (emergencyPath.isEmpty() || !isPathReachable(emergencyPath, emergencyPos)) {
                String blockingAreaName = getBlockingRestrictedAreaName(startPosition, emergencyPos, restrictedAreas);
                boolean requiresHumanConfirmation = emergencyTask.getEmergencyLevel() >= 1 && emergencyTask.getEmergencyLevel() <= 4;

                throw new RestrictedAreaBlockageException(
                        emergencyTask.getId(),
                        blockingAreaName,
                        requiresHumanConfirmation
                );
            }
        }

        if (emergencyPath.isEmpty()) {
            emergencyPath.add(startPosition);
            emergencyPath.add(emergencyPos);
            System.out.println("Warning: Path generation failed, using default path");
        } else {
            System.out.println("Generated emergency task path, containing " + emergencyPath.size() + " nodes");
        }

        DeliveryPathResponse.Delivery emergencyDelivery = new DeliveryPathResponse.Delivery();
        emergencyDelivery.setDeliveryId(emergencyTask.getId());
        emergencyDelivery.setFlightPath(emergencyPath);

        return emergencyDelivery;
    }

    /**
     * Determine if should bypass restricted areas (level 5 and above directly bypass)
     */
    private boolean shouldBypassRestrictedAreas(EmergencyMedDispatchRec emergencyTask) {
        return emergencyTask.getEmergencyLevel() >= 5;
    }

    /**
     * Calculate direct path (without considering restricted areas)
     */
    private List<PositionDto> calculateDirectPath(PositionDto start, PositionDto end) {
        List<PositionDto> path = new ArrayList<>();
        path.add(start);

        double distance = calculateDistance(start, end);
        int steps = Math.max(2, (int) Math.ceil(distance / 0.00015));

        for (int i = 1; i <= steps; i++) {
            double ratio = (double) i / steps;
            PositionDto point = new PositionDto(
                    start.getLng() + (end.getLng() - start.getLng()) * ratio,
                    start.getLat() + (end.getLat() - start.getLat()) * ratio
            );
            path.add(point);
        }

        path.add(end);
        return path;
    }

    /**
     * Check if path can reach target position
     */
    private boolean isPathReachable(List<PositionDto> path, PositionDto target) {
        if (path.isEmpty()) return false;

        PositionDto lastPoint = path.get(path.size() - 1);
        return calculateDistance(lastPoint, target) < 0.00015;
    }

    /**
     * Calculate distance between two points (simplified version)
     */
    private double calculateDistance(PositionDto p1, PositionDto p2) {
        double dx = p1.getLng() - p2.getLng();
        double dy = p1.getLat() - p2.getLat();
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Get name of blocking restricted area
     */
    private String getBlockingRestrictedAreaName(PositionDto start, PositionDto end,
                                                 List<RestrictedArea> restrictedAreas) {
        for (RestrictedArea area : restrictedAreas) {
            if (droneService.doesLineIntersectPolygon(start, end, area.getVertices())) {
                return area.getName() != null ? area.getName() : "Unknown restricted area";
            }

            if (droneService.isPointInPolygon(start, area.getVertices()) ||
                    droneService.isPointInPolygon(end, area.getVertices())) {
                return area.getName() != null ? area.getName() : "Unknown restricted area";
            }
        }
        return "Unknown restricted area";
    }

    /**
     * Initialize simulation state for idle drone
     */
    private void initializeIdleDroneState(Drone drone) {
        DeliveryPathResponse.DronePath idleDronePath = new DeliveryPathResponse.DronePath();
        idleDronePath.setDroneId(drone.getId());
        idleDronePath.setDeliveries(new ArrayList<>());

        DeliveryPathResponse tempResponse = new DeliveryPathResponse();
        tempResponse.setDronePaths(Collections.singletonList(idleDronePath));

        if (!dynamicDispatchService.isSimulationRunning()) {
            dynamicDispatchService.startSimulation(tempResponse);
        } else {
            dynamicDispatchService.addDroneState(drone.getId(), idleDronePath);
        }
    }

    /**
     * Process emergency request (supports bypass restricted areas flag)
     */
    public EmergencyHandleResult processEmergencyRequestWithBypass(EmergencyDispatchRequest emergencyRequest,
                                                                   Map<Drone, List<MedDispatchRec>> droneToTasksMap,
                                                                   Map<Integer, PositionDto> taskLocations,
                                                                   boolean bypassRestrictedAreas) {

        if (emergencyRequest.getEmergencyTasks() == null || emergencyRequest.getEmergencyTasks().isEmpty()) {
            return new EmergencyHandleResult(
                    null,
                    0.0,
                    0.0,
                    "No emergency tasks in request.",
                    false
            );
        }

        EmergencyMedDispatchRec emergencyTask = emergencyRequest.getEmergencyTasks().get(0);
        System.out.println("\n=============================================");
        System.out.println("=== Processing emergency task with bypass ID: " + emergencyTask.getId() + " ===");
        System.out.println("=== Bypass restricted areas: " + bypassRestrictedAreas + " ===");
        System.out.println("=============================================");

        List<Drone> allWorkingDrones = new ArrayList<>(droneToTasksMap.keySet());
        List<DroneForServicePoint> availableDronesInfo = droneService.readAvailableDrones();

        List<Drone> allDrones = droneService.getAllDrones();
        List<Drone> candidateDrones = findCandidateDronesForEmergency(emergencyTask, allDrones, availableDronesInfo);

        if (candidateDrones.isEmpty()) {
            return new EmergencyHandleResult(
                    null,
                    0.0,
                    0.0,
                    "No drones found that meet capability requirements and can handle emergency task.",
                    false
            );
        }
        System.out.println("Found " + candidateDrones.size() + " candidate drones meeting capability requirements");

        Drone optimalDrone = findIdleDrone(candidateDrones, allWorkingDrones);
        if (optimalDrone != null) {
            System.out.println("✅ Strategy: Found idle drone " + optimalDrone.getId() + ", directly assigned.");
            return buildAndDispatchResultWithBypass(optimalDrone, emergencyTask, true, 0.0, bypassRestrictedAreas);
        }
        System.out.println("⚠️ Strategy: No idle drones, entering busy drone cost screening process.");

        Map<Drone, Double> interruptCosts = calculateInterruptCostsForCandidates(candidateDrones, droneToTasksMap, taskLocations);
        if (interruptCosts.isEmpty()) {
            return new EmergencyHandleResult(
                    null,
                    0.0,
                    0.0,
                    "Unable to calculate interrupt cost for any busy candidate drone.",
                    false
            );
        }
        Map<Drone, Double> reassignCosts = calculateReassignCosts(interruptCosts);
        optimalDrone = selectOptimalDrone(candidateDrones, interruptCosts, reassignCosts);

        if (optimalDrone == null) {
            return new EmergencyHandleResult(
                    null,
                    0.0,
                    0.0,
                    "All busy drones do not meet cost constraints, cannot interrupt.",
                    false
            );
        }

        double baseInterruptCost = interruptCosts.get(optimalDrone);
        return buildAndDispatchResultWithBypass(optimalDrone, emergencyTask, false, baseInterruptCost, bypassRestrictedAreas);
    }

    /**
     * Build dispatch result (supports bypass restricted areas)
     */
    private EmergencyHandleResult buildAndDispatchResultWithBypass(Drone drone, EmergencyMedDispatchRec emergencyTask,
                                                                   boolean isIdle, double baseCost, boolean bypassRestrictedAreas) {
        DeliveryPathResponse.Delivery emergencyDelivery = new DeliveryPathResponse.Delivery();
        emergencyDelivery.setDeliveryId(emergencyTask.getId());

        PositionDto emergencyPos = emergencyTask.getDelivery().toPositionDto();
        List<PositionDto> emergencyPath = new ArrayList<>();

        DynamicDispatchService.DroneStatusDto status = dynamicDispatchService.getCurrentDroneStatus(drone.getId());

        try {
            PositionDto currentPosition;
            if (status == null) {
                if (isIdle) {
                    System.out.println("Initializing idle drone " + drone.getId() + " simulation state");
                    initializeIdleDroneState(drone);
                    List<DroneForServicePoint> availableDronesInfo = droneService.readAvailableDrones();
                    PositionDto servicePointPos = droneService.getServicePointForDrone(drone, availableDronesInfo);

                    if (servicePointPos == null) {
                        return new EmergencyHandleResult(
                                drone.getId(),
                                0.0,
                                baseCost,
                                "Unable to get service point position for drone " + drone.getId() + ", task assignment failed.",
                                false
                        );
                    }
                    currentPosition = servicePointPos;
                } else {
                    return new EmergencyHandleResult(
                            drone.getId(),
                            0.0,
                            baseCost,
                            "Drone " + drone.getId() + " does not exist in system, cannot assign task",
                            false
                    );
                }
            } else {
                currentPosition = status.getCurrentPosition();
            }

            DeliveryPathResponse.Delivery calculatedDelivery = calculateEmergencyDeliveryPath(
                    drone, emergencyTask, currentPosition, isIdle, bypassRestrictedAreas
            );

            emergencyDelivery.setFlightPath(calculatedDelivery.getFlightPath());

            try {
                dynamicDispatchService.insertEmergencyTask(drone.getId(), emergencyDelivery);
            } catch (NoSuchElementException e) {
                return new EmergencyHandleResult(
                        drone.getId(),
                        0.0,
                        baseCost,
                        "Task assignment failed: " + e.getMessage(),
                        false
                );
            } catch (SecurityException e) {
                return new EmergencyHandleResult(
                        drone.getId(),
                        0.0,
                        baseCost,
                        "Task path exception: " + e.getMessage(),
                        false
                );
            }

            double finalCost = isIdle ? baseCost + 50.0 : baseCost + (baseCost * 1.5);

            String message = "Emergency task successfully assigned to drone " + drone.getId() + ", processing cost: " + finalCost;
            if (bypassRestrictedAreas) {
                message += " (bypassed restricted areas)";
            }

            return new EmergencyHandleResult(
                    drone.getId(),
                    finalCost,
                    baseCost,
                    message,
                    true
            );

        } catch (RestrictedAreaBlockageException e) {
            System.out.println("❌ Restricted area blockage exception: " + e.getMessage());
            return new EmergencyHandleResult(
                    drone.getId(),
                    0.0,
                    baseCost,
                    e.getMessage(),
                    false
            );
        } catch (Exception e) {
            System.err.println("❌ Emergency task assignment exception: " + e.getMessage());
            e.printStackTrace();
            return new EmergencyHandleResult(
                    drone.getId(),
                    0.0,
                    baseCost,
                    "Emergency task assignment failed: " + e.getMessage(),
                    false
            );
        }
    }
}