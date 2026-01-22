package ilp_cw1.ilp_cw1_rset.AstarTest;

import data.*;
import ilp_cw1.ilp_cw1_rset.Droneservice.droneService;
import ilp_cw1.ilp_cw1_rset.Droneservice.ilpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AStarAlgorithmEdgeCaseTest {

    @Mock private RestTemplate restTemplate;
    @Mock private ilpService ilpService;
    private droneService droneServiceUnderTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        droneServiceUnderTest = new droneService(restTemplate, ilpService);
    }

    private RestrictedArea createSquareObstacle(double cx, double cy, double half) {
        List<PositionDto> v = Arrays.asList(
                pos(cx - half, cy - half),
                pos(cx + half, cy - half),
                pos(cx + half, cy + half),
                pos(cx - half, cy + half)
        );
        RestrictedArea r = new RestrictedArea();
        r.setId(1L);
        r.setName("obs");
        r.setVertices(v);
        return r;
    }

    @Test
    void testStartEqualsGoal_ReturnsSinglePoint() {
        PositionDto start = pos(0.0, 0.0);
        PositionDto goal = pos(0.0, 0.0);
        Drone drone = new Drone("D", "D1", new Drone.DroneCapability(false, false, 10.0, 1000, 0.1, 1.0, 1.0));

        when(ilpService.isPointInPolygon(any(RegionRequest.class))).thenReturn(false);

        List<PositionDto> path = droneServiceUnderTest.calculateAStarPath(start, goal, new ArrayList<>(), drone);
        assertNotNull(path);
        assertEquals(1, path.size());
        assertEquals(0.0, path.get(0).getLng(), 1e-9);
        assertEquals(0.0, path.get(0).getLat(), 1e-9);
    }

    @Test
    void testNoPathExists_ReturnsEmptyList() {
        PositionDto start = pos(0.0, 0.0);
        PositionDto goal = pos(0.001, 0.0);
        RestrictedArea wall = createSquareObstacle(0.0005, 0.0, 0.0006);
        List<RestrictedArea> obstacles = Collections.singletonList(wall);
        Drone drone = new Drone("D", "D1", new Drone.DroneCapability(false, false, 10.0, 1000, 0.1, 1.0, 1.0));

        // Mock distance
        when(ilpService.distanceCalculate(any(DistanceRequest.class))).thenAnswer(inv -> {
            DistanceRequest r = inv.getArgument(0);
            double dx = r.getPosition1().getLng() - r.getPosition2().getLng();
            double dy = r.getPosition1().getLat() - r.getPosition2().getLat();
            return Math.sqrt(dx*dx + dy*dy);
        });

        // Mock movement: 16 directions, step ～0.00015
        when(ilpService.movementCalculate(any(MovementRequest.class))).thenAnswer(inv -> {
            MovementRequest m = inv.getArgument(0);
            double angleDeg = m.getAngle();
            double angleRad = Math.toRadians(angleDeg);
            double step = 0.00015;
            PositionDto p = new PositionDto();
            p.setLng(m.getStart().getLng() + step * Math.cos(angleRad));
            p.setLat(m.getStart().getLat() + step * Math.sin(angleRad));
            return p;
        });

        // Mock obstacle: block entire strip
        when(ilpService.isPointInPolygon(any(RegionRequest.class))).thenAnswer(inv -> {
            RegionRequest rr = inv.getArgument(0);
            PositionDto p = rr.getPosition();
            return p.getLng() >= -0.0001 && p.getLng() <= 0.0011 && Math.abs(p.getLat()) <= 0.0006;
        });

        List<PositionDto> path = droneServiceUnderTest.calculateAStarPath(start, goal, obstacles, drone);
        assertNotNull(path);
        assertTrue(path.isEmpty(), "Should return empty when completely blocked");
    }

    private PositionDto pos(double lng, double lat) {
        PositionDto p = new PositionDto();
        p.setLng(lng);
        p.setLat(lat);
        return p;
    }
}