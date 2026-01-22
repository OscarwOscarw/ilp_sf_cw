package ilp_cw1.ilp_cw1_rset.Ilpservice;

import data.PositionDto;
import data.Region;
import data.RegionRequest;
import ilp_cw1.ilp_cw1_rset.ilpController;
import ilp_cw1.ilp_cw1_rset.Droneservice.ilpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;


import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class regionTest {

    @Mock
    private ilpService ilpService;

    @InjectMocks
    private ilpController controller;

    // Test data - a square region centered around your base coordinates
    private PositionDto insidePoint;       // Point inside the region
    private PositionDto outsidePoint;      // Point outside the region
    private PositionDto edgePoint;         // Point exactly on the edge
    private Region validRegion;            // Valid polygon region (square)
    private RegionRequest validInsideRequest;
    private RegionRequest validOutsideRequest;
    private RegionRequest validEdgeRequest;

    /**
     * Initialize test data before each test
     * Creates a square region and test points in different locations relative to it
     */
    @BeforeEach
    void setUp() {
        // 1. Create a square region (polygon with 4 vertices + closing vertex)
        //    Vertices follow your base coordinate area: (-3.192473, 55.946233)
        PositionDto v1 = new PositionDto(-3.193, 55.947);  // Top-left
        PositionDto v2 = new PositionDto(-3.191, 55.947);  // Top-right
        PositionDto v3 = new PositionDto(-3.191, 55.945);  // Bottom-right
        PositionDto v4 = new PositionDto(-3.193, 55.945);  // Bottom-left
        PositionDto v5 = new PositionDto(-3.193, 55.947);  // Closing vertex (same as v1)

        validRegion = new Region();
        validRegion.setName("test-region");
        validRegion.setVertices(Arrays.asList(v1, v2, v3, v4, v5));  // 5 vertices (min 4 + closing)

        // 2. Test points
        insidePoint = new PositionDto(-3.192, 55.946);    // Inside the square
        outsidePoint = new PositionDto(-3.190, 55.946);   // Outside (right of square)
        edgePoint = new PositionDto(-3.192, 55.947);      // On top edge (between v1 and v2)

        // 3. Valid requests
        validInsideRequest = createRegionRequest(insidePoint, validRegion);
        validOutsideRequest = createRegionRequest(outsidePoint, validRegion);
        validEdgeRequest = createRegionRequest(edgePoint, validRegion);
    }

    /**
     * Helper method to create RegionRequest objects
     */
    private RegionRequest createRegionRequest(PositionDto position, Region region) {
        RegionRequest request = new RegionRequest();
        request.setPosition(position);
        request.setRegion(region);
        return request;
    }

    /**
     * Test scenario: Point is inside the region
     * Expected result: Returns true (200 OK)
     */
    @Test
    void testIsInRegion_PointInside_ReturnsTrue() {
        // Arrange: Mock service to return true for inside point
        when(ilpService.isPointInPolygon(validInsideRequest)).thenReturn(true);

        // Act
        ResponseEntity<Boolean> response = controller.isInRegion(validInsideRequest);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.hasBody());
        assertTrue(response.getBody(), "Should return true for points inside region");
    }

    /**
     * Test scenario: Point is outside the region
     * Expected result: Returns false (200 OK)
     */
    @Test
    void testIsInRegion_PointOutside_ReturnsFalse() {
        // Arrange: Mock service to return false for outside point
        when(ilpService.isPointInPolygon(validOutsideRequest)).thenReturn(false);

        // Act
        ResponseEntity<Boolean> response = controller.isInRegion(validOutsideRequest);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.hasBody());
        assertFalse(response.getBody(), "Should return false for points outside region");
    }

    /**
     * Test scenario: Point is exactly on the region edge
     * Expected result: Returns true (200 OK) based on service logic
     */
    @Test
    void testIsInRegion_PointOnEdge_ReturnsTrue() {
        // Arrange: Mock service to return true for edge point
        when(ilpService.isPointInPolygon(validEdgeRequest)).thenReturn(true);

        // Act
        ResponseEntity<Boolean> response = controller.isInRegion(validEdgeRequest);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody(), "Should return true for points on region edge");
    }

    /**
     * Test scenario: Null request object
     * Expected result: Returns 400 Bad Request
     */
    @Test
    void testIsInRegion_NullRequest_ReturnsBadRequest() {
        ResponseEntity<Boolean> response = controller.isInRegion(null);

        assertEquals(400, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    /**
     * Test scenario: Null position in request
     * Expected result: Returns 400 Bad Request
     */
    @Test
    void testIsInRegion_NullPosition_ReturnsBadRequest() {
        RegionRequest invalidRequest = new RegionRequest();
        invalidRequest.setPosition(null);  // Null position
        invalidRequest.setRegion(validRegion);

        ResponseEntity<Boolean> response = controller.isInRegion(invalidRequest);

        assertEquals(400, response.getStatusCodeValue());
    }

    /**
     * Test scenario: Null region in request
     * Expected result: Returns 400 Bad Request
     */
    @Test
    void testIsInRegion_NullRegion_ReturnsBadRequest() {
        RegionRequest invalidRequest = new RegionRequest();
        invalidRequest.setPosition(insidePoint);
        invalidRequest.setRegion(null);  // Null region

        ResponseEntity<Boolean> response = controller.isInRegion(invalidRequest);

        assertEquals(400, response.getStatusCodeValue());
    }

    /**
     * Test scenario: Region with insufficient vertices (less than 4)
     * Expected result: Returns 400 Bad Request
     */
    @Test
    void testIsInRegion_InsufficientVertices_ReturnsBadRequest() {
        // Create region with only 3 vertices (minimum required is 4 + closing)
        Region invalidRegion = new Region();
        invalidRegion.setName("invalid-region");
        invalidRegion.setVertices(Arrays.asList(
                new PositionDto(-3.193, 55.947),
                new PositionDto(-3.191, 55.947),
                new PositionDto(-3.192, 55.945)
        ));

        RegionRequest invalidRequest = createRegionRequest(insidePoint, invalidRegion);

        ResponseEntity<Boolean> response = controller.isInRegion(invalidRequest);

        assertEquals(400, response.getStatusCodeValue());
    }

    /**
     * Test scenario: Region vertices are null
     * Expected result: Returns 400 Bad Request
     */
    @Test
    void testIsInRegion_NullVertices_ReturnsBadRequest() {
        Region invalidRegion = new Region();
        invalidRegion.setName("invalid-region");
        invalidRegion.setVertices(null);  // Null vertices list

        RegionRequest invalidRequest = createRegionRequest(insidePoint, invalidRegion);

        ResponseEntity<Boolean> response = controller.isInRegion(invalidRequest);

        assertEquals(400, response.getStatusCodeValue());
    }

    /**
     * Test scenario: Region with unnamed region
     * Expected result: Returns 400 Bad Request
     */
    @Test
    void testIsInRegion_UnnamedRegion_ReturnsBadRequest() {
        Region invalidRegion = new Region();
        invalidRegion.setName(null);  // Null region name
        invalidRegion.setVertices(validRegion.getVertices());

        RegionRequest invalidRequest = createRegionRequest(insidePoint, invalidRegion);

        ResponseEntity<Boolean> response = controller.isInRegion(invalidRequest);

        assertEquals(400, response.getStatusCodeValue());
    }

    /**
     * Test scenario: Position missing longitude
     * Expected result: Returns 400 Bad Request
     */
    @Test
    void testIsInRegion_PositionMissingLongitude_ReturnsBadRequest() {
        PositionDto invalidPosition = new PositionDto();
        invalidPosition.setLat(insidePoint.getLat());  // Only latitude, no longitude

        RegionRequest invalidRequest = createRegionRequest(invalidPosition, validRegion);

        ResponseEntity<Boolean> response = controller.isInRegion(invalidRequest);

        assertEquals(400, response.getStatusCodeValue());
    }

    /**
     * Test scenario: Region with vertex missing latitude
     * Expected result: Returns 400 Bad Request
     */
    @Test
    void testIsInRegion_VertexMissingLatitude_ReturnsBadRequest() {
        // Create region with one invalid vertex (missing latitude)
        PositionDto invalidVertex = new PositionDto();
        invalidVertex.setLng(-3.193);  // Only longitude, no latitude

        Region invalidRegion = new Region();
        invalidRegion.setName("test-region");
        invalidRegion.setVertices(Arrays.asList(
                invalidVertex,
                new PositionDto(-3.191, 55.947),
                new PositionDto(-3.191, 55.945),
                new PositionDto(-3.193, 55.945),
                new PositionDto(-3.193, 55.947)
        ));

        RegionRequest invalidRequest = createRegionRequest(insidePoint, invalidRegion);

        ResponseEntity<Boolean> response = controller.isInRegion(invalidRequest);

        assertEquals(400, response.getStatusCodeValue());
    }

    /**
     * Test scenario: Region where first and last vertices are not the same
     * Expected result: Returns 400 Bad Request
     */
    @Test
    void testIsInRegion_UnclosedRegion_ReturnsBadRequest() {
        // Create region where first and last vertices don't match (unclosed polygon)
        Region invalidRegion = new Region();
        invalidRegion.setName("test-region");
        invalidRegion.setVertices(Arrays.asList(
                new PositionDto(-3.193, 55.947),  // First vertex
                new PositionDto(-3.191, 55.947),
                new PositionDto(-3.191, 55.945),
                new PositionDto(-3.193, 55.945),
                new PositionDto(-3.193, 55.946)   // Last vertex (different from first)
        ));

        RegionRequest invalidRequest = createRegionRequest(insidePoint, invalidRegion);

        ResponseEntity<Boolean> response = controller.isInRegion(invalidRequest);

        assertEquals(400, response.getStatusCodeValue());
    }
}
