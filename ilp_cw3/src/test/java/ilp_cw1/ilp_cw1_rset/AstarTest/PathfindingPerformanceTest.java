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

/**
 * Performance test for A* pathfinding.
 * Verifies that path calculation completes quickly (< 2 seconds)
 * even when 10 small obstacles are present.
 *
 * Note: This test uses mocked ILP service calls without artificial delays,
 * so execution time reflects only the efficiency of the A* implementation.
 */
public class PathfindingPerformanceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ilpService ilpService;

    private droneService droneServiceUnderTest;

    @BeforeEach
    void setUp() {
        // Initialize Mockito mocks
        MockitoAnnotations.openMocks(this);
        // Inject mocks into the service under test
        droneServiceUnderTest = new droneService(restTemplate, ilpService);
    }

    @Test
    void testPathfindingCompletesWithinTimeBudget() {
        // Define start and goal positions very close to each other
        PositionDto start = new PositionDto();
        start.setLng(0.0);
        start.setLat(0.0);

        PositionDto goal = new PositionDto();
        goal.setLng(0.001); // 1 meter east (approx)
        goal.setLat(0.0);

        // Create 10 small non-blocking obstacles near the path
        List<RestrictedArea> obstacles = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            RestrictedArea r = new RestrictedArea();
            r.setId((long) i);
            r.setName("obs" + i);
            // Each obstacle is a tiny rectangle offset slightly from the direct line
            r.setVertices(Arrays.asList(
                    pos(0.0002 + i * 0.00005, 0.0001),
                    pos(0.00025 + i * 0.00005, 0.0001),
                    pos(0.00025 + i * 0.00005, 0.0002),
                    pos(0.0002 + i * 0.00005, 0.0002)
            ));
            obstacles.add(r);
        }

        // Create a basic drone configuration
        Drone.DroneCapability capability = new Drone.DroneCapability(
                false,   // no vertical movement
                false,   // no hovering
                10.0,    // max speed (m/s)
                1000,    // battery life (seconds)
                0.1,     // min safe distance to obstacles (km → but treated as unitless here)
                1.0,     // sensor range
                1.0      // communication range
        );
        Drone drone = new Drone("D", "D1", capability);

        // Mock distance calculation: Euclidean distance (no delay)
        when(ilpService.distanceCalculate(any(DistanceRequest.class))).thenAnswer(invocation -> {
            DistanceRequest req = invocation.getArgument(0);
            double dx = req.getPosition1().getLng() - req.getPosition2().getLng();
            double dy = req.getPosition1().getLat() - req.getPosition2().getLat();
            return Math.sqrt(dx * dx + dy * dy);
        });

        // Mock movement calculation: move 0.0001 units east (simplified)
        when(ilpService.movementCalculate(any(MovementRequest.class))).thenAnswer(invocation -> {
            MovementRequest req = invocation.getArgument(0);
            PositionDto result = new PositionDto();
            result.setLng(req.getStart().getLng() + 0.0001);
            result.setLat(req.getStart().getLat());
            return result;
        });

        // Assume no point is inside any obstacle (obstacles do not block the path)
        when(ilpService.isPointInPolygon(any(RegionRequest.class))).thenReturn(false);

        // Measure execution time
        long startTimeNanos = System.nanoTime();
        List<PositionDto> path = droneServiceUnderTest.calculateAStarPath(start, goal, obstacles, drone);
        long durationMillis = (System.nanoTime() - startTimeNanos) / 1_000_000;

        // Assertions
        assertFalse(path.isEmpty(), "A valid path should be found between start and goal.");
        assertTrue(durationMillis < 2000,
                String.format("Pathfinding took %d ms — expected to complete within 2000 ms.", durationMillis));
    }

    /**
     * Helper method to create a PositionDto with given coordinates.
     */
    private PositionDto pos(double lng, double lat) {
        PositionDto p = new PositionDto();
        p.setLng(lng);
        p.setLat(lat);
        return p;
    }
}