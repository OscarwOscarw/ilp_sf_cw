package ilp_cw1.ilp_cw1_rset.Droneservice;

import data.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for drone operations including data retrieval, query processing,
 * path calculation, and delivery optimization
 */
@Service
public class droneService {
    private final RestTemplate restTemplate;
    final ilpService ilpService;
    private final String baseUrl;

    /**
     * Constructor for drone service
     * @param restTemplate the REST template for HTTP requests
     * @param ilpService the ILP service for calculations
     */
    public droneService(RestTemplate restTemplate, ilpService ilpService) {
        this.restTemplate = restTemplate;
        this.ilpService = ilpService;
        this.baseUrl = System.getenv().getOrDefault("ILP_ENDPOINT",
                "https://ilp-rest-2025-bvh6e9hschfagrgy.ukwest-01.azurewebsites.net/");
        System.out.println("Base URL: " + this.baseUrl);
    }

    // ==============================================
    // 1. Data Retrieval Module - Fetch basic data from API
    // ==============================================

    /**
     * Retrieves all drones from the API
     * @return list of all drones
     */
    public List<Drone> getAllDrones() {
        try {
            String url = baseUrl + "drones";
            System.out.println("Fetching data from: " + url);
            Drone[] dronesArray = restTemplate.getForObject(url, Drone[].class);
            if (dronesArray != null && dronesArray.length > 0) {
                System.out.println("Retrieved " + dronesArray.length + " drones");
                return Arrays.asList(dronesArray);
            } else {
                System.out.println("No drones found");
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch data: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Retrieves drones by their IDs
     * @param droneIds list of drone IDs to retrieve
     * @return list of drones matching the specified IDs
     */
    public List<Drone> getDronesByIds(List<String> droneIds) {
        try {
            String url = baseUrl + "drones";
            System.out.println("Fetching data from: " + url);
            Drone[] dronesArray = restTemplate.getForObject(url, Drone[].class);
            if (dronesArray != null && dronesArray.length > 0) {
                System.out.println("Retrieved " + dronesArray.length + " drones from API");
                List<Drone> filteredDrones = Arrays.stream(dronesArray)
                        .filter(drone -> droneIds.contains(drone.getId()))
                        .collect(Collectors.toList());
                System.out.println("Filtered to " + filteredDrones.size() + " drones with IDs: " + droneIds);
                return filteredDrones;
            } else {
                System.out.println("No drones found from API");
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("Failed to retrieve drones data: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Retrieves all service points from the API
     * @return list of all service points
     */
    public List<ServicePoint> getServicePoints() {
        try {
            String url = baseUrl + "service-points";
            System.out.println("Fetching service points from: " + url);
            ServicePoint[] servicePointsArray = restTemplate.getForObject(url, ServicePoint[].class);
            if (servicePointsArray != null && servicePointsArray.length > 0) {
                System.out.println("Retrieved " + servicePointsArray.length + " service points");
                return Arrays.asList(servicePointsArray);
            } else {
                System.out.println("No service points found");
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("Failed to retrieve service points: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Retrieves available drones for service points
     * @return list of drones available at service points
     */
    public List<DroneForServicePoint> readAvailableDrones() {
        try {
            String url = baseUrl + "drones-for-service-points";
            System.out.println("Fetching available drones from: " + url);
            DroneForServicePoint[] availableDronesArray = restTemplate.getForObject(url, DroneForServicePoint[].class);
            if (availableDronesArray != null && availableDronesArray.length > 0) {
                System.out.println("Retrieved " + availableDronesArray.length + " service points with available drones");
                return Arrays.asList(availableDronesArray);
            } else {
                System.out.println("No available drones found");
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("Failed to retrieve available drones data: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Retrieves restricted areas from the API
     * @return list of all restricted areas
     */
    public List<RestrictedArea> getRestrictedAreas() {
        List<RestrictedArea> allAreas = new ArrayList<>();

        try {
            List<RestrictedArea> apiAreas = getRestrictedAreasFromAPI();
            if (apiAreas != null) {
                allAreas.addAll(apiAreas);
            }
        } catch (Exception e) {
            System.err.println("Warning: Failed to get Restricted Areas from API: " + e.getMessage());
        }

        synchronized (areasLock) {
            allAreas.addAll(dynamicRestrictedAreas);
        }

        System.out.println("Restricted Areas: " + (allAreas.size() - dynamicRestrictedAreas.size()) +
                " from API, dynamic: " + dynamicRestrictedAreas.size() +
                ", total: " + allAreas.size());

        return allAreas;
    }

    // ==============================================
    // 2. Query and Comparison Module - For controller query functions
    // ==============================================

    /**
     * Gets the value of a specific attribute from a drone
     * @param drone the drone object
     * @param attributeName the name of the attribute
     * @return the attribute value
     */
    public Object getAttributeValue(Drone drone, String attributeName) {
        switch (attributeName.toLowerCase()) {
            case "id":
                return drone.getId();
            case "name":
                return drone.getName();
            case "capacity":
                return drone.getCapability() != null ? drone.getCapability().getCapacity() : null;
            case "cooling":
                return drone.getCapability() != null ? drone.getCapability().getCooling() : null;
            case "heating":
                return drone.getCapability() != null ? drone.getCapability().getHeating() : null;
            case "maxmoves":
                return drone.getCapability() != null ? drone.getCapability().getMaxMoves() : null;
            case "costpermove":
                return drone.getCapability() != null ? drone.getCapability().getCostPerMove() : null;
            case "costinitial":
                return drone.getCapability() != null ? drone.getCapability().getCostInitial() : null;
            case "costfinal":
                return drone.getCapability() != null ? drone.getCapability().getCostFinal() : null;
            default:
                return null;
        }
    }

    /**
     * Compares drone attribute value with input value by type
     * @param droneValue the drone's attribute value
     * @param inputValue the input value to compare against
     * @return true if values match, false otherwise
     */
    public boolean compareByType(Object droneValue, String inputValue) {
        if (droneValue == null) {
            return false;
        }
        try {
            if (droneValue instanceof Double) {
                double droneDouble = (Double) droneValue;
                double inputDouble = Double.parseDouble(inputValue);
                return droneDouble == inputDouble;
            } else if (droneValue instanceof Integer) {
                int droneInt = (Integer) droneValue;
                int inputInt = Integer.parseInt(inputValue);
                return droneInt == inputInt;
            } else if (droneValue instanceof Boolean) {
                boolean droneBool = (Boolean) droneValue;
                boolean inputBool = Boolean.parseBoolean(inputValue);
                return droneBool == inputBool;
            } else if (droneValue instanceof String) {
                return droneValue.equals(inputValue);
            } else {
                return droneValue.toString().equals(inputValue);
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Compares drone attribute value with input value using specified operator
     * @param droneValue the drone's attribute value
     * @param inputValue the input value to compare against
     * @param operator the comparison operator (=, !=, <, >, <=, >=)
     * @return true if condition is satisfied, false otherwise
     */
    public boolean compareWithOperator(Object droneValue, String inputValue, String operator) {
        if (droneValue == null) {
            return false;
        }
        try {
            if (droneValue instanceof Double || droneValue instanceof Integer) {
                double droneDouble = ((Number) droneValue).doubleValue();
                double inputDouble = Double.parseDouble(inputValue);
                return compareNumerical(droneDouble, inputDouble, operator);
            } else if (droneValue instanceof Boolean) {
                boolean droneBool = (Boolean) droneValue;
                boolean inputBool = Boolean.parseBoolean(inputValue);
                return compareBoolean(droneBool, inputBool, operator);
            } else {
                String droneStr = droneValue.toString();
                return compareString(droneStr, inputValue, operator);
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean compareNumerical(double droneValue, double inputValue, String operator) {
        switch (operator) {
            case "=": return Math.abs(droneValue - inputValue) < 0.0001;
            case "!=": return Math.abs(droneValue - inputValue) >= 0.0001;
            case "<": return droneValue < inputValue;
            case ">": return droneValue > inputValue;
            case "<=": return droneValue <= inputValue;
            case ">=": return droneValue >= inputValue;
            default: return false;
        }
    }

    private boolean compareBoolean(boolean droneValue, boolean inputValue, String operator) {
        switch (operator) {
            case "=": return droneValue == inputValue;
            case "!=": return droneValue != inputValue;
            default: return false;
        }
    }

    private boolean compareString(String droneValue, String inputValue, String operator) {
        switch (operator) {
            case "=": return droneValue.equals(inputValue);
            case "!=": return !droneValue.equals(inputValue);
            default: return false;
        }
    }

    // ==============================================
    // 3. Core Business Logic Module - Drone capability checks and task assignment
    // ==============================================

    /**
     * Checks if a drone can handle a task considering movement constraints and budget
     * @param drone the drone to check
     * @param task the task to be handled
     * @param availableDronesInfo list of available drones at service points
     * @param servicePoint the service point location
     * @param taskLocation the task delivery location
     * @param includeReturn whether to include return trip in calculations
     * @return true if drone can handle the task, false otherwise
     */
    public boolean canDroneHandleTaskWithMoves(Drone drone, MedDispatchRec task,
                                               List<DroneForServicePoint> availableDronesInfo,
                                               PositionDto servicePoint,
                                               PositionDto taskLocation,
                                               boolean includeReturn) {
        System.out.println("=== Checking movement and budget for drone " + drone.getId() + " and task " + task.getId() + " ===");

        // First check basic conditions using original method
        if (!canDroneHandleTask(drone, task, availableDronesInfo)) {
            System.out.println("FAIL: Basic requirements check failed for drone " + drone.getId());
            return false;
        }

        // Then add movement count check
        double distanceToTask = ilpService.distanceCalculate(new DistanceRequest(servicePoint, taskLocation));
        int estimatedMovesToTask = (int) Math.ceil(distanceToTask / 0.00015);

        int totalEstimatedMoves = estimatedMovesToTask;
        if (includeReturn) {
            totalEstimatedMoves += estimatedMovesToTask; // Return path same as outbound
        }

        System.out.println("Distance: " + String.format("%.6f", distanceToTask) +
                ", Moves to task: " + estimatedMovesToTask +
                ", Total moves: " + totalEstimatedMoves +
                ", Max moves: " + drone.getCapability().getMaxMoves());

        // Check movement limit
        if (totalEstimatedMoves > drone.getCapability().getMaxMoves()) {
            System.out.println("FAIL: Movement count " + totalEstimatedMoves +
                    " exceeds maximum " + drone.getCapability().getMaxMoves() + " for drone " + drone.getId());
            return false;
        }

        // Budget check - get maxCost from task requirements
        double maxCost = task.getRequirements().getMaxCost();
        if (maxCost > 0) {
            // Use universal solution: create a list containing only the current task
            List<MedDispatchRec> singleTaskList = Collections.singletonList(task);

            // Create task location mapping (only containing current task)
            Map<Integer, PositionDto> singleTaskLocations = new HashMap<>();
            singleTaskLocations.put(task.getId(), taskLocation);

            double estimatedCost = estimateMaxCost(drone, singleTaskList, servicePoint, singleTaskLocations);

            System.out.println("Budget check - Estimated cost: " + String.format("%.2f", estimatedCost) +
                    ", Max cost: " + maxCost);

            if (estimatedCost > maxCost) {
                System.out.println("FAIL: Estimated cost " + String.format("%.2f", estimatedCost) +
                        " exceeds max cost " + maxCost + " for drone " + drone.getId());
                return false;
            }
            System.out.println("PASS: Budget check passed for drone " + drone.getId());
        } else {
            System.out.println("INFO: No budget constraint for this task");
        }

        System.out.println("PASS: Drone " + drone.getId() + " passed all movement and budget checks for task " + task.getId());
        return true;
    }

    public double estimateMaxCost(Drone drone, List<MedDispatchRec> tasks,
                                  PositionDto servicePoint,
                                  Map<Integer, PositionDto> taskLocations) {

        if (drone == null || drone.getCapability() == null || servicePoint == null ||
                tasks == null || tasks.isEmpty()) {
            return 0.0;
        }

        Drone.DroneCapability capability = drone.getCapability();
        double costPerMove = capability.getCostPerMove() != null ? capability.getCostPerMove() : 0.0;
        double costInitial = capability.getCostInitial() != null ? capability.getCostInitial() : 0.0;
        double costFinal = capability.getCostFinal() != null ? capability.getCostFinal() : 0.0;

        double fixedCost = costInitial + costFinal;

        double totalDistance = calculateSequentialPathDistance(servicePoint, tasks, taskLocations);
        int totalMoves = (int) Math.ceil(totalDistance / 0.00015);

        int hoverMoves = tasks.size() + 1;
        totalMoves += hoverMoves;

        double totalCost = fixedCost + (totalMoves * costPerMove);

        System.out.println("Cost estimation for drone " + drone.getId() + ":");
        System.out.println("  - Path: ServicePoint → " + tasks.size() + " tasks → ServicePoint");
        System.out.println("  - Total distance: " + String.format("%.6f", totalDistance));
        System.out.println("  - Total moves: " + totalMoves + " (flight: " + (totalMoves - hoverMoves) + ", hover: " + hoverMoves + ")");
        System.out.println("  - Fixed cost: " + fixedCost);
        System.out.println("  - Move cost: " + (totalMoves * costPerMove));
        System.out.println("  - Total estimated cost: " + String.format("%.2f", totalCost));

        return totalCost;
    }

    private double calculateSequentialPathDistance(PositionDto servicePoint,
                                                   List<MedDispatchRec> tasks,
                                                   Map<Integer, PositionDto> taskLocations) {
        double totalDistance = 0.0;
        PositionDto currentPosition = servicePoint;

        // From service point to first task
        if (!tasks.isEmpty()) {
            PositionDto firstTask = taskLocations.get(tasks.get(0).getId());
            if (firstTask != null) {
                totalDistance += ilpService.distanceCalculate(new DistanceRequest(currentPosition, firstTask));
                currentPosition = firstTask;
            }
        }

        // Movement between tasks
        for (int i = 1; i < tasks.size(); i++) {
            PositionDto nextTask = taskLocations.get(tasks.get(i).getId());
            if (nextTask != null) {
                totalDistance += ilpService.distanceCalculate(new DistanceRequest(currentPosition, nextTask));
                currentPosition = nextTask;
            }
        }

        // Return from last task to service point
        if (!tasks.isEmpty()) {
            totalDistance += ilpService.distanceCalculate(new DistanceRequest(currentPosition, servicePoint));
        }

        return totalDistance;
    }

    public boolean isDroneWithinBudget(Drone drone, List<MedDispatchRec> tasks,
                                       PositionDto servicePoint,
                                       Map<Integer, PositionDto> taskLocations,
                                       Double maxBudget) {
        if (maxBudget == null) {
            return true;
        }

        if (drone == null || drone.getCapability() == null || servicePoint == null) {
            return false;
        }

        double estimatedCost = estimateMaxCost(drone, tasks, servicePoint, taskLocations);
        boolean withinBudget = estimatedCost <= maxBudget;

        if (!withinBudget) {
            System.out.println("FAIL: Drone " + drone.getId() + " exceeded budget: " +
                    String.format("%.2f", estimatedCost) + " > " + maxBudget);
        } else {
            System.out.println("PASS: Drone " + drone.getId() + " within budget: " +
                    String.format("%.2f", estimatedCost) + " <= " + maxBudget);
        }

        return withinBudget;
    }

    /**
     * Checks if a drone can handle a task based on basic requirements
     * @param drone the drone to check
     * @param task the task to be handled
     * @param availableDronesInfo list of available drones at service points
     * @return true if drone can handle the task, false otherwise
     */
    public boolean canDroneHandleTask(Drone drone, MedDispatchRec task,
                                      List<DroneForServicePoint> availableDronesInfo) {
        MedDispatchRec.Requirements req = task.getRequirements();

        System.out.println("=== Checking basic requirements for drone " + drone.getId() + " and task " + task.getId() + " ===");

        // Check cooling requirement
        if (req.isCooling() && !drone.getCapability().getCooling()) {
            System.out.println("FAIL: Task requires cooling but drone " + drone.getId() + " has no cooling capability");
            return false;
        }

        // Check heating requirement
        if (req.isHeating() && !drone.getCapability().getHeating()) {
            System.out.println("FAIL: Task requires heating but drone " + drone.getId() + " has no heating capability");
            return false;
        }

        // Check capacity requirement
        if (req.getCapacity() > drone.getCapability().getCapacity()) {
            System.out.println("FAIL: Task capacity " + req.getCapacity() +
                    " exceeds drone " + drone.getId() + " capacity " + drone.getCapability().getCapacity());
            return false;
        }

        // Check time availability
        if (!isDroneAvailableAtTime(drone, task.getDate(), task.getTime(), availableDronesInfo)) {
            System.out.println("FAIL: Drone " + drone.getId() + " not available at " + task.getDate() + " " + task.getTime());
            return false;
        }

        System.out.println("PASS: Drone " + drone.getId() + " meets all basic requirements for task " + task.getId());
        return true;
    }

    private boolean isDroneAvailableAtTime(Drone drone, LocalDate date, LocalTime time,
                                           List<DroneForServicePoint> availableDronesInfo) {
        for (DroneForServicePoint servicePoint : availableDronesInfo) {
            for (DroneForServicePoint.DroneAvailability droneAvail : servicePoint.getDrones()) {
                if (droneAvail.getId().equals(drone.getId())) {
                    if (isTimeInAvailability(droneAvail, date, time)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isTimeInAvailability(DroneForServicePoint.DroneAvailability droneAvail, LocalDate date, LocalTime time) {

        if (date == null) {
            System.err.println("Warning: date parameter is null, cannot check time availability");
            return false;
        }

        String dayOfWeek = date.getDayOfWeek().toString();
        for (DroneForServicePoint.DroneAvailability.AvailabilitySlot slot : droneAvail.getAvailability()) {
            if (slot.getDayOfWeek().equalsIgnoreCase(dayOfWeek)) {
                LocalTime fromTime = LocalTime.parse(slot.getFrom());
                LocalTime untilTime = LocalTime.parse(slot.getUntil());
                if ((time.equals(fromTime) || time.isAfter(fromTime)) &&
                        (time.equals(untilTime) || time.isBefore(untilTime))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Extracts actual location information from task data
     * @param tasks list of medical dispatch tasks
     * @return map of task IDs to their locations
     */
    public Map<Integer, PositionDto> assignTaskLocations(List<MedDispatchRec> tasks) {
        Map<Integer, PositionDto> locations = new HashMap<>();
        for (MedDispatchRec task : tasks) {
            if (task.getDelivery() == null) {
                throw new IllegalArgumentException("Task " + task.getId() + " is missing delivery location");
            }
            PositionDto taskLocation = task.getDelivery().toPositionDto();
            locations.put(task.getId(), taskLocation);
            System.out.println("Task " + task.getId() + " location: (" +
                    taskLocation.getLng() + ", " + taskLocation.getLat() + ")");
        }
        return locations;
    }

    // ==============================================
    // 4. Single Drone Solution Module
    // ==============================================

    /**
     * Attempts to find a single drone that can complete all tasks
     * @param availableDrones list of available drones
     * @param tasks list of tasks to complete
     * @param taskLocations map of task locations
     * @param availableDronesInfo list of available drones at service points
     * @param restrictedAreas list of restricted areas to avoid
     * @return delivery path response for single drone solution
     */
    public DeliveryPathResponse findAndBuildSingleDroneResponse(List<Drone> availableDrones,
                                                                List<MedDispatchRec> tasks,
                                                                Map<Integer, PositionDto> taskLocations,
                                                                List<DroneForServicePoint> availableDronesInfo,
                                                                List<RestrictedArea> restrictedAreas) {
        DeliveryPathResponse response = new DeliveryPathResponse();
        for (Drone drone : availableDrones) {
            PositionDto servicePoint = getServicePointForDrone(drone, availableDronesInfo);
            if (servicePoint == null) continue;
            if (!satisfiesCapacityLimit(drone, tasks)) continue;
            if (!satisfiesTemperatureRequirements(drone, tasks)) continue;

            DeliveryPathResponse.DronePath dronePath = calculateSingleDronePath(
                    drone, tasks, servicePoint, taskLocations, restrictedAreas);

            if (dronePath != null) {
                response.setDronePaths(Collections.singletonList(dronePath));
                response.setTotalCost(calculateTotalCost(dronePath, drone));
                response.setTotalMoves(calculateTotalMoves(dronePath));
                return response;
            }
        }
        response.setDronePaths(new ArrayList<>());
        response.setTotalCost(0);
        response.setTotalMoves(0);
        return response;
    }

    /**
     * Calculates delivery path for a single drone handling multiple tasks
     * @param drone the drone to calculate path for
     * @param tasks list of tasks to deliver
     * @param servicePoint the drone's service point location
     * @param taskLocations map of task locations
     * @param restrictedAreas list of restricted areas to avoid
     * @return calculated drone path
     */
    public DeliveryPathResponse.DronePath calculateSingleDronePath(Drone drone,
                                                                   List<MedDispatchRec> tasks,
                                                                   PositionDto servicePoint,
                                                                   Map<Integer, PositionDto> taskLocations,
                                                                   List<RestrictedArea> restrictedAreas) {
        List<DeliveryPathResponse.Delivery> deliveries = new ArrayList<>();
        PositionDto currentPosition = servicePoint;
        int totalMoves = 0;

        if (servicePoint == null) {
            throw new IllegalArgumentException("Service point for drone " + drone.getId() + " cannot be null");
        }

        System.out.println("=== Starting Single Drone Path Calculation ===");
        System.out.println("Drone: " + drone.getId() + ", Service Point: (" +
                servicePoint.getLng() + ", " + servicePoint.getLat() + ")");
        System.out.println("Tasks to deliver: " + tasks.size());

        for (int i = 0; i < tasks.size(); i++) {
            MedDispatchRec task = tasks.get(i);
            PositionDto target = taskLocations.get(task.getId());
            if (target == null) {
                throw new IllegalArgumentException("Location for task " + task.getId() + " does not exist");
            }

            System.out.println("Task " + (i + 1) + " (ID: " + task.getId() + "): From (" +
                    currentPosition.getLng() + ", " + currentPosition.getLat() +
                    ") to (" + target.getLng() + ", " + target.getLat() + ")");

            // Convert A* returned path to modifiable ArrayList
            List<PositionDto> flightPath = new ArrayList<>(calculateAStarPath(currentPosition, target, restrictedAreas, drone));

            if (!flightPath.isEmpty() && !isSamePosition(flightPath.get(0), currentPosition)) {
                flightPath.add(0, currentPosition);
            }

            int deliveryMoves = flightPath.size() - 1;
            totalMoves += deliveryMoves;

            totalMoves += 1;
            System.out.println("  Path calculated: " + flightPath.size() + " points, " + deliveryMoves + " moves + 1 hover move");

            System.out.println("  Total moves so far: " + totalMoves);

            if (totalMoves > drone.getCapability().getMaxMoves()) {
                throw new IllegalArgumentException("Drone " + drone.getId() + " movement count exceeded: " +
                        totalMoves + " > " + drone.getCapability().getMaxMoves());
            }

            if (!flightPath.isEmpty()) {
                PositionDto lastPosition = flightPath.get(flightPath.size() - 1);
                flightPath.add(new PositionDto(lastPosition.getLng(), lastPosition.getLat()));
                System.out.println("  Added hover point at delivery location (+1 move)");
            }

            DeliveryPathResponse.Delivery delivery = new DeliveryPathResponse.Delivery();
            delivery.setDeliveryId(task.getId());
            delivery.setFlightPath(flightPath);
            deliveries.add(delivery);
            currentPosition = target;

            System.out.println("  Delivery completed, current position updated to target");
        }

        System.out.println("=== Calculating Return Path ===");
        System.out.println("Current position: (" + currentPosition.getLng() + ", " + currentPosition.getLat() + ")");
        System.out.println("Service point: (" + servicePoint.getLng() + ", " + servicePoint.getLat() + ")");

        if (!isSamePosition(currentPosition, servicePoint)) {
            System.out.println("Return path needed - positions are different");

            // Convert return path to modifiable ArrayList
            List<PositionDto> returnPath = new ArrayList<>(calculateAStarPath(currentPosition, servicePoint, restrictedAreas, drone));

            if (!returnPath.isEmpty() && !isSamePosition(returnPath.get(0), currentPosition)) {
                returnPath.add(0, currentPosition);
            }

            int returnMoves = returnPath.size() - 1;
            totalMoves += returnMoves;

            totalMoves += 1;

            System.out.println("Return path calculated: " + returnPath.size() + " points, " + returnMoves + " moves + 1 hover move");
            System.out.println("Final total moves: " + totalMoves);

            if (totalMoves > drone.getCapability().getMaxMoves()) {
                throw new IllegalArgumentException("Drone " + drone.getId() + " total movement count exceeded: " +
                        totalMoves + " > " + drone.getCapability().getMaxMoves());
            }

            if (!returnPath.isEmpty()) {
                PositionDto lastPosition = returnPath.get(returnPath.size() - 1);
                returnPath.add(new PositionDto(lastPosition.getLng(), lastPosition.getLat()));
                System.out.println("Added hover point at service point (return completion) (+1 move)");
            }

            // Prevent IndexOutOfBoundsException if returnPath is empty
            if (returnPath.isEmpty()) {
                System.out.println("WARNING: Return path is empty, using default service point path");
                returnPath.add(servicePoint);
                returnPath.add(servicePoint);
            }

            PositionDto finalPosition = returnPath.get(returnPath.size() - 1);
            boolean returnedToServicePoint = isSamePosition(finalPosition, servicePoint);
            System.out.println("Return verification - Final position matches service point: " + returnedToServicePoint);

            if (!returnedToServicePoint) {
                System.out.println("WARNING: Final position (" + finalPosition.getLng() + ", " + finalPosition.getLat() +
                        ") does not match service point (" + servicePoint.getLng() + ", " + servicePoint.getLat() + ")");
                returnPath.remove(returnPath.size() - 1);
                returnPath.add(new PositionDto(servicePoint.getLng(), servicePoint.getLat()));
                returnPath.add(new PositionDto(servicePoint.getLng(), servicePoint.getLat()));
                System.out.println("Forced final position to service point");
            }

            DeliveryPathResponse.Delivery returnDelivery = new DeliveryPathResponse.Delivery();
            returnDelivery.setDeliveryId(null);
            returnDelivery.setFlightPath(returnPath);
            deliveries.add(returnDelivery);

            System.out.println("Return path added to deliveries");
        } else {
            System.out.println("No return path needed - already at service point");

            List<PositionDto> servicePointPath = new ArrayList<>();
            servicePointPath.add(new PositionDto(servicePoint.getLng(), servicePoint.getLat()));
            servicePointPath.add(new PositionDto(servicePoint.getLng(), servicePoint.getLat()));

            DeliveryPathResponse.Delivery returnDelivery = new DeliveryPathResponse.Delivery();
            returnDelivery.setDeliveryId(null);
            returnDelivery.setFlightPath(servicePointPath);
            deliveries.add(returnDelivery);

            totalMoves += 1;
            System.out.println("Added hover move at service point (no return needed) (+1 move)");
        }

        System.out.println("=== Final Path Verification ===");
        System.out.println("Total deliveries: " + deliveries.size());
        System.out.println("Total moves including hovers: " + totalMoves);

        if (!deliveries.isEmpty()) {
            List<PositionDto> firstPath = deliveries.get(0).getFlightPath();
            if (!firstPath.isEmpty()) {
                PositionDto startPoint = firstPath.get(0);
                System.out.println("Start point: (" + startPoint.getLng() + ", " + startPoint.getLat() + ")");
            }

            List<PositionDto> lastPath = deliveries.get(deliveries.size() - 1).getFlightPath();
            if (!lastPath.isEmpty()) {
                PositionDto endPoint = lastPath.get(lastPath.size() - 1);
                System.out.println("End point: (" + endPoint.getLng() + ", " + endPoint.getLat() + ")");
                System.out.println("Start and end points match: " + isSamePosition(
                        deliveries.get(0).getFlightPath().get(0), endPoint));
            }
        }

        DeliveryPathResponse.DronePath dronePath = new DeliveryPathResponse.DronePath();
        dronePath.setDroneId(drone.getId());
        dronePath.setDeliveries(deliveries);

        System.out.println("=== Single Drone Path Calculation Completed ===");
        return dronePath;
    }

    // ==============================================
    // 5. Multi-Drone Solution Module
    // ==============================================

    /**
     * Calculates optimized multi-drone solution for task delivery
     * @param allDrones list of all available drones
     * @param tasks list of tasks to deliver
     * @param taskLocations map of task locations
     * @param availableDronesInfo list of available drones at service points
     * @param restrictedAreas list of restricted areas to avoid
     * @return delivery path response for multi-drone solution
     */
    public DeliveryPathResponse calculateOptimizedMultiDroneSolution(List<Drone> allDrones,
                                                                     List<MedDispatchRec> tasks,
                                                                     Map<Integer, PositionDto> taskLocations,
                                                                     List<DroneForServicePoint> availableDronesInfo,
                                                                     List<RestrictedArea> restrictedAreas) {

        allDrones = allDrones == null ? Collections.emptyList() : allDrones;
        tasks = tasks == null ? Collections.emptyList() : tasks;
        taskLocations = taskLocations == null ? Collections.emptyMap() : taskLocations;
        availableDronesInfo = availableDronesInfo == null ? Collections.emptyList() : availableDronesInfo;
        restrictedAreas = restrictedAreas == null ? Collections.emptyList() : restrictedAreas;

        // Phase 1: Intelligent drone filtering
        List<Drone> suitableDrones = intelligentDroneFiltering(allDrones, tasks, availableDronesInfo);
        if (suitableDrones.isEmpty()) {
            return createEmptyResponse();
        }
        // Phase 2: Priority-based task assignment
        List<DroneAssignment> assignments = priorityBasedTaskAssignment(suitableDrones, tasks, taskLocations, availableDronesInfo);
        if (assignments.isEmpty()) {
            return createEmptyResponse();
        }
        // Phase 3: Independent path calculation and merging
        return calculateIndependentDronePaths(assignments, taskLocations, restrictedAreas);
    }

    List<Drone> intelligentDroneFiltering(List<Drone> allDrones,
                                          List<MedDispatchRec> tasks,
                                          List<DroneForServicePoint> availableDronesInfo) {
        List<Drone> suitableDrones = new ArrayList<>();

        // Analyze task requirements
        boolean requiresCooling = tasks.stream().anyMatch(task -> task.getRequirements().isCooling());
        boolean requiresHeating = tasks.stream().anyMatch(task -> task.getRequirements().isHeating());

        System.out.println("=== Intelligent Drone Filtering Started ===");
        System.out.println("Task Requirements Analysis - Cooling needed: " + requiresCooling + ", Heating needed: " + requiresHeating);
        System.out.println("Total task count: " + tasks.size());
        System.out.println("Total drone count: " + allDrones.size());

        // Print task details
        System.out.println("--- Task Details ---");
        for (int i = 0; i < tasks.size(); i++) {
            MedDispatchRec task = tasks.get(i);
            MedDispatchRec.Requirements req = task.getRequirements();
            System.out.println("Task " + (i + 1) + " (ID: " + task.getId() + "): " +
                    "Capacity=" + req.getCapacity() +
                    ", Cooling=" + req.isCooling() +
                    ", Heating=" + req.isHeating() +
                    ", Time=" + task.getTime());
        }

        // Print drone capacity information
        System.out.println("--- Drone Capacity Information ---");
        for (Drone drone : allDrones) {
            System.out.println("Drone " + drone.getId() +
                    ": Capacity=" + drone.getCapability().getCapacity() +
                    ", Cooling=" + drone.getCapability().getCooling() +
                    ", Heating=" + drone.getCapability().getHeating());
        }

        int timeAvailabilityCount = 0;
        int canHandleTaskCount = 0;

        for (Drone drone : allDrones) {
            // Check if drone is available at any task time
            boolean isAvailableForAnyTask = isDroneAvailableForAnyTask(drone, tasks, availableDronesInfo);

            if (!isAvailableForAnyTask) {
                System.out.println("FAIL: Drone " + drone.getId() + " not available at any task time");
                continue;
            }
            timeAvailabilityCount++;

            // Check if drone can handle at least one task
            boolean canHandleAtLeastOneTask = false;
            List<MedDispatchRec> canHandleTasks = new ArrayList<>();

            for (MedDispatchRec task : tasks) {
                // Only check drones available at the specific task time
                if (isDroneAvailableAtTime(drone, task.getDate(), task.getTime(), availableDronesInfo) &&
                        canDroneHandleSingleTask(drone, task, availableDronesInfo)) {
                    canHandleAtLeastOneTask = true;
                    canHandleTasks.add(task);
                }
            }

            if (canHandleAtLeastOneTask) {
                canHandleTaskCount++;
                suitableDrones.add(drone);
                System.out.println("PASS: Drone " + drone.getId() + " suitable for handling some tasks");
                System.out.println("   Tasks it can handle:");
                for (MedDispatchRec task : canHandleTasks) {
                    System.out.println("     - Task " + task.getId() + " (Capacity: " +
                            task.getRequirements().getCapacity() + ", Time: " + task.getTime() + ")");
                }
            } else {
                System.out.println("FAIL: Drone " + drone.getId() + " cannot handle any tasks");
            }
        }

        System.out.println("=== Filtering Results Summary ===");
        System.out.println("Time-available drones: " + timeAvailabilityCount);
        System.out.println("Task-capable drones: " + canHandleTaskCount);
        System.out.println("Final suitable drones count: " + suitableDrones.size());

        if (suitableDrones.isEmpty()) {
            System.out.println("Warning: No suitable drones found!");
        } else {
            System.out.println("Suitable drone IDs: " +
                    suitableDrones.stream().map(Drone::getId).collect(Collectors.toList()));
        }

        return suitableDrones;
    }

    private List<DroneAssignment> priorityBasedTaskAssignment(List<Drone> suitableDrones,
                                                              List<MedDispatchRec> tasks,
                                                              Map<Integer, PositionDto> taskLocations,
                                                              List<DroneForServicePoint> availableDronesInfo) {
        List<DroneAssignment> assignments = new ArrayList<>();
        List<MedDispatchRec> remainingTasks = new ArrayList<>(tasks);
        List<Drone> prioritizedDrones = prioritizeDrones(suitableDrones, tasks);
        System.out.println("Starting task assignment, remaining tasks: " + remainingTasks.size());

        for (Drone drone : prioritizedDrones) {
            if (remainingTasks.isEmpty()) break;
            PositionDto servicePoint = getServicePointForDrone(drone, availableDronesInfo);
            if (servicePoint == null) continue;

            List<MedDispatchRec> assignedTasks = assignTasksToDrone(
                    drone, remainingTasks, servicePoint, taskLocations, availableDronesInfo);

            if (!assignedTasks.isEmpty()) {
                assignments.add(new DroneAssignment(drone, assignedTasks, servicePoint));
                remainingTasks.removeAll(assignedTasks);
                System.out.println("Drone " + drone.getId() + " assigned " + assignedTasks.size() + " tasks");
                System.out.println("Remaining tasks: " + remainingTasks.size());
            }
        }

        if (!remainingTasks.isEmpty()) {
            throw new IllegalArgumentException("Unable to assign all tasks, remaining: " + remainingTasks.size());
        }
        return assignments;
    }

    private List<Drone> prioritizeDrones(List<Drone> drones, List<MedDispatchRec> tasks) {
        // Analyze task requirements
        boolean hasCoolingTasks = tasks.stream().anyMatch(t -> t.getRequirements().isCooling());
        boolean hasHeatingTasks = tasks.stream().anyMatch(t -> t.getRequirements().isHeating());

        return drones.stream()
                .sorted((d1, d2) -> {
                    // Priority 1: Special function requirement matching
                    if (hasCoolingTasks) {
                        boolean d1Cooling = d1.getCapability().getCooling();
                        boolean d2Cooling = d2.getCapability().getCooling();
                        if (d1Cooling != d2Cooling) {
                            return Boolean.compare(d2Cooling, d1Cooling); // Cooling-capable first
                        }
                    }
                    if (hasHeatingTasks) {
                        boolean d1Heating = d1.getCapability().getHeating();
                        boolean d2Heating = d2.getCapability().getHeating();
                        if (d1Heating != d2Heating) {
                            return Boolean.compare(d2Heating, d1Heating); // Heating-capable first
                        }
                    }

                    // Priority 2: Temperature match score
                    int tempMatch1 = calculateTemperatureMatchScore(d1, tasks);
                    int tempMatch2 = calculateTemperatureMatchScore(d2, tasks);
                    if (tempMatch1 != tempMatch2) {
                        return Integer.compare(tempMatch2, tempMatch1);
                    }

                    // Priority 3: Capacity (larger capacity first, can handle more tasks)
                    double cap1 = d1.getCapability().getCapacity();
                    double cap2 = d2.getCapability().getCapacity();
                    if (cap1 != cap2) {
                        return Double.compare(cap2, cap1); // Descending order
                    }

                    // Priority 4: Cost
                    double fixedCost1 = d1.getCapability().getCostInitial() + d1.getCapability().getCostFinal();
                    double fixedCost2 = d2.getCapability().getCostInitial() + d2.getCapability().getCostFinal();
                    if (fixedCost1 != fixedCost2) {
                        return Double.compare(fixedCost1, fixedCost2);
                    }

                    return Double.compare(d1.getCapability().getCostPerMove(), d2.getCapability().getCostPerMove());
                })
                .collect(Collectors.toList());
    }

    private List<MedDispatchRec> assignTasksToDrone(Drone drone, List<MedDispatchRec> remainingTasks,
                                                    PositionDto servicePoint, Map<Integer, PositionDto> taskLocations,
                                                    List<DroneForServicePoint> availableDronesInfo) {
        List<MedDispatchRec> assigned = new ArrayList<>();
        double remainingCapacity = drone.getCapability().getCapacity();
        int remainingMoves = drone.getCapability().getMaxMoves(); // Add movement count limit

        // Only filter tasks that this drone can handle at corresponding times (including round-trip movement check)
        List<MedDispatchRec> feasibleTasks = remainingTasks.stream()
                .filter(task -> {
                    PositionDto taskLocation = taskLocations.get(task.getId());
                    // Include round-trip movement count check
                    return canDroneHandleTaskWithMoves(drone, task, availableDronesInfo, servicePoint, taskLocation, true);
                })
                .collect(Collectors.toList());

        if (feasibleTasks.isEmpty()) {
            return assigned;
        }

        List<MedDispatchRec> prioritizedTasks = feasibleTasks.stream()
                .sorted((t1, t2) -> {
                    boolean t1hasTemp = t1.getRequirements().isCooling() || t1.getRequirements().isHeating();
                    boolean t2hasTemp = t2.getRequirements().isCooling() || t2.getRequirements().isHeating();
                    if (t1hasTemp != t2hasTemp) {
                        return Boolean.compare(t2hasTemp, t1hasTemp);
                    }
                    double cap1 = t1.getRequirements().getCapacity();
                    double cap2 = t2.getRequirements().getCapacity();
                    if (cap1 != cap2) {
                        return Double.compare(cap2, cap1);
                    }
                    double dist1 = ilpService.distanceCalculate(new DistanceRequest(servicePoint, taskLocations.get(t1.getId())));
                    double dist2 = ilpService.distanceCalculate(new DistanceRequest(servicePoint, taskLocations.get(t2.getId())));
                    return Double.compare(dist1, dist2);
                })
                .collect(Collectors.toList());

        for (MedDispatchRec task : prioritizedTasks) {
            double taskCapacity = task.getRequirements().getCapacity();
            PositionDto taskLocation = taskLocations.get(task.getId());

            // Calculate round-trip movement count
            double distance = ilpService.distanceCalculate(new DistanceRequest(servicePoint, taskLocation));
            int estimatedMoves = (int) Math.ceil(distance / 0.00015) * 2; // Multiply by 2 for round trip

            if (taskCapacity <= remainingCapacity && estimatedMoves <= remainingMoves) {
                assigned.add(task);
                remainingCapacity -= taskCapacity;
                remainingMoves -= estimatedMoves;
            }
        }
        return assigned;
    }

    // ==============================================
    // 6. Path Planning and A* Algorithm Module
    // ==============================================

    /**
     * Calculates A* path between two points avoiding restricted areas
     * @param start starting position
     * @param goal target position
     * @param restrictedAreas list of restricted areas to avoid
     * @param drone the drone for cost calculation
     * @return list of positions forming the path
     */
    public List<PositionDto> calculateAStarPath(PositionDto start, PositionDto goal,
                                                List<RestrictedArea> restrictedAreas, Drone drone) {

        for (RestrictedArea area : restrictedAreas) {
            if (isPointInPolygon(start, area.getVertices()) ||
                    isPointInPolygon(goal, area.getVertices())) {
                return new ArrayList<>();
            }
        }

        if (isSamePosition(start, goal)) {
            return Collections.singletonList(start);
        }

        PriorityQueue<AStarNode> openSet = new PriorityQueue<>();
        Map<PositionDto, Double> gScore = new HashMap<>();
        Map<PositionDto, AStarNode> nodeMap = new HashMap<>();

        AStarNode startNode = new AStarNode(start, heuristicCostEstimate(start, goal, drone), null);
        gScore.put(start, 0.0);
        openSet.add(startNode);
        nodeMap.put(start, startNode);

        while (!openSet.isEmpty()) {
            AStarNode current = openSet.poll();

            if (ilpService.distanceCalculate(new DistanceRequest(current.position, goal)) < 0.00015) {
                List<PositionDto> path = reconstructPath(current);
                if (!path.isEmpty() && !isSamePosition(path.get(0), start)) {
                    path.add(0, start);
                }
                return path;
            }

            for (PositionDto neighborPos : getSafeNeighbors(current.position, restrictedAreas)) {
                double distance = ilpService.distanceCalculate(new DistanceRequest(current.position, neighborPos));
                double newGScore = gScore.get(current.position) + distance;
                if (newGScore < gScore.getOrDefault(neighborPos, Double.MAX_VALUE)) {
                    gScore.put(neighborPos, newGScore);
                    double fScore = newGScore + heuristicCostEstimate(neighborPos, goal, drone);
                    AStarNode neighborNode = new AStarNode(neighborPos, fScore, current);
                    openSet.add(neighborNode);
                    nodeMap.put(neighborPos, neighborNode);
                }
            }
        }

        List<PositionDto> safePath = calculateSafeStraightPath(start, goal, restrictedAreas);

        if (!safePath.isEmpty() && !isSamePosition(safePath.get(0), start)) {
            safePath.add(0, start);
        }

        if (!safePath.isEmpty()) {
            PositionDto last = safePath.get(safePath.size() - 1);
            if (!isSamePosition(last, goal)) {
                return new ArrayList<>();
            }
        }

        return safePath;
    }

    private double heuristicCostEstimate(PositionDto from, PositionDto to, Drone drone) {
        double distance = ilpService.distanceCalculate(new DistanceRequest(from, to));
        double exactMoves = distance / 0.00015;
        double obstacleBuffer = 1.25;
        double encouragementWeight = 1.1;
        return exactMoves * obstacleBuffer * encouragementWeight * drone.getCapability().getCostPerMove();
    }

    /**
     * Reconstructs path from A* node
     * @param endNode the final node in the path
     * @return list of positions forming the path
     */
    public List<PositionDto> reconstructPath(AStarNode endNode) {
        List<PositionDto> path = new ArrayList<>();
        AStarNode current = endNode;
        while (current != null) {
            path.add(0, current.position);
            current = current.cameFrom;
        }
        return path;
    }

    /**
     * Gets safe neighboring positions for path planning
     * @param position current position
     * @param restrictedAreas list of restricted areas to avoid
     * @return list of safe neighboring positions
     */
    public List<PositionDto> getSafeNeighbors(PositionDto position, List<RestrictedArea> restrictedAreas) {
        List<PositionDto> safeNeighbors = new ArrayList<>();
        double[] angles = {0, 22.5, 45, 67.5, 90, 112.5, 135, 157.5,
                180, 202.5, 225, 247.5, 270, 292.5, 315, 337.5};
        for (double angle : angles) {
            PositionDto neighbor = ilpService.movementCalculate(new MovementRequest(position, angle));
            if (isMoveSafe(position, neighbor, restrictedAreas)) {
                safeNeighbors.add(neighbor);
            }
        }
        return safeNeighbors;
    }

    /**
     * Checks if a move between two positions is safe
     * @param from starting position
     * @param to target position
     * @param restrictedAreas list of restricted areas to avoid
     * @return true if move is safe, false otherwise
     */
    public boolean isMoveSafe(PositionDto from, PositionDto to, List<RestrictedArea> restrictedAreas) {
        for (RestrictedArea area : restrictedAreas) {
            if (isPointInPolygon(from, area.getVertices()) || isPointInPolygon(to, area.getVertices())) {
                return false;
            }
        }
        for (RestrictedArea area : restrictedAreas) {
            if (doesLineIntersectPolygon(from, to, area.getVertices())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if a line intersects with a polygon
     * @param p1 line start point
     * @param p2 line end point
     * @param polygon vertices of the polygon
     * @return true if line intersects polygon, false otherwise
     */
    public boolean doesLineIntersectPolygon(PositionDto p1, PositionDto p2, List<PositionDto> polygon) {
        int n = polygon.size();
        for (int i = 0; i < n; i++) {
            PositionDto a = polygon.get(i);
            PositionDto b = polygon.get((i + 1) % n);
            if (doLinesIntersect(p1, p2, a, b)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if two line segments intersect
     * @param p1 first line segment start
     * @param p2 first line segment end
     * @param q1 second line segment start
     * @param q2 second line segment end
     * @return true if line segments intersect, false otherwise
     */
    public boolean doLinesIntersect(PositionDto p1, PositionDto p2, PositionDto q1, PositionDto q2) {
        int o1 = orientation(p1, p2, q1);
        int o2 = orientation(p1, p2, q2);
        int o3 = orientation(q1, q2, p1);
        int o4 = orientation(q1, q2, p2);
        if (o1 != o2 && o3 != o4) return true;
        if (o1 == 0 && onSegment(p1, q1, p2)) return true;
        if (o2 == 0 && onSegment(p1, q2, p2)) return true;
        if (o3 == 0 && onSegment(q1, p1, q2)) return true;
        if (o4 == 0 && onSegment(q1, p2, q2)) return true;
        return false;
    }

    /**
     * Calculates orientation of three points
     * @param p first point
     * @param q second point
     * @param r third point
     * @return 0 if collinear, 1 if clockwise, 2 if counterclockwise
     */
    public int orientation(PositionDto p, PositionDto q, PositionDto r) {
        double val = (q.getLat() - p.getLat()) * (r.getLng() - q.getLng()) -
                (q.getLng() - p.getLng()) * (r.getLat() - q.getLat());
        if (val == 0) return 0;
        return (val > 0) ? 1 : 2;
    }

    /**
     * Checks if point q lies on segment pr
     * @param p segment start
     * @param q point to check
     * @param r segment end
     * @return true if point lies on segment, false otherwise
     */
    public boolean onSegment(PositionDto p, PositionDto q, PositionDto r) {
        return q.getLng() <= Math.max(p.getLng(), r.getLng()) &&
                q.getLng() >= Math.min(p.getLng(), r.getLng()) &&
                q.getLat() <= Math.max(p.getLat(), r.getLat()) &&
                q.getLat() >= Math.min(p.getLat(), r.getLat());
    }

    /**
     * Calculates a safe straight path between two points
     * @param start starting position
     * @param end target position
     * @param restrictedAreas list of restricted areas to avoid
     * @return list of positions forming a safe path
     */
    public List<PositionDto> calculateSafeStraightPath(PositionDto start, PositionDto end,
                                                       List<RestrictedArea> restrictedAreas) {
        List<PositionDto> path = new ArrayList<>();
        path.add(start);
        double distance = ilpService.distanceCalculate(new DistanceRequest(start, end));
        int steps = Math.max(1, (int) Math.ceil(distance / 0.00015));
        PositionDto lastSafe = start;

        for (int i = 1; i <= steps; i++) {
            double ratio = (double) i / steps;
            PositionDto candidate = new PositionDto(
                    start.getLng() + (end.getLng() - start.getLng()) * ratio,
                    start.getLat() + (end.getLat() - start.getLat()) * ratio
            );
            if (isMoveSafe(lastSafe, candidate, restrictedAreas)) {
                path.add(candidate);
                lastSafe = candidate;
            } else {
                break;
            }
        }

        if (!path.get(path.size() - 1).equals(end)) {
            path.add(end);
        }
        return path;
    }

    /**
     * Checks if a point is inside a polygon
     * @param point the point to check
     * @param polygon vertices of the polygon
     * @return true if point is inside polygon, false otherwise
     */
    public boolean isPointInPolygon(PositionDto point, List<PositionDto> polygon) {
        RegionRequest request = new RegionRequest(point, new Region("temp", polygon));
        return ilpService.isPointInPolygon(request);
    }

    // ==============================================
    // 7. Helper Methods and Utility Functions Module
    // ==============================================

    /**
     * Gets the service point location for a drone
     * @param drone the drone to find service point for
     * @param availableDronesInfo list of available drones at service points
     * @return service point location, or null if not found
     */
    public PositionDto getServicePointForDrone(Drone drone, List<DroneForServicePoint> availableDronesInfo) {
        System.out.println("Finding service point for drone " + drone.getId() + "...");
        for (DroneForServicePoint servicePointInfo : availableDronesInfo) {
            for (DroneForServicePoint.DroneAvailability droneAvail : servicePointInfo.getDrones()) {
                if (droneAvail.getId().equals(drone.getId())) {
                    System.out.println("Found drone " + drone.getId() + " at service point " + servicePointInfo.getServicePointId());
                    List<ServicePoint> allServicePoints = getServicePoints();
                    for (ServicePoint servicePoint : allServicePoints) {
                        if (servicePoint.getId() == servicePointInfo.getServicePointId()) {
                            PositionDto location = servicePoint.getLocation();
                            System.out.println("Service point location: (" + location.getLng() + ", " + location.getLat() + ")");
                            return location;
                        }
                    }
                }
            }
        }
        System.out.println("FAIL: No service point found for drone " + drone.getId());
        return null;
    }

    boolean isSamePosition(PositionDto p1, PositionDto p2) {
        if (p1 == p2) return true;
        if (p1 == null || p2 == null) return false;
        return Math.abs(p1.getLng() - p2.getLng()) < 0.00001 &&
                Math.abs(p1.getLat() - p2.getLat()) < 0.00001;
    }

    private boolean satisfiesCapacityLimit(Drone drone, List<MedDispatchRec> tasks) {
        // Correct approach: Check if drone can handle at least one task
        boolean canHandleAtLeastOneTask = tasks.stream()
                .anyMatch(task -> task.getRequirements().getCapacity() <= drone.getCapability().getCapacity());

        System.out.println("Capacity check - Drone " + drone.getId() + ": Can handle at least one task = " + canHandleAtLeastOneTask);
        return canHandleAtLeastOneTask;
    }

    private boolean satisfiesTemperatureRequirements(Drone drone, List<MedDispatchRec> tasks) {
        boolean hasCooling = false;
        boolean hasHeating = false;
        for (MedDispatchRec task : tasks) {
            MedDispatchRec.Requirements req = task.getRequirements();
            if (req.isCooling()) {
                hasCooling = true;
                if (!drone.getCapability().getCooling()) {
                    return false;
                }
            }
            if (req.isHeating()) {
                hasHeating = true;
                if (!drone.getCapability().getHeating()) {
                    return false;
                }
            }
        }
        return !(hasCooling && hasHeating);
    }

    private boolean canDroneHandleSingleTask(Drone drone, MedDispatchRec task,
                                             List<DroneForServicePoint> availableDronesInfo) {
        MedDispatchRec.Requirements req = task.getRequirements();
        if (req.isCooling() && !drone.getCapability().getCooling()) {
            return false;
        }
        if (req.isHeating() && !drone.getCapability().getHeating()) {
            return false;
        }
        if (req.getCapacity() > drone.getCapability().getCapacity()) {
            return false;
        }
        return isDroneAvailableAtTime(drone, task.getDate(), task.getTime(), availableDronesInfo);
    }

    private boolean isDroneAvailableForAnyTask(Drone drone, List<MedDispatchRec> tasks,
                                               List<DroneForServicePoint> availableDronesInfo) {
        // New logic: Drone is suitable if available at any task time
        for (MedDispatchRec task : tasks) {
            if (isDroneAvailableAtTime(drone, task.getDate(), task.getTime(), availableDronesInfo)) {
                return true;
            }
        }
        return false;
    }

    private int calculateTemperatureMatchScore(Drone drone, List<MedDispatchRec> tasks) {
        int score = 0;
        for (MedDispatchRec task : tasks) {
            if (task.getRequirements().isCooling() && drone.getCapability().getCooling()) {
                score += 2;
            }
            if (task.getRequirements().isHeating() && drone.getCapability().getHeating()) {
                score += 2;
            }
        }
        return score;
    }

    private double calculateCapacityScore(Drone drone, List<MedDispatchRec> tasks) {
        double totalCapacityNeeded = tasks.stream()
                .mapToDouble(task -> task.getRequirements().getCapacity())
                .sum();
        return drone.getCapability().getCapacity() / totalCapacityNeeded;
    }

    private DeliveryPathResponse calculateIndependentDronePaths(List<DroneAssignment> assignments,
                                                                Map<Integer, PositionDto> taskLocations,
                                                                List<RestrictedArea> restrictedAreas) {
        List<DeliveryPathResponse.DronePath> allDronePaths = new ArrayList<>();
        double totalCost = 0;
        int totalMoves = 0;
        for (DroneAssignment assignment : assignments) {
            DeliveryPathResponse.DronePath dronePath = calculateSingleDronePath(
                    assignment.getDrone(),
                    assignment.getTasks(),
                    assignment.getServicePoint(),
                    taskLocations,
                    restrictedAreas
            );
            if (dronePath != null) {
                allDronePaths.add(dronePath);
                totalCost += calculateTotalCost(dronePath, assignment.getDrone());
                totalMoves += calculateTotalMoves(dronePath);
            }
        }
        DeliveryPathResponse response = new DeliveryPathResponse();
        response.setDronePaths(allDronePaths);
        response.setTotalCost(totalCost);
        response.setTotalMoves(totalMoves);
        return response;
    }

    /**
     * Calculates total cost for a drone path
     * @param dronePath the drone path to calculate cost for
     * @param drone the drone used
     * @return total cost including fixed and variable costs
     */
    public double calculateTotalCost(DeliveryPathResponse.DronePath dronePath, Drone drone) {
        int totalMoves = calculateTotalMoves(dronePath);
        return drone.getCapability().getCostInitial() +
                drone.getCapability().getCostFinal() +
                (totalMoves * drone.getCapability().getCostPerMove());
    }

    /**
     * Calculates total moves for a drone path
     * @param dronePath the drone path to calculate moves for
     * @return total number of moves
     */
    public int calculateTotalMoves(DeliveryPathResponse.DronePath dronePath) {
        int totalMoves = 0;
        for (DeliveryPathResponse.Delivery delivery : dronePath.getDeliveries()) {
            totalMoves += delivery.getFlightPath().size() - 1;
        }
        return totalMoves;
    }

    // ==============================================
    // 8. Solution Selection and GeoJSON Conversion Module
    // ==============================================

    /**
     * Selects the better solution between single and multi-drone options
     * @param singleDrone single drone solution
     * @param multiDrone multi-drone solution
     * @return the solution with lower cost
     */
    public DeliveryPathResponse selectBetterSolution(DeliveryPathResponse singleDrone,
                                                     DeliveryPathResponse multiDrone) {
        if (singleDrone.getDronePaths().isEmpty()) {
            return multiDrone;
        }
        if (multiDrone.getDronePaths().isEmpty()) {
            return singleDrone;
        }
        if (singleDrone.getTotalCost() <= multiDrone.getTotalCost()) {
            System.out.println("Selected single drone solution, cost: " + singleDrone.getTotalCost());
            return singleDrone;
        } else {
            System.out.println("Selected multi-drone solution, cost: " + multiDrone.getTotalCost());
            return multiDrone;
        }
    }

    /**
     * Converts drone path to GeoJSON format
     * @param dronePath the drone path to convert
     * @return GeoJSON string representation
     */
    public String convertToGeoJson(DeliveryPathResponse.DronePath dronePath) {
        if (dronePath == null || dronePath.getDeliveries() == null || dronePath.getDeliveries().isEmpty()) {
            return createEmptyGeoJson();
        }
        try {
            List<PositionDto> completePath = new ArrayList<>();
            for (DeliveryPathResponse.Delivery delivery : dronePath.getDeliveries()) {
                List<PositionDto> flightPath = delivery.getFlightPath();
                if (flightPath != null && !flightPath.isEmpty()) {
                    if (completePath.isEmpty()) {
                        completePath.addAll(flightPath);
                    } else {
                        if (flightPath.size() > 1) {
                            completePath.addAll(flightPath.subList(1, flightPath.size()));
                        }
                    }
                }
            }
            return buildGeoJsonLineString(completePath, dronePath.getDroneId());
        } catch (Exception e) {
            return createEmptyGeoJson();
        }
    }

    /**
     * Converts multiple drone paths and restricted areas to a single GeoJSON
     * @param dronePaths list of drone paths to convert
     * @param restrictedAreas list of restricted areas to display
     * @return GeoJSON string with multiple features including paths and restricted areas
     */
    public String convertMultipleDronesToGeoJson(List<DeliveryPathResponse.DronePath> dronePaths,
                                                 List<RestrictedArea> restrictedAreas) {
        if ((dronePaths == null || dronePaths.isEmpty()) &&
                (restrictedAreas == null || restrictedAreas.isEmpty())) {
            return createEmptyGeoJson();
        }

        try {
            StringBuilder geoJson = new StringBuilder();
            geoJson.append("{");
            geoJson.append("\"type\": \"FeatureCollection\",");
            geoJson.append("\"features\": [");

            int featureCount = 0;

            // Add drone paths
            if (dronePaths != null) {
                for (int i = 0; i < dronePaths.size(); i++) {
                    DeliveryPathResponse.DronePath dronePath = dronePaths.get(i);

                    // Get complete path for this drone
                    List<PositionDto> completePath = new ArrayList<>();
                    for (DeliveryPathResponse.Delivery delivery : dronePath.getDeliveries()) {
                        List<PositionDto> flightPath = delivery.getFlightPath();
                        if (flightPath != null && !flightPath.isEmpty()) {
                            if (completePath.isEmpty()) {
                                completePath.addAll(flightPath);
                            } else {
                                // Avoid duplicate points, add from second point
                                if (flightPath.size() > 1) {
                                    completePath.addAll(flightPath.subList(1, flightPath.size()));
                                }
                            }
                        }
                    }

                    if (completePath.size() < 2) {
                        continue; // Skip invalid paths
                    }

                    // Add GeoJSON Feature for drone path
                    if (featureCount > 0) {
                        geoJson.append(",");
                    }

                    geoJson.append("{");
                    geoJson.append("\"type\": \"Feature\",");
                    geoJson.append("\"geometry\": {");
                    geoJson.append("\"type\": \"LineString\",");
                    geoJson.append("\"coordinates\": [");

                    for (int j = 0; j < completePath.size(); j++) {
                        PositionDto point = completePath.get(j);
                        geoJson.append("[").append(point.getLng()).append(",").append(point.getLat()).append("]");
                        if (j < completePath.size() - 1) {
                            geoJson.append(",");
                        }
                    }

                    geoJson.append("]");
                    geoJson.append("},");
                    geoJson.append("\"properties\": {");
                    geoJson.append("\"name\": \"Drone Delivery Path\",");
                    geoJson.append("\"droneId\": \"").append(dronePath.getDroneId()).append("\",");
                    geoJson.append("\"type\": \"drone_path\"");
                    geoJson.append("}");
                    geoJson.append("}");

                    featureCount++;
                }
            }

            // Add restricted areas
            if (restrictedAreas != null) {
                for (int i = 0; i < restrictedAreas.size(); i++) {
                    RestrictedArea area = restrictedAreas.get(i);
                    List<PositionDto> vertices = area.getVertices();

                    if (vertices == null || vertices.size() < 3) {
                        continue; // Skip invalid areas
                    }

                    // Add GeoJSON Feature for restricted area
                    if (featureCount > 0) {
                        geoJson.append(",");
                    }

                    geoJson.append("{");
                    geoJson.append("\"type\": \"Feature\",");
                    geoJson.append("\"geometry\": {");
                    geoJson.append("\"type\": \"Polygon\",");
                    geoJson.append("\"coordinates\": [[");

                    // Add all vertices (close the polygon by repeating the first point)
                    for (int j = 0; j <= vertices.size(); j++) {
                        PositionDto vertex = vertices.get(j % vertices.size());
                        geoJson.append("[").append(vertex.getLng()).append(",").append(vertex.getLat()).append("]");
                        if (j < vertices.size()) {
                            geoJson.append(",");
                        }
                    }

                    geoJson.append("]]");
                    geoJson.append("},");
                    geoJson.append("\"properties\": {");
                    geoJson.append("\"name\": \"").append(area.getName() != null ? area.getName() : "Restricted Area").append("\",");
                    geoJson.append("\"areaId\": ").append(area.getId()).append(",");
                    geoJson.append("\"type\": \"restricted_area\"");
                    geoJson.append("}");
                    geoJson.append("}");

                    featureCount++;
                }
            }

            geoJson.append("]");
            geoJson.append("}");

            return geoJson.toString();

        } catch (Exception e) {
            System.err.println("Error converting multiple drones and restricted areas to GeoJSON: " + e.getMessage());
            return createEmptyGeoJson();
        }
    }

    private String buildGeoJsonLineString(List<PositionDto> path, String droneId) {
        if (path == null || path.size() < 2) {
            return createEmptyGeoJson();
        }
        try {
            StringBuilder geoJson = new StringBuilder();
            geoJson.append("{");
            geoJson.append("\"type\": \"FeatureCollection\",");
            geoJson.append("\"features\": [{");
            geoJson.append("\"type\": \"Feature\",");
            geoJson.append("\"geometry\": {");
            geoJson.append("\"type\": \"LineString\",");
            geoJson.append("\"coordinates\": [");
            for (int i = 0; i < path.size(); i++) {
                PositionDto point = path.get(i);
                geoJson.append("[").append(point.getLng()).append(",").append(point.getLat()).append("]");
                if (i < path.size() - 1) {
                    geoJson.append(",");
                }
            }
            geoJson.append("]");
            geoJson.append("},");
            geoJson.append("\"properties\": {");
            geoJson.append("\"name\": \"Single Drone Delivery Path\",");
            geoJson.append("\"droneId\": \"").append(droneId).append("\"");
            geoJson.append("}");
            geoJson.append("}]");
            geoJson.append("}");
            return geoJson.toString();
        } catch (Exception e) {
            return createEmptyGeoJson();
        }
    }

    private String createEmptyGeoJson() {
        return "{\"type\":\"FeatureCollection\",\"features\":[]}";
    }

    private DeliveryPathResponse createEmptyResponse() {
        DeliveryPathResponse response = new DeliveryPathResponse();
        response.setDronePaths(new ArrayList<>());
        response.setTotalCost(0);
        response.setTotalMoves(0);
        return response;
    }

    // ==============================================
    // 9. Cost Estimation Module - For emergency dispatch
    // ==============================================

    /**
     * Estimates the cost of interrupting a drone currently performing tasks.
     * This cost mainly consists of two parts:
     * 1. Wasted cost of completed work (from service point to current position).
     * 2. Expected cost of completing remaining tasks from current position and returning to service point.
     * Total cost = wasted cost + remaining task cost.
     *
     * @param drone    drone to be interrupted
     * @param status   drone's current status information
     * @param tasks    list of all tasks the drone is performing
     * @param taskLocations mapping of all task locations
     * @return estimated interruption cost
     */
    public double estimateInterruptCost(Drone drone, DynamicDispatchService.DroneStatusDto status,
                                        List<MedDispatchRec> tasks, Map<Integer, PositionDto> taskLocations) {
        if (drone == null || status == null || tasks == null || tasks.isEmpty() || taskLocations == null) {
            return 0.0;
        }

        // If drone is not moving, interruption cost is 0
        if (status.getStatus() != DynamicDispatchService.DroneStatus.MOVING) {
            return 0.0;
        }

        PositionDto servicePoint = getServicePointForDrone(drone, readAvailableDrones());
        if (servicePoint == null) {
            System.err.println("Warning: Cannot find service point for drone " + drone.getId() + ", interruption cost estimation failed.");
            return 0.0;
        }

        PositionDto currentPosition = status.getCurrentPosition();
        if (currentPosition == null) {
            System.err.println("Warning: Drone " + drone.getId() + " current position unknown, interruption cost estimation failed.");
            return 0.0;
        }

        // --- 1. Calculate wasted cost (from service point to current position) ---
        // Create a virtual task with path from service point to current position
        List<PositionDto> wastedPath = new ArrayList<>();
        wastedPath.add(servicePoint);
        wastedPath.add(currentPosition);

        double wastedDistance = calculatePathDistance(wastedPath);
        int wastedMoves = (int) Math.ceil(wastedDistance / 0.00015);
        double wastedCost = calculateMoveCost(drone, wastedMoves);

        // --- 2. Calculate remaining task cost (from current position to completing all tasks and returning to service point) ---
        // a. Find current executing task
        Integer currentTaskId = status.getCurrentTaskId();
        if (currentTaskId == null) {
            System.err.println("Warning: Drone " + drone.getId() + " current task ID unknown, interruption cost estimation failed.");
            return wastedCost; // At least return wasted cost
        }

        // b. Filter remaining tasks (including currently executing task)
        List<MedDispatchRec> remainingTasks = new ArrayList<>();
        boolean currentTaskFound = false;
        for (MedDispatchRec task : tasks) {
            if (currentTaskFound || task.getId() == currentTaskId)  {
                remainingTasks.add(task);
                currentTaskFound = true;
            }
        }

        if (remainingTasks.isEmpty()) {
            System.err.println("Warning: No remaining tasks found for drone " + drone.getId() + ", interruption cost estimation failed.");
            return wastedCost; // At least return wasted cost
        }

        // c. Estimate total cost of remaining tasks
        // To use existing estimateMaxCost, we need to create a list containing remaining tasks and location mapping
        Map<Integer, PositionDto> remainingTaskLocations = new HashMap<>();
        for (MedDispatchRec task : remainingTasks) {
            PositionDto loc = taskLocations.get(task.getId());
            if (loc != null) {
                remainingTaskLocations.put(task.getId(), loc);
            }
        }

        // estimateMaxCost calculates cost from service point, we need to adjust starting point
        // We'll use it to calculate cost from current position
        double remainingCost = estimateMaxCostFromPoint(drone, remainingTasks, currentPosition, servicePoint, remainingTaskLocations);

        // --- 3. Calculate total interruption cost ---
        double totalInterruptCost = wastedCost + remainingCost;

        System.out.printf("[Interruption Cost Estimation] Drone: %s, Wasted cost: %.2f, Remaining task cost: %.2f, Total interruption cost: %.2f%n",
                drone.getId(), wastedCost, remainingCost, totalInterruptCost);

        return totalInterruptCost;
    }

    /**
     * Helper method: Calculate cost from specified start point to completing tasks and returning to end point.
     * This is a simplified version of estimateMaxCost that allows custom start and end points.
     */
    private double estimateMaxCostFromPoint(Drone drone, List<MedDispatchRec> tasks,
                                            PositionDto startPoint, PositionDto endPoint,
                                            Map<Integer, PositionDto> taskLocations) {
        if (drone == null || drone.getCapability() == null || startPoint == null || tasks.isEmpty()) {
            return 0.0;
        }

        Drone.DroneCapability capability = drone.getCapability();
        double costPerMove = capability.getCostPerMove() != null ? capability.getCostPerMove() : 0.0;

        // Calculate total distance from start to first task, between tasks, and from last task to end point
        double totalDistance = 0.0;
        PositionDto currentPos = startPoint;

        for (MedDispatchRec task : tasks) {
            PositionDto taskLoc = taskLocations.get(task.getId());
            if (taskLoc != null) {
                totalDistance += ilpService.distanceCalculate(new DistanceRequest(currentPos, taskLoc));
                currentPos = taskLoc;
            }
        }

        // Return from last task to end point
        totalDistance += ilpService.distanceCalculate(new DistanceRequest(currentPos, endPoint));

        int totalMoves = (int) Math.ceil(totalDistance / 0.00015);
        int hoverMoves = tasks.size(); // Hover once after each task completion

        return (totalMoves + hoverMoves) * costPerMove;
    }

    /**
     * Helper method: Calculate total distance of a path list.
     */
    private double calculatePathDistance(List<PositionDto> path) {
        double distance = 0.0;
        for (int i = 0; i < path.size() - 1; i++) {
            distance += ilpService.distanceCalculate(new DistanceRequest(path.get(i), path.get(i + 1)));
        }
        return distance;
    }

    /**
     * Helper method: Calculate movement cost based on number of moves (excluding initial and final costs).
     */
    private double calculateMoveCost(Drone drone, int moves) {
        if (drone == null || drone.getCapability() == null) {
            return 0.0;
        }
        double costPerMove = drone.getCapability().getCostPerMove() != null ? drone.getCapability().getCostPerMove() : 0.0;
        return moves * costPerMove;
    }

    // Add fields to store dynamic restricted areas
    private final List<RestrictedArea> dynamicRestrictedAreas = new ArrayList<>();
    private final Object areasLock = new Object();

    // Add restricted area management methods
    public void addRestrictedArea(RestrictedArea area) {
        synchronized (areasLock) {
            // Ensure ID is unique
            if (dynamicRestrictedAreas.stream().anyMatch(a -> a.getId() == area.getId())) {
                throw new IllegalArgumentException("Restricted area ID already exists: " + area.getId());
            }
            dynamicRestrictedAreas.add(area);
            System.out.println("SUCCESS: Restricted area added: " + area.getName() + " (ID: " + area.getId() + ")");
            System.out.println("   Vertices: " + (area.getVertices() != null ? area.getVertices().size() : 0));
        }
    }

    public void deleteRestrictedAreaByName(String areaName) {
        System.out.println("=== droneService.deleteRestrictedAreaByName ===");
        System.out.println("Area name to delete: " + areaName);

        if (areaName == null || areaName.trim().isEmpty()) {
            throw new IllegalArgumentException("Restricted area name cannot be empty");
        }

        synchronized (areasLock) {
            System.out.println("Dynamic restricted areas before deletion: " + dynamicRestrictedAreas.size());
            System.out.println("Dynamic restricted area names before deletion: " + dynamicRestrictedAreas.stream()
                    .map(area -> area.getName())
                    .collect(Collectors.joining(", ")));

            boolean removed = dynamicRestrictedAreas.removeIf(area -> {
                boolean match = areaName.equals(area.getName());
                System.out.println("Checking '" + area.getName() + "' == '" + areaName + "' ? " + match);
                return match;
            });

            System.out.println("Deletion result: " + (removed ? "SUCCESS" : "FAILED"));
            System.out.println("Dynamic restricted areas after deletion: " + dynamicRestrictedAreas.size());

            if (!removed) {
                throw new IllegalArgumentException("Restricted area does not exist: " + areaName);
            }
        }
    }

    public void clearAllRestrictedAreas() {
        synchronized (areasLock) {
            int count = dynamicRestrictedAreas.size();
            dynamicRestrictedAreas.clear();
            System.out.println("SUCCESS: All restricted areas cleared, total deleted: " + count + " areas");
        }
    }

    // Original method to get restricted areas from API (unchanged)
    private List<RestrictedArea> getRestrictedAreasFromAPI() {
        try {
            String url = baseUrl + "restricted-areas";
            System.out.println("Fetching restricted areas from: " + url);
            RestrictedArea[] restrictedAreasArray = restTemplate.getForObject(url, RestrictedArea[].class);
            if (restrictedAreasArray != null && restrictedAreasArray.length > 0) {
                System.out.println("Retrieved " + restrictedAreasArray.length + " restricted areas from API");
                return Arrays.asList(restrictedAreasArray);
            } else {
                System.out.println("No restricted areas found from API");
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("Failed to retrieve restricted areas from API: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Add method to get only dynamic restricted areas (for frontend management)
    public List<RestrictedArea> getDynamicRestrictedAreas() {
        synchronized (areasLock) {
            return new ArrayList<>(dynamicRestrictedAreas);
        }
    }
}