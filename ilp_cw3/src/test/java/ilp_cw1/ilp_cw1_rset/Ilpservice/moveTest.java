package ilp_cw1.ilp_cw1_rset.Ilpservice;

import data.MovementRequest;
import data.PositionDto;
import ilp_cw1.ilp_cw1_rset.ilpController;
import ilp_cw1.ilp_cw1_rset.Droneservice.ilpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class moveTest {

    @Mock
    private ilpService ilpService;

    @InjectMocks
    private ilpController controller;

    // Test data
    private PositionDto startPosition;
    private MovementRequest validMovementRequest;
    private PositionDto expectedNextPosition;

    /**
     * Initialize test data before each test
     * Sets up a base starting position and valid movement request
     */
    @BeforeEach
    void setUp() {
        // Base starting position (using coordinates similar to your data)
        startPosition = new PositionDto();
        startPosition.setLng(-3.192473);
        startPosition.setLat(55.946233);

        // Valid movement request with 0° angle (due north)
        validMovementRequest = new MovementRequest();
        validMovementRequest.setStart(startPosition);
        validMovementRequest.setAngle(0.0);

        // Expected position after movement (mock result)
        expectedNextPosition = new PositionDto();
        expectedNextPosition.setLng(-3.192473);
        expectedNextPosition.setLat(55.946383); // Moved north by 0.00015
    }

    /**
     * Test scenario: Valid movement request with 0° angle
     * Expected result: Returns calculated next position (200 OK)
     */
    @Test
    void testCalculateNextPosition_ValidRequest_ReturnsNextPosition() {
        // Arrange: Mock service to return expected position
        when(ilpService.movementCalculate(validMovementRequest)).thenReturn(expectedNextPosition);

        // Act: Call controller method
        ResponseEntity<PositionDto> response = controller.calculateNextPosition(validMovementRequest);

        // Assert: Verify successful response with correct position
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(expectedNextPosition.getLng(), response.getBody().getLng());
        assertEquals(expectedNextPosition.getLat(), response.getBody().getLat());
    }

    /**
     * Test scenario: Valid angle that's a multiple of 22.5° (90° east)
     * Expected result: Accepts angle and returns position
     */
    @Test
    void testCalculateNextPosition_ValidAngleMultiple_ReturnsPosition() {
        // Arrange: Use 90° angle (valid multiple of 22.5°)
        MovementRequest request = new MovementRequest();
        request.setStart(startPosition);
        request.setAngle(90.0);

        when(ilpService.movementCalculate(any(MovementRequest.class))).thenReturn(expectedNextPosition);

        // Act
        ResponseEntity<PositionDto> response = controller.calculateNextPosition(request);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    /**
     * Test scenario: Angle normalization (370° should normalize to 10°)
     * Expected result: Accepts angle and returns position
     */
    @Test
    void testCalculateNextPosition_AngleNormalization_ReturnsPosition() {
        // Arrange: 370° is equivalent to 10° (370 - 360 = 10)
        MovementRequest request = new MovementRequest();
        request.setStart(startPosition);
        request.setAngle(382.5);

        when(ilpService.movementCalculate(any(MovementRequest.class))).thenReturn(expectedNextPosition);

        // Act
        ResponseEntity<PositionDto> response = controller.calculateNextPosition(request);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
    }

    /**
     * Test scenario: Invalid angle (not a multiple of 22.5°)
     * Expected result: Returns 400 Bad Request
     */
    @Test
    void testCalculateNextPosition_InvalidAngle_ReturnsBadRequest() {
        // Arrange: 10° is not a multiple of 22.5°
        MovementRequest invalidRequest = new MovementRequest();
        invalidRequest.setStart(startPosition);
        invalidRequest.setAngle(10.0);

        // Act
        ResponseEntity<PositionDto> response = controller.calculateNextPosition(invalidRequest);

        // Assert
        assertEquals(400, response.getStatusCodeValue());
    }

    /**
     * Test scenario: Null movement request
     * Expected result: Returns 400 Bad Request
     */
    @Test
    void testCalculateNextPosition_NullRequest_ReturnsBadRequest() {
        ResponseEntity<PositionDto> response = controller.calculateNextPosition(null);

        assertEquals(400, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    /**
     * Test scenario: Null start position in request
     * Expected result: Returns 400 Bad Request
     */
    @Test
    void testCalculateNextPosition_NullStartPosition_ReturnsBadRequest() {
        MovementRequest invalidRequest = new MovementRequest();
        invalidRequest.setStart(null); // Null start position
        invalidRequest.setAngle(0.0);

        ResponseEntity<PositionDto> response = controller.calculateNextPosition(invalidRequest);

        assertEquals(400, response.getStatusCodeValue());
    }

    /**
     * Test scenario: Start position missing longitude
     * Expected result: Returns 400 Bad Request
     */
    @Test
    void testCalculateNextPosition_StartMissingLongitude_ReturnsBadRequest() {
        PositionDto invalidStart = new PositionDto();
        invalidStart.setLat(startPosition.getLat()); // Only latitude, no longitude

        MovementRequest invalidRequest = new MovementRequest();
        invalidRequest.setStart(invalidStart);
        invalidRequest.setAngle(0.0);

        ResponseEntity<PositionDto> response = controller.calculateNextPosition(invalidRequest);

        assertEquals(400, response.getStatusCodeValue());
    }

    /**
     * Test scenario: Start position missing latitude
     * Expected result: Returns 400 Bad Request
     */
    @Test
    void testCalculateNextPosition_StartMissingLatitude_ReturnsBadRequest() {
        PositionDto invalidStart = new PositionDto();
        invalidStart.setLng(startPosition.getLng()); // Only longitude, no latitude

        MovementRequest invalidRequest = new MovementRequest();
        invalidRequest.setStart(invalidStart);
        invalidRequest.setAngle(0.0);

        ResponseEntity<PositionDto> response = controller.calculateNextPosition(invalidRequest);

        assertEquals(400, response.getStatusCodeValue());
    }
}

