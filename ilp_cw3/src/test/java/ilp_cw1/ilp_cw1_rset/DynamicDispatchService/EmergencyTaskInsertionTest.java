package ilp_cw1.ilp_cw1_rset.DynamicDispatchService;

import data.*;
import ilp_cw1.ilp_cw1_rset.Droneservice.DynamicDispatchService;
import ilp_cw1.ilp_cw1_rset.Droneservice.droneService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EmergencyTaskInsertionTest {

    @Mock
    private droneService droneService;

    @InjectMocks
    private DynamicDispatchService dynamicDispatchService;

    @BeforeEach
    public void setup() {
        when(droneService.getRestrictedAreas()).thenReturn(Collections.emptyList());
    }

    private DeliveryPathResponse createSimulationWithDrone(String droneId, int taskId) {
        DeliveryPathResponse response = new DeliveryPathResponse();
        DeliveryPathResponse.DronePath dronePath = new DeliveryPathResponse.DronePath();
        dronePath.setDroneId(droneId);

        DeliveryPathResponse.Delivery delivery = new DeliveryPathResponse.Delivery();
        delivery.setDeliveryId(taskId);
        delivery.setFlightPath(Arrays.asList(
                new PositionDto(0.0, 0.0),
                new PositionDto(0.001, 0.001),
                new PositionDto(0.002, 0.002)
        ));

        dronePath.setDeliveries(Collections.singletonList(delivery));
        response.setDronePaths(Collections.singletonList(dronePath));
        return response;
    }

    private DeliveryPathResponse.Delivery createEmergencyTask(int taskId) {
        DeliveryPathResponse.Delivery emergencyTask = new DeliveryPathResponse.Delivery();
        emergencyTask.setDeliveryId(taskId);
        emergencyTask.setFlightPath(Arrays.asList(
                new PositionDto(0.003, 0.003),
                new PositionDto(0.004, 0.004)
        ));
        return emergencyTask;
    }

    @Test
    public void EMER_001_insertEmergencyTask_ValidDrone_TaskInserted() {
        DeliveryPathResponse response = createSimulationWithDrone("drone-1", 1001);
        dynamicDispatchService.startSimulation(response);
        DeliveryPathResponse.Delivery emergencyTask = createEmergencyTask(9999);

        dynamicDispatchService.insertEmergencyTask("drone-1", emergencyTask);

        DynamicDispatchService.DroneStatusDto status = dynamicDispatchService.getCurrentDroneStatus("drone-1");
        assertNotNull(status);
        assertTrue(status.isProcessingEmergency());
        assertEquals(Integer.valueOf(9999), status.getCurrentEmergencyTaskId());
    }

    @Test
    public void EMER_002_insertEmergencyTask_InvalidDrone_ThrowsException() {
        DeliveryPathResponse response = createSimulationWithDrone("drone-1", 1001);
        dynamicDispatchService.startSimulation(response);
        DeliveryPathResponse.Delivery emergencyTask = createEmergencyTask(9999);

        assertThrows(NoSuchElementException.class, () -> {
            dynamicDispatchService.insertEmergencyTask("invalid-drone", emergencyTask);
        });
    }

    @Test
    public void EMER_003_insertEmergencyTask_NullTask_ThrowsException() {
        DeliveryPathResponse response = createSimulationWithDrone("drone-1", 1001);
        dynamicDispatchService.startSimulation(response);

        assertThrows(IllegalArgumentException.class, () -> {
            dynamicDispatchService.insertEmergencyTask("drone-1", null);
        });
    }

    @Test
    public void EMER_004_insertEmergencyTask_NullTaskId_ThrowsException() {
        DeliveryPathResponse response = createSimulationWithDrone("drone-1", 1001);
        dynamicDispatchService.startSimulation(response);

        DeliveryPathResponse.Delivery emergencyTask = new DeliveryPathResponse.Delivery();
        emergencyTask.setDeliveryId(null);

        assertThrows(IllegalArgumentException.class, () -> {
            dynamicDispatchService.insertEmergencyTask("drone-1", emergencyTask);
        });
    }

    @Test
    public void EMER_005_insertEmergencyTask_MultipleDrones_OnlyAffectsTarget() {
        // Create simulation with two drones
        DeliveryPathResponse response = new DeliveryPathResponse();
        List<DeliveryPathResponse.DronePath> dronePaths = new ArrayList<>();

        // Drone 1
        DeliveryPathResponse.DronePath dronePath1 = new DeliveryPathResponse.DronePath();
        dronePath1.setDroneId("drone-1");
        DeliveryPathResponse.Delivery delivery1 = new DeliveryPathResponse.Delivery();
        delivery1.setDeliveryId(1001);
        delivery1.setFlightPath(Arrays.asList(new PositionDto(0.0, 0.0), new PositionDto(0.001, 0.001)));
        dronePath1.setDeliveries(Collections.singletonList(delivery1));
        dronePaths.add(dronePath1);

        // Drone 2
        DeliveryPathResponse.DronePath dronePath2 = new DeliveryPathResponse.DronePath();
        dronePath2.setDroneId("drone-2");
        DeliveryPathResponse.Delivery delivery2 = new DeliveryPathResponse.Delivery();
        delivery2.setDeliveryId(1002);
        delivery2.setFlightPath(Arrays.asList(new PositionDto(1.0, 1.0), new PositionDto(1.001, 1.001)));
        dronePath2.setDeliveries(Collections.singletonList(delivery2));
        dronePaths.add(dronePath2);

        response.setDronePaths(dronePaths);
        dynamicDispatchService.startSimulation(response);

        // Insert emergency task only to drone-1
        DeliveryPathResponse.Delivery emergencyTask = createEmergencyTask(9999);
        dynamicDispatchService.insertEmergencyTask("drone-1", emergencyTask);

        // Assert only drone-1 has emergency
        DynamicDispatchService.DroneStatusDto status1 = dynamicDispatchService.getCurrentDroneStatus("drone-1");
        DynamicDispatchService.DroneStatusDto status2 = dynamicDispatchService.getCurrentDroneStatus("drone-2");

        assertTrue(status1.isProcessingEmergency());
        assertFalse(status2.isProcessingEmergency());
    }
}