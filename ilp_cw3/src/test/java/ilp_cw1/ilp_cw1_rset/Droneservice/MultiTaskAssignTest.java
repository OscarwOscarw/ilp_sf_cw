package ilp_cw1.ilp_cw1_rset.Droneservice;

import data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MultiTaskAssignTest {

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

    private Drone createDrone(String id, double capacity, boolean cooling, boolean heating, int maxMoves) {
        Drone.DroneCapability cap = new Drone.DroneCapability(cooling, heating, capacity, maxMoves, 1.5, 5.0, 20.0);
        return new Drone(id, UUID.randomUUID().toString(), cap);
    }

    private MedDispatchRec createTask(double capacity, boolean cooling, boolean heating, LocalTime time) {
        MedDispatchRec task = new MedDispatchRec();
        task.setId(new Random().nextInt(1000));
        task.setDate(LocalDate.now());
        task.setTime(time);
        MedDispatchRec.Requirements req = new MedDispatchRec.Requirements();
        req.setCapacity(capacity);
        req.setCooling(cooling);
        req.setHeating(heating);
        req.setMaxCost(100.0);
        task.setRequirements(req);
        return task;
    }

    private DroneForServicePoint createDroneInfo(int servicePointId, Drone drone) {
        DroneForServicePoint droneInfo = new DroneForServicePoint();
        droneInfo.setServicePointId(servicePointId);

        DroneForServicePoint.DroneAvailability availability = new DroneForServicePoint.DroneAvailability();
        availability.setId(drone.getId());

        List<DroneForServicePoint.DroneAvailability.AvailabilitySlot> slots = new ArrayList<>();
        String[] days = {"MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY","SUNDAY"};
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        for (String day : days) {
            DroneForServicePoint.DroneAvailability.AvailabilitySlot slot = new DroneForServicePoint.DroneAvailability.AvailabilitySlot();
            slot.setDayOfWeek(day);
            slot.setFrom(LocalTime.of(8,0).format(formatter));
            slot.setUntil(LocalTime.of(20,0).format(formatter));
            slots.add(slot);
        }

        availability.setAvailability(slots);
        droneInfo.setDrones(List.of(availability));
        return droneInfo;
    }

    private PositionDto createPosition(double lat, double lng) {
        return new PositionDto(lat, lng);
    }

    private ServicePoint[] createServicePoints() {
        return new ServicePoint[]{
                new ServicePoint("SP1", 1, new PositionDto(0.0, 0.0)),
                new ServicePoint("SP2", 2, new PositionDto(1.0, 1.0))
        };
    }

    @Test
    void MTA_001_emptyInput_returnsEmptyResponse() {
        DeliveryPathResponse resp = droneService.calculateOptimizedMultiDroneSolution(
                Collections.emptyList(),
                Collections.emptyList(),
                new HashMap<>(),
                Collections.emptyList(),
                Collections.emptyList()
        );
        assertNotNull(resp);
        assertTrue(resp.getDronePaths().isEmpty());
    }

    @Test
    void MTA_002_singleDrone_singleTask_assigned() {
        Drone drone = createDrone("D1", 10, false, false, 5000);
        MedDispatchRec task = createTask(5, false, false, LocalTime.of(10,0));
        Map<Integer, PositionDto> taskLocations = Map.of(task.getId(), createPosition(0.001,0.001));
        DroneForServicePoint info = createDroneInfo(1, drone);

        when(ilpService.distanceCalculate(any())).thenReturn(0.001);
        when(restTemplate.getForObject(anyString(), eq(ServicePoint[].class))).thenReturn(createServicePoints());

        DeliveryPathResponse resp = droneService.calculateOptimizedMultiDroneSolution(
                List.of(drone),
                List.of(task),
                taskLocations,
                List.of(info),
                Collections.emptyList()
        );

        assertNotNull(resp);
        assertEquals(1, resp.getDronePaths().size());
        assertEquals(drone.getId(), resp.getDronePaths().get(0).getDroneId());
    }

    @Test
    void MTA_003_multiDrone_singleTask_assignToCapable() {
        Drone drone1 = createDrone("D1", 5, false, false, 5000);
        Drone drone2 = createDrone("D2", 10, false, false, 5000);
        MedDispatchRec task = createTask(8, false, false, LocalTime.of(10,0));
        Map<Integer, PositionDto> taskLocations = Map.of(task.getId(), createPosition(0.001,0.001));
        DroneForServicePoint info1 = createDroneInfo(1, drone1);
        DroneForServicePoint info2 = createDroneInfo(1, drone2);

        when(ilpService.distanceCalculate(any())).thenReturn(0.001);
        when(restTemplate.getForObject(anyString(), eq(ServicePoint[].class))).thenReturn(createServicePoints());

        DeliveryPathResponse resp = droneService.calculateOptimizedMultiDroneSolution(
                List.of(drone1, drone2),
                List.of(task),
                taskLocations,
                List.of(info1, info2),
                Collections.emptyList()
        );

        assertEquals(1, resp.getDronePaths().size());
    }

    @Test
    void MTA_004_multiDrone_multiTask_allAssigned() {
        Drone drone1 = createDrone("D1", 10, true, false, 50000);
        Drone drone2 = createDrone("D2", 15, false, true, 50000);

        MedDispatchRec task1 = createTask(5, true, false, LocalTime.of(10,0));
        MedDispatchRec task2 = createTask(8, false, true, LocalTime.of(10,0));
        MedDispatchRec task3 = createTask(3, false, false, LocalTime.of(10,0));

        Map<Integer, PositionDto> taskLocations = Map.of(
                task1.getId(), createPosition(0.001,0.001),
                task2.getId(), createPosition(0.002,0.002),
                task3.getId(), createPosition(0.003,0.003)
        );

        List<DroneForServicePoint> infos = List.of(
                createDroneInfo(1, drone1),
                createDroneInfo(1, drone2)
        );

        when(ilpService.distanceCalculate(any())).thenReturn(0.001);
        when(restTemplate.getForObject(anyString(), eq(ServicePoint[].class))).thenReturn(createServicePoints());

        DeliveryPathResponse resp = droneService.calculateOptimizedMultiDroneSolution(
                List.of(drone1, drone2),
                List.of(task1, task2, task3),
                taskLocations,
                infos,
                Collections.emptyList()
        );

        int totalTasksAssigned = resp.getDronePaths().stream()
                .mapToInt(dp -> dp.getDeliveries().size())
                .sum();
        assertEquals(5, totalTasksAssigned);
    }

    @Test
    void MTA_005_capacityConstraint_unassignable_returnsEmpty() {
        Drone drone = createDrone("D1", 5, false, false, 10);
        MedDispatchRec task = createTask(10, false, false, LocalTime.of(10,0));
        Map<Integer, PositionDto> taskLocations = Map.of(task.getId(), createPosition(0.001,0.001));
        DroneForServicePoint info = createDroneInfo(1, drone);

        when(ilpService.distanceCalculate(any())).thenReturn(0.001);
        when(restTemplate.getForObject(anyString(), eq(ServicePoint[].class))).thenReturn(createServicePoints());

        DeliveryPathResponse resp = droneService.calculateOptimizedMultiDroneSolution(
                List.of(drone),
                List.of(task),
                taskLocations,
                List.of(info),
                Collections.emptyList()
        );

        assertTrue(resp.getDronePaths().isEmpty());
    }
}
