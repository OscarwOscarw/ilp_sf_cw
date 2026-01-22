package ilp_cw1.ilp_cw1_rset.Droneservice;

import data.Drone;
import data.DroneForServicePoint;
import data.MedDispatchRec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class HandleTaskTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ilpService ilpService;

    @InjectMocks
    private droneService droneService;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    public void canDroneHandleTask_AllRequirementsMet_ReturnsTrue() {
        Drone.DroneCapability capability =
                new Drone.DroneCapability(true, false, 10.0, 100, 1.5, 5.0, 20.0);
        Drone drone = new Drone("DroneA", "1", capability);

        MedDispatchRec rec = new MedDispatchRec();
        rec.setId(1);
        rec.setDate(LocalDate.of(2026, 1, 14));
        rec.setTime(LocalTime.of(10, 30));

        MedDispatchRec.Requirements requirements = new MedDispatchRec.Requirements();
        requirements.setCapacity(5.0);
        requirements.setCooling(true);
        requirements.setHeating(false);
        requirements.setMaxCost(100.0);
        rec.setRequirements(requirements);

        MedDispatchRec.Delivery delivery = new MedDispatchRec.Delivery();
        delivery.setLng(-3.1883);
        delivery.setLat(55.9533);
        rec.setDelivery(delivery);

        DroneForServicePoint.DroneAvailability.AvailabilitySlot slot1 =
                new DroneForServicePoint.DroneAvailability.AvailabilitySlot();
        slot1.setDayOfWeek("WEDNESDAY");
        slot1.setFrom("09:00");
        slot1.setUntil("17:00");

        DroneForServicePoint.DroneAvailability drone1 = new DroneForServicePoint.DroneAvailability();
        drone1.setId("1");
        drone1.setAvailability(List.of(slot1));

        DroneForServicePoint servicePoint = new DroneForServicePoint();
        servicePoint.setServicePointId(1001);
        servicePoint.setDrones(List.of(drone1));

        boolean result = droneService.canDroneHandleTask(drone, rec, List.of(servicePoint));

        assertTrue(result);
    }

    @Test
    public void canDroneHandleTask_CapacityExceeded_ReturnsFalse() {
        Drone drone = createDroneWithCapability(10.0, true, false, 100);
        MedDispatchRec task = createTaskWithRequirements(15.0, true, false);

        boolean result = droneService.canDroneHandleTask(
                drone, task, createAvailableDronesInfo(drone, task.getDate(), task.getTime())
        );

        assertFalse(result);
    }

    @Test
    public void canDroneHandleTask_TemperatureMismatch_ReturnsFalse() {
        Drone drone1 = createDroneWithCapability(20.0, false, false, 100);
        MedDispatchRec task1 = createTaskWithRequirements(15.0, true, false);

        Drone drone2 = createDroneWithCapability(20.0, false, false, 100);
        MedDispatchRec task2 = createTaskWithRequirements(15.0, false, true);

        boolean result1 = droneService.canDroneHandleTask(
                drone1, task1, createAvailableDronesInfo(drone1, task1.getDate(), task1.getTime())
        );
        boolean result2 = droneService.canDroneHandleTask(
                drone2, task2, createAvailableDronesInfo(drone2, task2.getDate(), task2.getTime())
        );

        assertFalse(result1);
        assertFalse(result2);
    }

    @Test
    public void canDroneHandleTask_DroneNotAvailableAtTime_ReturnsFalse() {
        Drone drone = createDroneWithCapability(20.0, true, false, 100);
        MedDispatchRec task = createTaskWithRequirements(15.0, true, false);

        LocalDate differentDate = LocalDate.now().plusDays(1);
        LocalTime differentTime = LocalTime.now().plusHours(1);
        List<DroneForServicePoint> availability = createAvailableDronesInfo(
                drone, differentDate, differentTime
        );

        boolean result = droneService.canDroneHandleTask(drone, task, availability);

        assertFalse(result);
    }

    @Test
    public void canDroneHandleTaskWithMoves_ExceedsMaxMoves_ReturnsFalse() {
        Drone drone = createDroneWithCapability(20.0, true, false, 10);
        MedDispatchRec task = createTaskWithRequirements(15.0, true, false);
        data.PositionDto servicePoint = new data.PositionDto(0.0, 0.0);
        data.PositionDto taskLocation = new data.PositionDto(0.01, 0.01);

        when(ilpService.distanceCalculate(any())).thenReturn(0.01);

        boolean result = droneService.canDroneHandleTaskWithMoves(
                drone, task,
                createAvailableDronesInfo(drone, task.getDate(), task.getTime()),
                servicePoint, taskLocation, true
        );

        assertFalse(result);
    }

    private Drone createDroneWithCapability(double capacity, boolean cooling, boolean heating, int maxMoves) {
        Drone.DroneCapability capability = new Drone.DroneCapability(cooling, heating, capacity, maxMoves, 1.5, 5.0, 20.0);
        return new Drone("DroneTest", "1", capability);
    }

    private MedDispatchRec createTaskWithRequirements(double capacity, boolean cooling, boolean heating) {
        MedDispatchRec rec = new MedDispatchRec();
        rec.setId(1);
        rec.setDate(LocalDate.of(2026, 1, 14));
        rec.setTime(LocalTime.of(10, 30));

        MedDispatchRec.Requirements requirements = new MedDispatchRec.Requirements();
        requirements.setCapacity(capacity);
        requirements.setCooling(cooling);
        requirements.setHeating(heating);
        requirements.setMaxCost(100.0);
        rec.setRequirements(requirements);

        MedDispatchRec.Delivery delivery = new MedDispatchRec.Delivery();
        delivery.setLng(-3.1883);
        delivery.setLat(55.9533);
        rec.setDelivery(delivery);

        return rec;
    }

    private List<DroneForServicePoint> createAvailableDronesInfo(Drone drone, LocalDate date, LocalTime time) {
        DroneForServicePoint.DroneAvailability.AvailabilitySlot slot =
                new DroneForServicePoint.DroneAvailability.AvailabilitySlot();
        slot.setDayOfWeek(date.getDayOfWeek().name());
        slot.setFrom("09:00");
        slot.setUntil("17:00");

        DroneForServicePoint.DroneAvailability droneAvailability = new DroneForServicePoint.DroneAvailability();
        droneAvailability.setId(drone.getId());
        droneAvailability.setAvailability(List.of(slot));

        DroneForServicePoint servicePoint = new DroneForServicePoint();
        servicePoint.setServicePointId(1001);
        servicePoint.setDrones(List.of(droneAvailability));

        return List.of(servicePoint);
    }
}
