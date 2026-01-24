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
public class PathCalculationTest {

    @Mock
    private droneService droneService;

    @Mock
    private DynamicDispatchService dynamicDispatchService;

    @InjectMocks
    private EmergencyDispatchService emergencyDispatchService;

    private Drone createDrone(String id) {
        Drone.DroneCapability capability = new Drone.DroneCapability(
                true, false, 10.0, 100, 1.5, 5.0, 20.0
        );
        Drone drone = new Drone();
        drone.setId(id);
        drone.setCapability(capability);
        return drone;
    }

    private EmergencyMedDispatchRec createEmergencyTask(int id, int level) {
        EmergencyMedDispatchRec task = new EmergencyMedDispatchRec();
        task.setId(id);
        task.setEmergencyLevel(level);

        MedDispatchRec.Requirements requirements = new MedDispatchRec.Requirements();
        requirements.setCapacity(5.0);
        requirements.setCooling(true);
        requirements.setHeating(false);
        requirements.setMaxCost(100.0);
        task.setRequirements(requirements);

        EmergencyMedDispatchRec.Delivery delivery = new EmergencyMedDispatchRec.Delivery();
        delivery.setLng(1.0);
        delivery.setLat(1.0);
        task.setDelivery(delivery);

        return task;
    }

    private RestrictedArea createRestrictedArea(String name, PositionDto... vertices) {
        RestrictedArea area = new RestrictedArea();
        area.setName(name);
        area.setVertices(Arrays.asList(vertices));
        return area;
    }

    @BeforeEach
    public void setup() {
        // Setup service point
        when(droneService.getServicePointForDrone(any(Drone.class), anyList()))
                .thenReturn(new PositionDto(0.0, 0.0));

        // Setup available drones info
        DroneForServicePoint.DroneAvailability.AvailabilitySlot slot =
                new DroneForServicePoint.DroneAvailability.AvailabilitySlot();
        slot.setDayOfWeek("MONDAY");
        slot.setFrom("09:00");
        slot.setUntil("17:00");

        DroneForServicePoint.DroneAvailability droneAvailability =
                new DroneForServicePoint.DroneAvailability();
        droneAvailability.setId("1");
        droneAvailability.setAvailability(Collections.singletonList(slot));

        DroneForServicePoint servicePoint = new DroneForServicePoint();
        servicePoint.setServicePointId(1001);
        servicePoint.setDrones(Collections.singletonList(droneAvailability));

        when(droneService.readAvailableDrones()).thenReturn(Collections.singletonList(servicePoint));
    }

    @Test
    public void PATH_001_shouldBypassRestrictedAreas_Level5Plus_ReturnsTrue() {
        EmergencyMedDispatchRec level5Task = createEmergencyTask(1, 5);
        EmergencyMedDispatchRec level6Task = createEmergencyTask(2, 6);
        EmergencyMedDispatchRec level10Task = createEmergencyTask(3, 10);

        assertTrue(emergencyDispatchService.shouldBypassRestrictedAreas(level5Task));
        assertTrue(emergencyDispatchService.shouldBypassRestrictedAreas(level6Task));
        assertTrue(emergencyDispatchService.shouldBypassRestrictedAreas(level10Task));
    }

    @Test
    public void PATH_002_shouldBypassRestrictedAreas_LevelBelow5_ReturnsFalse() {
        EmergencyMedDispatchRec level1Task = createEmergencyTask(1, 1);
        EmergencyMedDispatchRec level2Task = createEmergencyTask(2, 2);
        EmergencyMedDispatchRec level3Task = createEmergencyTask(3, 3);
        EmergencyMedDispatchRec level4Task = createEmergencyTask(4, 4);

        assertFalse(emergencyDispatchService.shouldBypassRestrictedAreas(level1Task));
        assertFalse(emergencyDispatchService.shouldBypassRestrictedAreas(level2Task));
        assertFalse(emergencyDispatchService.shouldBypassRestrictedAreas(level3Task));
        assertFalse(emergencyDispatchService.shouldBypassRestrictedAreas(level4Task));
    }

    @Test
    public void PATH_003_calculateDirectPath_ValidPoints_ReturnsInterpolatedPath() {
        PositionDto start = new PositionDto(0.0, 0.0);
        PositionDto end = new PositionDto(1.0, 1.0);

        List<PositionDto> path = emergencyDispatchService.calculateDirectPath(start, end);

        assertNotNull(path);
        assertTrue(path.size() >= 2);
        assertEquals(start, path.get(0));
        assertEquals(end, path.get(path.size() - 1));

        // Should have interpolated points
        assertTrue(path.size() > 2);
    }

    @Test
    public void PATH_004_calculateDirectPath_SamePoints_ReturnsPathWithTwoPoints() {
        PositionDto point = new PositionDto(1.0, 1.0);

        List<PositionDto> path = emergencyDispatchService.calculateDirectPath(point, point);

        assertEquals(4, path.size());
        assertEquals(point, path.get(0));
    }

    @Test
    public void PATH_005_calculateDirectPath_VeryClosePoints_ReturnsPath() {
        PositionDto start = new PositionDto(0.0, 0.0);
        PositionDto end = new PositionDto(0.0001, 0.0001); // Very close

        List<PositionDto> path = emergencyDispatchService.calculateDirectPath(start, end);

        assertNotNull(path);
        assertTrue(path.size() >= 2);
    }

    @Test
    public void PATH_006_isPathReachable_ReachablePath_ReturnsTrue() {
        List<PositionDto> path = Arrays.asList(
                new PositionDto(0.0, 0.0),
                new PositionDto(0.5, 0.5),
                new PositionDto(1.0, 1.0)
        );

        PositionDto target = new PositionDto(1.0, 1.0);

        boolean reachable = emergencyDispatchService.isPathReachable(path, target);

        assertTrue(reachable);
    }

    @Test
    public void PATH_007_isPathReachable_UnreachablePath_ReturnsFalse() {
        List<PositionDto> path = Arrays.asList(
                new PositionDto(0.0, 0.0),
                new PositionDto(0.5, 0.5)
        );

        PositionDto target = new PositionDto(1.0, 1.0); // Far away

        boolean reachable = emergencyDispatchService.isPathReachable(path, target);

        assertFalse(reachable);
    }

    @Test
    public void PATH_008_isPathReachable_EmptyPath_ReturnsFalse() {
        List<PositionDto> path = Collections.emptyList();
        PositionDto target = new PositionDto(1.0, 1.0);

        boolean reachable = emergencyDispatchService.isPathReachable(path, target);

        assertFalse(reachable);
    }

    @Test
    public void PATH_009_calculateDistance_TwoPoints_ReturnsEuclideanDistance() {
        PositionDto p1 = new PositionDto(0.0, 0.0);
        PositionDto p2 = new PositionDto(3.0, 4.0); // Distance should be 5

        double distance = emergencyDispatchService.calculateDistance(p1, p2);

        assertEquals(5.0, distance, 0.001);
    }

    @Test
    public void PATH_010_calculateDistance_SamePoint_ReturnsZero() {
        PositionDto p = new PositionDto(1.0, 1.0);

        double distance = emergencyDispatchService.calculateDistance(p, p);

        assertEquals(0.0, distance, 0.001);
    }

    @Test
    public void PATH_011_getBlockingRestrictedAreaName_IntersectsArea_ReturnsName() {
        PositionDto start = new PositionDto(0.0, 0.0);
        PositionDto end = new PositionDto(2.0, 2.0);

        RestrictedArea area1 = createRestrictedArea("Area1",
                new PositionDto(0.5, 0.5),
                new PositionDto(0.5, 1.5),
                new PositionDto(1.5, 1.5),
                new PositionDto(1.5, 0.5)
        );

        List<RestrictedArea> restrictedAreas = Collections.singletonList(area1);

        when(droneService.doesLineIntersectPolygon(any(), any(), any())).thenReturn(true);
        when(droneService.isPointInPolygon(any(), any())).thenReturn(false);

        String areaName = emergencyDispatchService.getBlockingRestrictedAreaName(start, end, restrictedAreas);

        assertEquals("Area1", areaName);
    }

    @Test
    public void PATH_012_getBlockingRestrictedAreaName_NoIntersection_ReturnsUnknown() {
        PositionDto start = new PositionDto(0.0, 0.0);
        PositionDto end = new PositionDto(2.0, 2.0);

        RestrictedArea area1 = createRestrictedArea("Area1",
                new PositionDto(3.0, 3.0),
                new PositionDto(3.0, 4.0),
                new PositionDto(4.0, 4.0),
                new PositionDto(4.0, 3.0)
        );

        List<RestrictedArea> restrictedAreas = Collections.singletonList(area1);

        when(droneService.doesLineIntersectPolygon(any(), any(), any())).thenReturn(false);
        when(droneService.isPointInPolygon(any(), any())).thenReturn(false);

        String areaName = emergencyDispatchService.getBlockingRestrictedAreaName(start, end, restrictedAreas);

        assertEquals("Unknown restricted area", areaName);
    }

    @Test
    public void PATH_013_getBlockingRestrictedAreaName_NullAreaName_ReturnsUnknown() {
        PositionDto start = new PositionDto(0.0, 0.0);
        PositionDto end = new PositionDto(2.0, 2.0);

        RestrictedArea area = new RestrictedArea();
        area.setName(null); // Null name
        area.setVertices(Arrays.asList(
                new PositionDto(0.5, 0.5),
                new PositionDto(0.5, 1.5),
                new PositionDto(1.5, 1.5)
        ));

        List<RestrictedArea> restrictedAreas = Collections.singletonList(area);

        when(droneService.doesLineIntersectPolygon(any(), any(), any())).thenReturn(true);

        String areaName = emergencyDispatchService.getBlockingRestrictedAreaName(start, end, restrictedAreas);

        assertEquals("Unknown restricted area", areaName);
    }

    @Test
    public void PATH_014_initializeIdleDroneState_SimulationRunning_AddsDroneState() {
        Drone drone = createDrone("idle-1");

        when(dynamicDispatchService.isSimulationRunning()).thenReturn(true);

        emergencyDispatchService.initializeIdleDroneState(drone);

        verify(dynamicDispatchService).addDroneState(eq("idle-1"), any(DeliveryPathResponse.DronePath.class));
    }

    @Test
    public void PATH_015_initializeIdleDroneState_SimulationNotRunning_StartsSimulation() {
        Drone drone = createDrone("idle-1");

        when(dynamicDispatchService.isSimulationRunning()).thenReturn(false);

        emergencyDispatchService.initializeIdleDroneState(drone);

        verify(dynamicDispatchService).startSimulation(any(DeliveryPathResponse.class));
    }

    @Test
    public void PATH_016_calculateEmergencyDeliveryPath_Level5_BypassesRestrictedAreas() {
        Drone drone = createDrone("1");
        EmergencyMedDispatchRec task = createEmergencyTask(999, 5); // Level 5
        PositionDto startPosition = new PositionDto(0.0, 0.0);

        // Mock restricted areas
        List<RestrictedArea> restrictedAreas = Arrays.asList(
                createRestrictedArea("NoFlyZone",
                        new PositionDto(0.5, 0.5),
                        new PositionDto(0.5, 1.5),
                        new PositionDto(1.5, 1.5),
                        new PositionDto(1.5, 0.5)
                )
        );

        when(droneService.getRestrictedAreas()).thenReturn(restrictedAreas);

        // Level 5 should bypass restricted areas, so A* won't be called
        when(droneService.calculateAStarPath(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        DeliveryPathResponse.Delivery result = emergencyDispatchService.calculateEmergencyDeliveryPath(
                drone, task, startPosition, false, false
        );

        assertNotNull(result);
        assertEquals(Integer.valueOf(999), result.getDeliveryId());
        // Should have a path (direct calculation)
        assertNotNull(result.getFlightPath());
    }

    @Test
    public void PATH_017_calculateEmergencyDeliveryPath_Level3_RespectsRestrictedAreas() {
        Drone drone = createDrone("1");
        EmergencyMedDispatchRec task = createEmergencyTask(999, 3); // Level 3
        PositionDto startPosition = new PositionDto(0.0, 0.0);

        // Mock A* path
        List<PositionDto> aStarPath = Arrays.asList(
                new PositionDto(0.0, 0.0),
                new PositionDto(0.5, 0.5),
                new PositionDto(1.0, 1.0)
        );

        when(droneService.calculateAStarPath(any(), any(), any(), any())).thenReturn(aStarPath);

        DeliveryPathResponse.Delivery result = emergencyDispatchService.calculateEmergencyDeliveryPath(
                drone, task, startPosition, false, false
        );

        assertNotNull(result);
        assertEquals(Integer.valueOf(999), result.getDeliveryId());
        assertEquals(aStarPath, result.getFlightPath());

        // Verify A* was called (not bypass)
        verify(droneService).calculateAStarPath(any(), any(), any(), any());
    }

    @Test
    public void PATH_018_calculateEmergencyDeliveryPath_ForceBypass_IgnoresLevel() {
        Drone drone = createDrone("1");
        EmergencyMedDispatchRec task = createEmergencyTask(999, 3); // Level 3
        PositionDto startPosition = new PositionDto(0.0, 0.0);

        // With forceBypass = true, should use direct path regardless of level
        DeliveryPathResponse.Delivery result = emergencyDispatchService.calculateEmergencyDeliveryPath(
                drone, task, startPosition, false, true // forceBypass = true
        );

        assertNotNull(result);
        assertEquals(Integer.valueOf(999), result.getDeliveryId());

        // Should not call A* when forceBypass is true
        verify(droneService, never()).calculateAStarPath(any(), any(), any(), any());
    }
}