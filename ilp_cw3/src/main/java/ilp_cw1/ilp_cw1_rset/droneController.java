package ilp_cw1.ilp_cw1_rset;

import data.*;
import ilp_cw1.ilp_cw1_rset.Droneservice.droneService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;


/**
 * REST controller for managing drone operations and queries
 * Provides endpoints for drone filtering, querying, and delivery path calculation
 */
@RestController
@RequestMapping("/api/v1")
public class droneController {
    private final droneService droneService;

    /**
     * Constructor for drone controller
     * @param droneService the drone service instance
     */
    public droneController(droneService droneService) {

        this.droneService = droneService;
    }

    /**
     * Retrieves drone IDs based on cooling capability
     * @param state the desired cooling state (true for cooling enabled, false for disabled)
     * @return list of drone IDs that match the specified cooling state
     */
    @GetMapping("/dronesWithCooling/{state}")
    public ResponseEntity<List<String>> getDronesWithCooling(@PathVariable String state) {
        // Always return 200, even for invalid inputs
        List<String> result = new ArrayList<>();

        // Handle invalid state parameter gracefully
        if (state != null && (state.equalsIgnoreCase("true") || state.equalsIgnoreCase("false"))) {
            boolean coolingState = Boolean.parseBoolean(state);
            List<Drone> allDrones = droneService.getAllDrones();
            for (Drone drone : allDrones) {
                boolean cooling = drone.hasCooling();
                if (cooling == coolingState){
                    result.add(drone.getId());
                }
            }
        }
        // For invalid state, return empty list with 200 status
        return ResponseEntity.ok(result);
    }

    /**
     * Retrieves detailed information for a specific drone
     * @param id the unique identifier of the drone
     * @return ResponseEntity containing drone details if found, or 404 if not found
     */
    @GetMapping("/droneDetails/{id}")
    public ResponseEntity<Drone> getDroneDetails(@PathVariable String id) {
        // This is the ONLY endpoint that can return 404
        if (id == null || id.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<Drone> allDrones = droneService.getAllDrones();

        Optional<Drone> foundDrone = allDrones.stream()
                .filter(drone -> id.equals(drone.getId()))
                .findFirst();

        return foundDrone
                .map(drone -> ResponseEntity.ok(drone))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Queries drones based on attribute value matching
     * @param attributeName the name of the attribute to filter by
     * @param attributeValue the value to match against the attribute
     * @return list of drone IDs that satisfy the attribute condition
     */
    @GetMapping("/queryAsPath/{attributeName}/{attributeValue}")
    public ResponseEntity<List<String>> queryAsPath(@PathVariable String attributeName, @PathVariable String attributeValue) {
        List<String> result = new ArrayList<>();

        // Always return 200, handle invalid inputs gracefully
        if (attributeName != null && attributeValue != null) {
            List<Drone> allDrones = droneService.getAllDrones();
            String parameter = attributeName.toLowerCase();
            for (Drone drone : allDrones) {
                try {
                    Object droneValue = droneService.getAttributeValue(drone, parameter);
                    if (droneService.compareByType(droneValue, attributeValue)) {
                        result.add(drone.getId());
                        System.out.println("Drone ID: " + drone.getId() + ", " + parameter + " = " + droneValue);
                    }
                } catch (Exception e) {
                    // Ignore and continue - invalid attribute name
                }
            }
        }
        return ResponseEntity.ok(result);
    }


    /**
     * Queries drones based on multiple conditions
     * @param queryRequests list of query conditions with attributes, operators, and values
     * @return list of drone IDs that satisfy all conditions
     */
    @PostMapping("/query")
    public ResponseEntity<List<String>> queryDrones(@RequestBody(required = false) @org.springframework.lang.Nullable List<QueryRequest> queryRequests) {
        List<String> result = new ArrayList<>();

        if (queryRequests == null || queryRequests.isEmpty()) {
            System.out.println("Received null or empty queryRequests, returning []");
            System.out.flush();
            return ResponseEntity.ok(result);
        }

        List<Drone> allDrones = droneService.getAllDrones();

        for (Drone drone : allDrones) {
            boolean satisfiesAllConditions = true;

            for (QueryRequest queryRequest : queryRequests) {
                if (queryRequest == null ||
                        queryRequest.getAttribute() == null ||
                        queryRequest.getOperator() == null ||
                        queryRequest.getValue() == null) {
                    satisfiesAllConditions = false;
                    break;
                }

                String attribute = queryRequest.getAttribute().toLowerCase();
                String operator = queryRequest.getOperator();
                String value = queryRequest.getValue();

                try {
                    Object droneValue = droneService.getAttributeValue(drone, attribute);
                    boolean conditionSatisfied = droneService.compareWithOperator(droneValue, value, operator);
                    if (!conditionSatisfied) {
                        satisfiesAllConditions = false;
                        break;
                    }
                } catch (Exception e) {
                    satisfiesAllConditions = false;
                    break;
                }
            }

            if (satisfiesAllConditions) {
                result.add(drone.getId());
                System.out.println("Matched Drone ID: " + drone.getId());
                System.out.flush();
            }
        }

        return ResponseEntity.ok(result);
    }



    /**
     * Queries available drones that can handle medical dispatch tasks
     * @param medDispatchRecs list of medical dispatch tasks with requirements
     * @return list of drone IDs capable of handling all tasks
     */
    @PostMapping("/queryAvailableDrones")
    public ResponseEntity<List<String>> queryAvailableDrones(@RequestBody List<MedDispatchRec> medDispatchRecs) {
        List<String> result = new ArrayList<>();

        // Always return 200, handle invalid inputs gracefully
        if (medDispatchRecs != null && !medDispatchRecs.isEmpty()) {
            boolean allTasksValid = true;

            // Validate tasks but don't throw exceptions
            for (MedDispatchRec task : medDispatchRecs) {
                if (task == null || task.getId() == 0 || task.getRequirements() == null) {
                    allTasksValid = false;
                    break;
                }
                try {
                    if (task.getRequirements().getCapacity() <= 0) {
                        allTasksValid = false;
                        break;
                    }
                } catch (Exception e) {
                    allTasksValid = false;
                    break;
                }

                Boolean cooling = task.getRequirements().isCooling();
                Boolean heating = task.getRequirements().isHeating();
                if (cooling != null && heating != null && cooling && heating) {
                    allTasksValid = false;
                    break;
                }
            }

            // Only process if all tasks are valid
            if (allTasksValid) {
                List<Drone> allDrones = droneService.getAllDrones();
                List<DroneForServicePoint> droneForServicePoints = droneService.readAvailableDrones();
                Map<Integer, PositionDto> taskLocations;
                try {
                    taskLocations = droneService.assignTaskLocations(medDispatchRecs);
                } catch (IllegalArgumentException e) {
                    taskLocations = new HashMap<>();
                }

                for (Drone drone : allDrones) {
                    boolean satisfiesAllConditions = true;
                    PositionDto servicePoint = droneService.getServicePointForDrone(drone, droneForServicePoints);

                    if (servicePoint == null) {
                        continue;
                    }

                    for (MedDispatchRec medDispatchRec : medDispatchRecs) {
                        PositionDto taskLocation = taskLocations.get(medDispatchRec.getId());

                        if (taskLocation == null) {
                            if (!droneService.canDroneHandleTask(drone, medDispatchRec, droneForServicePoints)) {
                                satisfiesAllConditions = false;
                                break;
                            }
                        } else {
                            if (!droneService.canDroneHandleTaskWithMoves(drone, medDispatchRec, droneForServicePoints,
                                    servicePoint, taskLocation, true)) {
                                satisfiesAllConditions = false;
                                break;
                            }
                        }
                    }

                    if (satisfiesAllConditions) {
                        result.add(drone.getId());
                    }
                }
            }
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Calculates optimal delivery path for medical dispatch tasks
     * @param medDispatchRecs list of medical dispatch tasks
     * @return delivery path response with optimal solution
     */
    @PostMapping("/calcDeliveryPath")
    public ResponseEntity<DeliveryPathResponse> calcDeliveryPath(@RequestBody List<MedDispatchRec> medDispatchRecs) {
        DeliveryPathResponse result = new DeliveryPathResponse();
        result.setTotalCost(0.0);
        result.setTotalMoves(0);
        result.setDronePaths(new ArrayList<>());

        // Always return 200, handle invalid inputs gracefully
        if (medDispatchRecs != null && !medDispatchRecs.isEmpty()) {
            boolean allTasksValid = true;

            // Validate tasks but don't throw exceptions
            for (MedDispatchRec task : medDispatchRecs) {
                if (task == null || task.getId() == 0 || task.getRequirements() == null) {
                    allTasksValid = false;
                    break;
                }
                try {
                    if (task.getRequirements().getCapacity() <= 0) {
                        allTasksValid = false;
                        break;
                    }
                } catch (Exception e) {
                    allTasksValid = false;
                    break;
                }

                Boolean cooling = task.getRequirements().isCooling();
                Boolean heating = task.getRequirements().isHeating();
                if (cooling != null && heating != null && cooling && heating) {
                    allTasksValid = false;
                    break;
                }
            }

            // Only process if all tasks are valid
            if (allTasksValid) {
                try {
                    List<Drone> allDrones = droneService.getAllDrones();
                    List<DroneForServicePoint> availableDronesInfo = droneService.readAvailableDrones();
                    Map<Integer, PositionDto> taskLocations = droneService.assignTaskLocations(medDispatchRecs);
                    List<RestrictedArea> restrictedAreas = droneService.getRestrictedAreas();

                    DeliveryPathResponse singleDroneResponse = droneService.findAndBuildSingleDroneResponse(
                            allDrones, medDispatchRecs, taskLocations, availableDronesInfo, restrictedAreas);

                    DeliveryPathResponse multiDroneResponse = droneService.calculateOptimizedMultiDroneSolution(
                            allDrones, medDispatchRecs, taskLocations, availableDronesInfo, restrictedAreas);

                    result = droneService.selectBetterSolution(singleDroneResponse, multiDroneResponse);
                } catch (Exception e) {
                    // If calculation fails, return empty result with 200 status
                    result.setTotalCost(0.0);
                    result.setTotalMoves(0);
                    result.setDronePaths(new ArrayList<>());
                }
            }
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Calculates delivery path and returns result in GeoJSON format
     * @param medDispatchRecs list of medical dispatch tasks
     * @return GeoJSON string representing the delivery path
     */
    @PostMapping("/calcDeliveryPathAsGeoJson")
    public ResponseEntity<String> calcDeliveryPathAsGeoJson(@RequestBody List<MedDispatchRec> medDispatchRecs) {
        String result = "{}"; // Empty GeoJSON as default

        // Always return 200, handle invalid inputs gracefully
        if (medDispatchRecs != null && !medDispatchRecs.isEmpty()) {
            boolean allTasksValid = true;

            // Validate tasks but don't throw exceptions
            for (MedDispatchRec task : medDispatchRecs) {
                if (task == null || task.getId() == 0 || task.getRequirements() == null) {
                    allTasksValid = false;
                    break;
                }
                try {
                    if (task.getRequirements().getCapacity() <= 0) {
                        allTasksValid = false;
                        break;
                    }
                } catch (Exception e) {
                    allTasksValid = false;
                    break;
                }

                Boolean cooling = task.getRequirements().isCooling();
                Boolean heating = task.getRequirements().isHeating();
                if (cooling != null && heating != null && cooling && heating) {
                    allTasksValid = false;
                    break;
                }
            }

            // Only process if all tasks are valid
            if (allTasksValid) {
                try {
                    List<Drone> allDrones = droneService.getAllDrones();
                    List<DroneForServicePoint> availableDronesInfo = droneService.readAvailableDrones();
                    Map<Integer, PositionDto> taskLocations = droneService.assignTaskLocations(medDispatchRecs);
                    List<RestrictedArea> restrictedAreas = droneService.getRestrictedAreas();

                    DeliveryPathResponse singleDroneResponse = droneService.findAndBuildSingleDroneResponse(
                            allDrones, medDispatchRecs, taskLocations, availableDronesInfo, restrictedAreas);

                    DeliveryPathResponse multiDroneResponse = droneService.calculateOptimizedMultiDroneSolution(
                            allDrones, medDispatchRecs, taskLocations, availableDronesInfo, restrictedAreas);

                    DeliveryPathResponse finalResponse = droneService.selectBetterSolution(singleDroneResponse, multiDroneResponse);

                    if (finalResponse.getDronePaths() != null && !finalResponse.getDronePaths().isEmpty()) {
                        result = droneService.convertMultipleDronesToGeoJson(finalResponse.getDronePaths(), restrictedAreas);
                    } else {

                        result = droneService.convertMultipleDronesToGeoJson(new ArrayList<>(), restrictedAreas);
                    }
                } catch (Exception e) {
                    System.err.println("Error in calcDeliveryPathAsGeoJson: " + e.getMessage());
                    e.printStackTrace();
                    // If conversion fails, return empty GeoJSON with 200 status
                    result = "{}";
                }
            }
        }
        return ResponseEntity.ok(result);
    }


}