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

public class BoundaryValuePathTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ilpService ilpService;

    private droneService droneServiceUnderTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        droneServiceUnderTest = new droneService(restTemplate, ilpService);
    }

    @Test
    void testGoalExactlyOnObstacleBoundary_IsBlocked() {
        PositionDto start = new PositionDto();
        start.setLng(0.0); start.setLat(0.0);
        PositionDto goal = new PositionDto();
        goal.setLng(0.5); goal.setLat(0.5); // corner of obstacle

        RestrictedArea obs = new RestrictedArea();
        obs.setId(1L);
        obs.setName("square");
        obs.setVertices(Arrays.asList(
                pos(0.5, 0.5), pos(0.6, 0.5),
                pos(0.6, 0.6), pos(0.5, 0.6)
        ));

        Drone drone = new Drone("D", "D1", new Drone.DroneCapability(false, false, 10.0, 1000, 0.1, 1.0, 1.0));

        when(ilpService.distanceCalculate(any(DistanceRequest.class))).thenReturn(0.707);
        when(ilpService.movementCalculate(any(MovementRequest.class))).thenReturn(pos(0.1, 0.0));
        when(ilpService.isPointInPolygon(any(RegionRequest.class))).thenAnswer(inv -> {
            RegionRequest r = inv.getArgument(0);
            PositionDto p = r.getPosition();
            // Treat boundary as inside (common in GIS)
            return p.getLng() >= 0.5 && p.getLng() <= 0.6 && p.getLat() >= 0.5 && p.getLat() <= 0.6;
        });

        List<PositionDto> path = droneServiceUnderTest.calculateAStarPath(start, goal, List.of(obs), drone);

        assertTrue(path.isEmpty(), "Goal on obstacle boundary should be unreachable");
    }

    private PositionDto pos(double lng, double lat) {
        PositionDto p = new PositionDto();
        p.setLng(lng);
        p.setLat(lat);
        return p;
    }
}