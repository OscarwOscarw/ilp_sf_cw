package ilp_cw1.ilp_cw1_rset;

import ilp_cw1.ilp_cw1_rset.Droneservice.DynamicDispatchService;
import ilp_cw1.ilp_cw1_rset.Droneservice.EmergencyDispatchService;
import ilp_cw1.ilp_cw1_rset.Droneservice.droneService;
import data.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/dispatch")
public class DynamicDispatchController {

    private final DynamicDispatchService dynamicDispatchService;
    private final droneService droneService;
    private final EmergencyDispatchService emergencyDispatchService;

    @Autowired
    public DynamicDispatchController(DynamicDispatchService dynamicDispatchService,
                                     droneService droneService,
                                     EmergencyDispatchService emergencyDispatchService) {
        this.dynamicDispatchService = dynamicDispatchService;
        this.droneService = droneService;
        this.emergencyDispatchService = emergencyDispatchService;
    }

    @PostConstruct
    public void init() {
        System.out.println("✅ DynamicDispatchController loaded");
    }

    @PostMapping("/simulate")
    public ResponseEntity<?> simulateDynamicPath(@RequestBody List<MedDispatchRec> medDispatchRecs) {
        if (medDispatchRecs == null || medDispatchRecs.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: Task list cannot be empty.");
        }

        try {
            System.out.println("=== Received dynamic simulation request ===");
            System.out.println("Number of tasks to simulate: " + medDispatchRecs.size());

            Map<Integer, PositionDto> taskLocations;
            try {
                taskLocations = droneService.assignTaskLocations(medDispatchRecs);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Error: " + e.getMessage());
            }

            List<RestrictedArea> restrictedAreas = droneService.getRestrictedAreas();
            List<Drone> allDrones = droneService.getAllDrones();
            List<DroneForServicePoint> availableDronesInfo = droneService.readAvailableDrones();

            System.out.println("Starting optimized delivery path calculation...");
            DeliveryPathResponse staticPathResponse = droneService.calculateOptimizedMultiDroneSolution(
                    allDrones,
                    medDispatchRecs,
                    taskLocations,
                    availableDronesInfo,
                    restrictedAreas
            );

            if (staticPathResponse.getDronePaths().isEmpty()) {
                return ResponseEntity.internalServerError().body("Unable to calculate valid delivery path, simulation cannot start.");
            }

            System.out.println("Static path calculation completed, starting dynamic simulation with " + staticPathResponse.getDronePaths().size() + " drones.");

            dynamicDispatchService.startSimulation(staticPathResponse);

            return ResponseEntity.ok().body("Dynamic simulation started successfully.");

        } catch (IllegalArgumentException e) {
            System.err.println("Simulation startup failed: " + e.getMessage());
            return ResponseEntity.badRequest().body("Simulation startup failed: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Internal server error, simulation startup failed: " + e.getMessage());
        }
    }

    @PostMapping("/emergency")
    public ResponseEntity<EmergencyHandleResult> handleEmergencyOrder(@RequestBody EmergencyDispatchRequest request) {
        try {
            System.out.println("=== Received emergency order request ===");

            if (!dynamicDispatchService.isSimulationRunning()) {
                String errorMessage = "No delivery simulation is currently running, cannot process emergency order.";
                System.out.println("❌ " + errorMessage);
                return ResponseEntity.badRequest().body(new EmergencyHandleResult(
                        null, 0.0, 0.0, errorMessage, false
                ));
            }

            Map<Drone, List<MedDispatchRec>> droneToTasksMap = dynamicDispatchService.getOngoingTasks();
            if (droneToTasksMap == null || droneToTasksMap.isEmpty()) {
                String errorMessage = "No ongoing task information retrieved.";
                System.out.println("❌ " + errorMessage);
                return ResponseEntity.internalServerError().body(new EmergencyHandleResult(
                        null, 0.0, 0.0, errorMessage, false
                ));
            }

            Map<Integer, PositionDto> taskLocations = new HashMap<>();

            droneToTasksMap.values().stream()
                    .flatMap(List::stream)
                    .forEach(task -> {
                        if (task.getDelivery() != null) {
                            taskLocations.put(task.getId(), task.getDelivery().toPositionDto());
                        }
                    });

            if (request.getEmergencyTasks() != null && !request.getEmergencyTasks().isEmpty()) {
                request.getEmergencyTasks().forEach(emergencyTask -> {
                    if (emergencyTask.getDelivery() != null) {
                        taskLocations.put(emergencyTask.getId(), emergencyTask.getDelivery().toPositionDto());
                    }
                });
            }

            EmergencyHandleResult result = emergencyDispatchService.processEmergencyRequest(
                    request,
                    droneToTasksMap,
                    taskLocations
            );

            if (result.isSuccess()) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }

        } catch (Exception e) {
            e.printStackTrace();
            EmergencyHandleResult errorResult = new EmergencyHandleResult(
                    null,
                    0.0,
                    0.0,
                    "Severe error occurred while processing emergency order: " + e.getMessage(),
                    false
            );
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }

    @PostMapping("/stop")
    public ResponseEntity<?> stopSimulation() {
        try {
            dynamicDispatchService.stopSimulation();
            return ResponseEntity.ok().body("Dynamic simulation stopped successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error stopping simulation: " + e.getMessage());
        }
    }

    @GetMapping("/drone/{droneId}/status")
    public ResponseEntity<?> getDroneStatus(@PathVariable String droneId) {
        try {
            DynamicDispatchService.DroneStatusDto statusDto = dynamicDispatchService.getCurrentDroneStatus(droneId);
            if (statusDto == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().body(statusDto);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to get drone status: " + e.getMessage());
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getAllDronesStatus() {
        try {
            List<DynamicDispatchService.DroneStatusDto> allStatuses = dynamicDispatchService.getAllCurrentDroneStatuses();
            return ResponseEntity.ok().body(allStatuses);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to get all drones status: " + e.getMessage());
        }
    }

    @GetMapping("/simulation-state")
    public ResponseEntity<?> getSimulationState() {
        try {
            return ResponseEntity.ok().body(dynamicDispatchService.isSimulationRunning());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to get simulation state: " + e.getMessage());
        }
    }

    @GetMapping("/restricted-areas")
    public ResponseEntity<List<RestrictedArea>> getRestrictedAreas() {
        try {
            List<RestrictedArea> areas = droneService.getRestrictedAreas();
            return ResponseEntity.ok(areas);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/restricted-areas")
    public ResponseEntity<?> addRestrictedArea(@RequestBody RestrictedArea area) {
        try {
            if (area.getId() == null) {
                area.setId(System.currentTimeMillis() % Integer.MAX_VALUE);
            }

            droneService.addRestrictedArea(area);
            return ResponseEntity.ok().body(area);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to add restricted area: " + e.getMessage());
        }
    }

//    @DeleteMapping("/restricted-areas/by-name/{areaName}")
//    public ResponseEntity<?> deleteRestrictedAreaByName(@PathVariable String areaName) {
//        try {
//            System.out.println("=== Delete restricted area by name request ===");
//            System.out.println("Received areaName: " + areaName);
//
//            droneService.deleteRestrictedAreaByName(areaName);
//
//            System.out.println("✅ Restricted area deleted successfully: " + areaName);
//            return ResponseEntity.ok().body("Restricted area deleted");
//
//        } catch (Exception e) {
//            System.err.println("❌ Delete restricted area exception: " + e.getMessage());
//            e.printStackTrace();
//            return ResponseEntity.internalServerError().body("Failed to delete restricted area: " + e.getMessage());
//        }
//    }

    @DeleteMapping("/restricted-areas")
    public ResponseEntity<?> clearAllRestrictedAreas() {
        try {
            droneService.clearAllRestrictedAreas();
            return ResponseEntity.ok().body("All restricted areas cleared");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to clear restricted areas: " + e.getMessage());
        }
    }

    @PostMapping("/restricted-areas/refresh")
    public ResponseEntity<?> refreshRestrictedAreas() {
        try {
            dynamicDispatchService.refreshRestrictedAreas();
            return ResponseEntity.ok().body("Restricted areas data refreshed");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to refresh restricted areas: " + e.getMessage());
        }
    }

//    @PostMapping("/emergency/confirm-bypass")
//    public ResponseEntity<?> confirmRestrictedAreaBypass(@RequestBody BypassConfirmationRequest request) {
//        try {
//            System.out.println("=== Received restricted area bypass confirmation request ===");
//            System.out.println("Task ID: " + request.getTaskId() + ", Confirmation result: " + request.isConfirmed());
//
//            if (!request.isConfirmed()) {
//                String message = "Order " + request.getTaskId() + " cannot fly through restricted area, order cannot be delivered";
//                System.out.println("❌ " + message);
//                return ResponseEntity.ok().body(new BypassConfirmationResult(
//                        false,
//                        message,
//                        request.getTaskId(),
//                        request.getRestrictedAreaName()
//                ));
//            }
//
//            EmergencyDispatchRequest originalRequest = request.getOriginalRequest();
//            if (originalRequest == null) {
//                return ResponseEntity.badRequest().body(new BypassConfirmationResult(
//                        false,
//                        "Missing original request data, cannot reprocess order",
//                        request.getTaskId(),
//                        request.getRestrictedAreaName()
//                ));
//            }
//
//            originalRequest.setBypassRestrictedAreas(true);
//
//            EmergencyHandleResult result = processEmergencyOrderInternal(originalRequest);
//
//            if (result.isSuccess()) {
//                return ResponseEntity.ok().body(new BypassConfirmationResult(
//                        true,
//                        "Emergency order assigned successfully!" + result.getMessage(),
//                        request.getTaskId(),
//                        request.getRestrictedAreaName()
//                ));
//            } else {
//                return ResponseEntity.ok().body(new BypassConfirmationResult(
//                        false,
//                        "Order processing failed: " + result.getMessage(),
//                        request.getTaskId(),
//                        request.getRestrictedAreaName()
//                ));
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.internalServerError().body(new BypassConfirmationResult(
//                    false,
//                    "Error processing confirmation request: " + e.getMessage(),
//                    request.getTaskId(),
//                    null
//            ));
//        }
//    }

    private EmergencyHandleResult processEmergencyOrderInternal(EmergencyDispatchRequest request) {
        try {
            System.out.println("=== Internal emergency order processing ===");
            System.out.println("Bypass restricted areas flag: " + request.isBypassRestrictedAreas());

            if (!dynamicDispatchService.isSimulationRunning()) {
                String errorMessage = "No delivery simulation is currently running, cannot process emergency order.";
                System.out.println("❌ " + errorMessage);
                return new EmergencyHandleResult(null, 0.0, 0.0, errorMessage, false);
            }

            Map<Drone, List<MedDispatchRec>> droneToTasksMap = dynamicDispatchService.getOngoingTasks();
            if (droneToTasksMap == null || droneToTasksMap.isEmpty()) {
                String errorMessage = "No ongoing task information retrieved.";
                System.out.println("❌ " + errorMessage);
                return new EmergencyHandleResult(null, 0.0, 0.0, errorMessage, false);
            }

            Map<Integer, PositionDto> taskLocations = new HashMap<>();

            droneToTasksMap.values().stream()
                    .flatMap(List::stream)
                    .forEach(task -> {
                        if (task.getDelivery() != null) {
                            taskLocations.put(task.getId(), task.getDelivery().toPositionDto());
                        }
                    });

            if (request.getEmergencyTasks() != null && !request.getEmergencyTasks().isEmpty()) {
                request.getEmergencyTasks().forEach(emergencyTask -> {
                    if (emergencyTask.getDelivery() != null) {
                        taskLocations.put(emergencyTask.getId(), emergencyTask.getDelivery().toPositionDto());
                    }
                });
            }

            return emergencyDispatchService.processEmergencyRequestWithBypass(
                    request,
                    droneToTasksMap,
                    taskLocations,
                    request.isBypassRestrictedAreas()
            );

        } catch (Exception e) {
            e.printStackTrace();
            return new EmergencyHandleResult(
                    null,
                    0.0,
                    0.0,
                    "Severe error occurred while processing emergency order: " + e.getMessage(),
                    false
            );
        }
    }

    @PostMapping("/emergency/with-bypass")
    public ResponseEntity<EmergencyHandleResult> handleEmergencyOrderWithBypass(@RequestBody EmergencyDispatchRequest request) {
        EmergencyHandleResult result = processEmergencyOrderInternal(request);
        return ResponseEntity.ok(result);
    }

}