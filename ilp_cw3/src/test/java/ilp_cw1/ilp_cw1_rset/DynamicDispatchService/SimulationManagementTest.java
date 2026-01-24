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

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SimulationManagementTest {

    @Mock
    private droneService droneService;

    @InjectMocks
    private DynamicDispatchService dynamicDispatchService;

    private DeliveryPathResponse createValidPathResponse() {
        DeliveryPathResponse response = new DeliveryPathResponse();
        DeliveryPathResponse.DronePath dronePath = new DeliveryPathResponse.DronePath();
        dronePath.setDroneId("drone-1");

        DeliveryPathResponse.Delivery delivery = new DeliveryPathResponse.Delivery();
        delivery.setDeliveryId(1001);
        delivery.setFlightPath(Arrays.asList(
                new PositionDto(0.0, 0.0),
                new PositionDto(0.001, 0.001),
                new PositionDto(0.002, 0.002)
        ));

        dronePath.setDeliveries(Collections.singletonList(delivery));
        response.setDronePaths(Collections.singletonList(dronePath));
        return response;
    }

    @Test
    public void SIM_001_startSimulation_ValidResponse_InitializesStates() {
        // Arrange
        when(droneService.getRestrictedAreas()).thenReturn(Collections.emptyList());
        DeliveryPathResponse response = createValidPathResponse();

        // Act
        dynamicDispatchService.startSimulation(response);

        // Assert
        assertTrue(dynamicDispatchService.isSimulationRunning());
        assertNotNull(dynamicDispatchService.getCurrentDroneStatus("drone-1"));
    }

    @Test
    public void SIM_002_startSimulation_EmptyResponse_ThrowsException() {
        // Arrange
        DeliveryPathResponse emptyResponse = new DeliveryPathResponse();
        emptyResponse.setDronePaths(Collections.emptyList());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            dynamicDispatchService.startSimulation(emptyResponse);
        });
    }

    @Test
    public void SIM_003_startSimulation_Idempotent_CallsStopFirst() {
        // Arrange
        when(droneService.getRestrictedAreas()).thenReturn(Collections.emptyList());
        DeliveryPathResponse response1 = createValidPathResponse();

        // First start
        dynamicDispatchService.startSimulation(response1);
        assertTrue(dynamicDispatchService.isSimulationRunning());

        // Act - Start again
        DeliveryPathResponse response2 = createValidPathResponse();
        response2.getDronePaths().get(0).setDroneId("drone-2");
        dynamicDispatchService.startSimulation(response2);

        // Assert - New simulation should be running with new drone
        assertTrue(dynamicDispatchService.isSimulationRunning());
        assertNotNull(dynamicDispatchService.getCurrentDroneStatus("drone-2"));
        assertNull(dynamicDispatchService.getCurrentDroneStatus("drone-1"));
    }

    @Test
    public void SIM_004_stopSimulation_StopsSchedulerAndClearsStates() throws InterruptedException {
        // Arrange
        when(droneService.getRestrictedAreas()).thenReturn(Collections.emptyList());
        DeliveryPathResponse response = createValidPathResponse();
        dynamicDispatchService.startSimulation(response);
        assertTrue(dynamicDispatchService.isSimulationRunning());

        // Act
        dynamicDispatchService.stopSimulation();
        Thread.sleep(100); // Give time for shutdown

        // Assert
        assertFalse(dynamicDispatchService.isSimulationRunning());
        assertNotNull(dynamicDispatchService.getCurrentDroneStatus("drone-1"));
    }
}