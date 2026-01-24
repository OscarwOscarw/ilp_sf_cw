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
public class CostCalculationTest {

    @Mock
    private droneService droneService;

    @Mock
    private DynamicDispatchService dynamicDispatchService;

    @InjectMocks
    private EmergencyDispatchService emergencyDispatchService;

    private Drone createDrone(String id, double costPerMove) {
        Drone.DroneCapability capability = new Drone.DroneCapability(
                true, false, 10.0, 100, costPerMove, 5.0, 20.0
        );
        Drone drone = new Drone();
        drone.setId(id);
        drone.setCapability(capability);
        return drone;
    }

    private MedDispatchRec createTask(int id) {
        MedDispatchRec task = new MedDispatchRec();
        task.setId(id);
        task.setDate(LocalDate.now());
        task.setTime(LocalTime.of(10, 0));

        MedDispatchRec.Requirements requirements = new MedDispatchRec.Requirements();
        requirements.setCapacity(5.0);
        requirements.setCooling(true);
        requirements.setHeating(false);
        requirements.setMaxCost(100.0);
        task.setRequirements(requirements);

        MedDispatchRec.Delivery delivery = new MedDispatchRec.Delivery();
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
        when(droneService.readAvailableDrones()).thenReturn(
                Collections.singletonList(createAvailableDroneInfo("1"))
        );
    }

    @Test
    public void COST_001_calculateInterruptCostsForCandidates_MovingDrone_CalculatesCost() {
        Drone drone = createDrone("1", 1.5);
        List<Drone> candidates = Collections.singletonList(drone);

        Map<Drone, List<MedDispatchRec>> droneToTasksMap = new HashMap<>();
        droneToTasksMap.put(drone, Arrays.asList(createTask(1), createTask(2)));

        Map<Integer, PositionDto> taskLocations = new HashMap<>();
        taskLocations.put(1, new PositionDto(0.001, 0.001));
        taskLocations.put(2, new PositionDto(0.002, 0.002));

        DynamicDispatchService.DroneStatusDto status =
                new DynamicDispatchService.DroneStatusDto(
                        "1",
                        new PositionDto(0.0, 0.0),
                        DynamicDispatchService.DroneStatus.MOVING,
                        1, 2, 0, false, null
                );

        when(dynamicDispatchService.getCurrentDroneStatus("1")).thenReturn(status);
        when(dynamicDispatchService.estimateInterruptCost("1")).thenReturn(0.5);

        Map<Drone, Double> costs = emergencyDispatchService.calculateInterruptCostsForCandidates(
                candidates, droneToTasksMap, taskLocations
        );

        assertNotNull(costs);
        assertTrue(costs.containsKey(drone));
        double cost = costs.get(drone);
        assertTrue(cost > 0);
        // Formula: remainingPathRatio * costPerMove * (tasks.size() + 1)
        // = 0.5 * 1.5 * (2 + 1) = 0.5 * 1.5 * 3 = 2.25
        assertEquals(2.25, cost, 0.01);
    }

    @Test
    public void COST_002_calculateInterruptCostsForCandidates_IdleDrone_ReturnsEmpty() {
        Drone drone = createDrone("1", 1.5);
        List<Drone> candidates = Collections.singletonList(drone);

        Map<Drone, List<MedDispatchRec>> droneToTasksMap = new HashMap<>();
        droneToTasksMap.put(drone, Arrays.asList(createTask(1), createTask(2)));

        Map<Integer, PositionDto> taskLocations = new HashMap<>();
        taskLocations.put(1, new PositionDto(0.001, 0.001));
        taskLocations.put(2, new PositionDto(0.002, 0.002));

        // Drone is idle (not in working drones)
        when(dynamicDispatchService.getCurrentDroneStatus("1")).thenReturn(null);

        Map<Drone, Double> costs = emergencyDispatchService.calculateInterruptCostsForCandidates(
                candidates, droneToTasksMap, taskLocations
        );

        assertNotNull(costs);
        assertTrue(costs.isEmpty()); // Idle drone has no interrupt cost
    }

    @Test
    public void COST_003_calculateInterruptCostsForCandidates_DroneNotMoving_Skips() {
        Drone drone = createDrone("1", 1.5);
        List<Drone> candidates = Collections.singletonList(drone);

        Map<Drone, List<MedDispatchRec>> droneToTasksMap = new HashMap<>();
        droneToTasksMap.put(drone, Arrays.asList(createTask(1), createTask(2)));

        Map<Integer, PositionDto> taskLocations = new HashMap<>();
        taskLocations.put(1, new PositionDto(0.001, 0.001));
        taskLocations.put(2, new PositionDto(0.002, 0.002));

        // Drone is READY (not MOVING)
        DynamicDispatchService.DroneStatusDto status =
                new DynamicDispatchService.DroneStatusDto(
                        "1",
                        new PositionDto(0.0, 0.0),
                        DynamicDispatchService.DroneStatus.READY,
                        1, 2, 0, false, null
                );

        when(dynamicDispatchService.getCurrentDroneStatus("1")).thenReturn(status);

        Map<Drone, Double> costs = emergencyDispatchService.calculateInterruptCostsForCandidates(
                candidates, droneToTasksMap, taskLocations
        );

        assertNotNull(costs);
        assertTrue(costs.isEmpty()); // READY drone skipped
    }

    @Test
    public void COST_004_calculateInterruptCostsForCandidates_ExceptionHandling_ReturnsDefaultCost() {
        Drone drone = createDrone("1", 1.5);
        List<Drone> candidates = Collections.singletonList(drone);

        Map<Drone, List<MedDispatchRec>> droneToTasksMap = new HashMap<>();
        droneToTasksMap.put(drone, Arrays.asList(createTask(1), createTask(2)));

        Map<Integer, PositionDto> taskLocations = new HashMap<>();
        taskLocations.put(1, new PositionDto(0.001, 0.001));
        taskLocations.put(2, new PositionDto(0.002, 0.002));

        DynamicDispatchService.DroneStatusDto status =
                new DynamicDispatchService.DroneStatusDto(
                        "1",
                        new PositionDto(0.0, 0.0),
                        DynamicDispatchService.DroneStatus.MOVING,
                        1, 2, 0, false, null
                );

        when(dynamicDispatchService.getCurrentDroneStatus("1")).thenReturn(status);
        when(dynamicDispatchService.estimateInterruptCost("1")).thenThrow(new RuntimeException("Calculation error"));

        Map<Drone, Double> costs = emergencyDispatchService.calculateInterruptCostsForCandidates(
                candidates, droneToTasksMap, taskLocations
        );

        assertNotNull(costs);
        assertTrue(costs.containsKey(drone));
        assertEquals(1500.0, costs.get(drone), 0.01); // Default error cost
    }

    @Test
    public void COST_005_calculateInterruptCostsForCandidates_InvalidCost_ReturnsDefault() {
        Drone drone = createDrone("1", 1.5);
        List<Drone> candidates = Collections.singletonList(drone);

        Map<Drone, List<MedDispatchRec>> droneToTasksMap = new HashMap<>();
        droneToTasksMap.put(drone, Arrays.asList(createTask(1), createTask(2)));

        Map<Integer, PositionDto> taskLocations = new HashMap<>();
        taskLocations.put(1, new PositionDto(0.001, 0.001));
        taskLocations.put(2, new PositionDto(0.002, 0.002));

        DynamicDispatchService.DroneStatusDto status =
                new DynamicDispatchService.DroneStatusDto(
                        "1",
                        new PositionDto(0.0, 0.0),
                        DynamicDispatchService.DroneStatus.MOVING,
                        1, 2, 0, false, null
                );

        when(dynamicDispatchService.getCurrentDroneStatus("1")).thenReturn(status);
        when(dynamicDispatchService.estimateInterruptCost("1")).thenReturn(Double.NaN); // Invalid cost

        Map<Drone, Double> costs = emergencyDispatchService.calculateInterruptCostsForCandidates(
                candidates, droneToTasksMap, taskLocations
        );

        assertNotNull(costs);
        assertTrue(costs.containsKey(drone));
        assertEquals(1000.0, costs.get(drone), 0.01); // Default NaN cost
    }

    @Test
    public void COST_006_calculateReassignCosts_ReturnsMultipliedCosts() {
        Drone drone1 = createDrone("1", 1.0);
        Drone drone2 = createDrone("2", 2.0);

        Map<Drone, Double> interruptCosts = new HashMap<>();
        interruptCosts.put(drone1, 100.0);
        interruptCosts.put(drone2, 200.0);

        Map<Drone, Double> reassignCosts = emergencyDispatchService.calculateReassignCosts(interruptCosts);

        assertEquals(2, reassignCosts.size());
        assertEquals(150.0, reassignCosts.get(drone1), 0.01); // 100 * 1.5
        assertEquals(300.0, reassignCosts.get(drone2), 0.01); // 200 * 1.5
    }

    @Test
    public void COST_007_calculateReassignCosts_EmptyInput_ReturnsEmpty() {
        Map<Drone, Double> interruptCosts = new HashMap<>();

        Map<Drone, Double> reassignCosts = emergencyDispatchService.calculateReassignCosts(interruptCosts);

        assertNotNull(reassignCosts);
        assertTrue(reassignCosts.isEmpty());
    }

    @Test
    public void COST_008_estimateInterruptCost_ValidParameters_CalculatesCost() {
        Drone drone = createDrone("1", 2.0);

        DynamicDispatchService.DroneStatusDto status =
                new DynamicDispatchService.DroneStatusDto(
                        "1",
                        new PositionDto(0.0, 0.0),
                        DynamicDispatchService.DroneStatus.MOVING,
                        1, 5, 2, false, null
                );

        List<MedDispatchRec> tasks = Arrays.asList(
                createTask(1), createTask(2), createTask(3)
        );

        Map<Integer, PositionDto> taskLocations = new HashMap<>();
        taskLocations.put(1, new PositionDto(0.001, 0.001));
        taskLocations.put(2, new PositionDto(0.002, 0.002));
        taskLocations.put(3, new PositionDto(0.003, 0.003));

        when(dynamicDispatchService.estimateInterruptCost("1")).thenReturn(0.6);

        double cost = emergencyDispatchService.estimateInterruptCost(drone, status, tasks, taskLocations);

        // Formula: remainingPathRatio * costPerMove * (tasks.size() + 1)
        // = 0.6 * 2.0 * (3 + 1) = 0.6 * 2.0 * 4 = 4.8
        assertEquals(4.8, cost, 0.01);
    }

    @Test
    public void COST_009_estimateInterruptCost_NullCapability_UsesDefaultCostPerMove() {
        Drone drone = new Drone();
        drone.setId("1");
        // No capability set

        DynamicDispatchService.DroneStatusDto status =
                new DynamicDispatchService.DroneStatusDto(
                        "1",
                        new PositionDto(0.0, 0.0),
                        DynamicDispatchService.DroneStatus.MOVING,
                        1, 5, 2, false, null
                );

        List<MedDispatchRec> tasks = Arrays.asList(createTask(1), createTask(2));
        Map<Integer, PositionDto> taskLocations = new HashMap<>();

        when(dynamicDispatchService.estimateInterruptCost("1")).thenReturn(0.5);

        double cost = emergencyDispatchService.estimateInterruptCost(drone, status, tasks, taskLocations);

        // With null capability, costPerMove defaults to 1.0
        // Formula: 0.5 * 1.0 * (2 + 1) = 0.5 * 1.0 * 3 = 1.5
        assertEquals(1.5, cost, 0.01);
    }

    @Test
    public void COST_010_selectOptimalDrone_ViableDrone_ReturnsLowestCost() {
        Drone drone1 = createDrone("1", 1.0);
        Drone drone2 = createDrone("2", 1.0);
        List<Drone> candidates = Arrays.asList(drone1, drone2);

        Map<Drone, Double> interruptCosts = new HashMap<>();
        interruptCosts.put(drone1, 100.0);
        interruptCosts.put(drone2, 200.0);

        Map<Drone, Double> reassignCosts = new HashMap<>();
        reassignCosts.put(drone1, 150.0); // 100 * 1.5
        reassignCosts.put(drone2, 300.0); // 200 * 1.5

        Drone optimal = emergencyDispatchService.selectOptimalDrone(candidates, interruptCosts, reassignCosts);

        assertNotNull(optimal);
        assertEquals("1", optimal.getId()); // Lower reassign cost (150 < 300)
    }

    @Test
    public void COST_011_selectOptimalDrone_NoViable_ReturnsNull() {
        Drone drone = createDrone("1", 1.0);
        List<Drone> candidates = Collections.singletonList(drone);

        Map<Drone, Double> interruptCosts = new HashMap<>();
        interruptCosts.put(drone, 100.0);

        Map<Drone, Double> reassignCosts = new HashMap<>();
        reassignCosts.put(drone, 1500.0); // Exceeds constraint (1500 >= 100 * 10)

        Drone optimal = emergencyDispatchService.selectOptimalDrone(candidates, interruptCosts, reassignCosts);

        assertNull(optimal); // Reassign cost too high
    }

    @Test
    public void COST_012_selectOptimalDrone_EmptyCandidates_ReturnsNull() {
        List<Drone> candidates = Collections.emptyList();
        Map<Drone, Double> interruptCosts = new HashMap<>();
        Map<Drone, Double> reassignCosts = new HashMap<>();

        Drone optimal = emergencyDispatchService.selectOptimalDrone(candidates, interruptCosts, reassignCosts);

        assertNull(optimal);
    }

    @Test
    public void COST_013_selectOptimalDrone_DroneMissingFromCostMaps_Skips() {
        Drone drone1 = createDrone("1", 1.0);
        Drone drone2 = createDrone("2", 1.0);
        List<Drone> candidates = Arrays.asList(drone1, drone2);

        // Only drone1 has costs
        Map<Drone, Double> interruptCosts = new HashMap<>();
        interruptCosts.put(drone1, 100.0);

        Map<Drone, Double> reassignCosts = new HashMap<>();
        reassignCosts.put(drone1, 150.0);

        Drone optimal = emergencyDispatchService.selectOptimalDrone(candidates, interruptCosts, reassignCosts);

        // Should select drone1 (only viable one)
        assertNotNull(optimal);
        assertEquals("1", optimal.getId());
    }
}