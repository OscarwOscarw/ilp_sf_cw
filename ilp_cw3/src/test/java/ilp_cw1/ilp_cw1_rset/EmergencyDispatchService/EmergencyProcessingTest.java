package ilp_cw1.ilp_cw1_rset.EmergencyDispatchService;

import data.*;
import ilp_cw1.ilp_cw1_rset.Droneservice.DynamicDispatchService;
import ilp_cw1.ilp_cw1_rset.Droneservice.EmergencyDispatchService;
import ilp_cw1.ilp_cw1_rset.Droneservice.droneService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EmergencyProcessingTest {

    @Mock
    private droneService droneService;

    @Mock
    private DynamicDispatchService dynamicDispatchService;

    @InjectMocks
    private EmergencyDispatchService emergencyDispatchService;

    private Drone createDrone(String id, boolean cooling, boolean heating, double capacity) {
        Drone.DroneCapability capability = new Drone.DroneCapability(
                cooling, heating, capacity, 100, 1.5, 5.0, 20.0
        );
        Drone drone = new Drone();
        drone.setId(id);
        drone.setCapability(capability);
        return drone;
    }

    private EmergencyMedDispatchRec createEmergencyTask(int id, boolean cooling, boolean heating, double capacity, int level) {
        EmergencyMedDispatchRec task = new EmergencyMedDispatchRec();
        task.setId(id);
        task.setEmergencyLevel(level);

        MedDispatchRec.Requirements requirements = new MedDispatchRec.Requirements();
        requirements.setCooling(cooling);
        requirements.setHeating(heating);
        requirements.setCapacity(capacity);
        requirements.setMaxCost(100.0);
        task.setRequirements(requirements);

        EmergencyMedDispatchRec.Delivery delivery = new EmergencyMedDispatchRec.Delivery();
        delivery.setLng(-3.1883);
        delivery.setLat(55.9533);
        task.setDelivery(delivery);

        return task;
    }

    private DroneForServicePoint createAvailableDroneInfo(String droneId) {
        DroneForServicePoint.DroneAvailability.AvailabilitySlot slot =
                new DroneForServicePoint.DroneAvailability.AvailabilitySlot();
        slot.setDayOfWeek("MONDAY");
        slot.setFrom("09:00");
        slot.setUntil("17:00");

        DroneForServicePoint.DroneAvailability droneAvailability =
                new DroneForServicePoint.DroneAvailability();
        droneAvailability.setId(droneId);
        droneAvailability.setAvailability(Collections.singletonList(slot));

        DroneForServicePoint servicePoint = new DroneForServicePoint();
        servicePoint.setServicePointId(1001);
        servicePoint.setDrones(Collections.singletonList(droneAvailability));

        return servicePoint;
    }

    @BeforeEach
    public void setup() {
        // Setup available drones info
        when(droneService.readAvailableDrones()).thenReturn(
                Arrays.asList(
                        createAvailableDroneInfo("1"),
                        createAvailableDroneInfo("2"),
                        createAvailableDroneInfo("3")
                )
        );

        // Setup restricted areas
        when(droneService.getRestrictedAreas()).thenReturn(Collections.emptyList());

        // Setup service point position
        when(droneService.getServicePointForDrone(any(Drone.class), anyList()))
                .thenReturn(new PositionDto(0.0, 0.0));

        // Setup path calculation
        List<PositionDto> mockPath = Arrays.asList(
                new PositionDto(0.0, 0.0),
                new PositionDto(-3.1883, 55.9533)
        );
        when(droneService.calculateAStarPath(any(), any(), any(), any())).thenReturn(mockPath);
        when(droneService.doesLineIntersectPolygon(any(), any(), any())).thenReturn(false);
        when(droneService.isPointInPolygon(any(), any())).thenReturn(false);
    }

    @Test
    public void EMER_PROC_001_processEmergencyRequest_ValidRequest_ReturnsResult() {
        Drone drone = createDrone("1", true, false, 10.0);
        when(droneService.getAllDrones()).thenReturn(Collections.singletonList(drone));

        EmergencyMedDispatchRec emergencyTask = createEmergencyTask(999, true, false, 5.0, 3);
        EmergencyDispatchRequest request = new EmergencyDispatchRequest();
        request.setEmergencyTasks(Collections.singletonList(emergencyTask));

        when(dynamicDispatchService.getCurrentDroneStatus(anyString())).thenReturn(null);

        EmergencyHandleResult result = emergencyDispatchService.processEmergencyRequest(
                request,
                Collections.emptyMap(),
                Collections.emptyMap()
        );

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("1", result.getDroneId());
        assertTrue(result.getFinalCost() > 0);
    }

    @Test
    public void EMER_PROC_002_processEmergencyRequest_EmptyTasks_ReturnsError() {
        EmergencyDispatchRequest request = new EmergencyDispatchRequest();
        request.setEmergencyTasks(Collections.emptyList());

        EmergencyHandleResult result = emergencyDispatchService.processEmergencyRequest(
                request,
                Collections.emptyMap(),
                Collections.emptyMap()
        );

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("No emergency tasks"));
        assertEquals(0.0, result.getFinalCost(), 0.001);
    }

    @Test
    public void EMER_PROC_003_processEmergencyRequest_NullRequest_ThrowsException() {
        assertThrows(NullPointerException.class, () -> {
            emergencyDispatchService.processEmergencyRequest(
                    null,
                    Collections.emptyMap(),
                    Collections.emptyMap()
            );
        });
    }

    @Test
    public void EMER_PROC_004_processEmergencyRequest_NoCandidateDrones_ReturnsError() {
        Drone drone = createDrone("1", false, false, 5.0); // No cooling
        when(droneService.getAllDrones()).thenReturn(Collections.singletonList(drone));

        EmergencyMedDispatchRec emergencyTask = createEmergencyTask(999, true, false, 10.0, 3); // Requires cooling
        EmergencyDispatchRequest request = new EmergencyDispatchRequest();
        request.setEmergencyTasks(Collections.singletonList(emergencyTask));

        EmergencyHandleResult result = emergencyDispatchService.processEmergencyRequest(
                request,
                Collections.emptyMap(),
                Collections.emptyMap()
        );

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("No drones found"));
    }

    @Test
    public void EMER_PROC_005_processEmergencyRequest_MultipleTasks_ProcessesFirst() {
        Drone drone = createDrone("1", true, false, 10.0);
        when(droneService.getAllDrones()).thenReturn(Collections.singletonList(drone));

        List<EmergencyMedDispatchRec> tasks = Arrays.asList(
                createEmergencyTask(999, true, false, 5.0, 3),
                createEmergencyTask(888, true, false, 8.0, 2)
        );

        EmergencyDispatchRequest request = new EmergencyDispatchRequest();
        request.setEmergencyTasks(tasks);

        when(dynamicDispatchService.getCurrentDroneStatus(anyString())).thenReturn(null);

        EmergencyHandleResult result = emergencyDispatchService.processEmergencyRequest(
                request,
                Collections.emptyMap(),
                Collections.emptyMap()
        );

        assertNotNull(result);
        assertTrue(result.isSuccess());
        // Should process first task (id 999)
        assertEquals("1", result.getDroneId());
    }

    @Test
    public void EMER_PROC_006_processEmergencyRequestWithBypass_BypassFlag_Respected() {
        Drone drone = createDrone("1", true, false, 10.0);
        when(droneService.getAllDrones()).thenReturn(Collections.singletonList(drone));

        EmergencyMedDispatchRec emergencyTask = createEmergencyTask(999, true, false, 5.0, 3);
        EmergencyDispatchRequest request = new EmergencyDispatchRequest();
        request.setEmergencyTasks(Collections.singletonList(emergencyTask));

        when(dynamicDispatchService.getCurrentDroneStatus(anyString())).thenReturn(null);

        // Test with bypass flag true
        EmergencyHandleResult result = emergencyDispatchService.processEmergencyRequestWithBypass(
                request,
                Collections.emptyMap(),
                Collections.emptyMap(),
                true
        );

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("1", result.getDroneId());
    }

    @Test
    public void EMER_PROC_007_processEmergencyRequestWithBypass_NoBypass_NormalProcessing() {
        Drone drone = createDrone("1", true, false, 10.0);
        when(droneService.getAllDrones()).thenReturn(Collections.singletonList(drone));

        EmergencyMedDispatchRec emergencyTask = createEmergencyTask(999, true, false, 5.0, 3);
        EmergencyDispatchRequest request = new EmergencyDispatchRequest();
        request.setEmergencyTasks(Collections.singletonList(emergencyTask));

        when(dynamicDispatchService.getCurrentDroneStatus(anyString())).thenReturn(null);

        // Test with bypass flag false
        EmergencyHandleResult result = emergencyDispatchService.processEmergencyRequestWithBypass(
                request,
                Collections.emptyMap(),
                Collections.emptyMap(),
                false
        );

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("1", result.getDroneId());
    }
}