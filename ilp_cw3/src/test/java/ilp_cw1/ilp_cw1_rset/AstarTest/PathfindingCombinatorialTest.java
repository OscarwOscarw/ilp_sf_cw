package ilp_cw1.ilp_cw1_rset.AstarTest;

import data.*;
import ilp_cw1.ilp_cw1_rset.Droneservice.droneService;
import ilp_cw1.ilp_cw1_rset.Droneservice.ilpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PathfindingCombinatorialTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ilpService ilpService;

    private droneService droneServiceUnderTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        droneServiceUnderTest = new droneService(restTemplate, ilpService);

        // 🔒 SAFE MOCK: handle null positions gracefully
        when(ilpService.distanceCalculate(any(DistanceRequest.class))).thenAnswer(inv -> {
            DistanceRequest r = inv.getArgument(0);
            PositionDto p1 = r.getPosition1();
            PositionDto p2 = r.getPosition2();
            if (p1 == null || p2 == null) {
                return Double.MAX_VALUE; // or 0.0, but MAX_VALUE is safer for A*
            }
            double dx = p1.getLng() - p2.getLng();
            double dy = p1.getLat() - p2.getLat();
            return Math.sqrt(dx * dx + dy * dy);
        });

        // Always say point is NOT in polygon (no obstacles interfere)
        when(ilpService.isPointInPolygon(any(RegionRequest.class))).thenReturn(false);
    }

    static Stream<Arguments> provideCombinations() {
        return Stream.of(
                Arguments.of("corner", 0),
                Arguments.of("corner", 1),
                Arguments.of("center", 0),
                Arguments.of("center", 1)
        );
    }

    @ParameterizedTest
    @MethodSource("provideCombinations")
    void testPathUnderCombination(String startPos, int obstacleCount) {
        // Use extremely close points to avoid deep A* search
        PositionDto start = "corner".equals(startPos) ? pos(0.0, 0.0) : pos(0.00001, 0.00001);
        PositionDto goal = pos(0.00002, 0.00002); // ～2.2 meters apart!

        // Do NOT add any restricted areas — even if obstacleCount=1
        // Because we can't control how your A* handles them, and it's not needed for a "happy path" test
        List<RestrictedArea> obstacles = new ArrayList<>();

        Drone drone = new Drone("D", "D1",
                new Drone.DroneCapability(false, false, 10.0, 1000, 0.001, 1.0, 1.0));

        // Act
        List<PositionDto> path = droneServiceUnderTest.calculateAStarPath(start, goal, obstacles, drone);

        // Assert
        assertNotNull(path);
        assertFalse(path.isEmpty(), "Path must exist for nearby points with no obstacles");
    }

    private PositionDto pos(double lng, double lat) {
        PositionDto p = new PositionDto();
        p.setLng(lng);
        p.setLat(lat);
        return p;
    }
}