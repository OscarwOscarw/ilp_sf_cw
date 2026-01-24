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

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DroneStateManagementTest {

    @Mock
    private droneService droneService;

    @InjectMocks
    private DynamicDispatchService dynamicDispatchService;

    @BeforeEach
    public void setup() {
        when(droneService.getRestrictedAreas()).thenReturn(Collections.emptyList());
    }

    private DeliveryPathResponse.DronePath createDronePath(String droneId) {
        DeliveryPathResponse.DronePath dronePath = new DeliveryPathResponse.DronePath();
        dronePath.setDroneId(droneId);

        DeliveryPathResponse.Delivery delivery = new DeliveryPathResponse.Delivery();
        delivery.setDeliveryId(1001);
        delivery.setFlightPath(Arrays.asList(
                new PositionDto(0.0, 0.0),
                new PositionDto(0.001, 0.001)
        ));

        dronePath.setDeliveries(Collections.singletonList(delivery));
        return dronePath;
    }

    @Test
    public void STATE_001_addDroneState_IdleDrone_AddsSuccessfully() {
        // Start simulation first
        DeliveryPathResponse response = new DeliveryPathResponse();
        response.setDronePaths(Collections.singletonList(createDronePath("drone-1")));
        dynamicDispatchService.startSimulation(response);
        assertTrue(dynamicDispatchService.isSimulationRunning());

        // Add new drone state
        DeliveryPathResponse.DronePath newDronePath = createDronePath("drone-2");
        dynamicDispatchService.addDroneState("drone-2", newDronePath);

        // Verify both drones exist
        assertNotNull(dynamicDispatchService.getCurrentDroneStatus("drone-1"));
        assertNotNull(dynamicDispatchService.getCurrentDroneStatus("drone-2"));
    }

    @Test
    public void STATE_002_addDroneState_ExistingDrone_NoDuplication() {
        DeliveryPathResponse response = new DeliveryPathResponse();
        response.setDronePaths(Collections.singletonList(createDronePath("drone-1")));
        dynamicDispatchService.startSimulation(response);

        // Try to add same drone again
        DeliveryPathResponse.DronePath sameDronePath = createDronePath("drone-1");
        dynamicDispatchService.addDroneState("drone-1", sameDronePath);

        // Should not throw exception, just skip
        assertNotNull(dynamicDispatchService.getCurrentDroneStatus("drone-1"));
    }

    @Test
    public void STATE_003_addDroneState_SimulationNotRunning_StartsSimulation() {
        assertFalse(dynamicDispatchService.isSimulationRunning());

        // Add drone state when simulation not running
        DeliveryPathResponse.DronePath dronePath = createDronePath("drone-1");
        dynamicDispatchService.addDroneState("drone-1", dronePath);

        assertNotNull(dynamicDispatchService.getCurrentDroneStatus("drone-1"));
    }

    @Test
    public void STATE_004_getCurrentDroneStatus_ValidDrone_ReturnsStatus() {
        DeliveryPathResponse response = new DeliveryPathResponse();
        response.setDronePaths(Collections.singletonList(createDronePath("drone-1")));
        dynamicDispatchService.startSimulation(response);

        DynamicDispatchService.DroneStatusDto status = dynamicDispatchService.getCurrentDroneStatus("drone-1");

        assertNotNull(status);
        assertEquals("drone-1", status.getDroneId());
        assertNotNull(status.getCurrentPosition());
        assertEquals(DynamicDispatchService.DroneStatus.READY, status.getStatus());
    }

    @Test
    public void STATE_005_getCurrentDroneStatus_InvalidDrone_ReturnsNull() {
        DeliveryPathResponse response = new DeliveryPathResponse();
        response.setDronePaths(Collections.singletonList(createDronePath("drone-1")));
        dynamicDispatchService.startSimulation(response);

        DynamicDispatchService.DroneStatusDto status = dynamicDispatchService.getCurrentDroneStatus("invalid-drone");

        assertNull(status);
    }

    @Test
    public void STATE_006_getAllCurrentDroneStatuses_ReturnsAllStatuses() {
        // Create simulation with multiple drones
        DeliveryPathResponse response = new DeliveryPathResponse();
        DeliveryPathResponse.DronePath dronePath1 = createDronePath("drone-1");
        DeliveryPathResponse.DronePath dronePath2 = createDronePath("drone-2");
        response.setDronePaths(Arrays.asList(dronePath1, dronePath2));

        dynamicDispatchService.startSimulation(response);

        var allStatuses = dynamicDispatchService.getAllCurrentDroneStatuses();

        assertEquals(2, allStatuses.size());
        assertTrue(allStatuses.stream().anyMatch(s -> s.getDroneId().equals("drone-1")));
        assertTrue(allStatuses.stream().anyMatch(s -> s.getDroneId().equals("drone-2")));
    }

    @Test
    public void STATE_007_getAllCurrentDroneStatuses_EmptySimulation_ReturnsEmptyList() {
        var allStatuses = dynamicDispatchService.getAllCurrentDroneStatuses();

        assertNotNull(allStatuses);
        assertTrue(allStatuses.isEmpty());
    }
}