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
public class distanceTest {

    @Mock
    private ilpService ilpService;

    @InjectMocks
    private ilpController controller;

    // Test data based on provided coordinates
    private PositionDto position1;
    private PositionDto position2;
    private DistanceRequest validDistanceRequest;

    @BeforeEach
    void setUp() {
        // Initialize positions with provided coordinates
        position1 = new PositionDto();
        position1.setLng(-3.192473);
        position1.setLat(55.946233);

        position2 = new PositionDto();
        position2.setLng(-3.192473);
        position2.setLat(55.942617);

        // Create valid distance request
        validDistanceRequest = new DistanceRequest();
        validDistanceRequest.setPosition1(position1);
        validDistanceRequest.setPosition2(position2);
    }

    /**
     * Test distance calculation with provided valid coordinates
     * Verifies controller returns correct distance from service
     */
    @Test
    void testDistanceTo_WithProvidedCoordinates_ReturnsServiceResult() {
        double serviceCalculatedDistance = 123.456;
        when(ilpService.distanceCalculate(validDistanceRequest)).thenReturn(serviceCalculatedDistance);
        ResponseEntity<Double> response = controller.distanceTo(validDistanceRequest);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(serviceCalculatedDistance, response.getBody());
    }

    /**
     * Test with null DistanceRequest
     * Verifies controller returns 400 Bad Request
     */
    @Test
    void testDistanceTo_WithNullRequest_ReturnsBadRequest() {
        ResponseEntity<Double> response = controller.distanceTo(null);

        assertEquals(400, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    /**
     * Test with valid position1 but null position2
     * Verifies validation catches incomplete request
     */
    @Test
    void testDistanceTo_WithNullPosition2_ReturnsBadRequest() {
        DistanceRequest invalidRequest = new DistanceRequest();
        invalidRequest.setPosition1(position1);
        invalidRequest.setPosition2(null);

        ResponseEntity<Double> response = controller.distanceTo(invalidRequest);

        assertEquals(400, response.getStatusCodeValue());
    }

    /**
     * Test with position1 missing longitude
     * Verifies controller validates complete coordinates
     */
    @Test
    void testDistanceTo_WithPosition1MissingLongitude_ReturnsBadRequest() {
        PositionDto invalidPosition = new PositionDto();
        invalidPosition.setLat(position1.getLat()); // Only set latitude
        // Longitude remains null

        DistanceRequest invalidRequest = new DistanceRequest();
        invalidRequest.setPosition1(invalidPosition);
        invalidRequest.setPosition2(position2);

        ResponseEntity<Double> response = controller.distanceTo(invalidRequest);

        assertEquals(400, response.getStatusCodeValue());
    }

    /**
     * Test with position2 missing latitude
     * Verifies strict validation of coordinate completeness
     */
    @Test
    void testDistanceTo_WithPosition2MissingLatitude_ReturnsBadRequest() {
        PositionDto invalidPosition = new PositionDto();
        invalidPosition.setLng(position2.getLng()); // Only set longitude
        // Latitude remains null

        DistanceRequest invalidRequest = new DistanceRequest();
        invalidRequest.setPosition1(position1);
        invalidRequest.setPosition2(invalidPosition);

        ResponseEntity<Double> response = controller.distanceTo(invalidRequest);

        assertEquals(400, response.getStatusCodeValue());
    }
}
