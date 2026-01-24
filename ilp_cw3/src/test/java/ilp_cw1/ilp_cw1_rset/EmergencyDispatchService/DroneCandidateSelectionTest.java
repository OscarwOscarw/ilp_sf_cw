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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DroneCandidateSelectionTest {

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

    private EmergencyMedDispatchRec createEmergencyTask(boolean cooling, boolean heating, double capacity) {
        EmergencyMedDispatchRec task = new EmergencyMedDispatchRec();
        task.setId(999);

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

    @BeforeEach
    public void setup() {
        // Setup available drones info for all drones
        List<DroneForServicePoint> availableInfo = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            DroneForServicePoint.DroneAvailability.AvailabilitySlot slot =
                    new DroneForServicePoint.DroneAvailability.AvailabilitySlot();
            slot.setDayOfWeek("MONDAY");
            slot.setFrom("09:00");
            slot.setUntil("17:00");

            DroneForServicePoint.DroneAvailability droneAvailability =
                    new DroneForServicePoint.DroneAvailability();
            droneAvailability.setId(String.valueOf(i));
            droneAvailability.setAvailability(Collections.singletonList(slot));

            DroneForServicePoint servicePoint = new DroneForServicePoint();
            servicePoint.setServicePointId(1000 + i);
            servicePoint.setDrones(Collections.singletonList(droneAvailability));
            availableInfo.add(servicePoint);
        }

        when(droneService.readAvailableDrones()).thenReturn(availableInfo);
    }

    @Test
    public void CAND_001_findCandidateDrones_MeetsRequirements_ReturnsList() {
        List<Drone> allDrones = Arrays.asList(
                createDrone("1", true, false, 10.0),   // Meets requirements
                createDrone("2", false, false, 5.0),   // No cooling
                createDrone("3", true, false, 3.0)     // Insufficient capacity
        );

        when(droneService.getAllDrones()).thenReturn(allDrones);

        EmergencyMedDispatchRec task = createEmergencyTask(true, false, 8.0);

        List<Drone> candidates = emergencyDispatchService.findCandidateDronesForEmergency(
                task, allDrones, droneService.readAvailableDrones()
        );

        assertEquals(1, candidates.size());
        assertEquals("1", candidates.get(0).getId());
    }

    @Test
    public void CAND_002_findCandidateDrones_AllMeetRequirements_ReturnsAll() {
        List<Drone> allDrones = Arrays.asList(
                createDrone("1", true, false, 10.0),
                createDrone("2", true, false, 12.0),
                createDrone("3", true, false, 15.0)
        );

        when(droneService.getAllDrones()).thenReturn(allDrones);

        EmergencyMedDispatchRec task = createEmergencyTask(true, false, 8.0);

        List<Drone> candidates = emergencyDispatchService.findCandidateDronesForEmergency(
                task, allDrones, droneService.readAvailableDrones()
        );

        assertEquals(3, candidates.size());
    }

    @Test
    public void CAND_003_findCandidateDrones_NoneMeetRequirements_ReturnsEmpty() {
        List<Drone> allDrones = Arrays.asList(
                createDrone("1", false, false, 5.0),   // No cooling
                createDrone("2", false, true, 10.0),   // Wrong temperature (needs cooling)
                createDrone("3", true, false, 3.0)     // Insufficient capacity
        );

        when(droneService.getAllDrones()).thenReturn(allDrones);

        EmergencyMedDispatchRec task = createEmergencyTask(true, false, 8.0);

        List<Drone> candidates = emergencyDispatchService.findCandidateDronesForEmergency(
                task, allDrones, droneService.readAvailableDrones()
        );

        assertTrue(candidates.isEmpty());
    }

    @Test
    public void CAND_004_canDroneHandleEmergency_CapabilityMatch_ReturnsTrue() {
        Drone drone = createDrone("1", true, false, 10.0);
        EmergencyMedDispatchRec task = createEmergencyTask(true, false, 8.0);

        boolean result = emergencyDispatchService.canDroneHandleEmergency(
                drone, task, droneService.readAvailableDrones()
        );

        assertTrue(result);
    }

    @Test
    public void CAND_005_canDroneHandleEmergency_CapacityExceeded_ReturnsFalse() {
        Drone drone = createDrone("1", true, false, 5.0);
        EmergencyMedDispatchRec task = createEmergencyTask(true, false, 8.0);

        boolean result = emergencyDispatchService.canDroneHandleEmergency(
                drone, task, droneService.readAvailableDrones()
        );

        assertFalse(result);
    }

    @Test
    public void CAND_006_canDroneHandleEmergency_TemperatureMismatch_ReturnsFalse() {
        // Test cooling requirement
        Drone drone1 = createDrone("1", false, false, 10.0);
        EmergencyMedDispatchRec task1 = createEmergencyTask(true, false, 8.0);
        boolean result1 = emergencyDispatchService.canDroneHandleEmergency(
                drone1, task1, droneService.readAvailableDrones()
        );
        assertFalse(result1);

        // Test heating requirement
        Drone drone2 = createDrone("2", false, false, 10.0);
        EmergencyMedDispatchRec task2 = createEmergencyTask(false, true, 8.0);
        boolean result2 = emergencyDispatchService.canDroneHandleEmergency(
                drone2, task2, droneService.readAvailableDrones()
        );
        assertFalse(result2);
    }

    @Test
    public void CAND_007_canDroneHandleEmergency_DroneWithoutCapability_ReturnsFalse() {
        Drone drone = new Drone();
        drone.setId("1");
        // No capability set

        EmergencyMedDispatchRec task = createEmergencyTask(true, false, 8.0);

        boolean result = emergencyDispatchService.canDroneHandleEmergency(
                drone, task, droneService.readAvailableDrones()
        );

        assertFalse(result);
    }

    @Test
    public void CAND_008_isDroneAvailable_AvailableDrone_ReturnsTrue() {
        List<DroneForServicePoint> availableInfo = droneService.readAvailableDrones();

        boolean result = emergencyDispatchService.isDroneAvailable("1", availableInfo);

        assertTrue(result);
    }

    @Test
    public void CAND_009_isDroneAvailable_UnavailableDrone_ReturnsFalse() {
        List<DroneForServicePoint> availableInfo = droneService.readAvailableDrones();

        // Check for non-existent drone
        boolean result = emergencyDispatchService.isDroneAvailable("999", availableInfo);

        assertFalse(result);
    }

    @Test
    public void CAND_010_isDroneAvailable_EmptyAvailability_ReturnsFalse() {
        // Create drone with empty availability
        DroneForServicePoint.DroneAvailability droneAvailability =
                new DroneForServicePoint.DroneAvailability();
        droneAvailability.setId("no-availability");
        droneAvailability.setAvailability(Collections.emptyList()); // Empty!

        DroneForServicePoint servicePoint = new DroneForServicePoint();
        servicePoint.setServicePointId(9999);
        servicePoint.setDrones(Collections.singletonList(droneAvailability));

        List<DroneForServicePoint> availableInfo = Collections.singletonList(servicePoint);

        boolean result = emergencyDispatchService.isDroneAvailable("no-availability", availableInfo);

        assertFalse(result);
    }

    @Test
    public void CAND_011_findIdleDrone_IdleExists_ReturnsDrone() {
        Drone idleDrone = createDrone("idle-1", true, false, 10.0);
        Drone busyDrone = createDrone("busy-1", true, false, 10.0);
        List<Drone> candidates = Arrays.asList(idleDrone, busyDrone);
        List<Drone> workingDrones = Collections.singletonList(busyDrone);

        Drone result = emergencyDispatchService.findIdleDrone(candidates, workingDrones);

        assertNotNull(result);
        assertEquals("idle-1", result.getId());
    }

    @Test
    public void CAND_012_findIdleDrone_AllBusy_ReturnsNull() {
        Drone busyDrone1 = createDrone("busy-1", true, false, 10.0);
        Drone busyDrone2 = createDrone("busy-2", true, false, 10.0);
        List<Drone> candidates = Arrays.asList(busyDrone1, busyDrone2);
        List<Drone> workingDrones = Arrays.asList(busyDrone1, busyDrone2);

        Drone result = emergencyDispatchService.findIdleDrone(candidates, workingDrones);

        assertNull(result);
    }

    @Test
    public void CAND_013_findIdleDrone_EmptyCandidates_ReturnsNull() {
        List<Drone> candidates = Collections.emptyList();
        List<Drone> workingDrones = Collections.emptyList();

        Drone result = emergencyDispatchService.findIdleDrone(candidates, workingDrones);

        assertNull(result);
    }
}