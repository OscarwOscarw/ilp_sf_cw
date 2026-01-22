package ilp_cw1.ilp_cw1_rset.Ilpservice;

import data.DistanceRequest;
import data.PositionDto;
import ilp_cw1.ilp_cw1_rset.ilpController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import ilp_cw1.ilp_cw1_rset.Droneservice.ilpService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class closeTest {

    @Mock
    private ilpService ilpService;

    @InjectMocks
    private ilpController controller;

    private PositionDto basePosition;
    private PositionDto closePosition;
    private PositionDto farPosition;
    private DistanceRequest validCloseRequest;
    private DistanceRequest validFarRequest;

    /**
     * Initializes test data before each test execution
     * Sets up base coordinates and creates test positions at different distances
     */
    @BeforeEach
    void setUp() {
        // Base coordinates (using your provided position data)
        basePosition = new PositionDto();
        basePosition.setLng(-3.192473);
        basePosition.setLat(55.946233);

        // Position that should be considered "close" (distance < 0.00015)
        closePosition = new PositionDto();
        closePosition.setLng(-3.192473);
        closePosition.setLat(55.946333);

        // Position that should be considered "far" (distance > 0.00015)
        farPosition = new PositionDto();
        farPosition.setLng(-3.192473);
        farPosition.setLat(55.946573);

        // Valid request with close positions
        validCloseRequest = new DistanceRequest();
        validCloseRequest.setPosition1(basePosition);
        validCloseRequest.setPosition2(closePosition);

        // Valid request with far positions
        validFarRequest = new DistanceRequest();
        validFarRequest.setPosition1(basePosition);
        validFarRequest.setPosition2(farPosition);
    }

    /**
     * Test scenario: Positions are close to each other
     * Expected result: Controller returns true (200 OK)
     */
    @Test
    void testIsCloseTo_ClosePositions_ReturnsTrue() {
        // Mock service to return a close distance (0.0001 < 0.00015)
        when(ilpService.distanceCalculate(validCloseRequest)).thenReturn(0.0001);

        ResponseEntity<Boolean> response = controller.isCloseTo(validCloseRequest);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.hasBody());
        assertTrue(response.getBody(), "Should return true for close positions");
    }

    /**
     * Test scenario: Positions are far from each other
     * Expected result: Controller returns false (200 OK)
     */
    @Test
    void testIsCloseTo_FarPositions_ReturnsFalse() {
        // Mock service to return a far distance (0.0002 > 0.00015)
        when(ilpService.distanceCalculate(validFarRequest)).thenReturn(0.0002);

        ResponseEntity<Boolean> response = controller.isCloseTo(validFarRequest);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.hasBody());
        assertFalse(response.getBody(), "Should return false for far positions");
    }

    /**
     * Test scenario: Distance equals the threshold exactly
     * Expected result: Controller returns false (does not meet "less than" condition)
     */
    @Test
    void testIsCloseTo_ExactThresholdDistance_ReturnsFalse() {
        // Mock service to return distance equal to threshold (0.00015 is not less than 0.00015)
        when(ilpService.distanceCalculate(validCloseRequest)).thenReturn(0.00015);

        ResponseEntity<Boolean> response = controller.isCloseTo(validCloseRequest);

        assertFalse(response.getBody(), "Should return false when distance equals threshold");
    }

    /**
     * Test scenario: Null request object
     * Expected result: Controller returns 400 Bad Request
     */
    @Test
    void testIsCloseTo_NullRequest_ReturnsBadRequest() {
        ResponseEntity<Boolean> response = controller.isCloseTo(null);

        assertEquals(400, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    /**
     * Test scenario: position1 is null
     * Expected result: Controller returns 400 Bad Request
     */
    @Test
    void testIsCloseTo_NullPosition1_ReturnsBadRequest() {
        DistanceRequest invalidRequest = new DistanceRequest();
        invalidRequest.setPosition1(null);
        invalidRequest.setPosition2(closePosition);

        ResponseEntity<Boolean> response = controller.isCloseTo(invalidRequest);

        assertEquals(400, response.getStatusCodeValue());
    }

    /**
     * Test scenario: position2 is null
     * Expected result: Controller returns 400 Bad Request
     */
    @Test
    void testIsCloseTo_NullPosition2_ReturnsBadRequest() {
        DistanceRequest invalidRequest = new DistanceRequest();
        invalidRequest.setPosition1(basePosition);
        invalidRequest.setPosition2(null);

        ResponseEntity<Boolean> response = controller.isCloseTo(invalidRequest);

        assertEquals(400, response.getStatusCodeValue());
    }

    /**
     * Test scenario: position1 is missing latitude
     * Expected result: Controller returns 400 Bad Request
     */
    @Test
    void testIsCloseTo_Position1MissingLatitude_ReturnsBadRequest() {
        PositionDto invalidPosition = new PositionDto();
        invalidPosition.setLng(basePosition.getLng()); // Only set longitude
        // Latitude remains null

        DistanceRequest invalidRequest = new DistanceRequest();
        invalidRequest.setPosition1(invalidPosition);
        invalidRequest.setPosition2(closePosition);

        ResponseEntity<Boolean> response = controller.isCloseTo(invalidRequest);

        assertEquals(400, response.getStatusCodeValue());
    }

    /**
     * Test scenario: position2 is missing longitude
     * Expected result: Controller returns 400 Bad Request
     */
    @Test
    void testIsCloseTo_Position2MissingLongitude_ReturnsBadRequest() {
        PositionDto invalidPosition = new PositionDto();
        invalidPosition.setLat(closePosition.getLat()); // Only set latitude
        // Longitude remains null

        DistanceRequest invalidRequest = new DistanceRequest();
        invalidRequest.setPosition1(basePosition);
        invalidRequest.setPosition2(invalidPosition);

        ResponseEntity<Boolean> response = controller.isCloseTo(invalidRequest);

        assertEquals(400, response.getStatusCodeValue());
    }
}
