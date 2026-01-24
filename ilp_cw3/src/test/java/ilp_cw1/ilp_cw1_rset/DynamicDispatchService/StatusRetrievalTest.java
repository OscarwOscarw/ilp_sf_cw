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
public class StatusRetrievalTest {

    @Mock
    private droneService droneService;

    @InjectMocks
    private DynamicDispatchService dynamicDispatchService;

    @BeforeEach
    public void setup() {
        when(droneService.getRestrictedAreas()).thenReturn(Collections.emptyList());
    }

    private DeliveryPathResponse createSimulationWithTasks(String droneId, int... taskIds) {
        DeliveryPathResponse response = new DeliveryPathResponse();
        DeliveryPathResponse.DronePath dronePath = new DeliveryPathResponse.DronePath();
        dronePath.setDroneId(droneId);

        List<DeliveryPathResponse.Delivery> deliveries = new ArrayList<>();
        for (int taskId : taskIds) {
            DeliveryPathResponse.Delivery delivery = new DeliveryPathResponse.Delivery();
            delivery.setDeliveryId(taskId);
            delivery.setFlightPath(Arrays.asList(
                    new PositionDto(0.0, 0.0),
                    new PositionDto(0.001, 0.001)
            ));
            deliveries.add(delivery);
        }

        dronePath.setDeliveries(deliveries);
        response.setDronePaths(Collections.singletonList(dronePath));
        return response;
    }

    @Test
    public void STATUS_001_getOngoingTasks_ReturnsCorrectTaskMapping() {
        DeliveryPathResponse response = createSimulationWithTasks("drone-1", 1001, 1002, 1003);
        dynamicDispatchService.startSimulation(response);

        Map<Drone, List<MedDispatchRec>> ongoingTasks = dynamicDispatchService.getOngoingTasks();

        assertNotNull(ongoingTasks);
        assertEquals(1, ongoingTasks.size());

        Drone drone = ongoingTasks.keySet().iterator().next();
        assertEquals("drone-1", drone.getId());

        List<MedDispatchRec> tasks = ongoingTasks.get(drone);
        assertEquals(3, tasks.size());
        assertEquals(1001, tasks.get(0).getId());
        assertEquals(1002, tasks.get(1).getId());
        assertEquals(1003, tasks.get(2).getId());
    }

    @Test
    public void STATUS_002_getOngoingTasks_WithEmergencyTask_IncludesEmergency() {
        DeliveryPathResponse response = createSimulationWithTasks("drone-1", 1001, 1002);
        dynamicDispatchService.startSimulation(response);

        // Insert emergency task
        DeliveryPathResponse.Delivery emergencyTask = new DeliveryPathResponse.Delivery();
        emergencyTask.setDeliveryId(9999);
        emergencyTask.setFlightPath(Arrays.asList(
                new PositionDto(0.003, 0.003),
                new PositionDto(0.004, 0.004)
        ));
        dynamicDispatchService.insertEmergencyTask("drone-1", emergencyTask);

        Map<Drone, List<MedDispatchRec>> ongoingTasks = dynamicDispatchService.getOngoingTasks();

        Drone drone = ongoingTasks.keySet().iterator().next();
        List<MedDispatchRec> tasks = ongoingTasks.get(drone);

        // Should include emergency task at the beginning
        assertEquals(3, tasks.size()); // Original 2 + emergency 1
        assertEquals(9999, tasks.get(0).getId()); // Emergency task first
    }

    @Test
    public void STATUS_003_getOngoingTasks_MultipleDrones_ReturnsAll() {
        DeliveryPathResponse response = new DeliveryPathResponse();

        // Drone 1 with 2 tasks
        DeliveryPathResponse.DronePath dronePath1 = new DeliveryPathResponse.DronePath();
        dronePath1.setDroneId("drone-1");
        DeliveryPathResponse.Delivery delivery1 = new DeliveryPathResponse.Delivery();
        delivery1.setDeliveryId(1001);
        delivery1.setFlightPath(Arrays.asList(new PositionDto(0.0, 0.0), new PositionDto(0.001, 0.001)));
        dronePath1.setDeliveries(Collections.singletonList(delivery1));

        // Drone 2 with 1 task
        DeliveryPathResponse.DronePath dronePath2 = new DeliveryPathResponse.DronePath();
        dronePath2.setDroneId("drone-2");
        DeliveryPathResponse.Delivery delivery2 = new DeliveryPathResponse.Delivery();
        delivery2.setDeliveryId(2001);
        delivery2.setFlightPath(Arrays.asList(new PositionDto(1.0, 1.0), new PositionDto(1.001, 1.001)));
        dronePath2.setDeliveries(Collections.singletonList(delivery2));

        response.setDronePaths(Arrays.asList(dronePath1, dronePath2));
        dynamicDispatchService.startSimulation(response);

        Map<Drone, List<MedDispatchRec>> ongoingTasks = dynamicDispatchService.getOngoingTasks();

        assertEquals(2, ongoingTasks.size());

        // Find drone-1
        Drone drone1 = ongoingTasks.keySet().stream()
                .filter(d -> d.getId().equals("drone-1"))
                .findFirst()
                .orElse(null);
        assertNotNull(drone1);
        assertEquals(1, ongoingTasks.get(drone1).size());

        // Find drone-2
        Drone drone2 = ongoingTasks.keySet().stream()
                .filter(d -> d.getId().equals("drone-2"))
                .findFirst()
                .orElse(null);
        assertNotNull(drone2);
        assertEquals(1, ongoingTasks.get(drone2).size());
    }

    @Test
    public void STATUS_004_getOngoingTasks_NoSimulation_ReturnsEmptyMap() {
        Map<Drone, List<MedDispatchRec>> ongoingTasks = dynamicDispatchService.getOngoingTasks();

        assertNotNull(ongoingTasks);
        assertTrue(ongoingTasks.isEmpty());
    }

    @Test
    public void STATUS_005_estimateInterruptCost_ValidDrone_ReturnsCost() {
        DeliveryPathResponse response = createSimulationWithTasks("drone-1", 1001, 1002, 1003);
        dynamicDispatchService.startSimulation(response);

        double cost = dynamicDispatchService.estimateInterruptCost("drone-1");

        assertTrue(cost >= 0.0 && cost <= 1.0); // Should be ratio between 0 and 1
    }

    @Test
    public void STATUS_006_estimateInterruptCost_InvalidDrone_ReturnsZero() {
        double cost = dynamicDispatchService.estimateInterruptCost("invalid-drone");

        assertEquals(0.0, cost, 0.001);
    }

    @Test
    public void STATUS_007_isSimulationRunning_Initial_False() {
        assertFalse(dynamicDispatchService.isSimulationRunning());
    }

    @Test
    public void STATUS_008_isSimulationRunning_AfterStart_True() {
        DeliveryPathResponse response = createSimulationWithTasks("drone-1", 1001);
        dynamicDispatchService.startSimulation(response);

        assertTrue(dynamicDispatchService.isSimulationRunning());
    }

    @Test
    public void STATUS_009_isSimulationRunning_AfterStop_False() {
        DeliveryPathResponse response = createSimulationWithTasks("drone-1", 1001);
        dynamicDispatchService.startSimulation(response);
        assertTrue(dynamicDispatchService.isSimulationRunning());

        dynamicDispatchService.stopSimulation();

        assertFalse(dynamicDispatchService.isSimulationRunning());
    }
}