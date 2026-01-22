package ilp_cw1.ilp_cw1_rset.Droneservice;

import data.Drone;
import data.MedDispatchRec;
import data.PositionDto;
import data.DeliveryPathResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;
import data.DeliveryPathResponse.Delivery;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SingleDronePathTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private droneService realDroneService;

    private droneService spyDroneService;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        spyDroneService = spy(realDroneService);
    }

    @Test
    public void calculateSingleDronePath_SingleTask_ReturnsValidPath() {
        Drone drone = createDroneWithCapability(20.0, false, false, 100);
        MedDispatchRec task = createTaskWithRequirements(10.0, false, false);

        PositionDto servicePoint = new PositionDto(0.0, 0.0);
        PositionDto taskLocation = new PositionDto(0.001, 0.001);

        Map<Integer, PositionDto> taskLocations = new HashMap<>();
        taskLocations.put(task.getId(), taskLocation);

        List<PositionDto> mockPathToTask = Arrays.asList(servicePoint, taskLocation);
        doReturn(mockPathToTask).when(spyDroneService).calculateAStarPath(
                eq(servicePoint),
                eq(taskLocation),
                anyList(),
                eq(drone)
        );

        List<PositionDto> mockReturnPath = Arrays.asList(taskLocation, servicePoint);
        doReturn(mockReturnPath).when(spyDroneService).calculateAStarPath(
                eq(taskLocation),
                eq(servicePoint),
                anyList(),
                eq(drone)
        );

        DeliveryPathResponse.DronePath dronePath = spyDroneService.calculateSingleDronePath(
                drone,
                Collections.singletonList(task),
                servicePoint,
                taskLocations,
                new ArrayList<>()
        );

        assertNotNull(dronePath);
        assertEquals(drone.getId(), dronePath.getDroneId());
        assertEquals(2, dronePath.getDeliveries().size());

        DeliveryPathResponse.Delivery delivery = dronePath.getDeliveries().get(0);
        List<PositionDto> flightPath = delivery.getFlightPath();

        boolean containsServicePoint = flightPath.stream().anyMatch(p ->
                Math.abs(p.getLat() - servicePoint.getLat()) < 1e-6 &&
                        Math.abs(p.getLng() - servicePoint.getLng()) < 1e-6
        );
        boolean containsTaskLocation = flightPath.stream().anyMatch(p ->
                Math.abs(p.getLat() - taskLocation.getLat()) < 1e-6 &&
                        Math.abs(p.getLng() - taskLocation.getLng()) < 1e-6
        );

        assertTrue(containsServicePoint);
        assertTrue(containsTaskLocation);
    }



    @Test
    public void calculateSingleDronePath_ExceedsMaxMoves_ThrowsException() {
        Drone drone = createDroneWithCapability(20.0, false, false, 5);
        MedDispatchRec task = createTaskWithRequirements(10.0, false, false);
        PositionDto servicePoint = new PositionDto(0.0, 0.0);
        PositionDto taskLocation = new PositionDto(0.01, 0.01);

        Map<Integer, PositionDto> taskLocations = new HashMap<>();
        taskLocations.put(task.getId(), taskLocation);

        List<PositionDto> longPath = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            longPath.add(new PositionDto(i * 0.001, i * 0.001));
        }

        doReturn(longPath).when(spyDroneService).calculateAStarPath(
                eq(servicePoint),
                eq(taskLocation),
                anyList(),
                eq(drone)
        );

        assertThrows(IllegalArgumentException.class, () -> {
            spyDroneService.calculateSingleDronePath(
                    drone,
                    Collections.singletonList(task),
                    servicePoint,
                    taskLocations,
                    new ArrayList<>()
            );
        });
    }

    private Drone createDroneWithCapability(double capacity, boolean cooling, boolean heating, int maxMoves) {
        Drone.DroneCapability capability = new Drone.DroneCapability(cooling, heating, capacity, maxMoves, 1.5, 5.0, 20.0);
        return new Drone("DroneA", UUID.randomUUID().toString(), capability);
    }

    private MedDispatchRec createTaskWithRequirements(double capacity, boolean cooling, boolean heating) {
        MedDispatchRec task = new MedDispatchRec();
        task.setId(new Random().nextInt(1000));
        task.setDate(LocalDate.now());
        task.setTime(LocalTime.of(10, 0));

        MedDispatchRec.Requirements requirements = new MedDispatchRec.Requirements();
        requirements.setCapacity(capacity);
        requirements.setCooling(cooling);
        requirements.setHeating(heating);
        requirements.setMaxCost(100.0);

        task.setRequirements(requirements);
        return task;
    }
}
