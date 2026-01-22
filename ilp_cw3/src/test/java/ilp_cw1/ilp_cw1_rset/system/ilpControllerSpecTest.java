package ilp_cw1.ilp_cw1_rset.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ILP CW1 REST API endpoints.
 * Covers validation, null checks, boundary conditions, and core functionality.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class ilpControllerSpecTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private PositionDto validPosition1;
    private PositionDto validPosition2;
    private DistanceRequest validDistanceRequest;

    /**
     * Setup test data before each test execution.
     * Initializes valid positions and a valid distance request.
     */
    @BeforeEach
    void setUp() {
        // Initialize two valid geographic positions
        validPosition1 = new PositionDto(-3.192473, 55.946233);
        validPosition2 = new PositionDto(-3.192473, 55.942617);

        // Create a valid distance request with two positions
        validDistanceRequest = new DistanceRequest();
        validDistanceRequest.setPosition1(validPosition1);
        validDistanceRequest.setPosition2(validPosition2);
    }

    /**
     * Test health check endpoint.
     * Verifies the application is running and healthy.
     */
    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    /**
     * Test UID endpoint.
     * Verifies the correct student ID is returned.
     */
    @Test
    void testUidEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/uid"))
                .andExpect(status().isOk())
                .andExpect(content().string("s2488412"));
    }

    /**
     * Test distance calculation with valid input.
     * Verifies a valid distance is returned for valid positions.
     */
    @Test
    void testDistanceTo_ValidInput() throws Exception {
        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDistanceRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNumber());
    }

    /**
     * Test distance calculation with null request body.
     * Verifies proper error handling for null input.
     */
    @Test
    void testDistanceTo_NullBody() throws Exception {
        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test distance calculation with null first position.
     * Verifies validation rejects requests missing position1.
     */
    @Test
    void testDistanceTo_NullPosition1() throws Exception {
        DistanceRequest invalidRequest = new DistanceRequest();
        invalidRequest.setPosition1(null);  // Null first position
        invalidRequest.setPosition2(validPosition2);

        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test distance calculation with null second position.
     * Verifies validation rejects requests missing position2.
     */
    @Test
    void testDistanceTo_NullPosition2() throws Exception {
        DistanceRequest invalidRequest = new DistanceRequest();
        invalidRequest.setPosition1(validPosition1);
        invalidRequest.setPosition2(null);  // Null second position

        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test distance calculation with position1 missing latitude.
     * Verifies validation checks for complete position data.
     */
    @Test
    void testDistanceTo_Position1MissingLatitude() throws Exception {
        PositionDto invalidPos = new PositionDto();
        invalidPos.setLng(validPosition1.getLng());  // Only set longitude
        // Latitude remains null

        DistanceRequest invalidRequest = new DistanceRequest();
        invalidRequest.setPosition1(invalidPos);
        invalidRequest.setPosition2(validPosition2);

        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test distance calculation with position1 missing longitude.
     * Verifies validation checks for complete position data.
     */
    @Test
    void testDistanceTo_Position1MissingLongitude() throws Exception {
        PositionDto invalidPos = new PositionDto();
        invalidPos.setLat(validPosition1.getLat());  // Only set latitude
        // Longitude remains null

        DistanceRequest invalidRequest = new DistanceRequest();
        invalidRequest.setPosition1(invalidPos);
        invalidRequest.setPosition2(validPosition2);

        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test proximity check with valid input.
     * Verifies the endpoint correctly determines if positions are close.
     */
    @Test
    void testIsCloseTo_ValidInput() throws Exception {
        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDistanceRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isBoolean());
    }

    /**
     * Test proximity check with null request body.
     * Verifies proper error handling for null input.
     */
    @Test
    void testIsCloseTo_NullBody() throws Exception {
        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test proximity check with empty request body.
     * Verifies validation rejects empty requests.
     */
    @Test
    void testIsCloseTo_EmptyBody() throws Exception {
        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test proximity check with both positions null.
     * Verifies validation rejects requests with incomplete data.
     */
    @Test
    void testIsCloseTo_BothPositionsNull() throws Exception {
        DistanceRequest invalidRequest = new DistanceRequest();
        invalidRequest.setPosition1(null);
        invalidRequest.setPosition2(null);

        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test next position calculation with valid angle (multiple of 22.5).
     * Verifies correct position calculation for valid input.
     */
    @Test
    void testNextPosition_ValidAngle() throws Exception {
        MovementRequest validMove = new MovementRequest();
        validMove.setStart(validPosition1);
        validMove.setAngle(45.0);  // Valid angle (multiple of 22.5)

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validMove)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lng").isNumber())
                .andExpect(jsonPath("$.lat").isNumber());
    }

    /**
     * Test next position calculation with invalid angle (not multiple of 22.5).
     * Verifies validation rejects invalid angles.
     */
    @Test
    void testNextPosition_InvalidAngle() throws Exception {
        MovementRequest invalidMove = new MovementRequest();
        invalidMove.setStart(validPosition1);
        invalidMove.setAngle(10.0);  // Invalid angle (not multiple of 22.5)

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidMove)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test next position calculation with null start position.
     * Verifies validation rejects requests missing start position.
     */
    @Test
    void testNextPosition_NullStartPosition() throws Exception {
        MovementRequest invalidMove = new MovementRequest();
        invalidMove.setStart(null);  // Null start position
        invalidMove.setAngle(45.0);  // Valid angle

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidMove)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test next position calculation with start position missing longitude.
     * Verifies validation checks for complete position data.
     */
    @Test
    void testNextPosition_StartMissingLongitude() throws Exception {
        PositionDto invalidPos = new PositionDto();
        invalidPos.setLat(validPosition1.getLat());  // Only set latitude
        // Longitude remains null

        MovementRequest invalidMove = new MovementRequest();
        invalidMove.setStart(invalidPos);
        invalidMove.setAngle(45.0);

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidMove)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test region membership check with valid closed polygon.
     * Verifies correct detection of point inside region.
     */
    @Test
    void testIsInRegion_ValidClosedPolygon() throws Exception {
        // Create a closed rectangular region (first and last vertices are the same)
        List<PositionDto> vertices = new ArrayList<>();
        vertices.add(new PositionDto(-3.192473, 55.946233));
        vertices.add(new PositionDto(-3.192473, 55.942617));
        vertices.add(new PositionDto(-3.184319, 55.942617));
        vertices.add(new PositionDto(-3.184319, 55.946233));
        vertices.add(new PositionDto(-3.192473, 55.946233));  // Closing vertex

        Region validRegion = new Region();
        validRegion.setName("central");
        validRegion.setVertices(vertices);

        PositionDto pointInside = new PositionDto(-3.188000, 55.944000);

        RegionRequest validRequest = new RegionRequest();
        validRequest.setPosition(pointInside);
        validRequest.setRegion(validRegion);

        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    /**
     * Test /api/v1/isInRegion endpoint when position has null longitude.
     * Verifies validation logic triggers bad request for null longitude.
     */
    @Test
    void testIsInRegion_PositionWithNullLongitude() throws Exception {
        // Create valid closed polygon region
        List<PositionDto> vertices = new ArrayList<>();
        vertices.add(new PositionDto(-3.192473, 55.946233));
        vertices.add(new PositionDto(-3.192473, 55.942617));
        vertices.add(new PositionDto(-3.184319, 55.942617));
        vertices.add(new PositionDto(-3.184319, 55.946233));
        vertices.add(new PositionDto(-3.192473, 55.946233)); // Close polygon

        Region validRegion = new Region();
        validRegion.setName("testRegion");
        validRegion.setVertices(vertices);

        // Create position with valid latitude but null longitude
        PositionDto invalidPosition = new PositionDto();
        invalidPosition.setLat(55.944000);  // Valid latitude
        invalidPosition.setLng(null);       // Null longitude - should trigger validation

        RegionRequest request = new RegionRequest();
        request.setPosition(invalidPosition);
        request.setRegion(validRegion);

        // Verify bad request response
        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test /api/v1/isInRegion endpoint when position has null latitude.
     * Verifies validation logic triggers bad request for null latitude.
     */
    @Test
    void testIsInRegion_PositionWithNullLatitude() throws Exception {
        // Create valid closed polygon region
        List<PositionDto> vertices = new ArrayList<>();
        vertices.add(new PositionDto(-3.192473, 55.946233));
        vertices.add(new PositionDto(-3.192473, 55.942617));
        vertices.add(new PositionDto(-3.184319, 55.942617));
        vertices.add(new PositionDto(-3.184319, 55.946233));
        vertices.add(new PositionDto(-3.192473, 55.946233)); // Close polygon

        Region validRegion = new Region();
        validRegion.setName("testRegion");
        validRegion.setVertices(vertices);

        // Create position with valid longitude but null latitude
        PositionDto invalidPosition = new PositionDto();
        invalidPosition.setLng(-3.188000);  // Valid longitude
        invalidPosition.setLat(null);       // Null latitude - should trigger validation

        RegionRequest request = new RegionRequest();
        request.setPosition(invalidPosition);
        request.setRegion(validRegion);

        // Verify bad request response
        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test /api/v1/isInRegion endpoint when position has both coordinates null.
     * Verifies validation logic triggers bad request for completely null coordinates.
     */
    @Test
    void testIsInRegion_PositionWithBothNullCoordinates() throws Exception {
        // Create valid closed polygon region
        List<PositionDto> vertices = new ArrayList<>();
        vertices.add(new PositionDto(-3.192473, 55.946233));
        vertices.add(new PositionDto(-3.192473, 55.942617));
        vertices.add(new PositionDto(-3.184319, 55.942617));
        vertices.add(new PositionDto(-3.184319, 55.946233));
        vertices.add(new PositionDto(-3.192473, 55.946233)); // Close polygon

        Region validRegion = new Region();
        validRegion.setName("testRegion");
        validRegion.setVertices(vertices);

        // Create position with both coordinates null
        PositionDto invalidPosition = new PositionDto();
        invalidPosition.setLng(null);  // Null longitude
        invalidPosition.setLat(null);  // Null latitude - both should trigger validation

        RegionRequest request = new RegionRequest();
        request.setPosition(invalidPosition);
        request.setRegion(validRegion);

        // Verify bad request response
        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }


    /**
     * Test region membership check with open polygon (not closed).
     * Verifies validation rejects non-closed regions.
     */
    @Test
    void testIsInRegion_OpenPolygon() throws Exception {
        // Create an open polygon (missing closing vertex)
        List<PositionDto> vertices = new ArrayList<>();
        vertices.add(new PositionDto(-3.192473, 55.946233));
        vertices.add(new PositionDto(-3.192473, 55.942617));
        vertices.add(new PositionDto(-3.184319, 55.942617));
        vertices.add(new PositionDto(-3.184319, 55.946233));
        // Missing closing vertex

        Region invalidRegion = new Region();
        invalidRegion.setName("openRegion");
        invalidRegion.setVertices(vertices);

        RegionRequest invalidRequest = new RegionRequest();
        invalidRequest.setRegion(invalidRegion);
        invalidRequest.setPosition(validPosition1);

        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test region membership check with null request body.
     * Verifies proper error handling for null input.
     */
    @Test
    void testIsInRegion_NullBody() throws Exception {
        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test region membership check with null position.
     * Verifies validation rejects requests missing position.
     */
    @Test
    void testIsInRegion_NullPosition() throws Exception {
        Region validRegion = new Region();
        validRegion.setName("testRegion");
        validRegion.setVertices(new ArrayList<>());

        RegionRequest invalidRequest = new RegionRequest();
        invalidRequest.setPosition(null);  // Null position
        invalidRequest.setRegion(validRegion);

        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test region membership check with null region.
     * Verifies validation rejects requests missing region.
     */
    @Test
    void testIsInRegion_NullRegion() throws Exception {
        RegionRequest invalidRequest = new RegionRequest();
        invalidRequest.setPosition(validPosition1);
        invalidRequest.setRegion(null);  // Null region

        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test region membership check with region missing vertices.
     * Verifies validation requires vertices for region definition.
     */
    @Test
    void testIsInRegion_RegionMissingVertices() throws Exception {
        Region invalidRegion = new Region();
        invalidRegion.setName("noVerticesRegion");
        invalidRegion.setVertices(null);  // Null vertices list

        RegionRequest invalidRequest = new RegionRequest();
        invalidRequest.setPosition(validPosition1);
        invalidRequest.setRegion(invalidRegion);

        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test region membership check with insufficient vertices (less than 4).
     * Verifies validation requires minimum vertex count for valid region.
     */
    @Test
    void testIsInRegion_InsufficientVertices() throws Exception {
        // Create region with only 3 vertices (minimum required is 4)
        List<PositionDto> vertices = new ArrayList<>();
        vertices.add(validPosition1);
        vertices.add(validPosition2);
        vertices.add(new PositionDto(-3.184319, 55.942617));

        Region invalidRegion = new Region();
        invalidRegion.setName("smallRegion");
        invalidRegion.setVertices(vertices);

        RegionRequest invalidRequest = new RegionRequest();
        invalidRequest.setPosition(validPosition1);
        invalidRequest.setRegion(invalidRegion);

        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test region membership check with vertex missing longitude.
     * Verifies all vertices must have complete coordinate data.
     */
    @Test
    void testIsInRegion_VertexMissingLongitude() throws Exception {
        List<PositionDto> vertices = new ArrayList<>();
        vertices.add(validPosition1);
        vertices.add(validPosition2);

        // Create a vertex with missing longitude
        PositionDto invalidVertex = new PositionDto();
        invalidVertex.setLat(55.942617);  // Only set latitude
        // Longitude remains null

        vertices.add(invalidVertex);
        vertices.add(validPosition1);  // Close the polygon

        Region invalidRegion = new Region();
        invalidRegion.setName("invalidVertexRegion");
        invalidRegion.setVertices(vertices);

        RegionRequest invalidRequest = new RegionRequest();
        invalidRequest.setPosition(validPosition1);
        invalidRequest.setRegion(invalidRegion);

        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test nextPosition with angle that is a valid multiple of 22.5 degrees.
     * Verifies valid angles are accepted after normalization.
     */
    @Test
    void testNextPosition_ValidAngleMultipleOf22_5() throws Exception {
        MovementRequest request = new MovementRequest();
        request.setStart(validPosition1);
        request.setAngle(22.5);  // Exactly 1x 22.5

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    /**
     * Test nextPosition with angle that normalizes to a valid multiple of 22.5.
     * Verifies angles larger than 360 are properly normalized.
     */
    @Test
    void testNextPosition_AngleNormalizesToValid() throws Exception {
        MovementRequest request = new MovementRequest();
        request.setStart(validPosition1);
        request.setAngle(382.5);  // 360 + 22.5 = normalizes to 22.5

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    /**
     * Test nextPosition with negative angle that normalizes to valid multiple.
     * Verifies negative angles are properly normalized.
     */
    @Test
    void testNextPosition_NegativeAngleNormalizesToValid() throws Exception {
        MovementRequest request = new MovementRequest();
        request.setStart(validPosition1);
        request.setAngle(-337.5);  // Normalizes to 22.5 (360 - 337.5 = 22.5)

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    /**
     * Test nextPosition with angle just below valid multiple (within epsilon).
     * Verifies floating point tolerance works correctly.
     */
    @Test
    void testNextPosition_AngleJustBelowValidWithEpsilon() throws Exception {
        MovementRequest request = new MovementRequest();
        request.setStart(validPosition1);
        request.setAngle(22.5 - 1e-13);  // Just below 22.5 (within tolerance)

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    /**
     * Test nextPosition with angle just above valid multiple (within epsilon).
     * Verifies floating point tolerance works correctly.
     */
    @Test
    void testNextPosition_AngleJustAboveValidWithEpsilon() throws Exception {
        MovementRequest request = new MovementRequest();
        request.setStart(validPosition1);
        request.setAngle(22.5 + 1e-13);  // Just above 22.5 (within tolerance)

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    /**
     * Test nextPosition with angle that is not a multiple of 22.5.
     * Verifies invalid angles are rejected.
     */
    @Test
    void testNextPosition_InvalidAngleNotMultiple() throws Exception {
        MovementRequest request = new MovementRequest();
        request.setStart(validPosition1);
        request.setAngle(10.0);  // Not a multiple of 22.5

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test nextPosition with angle just outside epsilon tolerance.
     * Verifies strict tolerance enforcement.
     */
    @Test
    void testNextPosition_AngleOutsideEpsilonTolerance() throws Exception {
        MovementRequest request = new MovementRequest();
        request.setStart(validPosition1);
        request.setAngle(22.5 + 1e-11);  // Beyond epsilon tolerance

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test nextPosition with angle that normalizes to invalid value.
     * Verifies normalization doesn't hide invalid angles.
     */
    @Test
    void testNextPosition_NormalizedAngleInvalid() throws Exception {
        MovementRequest request = new MovementRequest();
        request.setStart(validPosition1);
        request.setAngle(370.0);  // Normalizes to 10.0 (invalid)

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

}
