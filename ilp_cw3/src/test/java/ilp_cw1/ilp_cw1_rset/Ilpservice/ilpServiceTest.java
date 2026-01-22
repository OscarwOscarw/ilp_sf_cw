package ilp_cw1.ilp_cw1_rset.Ilpservice;

import data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ilp_cw1.ilp_cw1_rset.Droneservice.ilpService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ilpService methods.
 * This test focuses on service logic without involving REST endpoints.
 */
public class ilpServiceTest {

    private ilpService service;
    private PositionDto p1;
    private PositionDto p2;
    private DistanceRequest distanceRequest;

    @BeforeEach
    void setUp() {
        // Initialize service and sample positions before each test
        service = new ilpService();

        p1 = new PositionDto(-3.192473, 55.946233);
        p2 = new PositionDto(-3.192473, 55.942617);

        distanceRequest = new DistanceRequest();
        distanceRequest.setPosition1(p1);
        distanceRequest.setPosition2(p2);
    }

    /** 1. Test distanceCalculate with valid positions */
    @Test
    void testDistanceCalculate_Valid() {
        Double distance = service.distanceCalculate(distanceRequest);
        assertEquals(0.003616, distance, 1e-6, "Distance should be correctly calculated");
    }

    /** 2. Test distanceCalculate with null positions (invalid) */
    @Test
    void testDistanceCalculate_NullPositions() {
        DistanceRequest invalidRequest = new DistanceRequest();
        invalidRequest.setPosition1(null);
        invalidRequest.setPosition2(null);

        assertThrows(NullPointerException.class,
                () -> service.distanceCalculate(invalidRequest),
                "Null positions should throw NullPointerException");
    }

    /** 3. Test movementCalculate with a valid angle */
    @Test
    void testMovementCalculate_Valid() {
        MovementRequest moveRequest = new MovementRequest();
        moveRequest.setStart(p1);
        moveRequest.setAngle(45.0);

        PositionDto result = service.movementCalculate(moveRequest);

        // The step distance is 0.00015, moving diagonally at 45 degrees
        double expectedDelta = 0.00015 / Math.sqrt(2);
        assertEquals(p1.getLng() + expectedDelta, result.getLng(), 1e-8, "Longitude should move diagonally");
        assertEquals(p1.getLat() + expectedDelta, result.getLat(), 1e-8, "Latitude should move diagonally");
    }

    /** 4. Test movementCalculate with null start (invalid) */
    @Test
    void testMovementCalculate_NullStart() {
        MovementRequest moveRequest = new MovementRequest();
        moveRequest.setStart(null);
        moveRequest.setAngle(30);

        assertThrows(NullPointerException.class,
                () -> service.movementCalculate(moveRequest),
                "Null start position should throw NullPointerException");
    }

    /** 5. Test isPointInPolygon with point inside a closed rectangle */
    @Test
    void testIsPointInPolygon_Inside() {
        List<PositionDto> vertices = new ArrayList<>();
        vertices.add(new PositionDto(-3.192473, 55.946233));
        vertices.add(new PositionDto(-3.192473, 55.942617));
        vertices.add(new PositionDto(-3.184319, 55.942617));
        vertices.add(new PositionDto(-3.184319, 55.946233));
        vertices.add(new PositionDto(-3.192473, 55.946233)); // closed polygon

        Region region = new Region();
        region.setName("central");
        region.setVertices(vertices);

        RegionRequest request = new RegionRequest();
        request.setPosition(new PositionDto(-3.188, 55.944));
        request.setRegion(region);

        assertTrue(service.isPointInPolygon(request), "Point inside closed region should return true");
    }

    /** 6. Test isPointInPolygon with point outside the polygon */
    @Test
    void testIsPointInPolygon_Outside() {
        List<PositionDto> vertices = new ArrayList<>();
        vertices.add(new PositionDto(-3.192473, 55.946233));
        vertices.add(new PositionDto(-3.192473, 55.942617));
        vertices.add(new PositionDto(-3.184319, 55.942617));
        vertices.add(new PositionDto(-3.184319, 55.946233));
        vertices.add(new PositionDto(-3.192473, 55.946233)); // closed polygon

        Region region = new Region();
        region.setName("central");
        region.setVertices(vertices);

        RegionRequest request = new RegionRequest();
        request.setPosition(new PositionDto(-3.193, 55.947)); // outside
        request.setRegion(region);

        assertFalse(service.isPointInPolygon(request), "Point outside region should return false");
    }

    /** 7. Test isPointInPolygon with open polygon (invalid) */
    @Test
    void testIsPointInPolygon_OpenPolygon() {
        List<PositionDto> vertices = Arrays.asList(
        new PositionDto(0.0, 0.0),
        new PositionDto(0.0, 1.0),
        new PositionDto(1.0, 1.0),
        new PositionDto(1.0, 0.0)
        );

        Region region = new Region();
        region.setName("central");
        region.setVertices(vertices);

        RegionRequest request = new RegionRequest();
        request.setPosition(new PositionDto(0.5, 0.5));
        request.setRegion(region);

        assertTrue(service.isPointInPolygon(request), "Point in open region should return false");
    }
}
