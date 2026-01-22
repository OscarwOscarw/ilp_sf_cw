package ilp_cw1.ilp_cw1_rset;

import data.DistanceRequest;
import data.MovementRequest;
import data.PositionDto;
import data.RegionRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ilp_cw1.ilp_cw1_rset.Droneservice.ilpService;

import java.util.List;

/**
 * REST controller that provides APIs for distance calculation,
 * movement computation, and region checking operations.
 */
@RestController
@RequestMapping("/api/v1")
public class ilpController {

    /** Small tolerance constant for floating-point comparisons */
    private static final double EPSILON = 1e-12;

    /** Service layer instance used for calculations */
    private final ilpService ilpService;

    public ilpController(ilpService ilpService) {
        this.ilpService = ilpService;
    }

    /**
     * Returns the student UID.
     * @return the UID string
     */
    @GetMapping("/uid")
    public String uid() {
        return "s2488412";
    }

    /**
     * Calculates the distance between two positions.
     * @param distanceRequest request body containing two position objects
     * @return the calculated distance or bad request if input is invalid
     */
    @PostMapping("/distanceTo")
    public ResponseEntity<Double> distanceTo(@RequestBody DistanceRequest distanceRequest) {
        // Validate request and position data
        if (distanceRequest == null
                || distanceRequest.getPosition1() == null
                || distanceRequest.getPosition2() == null
                || distanceRequest.getPosition1().getLat() == null
                || distanceRequest.getPosition1().getLng() == null
                || distanceRequest.getPosition2().getLat() == null
                || distanceRequest.getPosition2().getLng() == null) {
            return ResponseEntity.badRequest().build();
        }

        // Calculate and return distance
        return ResponseEntity.ok(ilpService.distanceCalculate(distanceRequest));
    }

    /**
     * Checks whether two positions are close to each other.
     * @param distanceRequest request body containing two position objects
     * @return true if positions are close, otherwise false
     */
    @PostMapping("/isCloseTo")
    public ResponseEntity<Boolean> isCloseTo(@RequestBody DistanceRequest distanceRequest) {
        // Validate request and position data
        if (distanceRequest == null
                || distanceRequest.getPosition1() == null
                || distanceRequest.getPosition2() == null
                || distanceRequest.getPosition1().getLat() == null
                || distanceRequest.getPosition1().getLng() == null
                || distanceRequest.getPosition2().getLat() == null
                || distanceRequest.getPosition2().getLng() == null) {
            return ResponseEntity.badRequest().build();
        }

        // Compare distance with threshold
        boolean isClose = ilpService.distanceCalculate(distanceRequest) < 0.00015;
        return ResponseEntity.ok(isClose);
    }

    /**
     * Calculates the next position after moving from a start point at a given angle.
     * @param movementRequest request body containing start position and angle
     * @return the next position after movement
     */
    @PostMapping("/nextPosition")
    public ResponseEntity<PositionDto> calculateNextPosition(@RequestBody MovementRequest movementRequest) {
        // Validate request and position data
        if (movementRequest == null
                || movementRequest.getStart() == null
                || movementRequest.getStart().getLng() == null
                || movementRequest.getStart().getLat() == null) {
            return ResponseEntity.badRequest().build();
        }

        // Normalize angle
        double angle = movementRequest.getAngle();
        double normalizedAngle = angle % 360;
        if (normalizedAngle < 0) {
            normalizedAngle += 360;
        }

        // Check if angle is a multiple of 22.5 degrees
        double remainder = normalizedAngle % 22.5;  // difference from valid angle
        if (!(remainder < 1e-12 || (22.5 - remainder) < 1e-12)) {
            return ResponseEntity.badRequest().build();
        }

        // Calculate and return new position
        PositionDto result = ilpService.movementCalculate(movementRequest);
        return ResponseEntity.ok(result);
    }

    /**
     * Checks whether a given position is inside a defined region.
     * @param inRegionRequest request body containing position and region data
     * @return true if position is inside region, otherwise false
     */
    @PostMapping("/isInRegion")
    public ResponseEntity<Boolean> isInRegion(@RequestBody RegionRequest inRegionRequest) {
        // Validate base request parameters
        if (inRegionRequest == null
                || inRegionRequest.getPosition() == null
                || inRegionRequest.getRegion() == null
                || inRegionRequest.getRegion().getVertices() == null
                || inRegionRequest.getRegion().getName() == null) {
            return ResponseEntity.badRequest().build();
        }

        // Validate position coordinates
        PositionDto position = inRegionRequest.getPosition();
        if (position.getLng() == null || position.getLat() == null) {
            return ResponseEntity.badRequest().build();
        }

        // Validate vertices
        List<PositionDto> vertices = inRegionRequest.getRegion().getVertices();
        if (vertices.size() < 4) {
            return ResponseEntity.badRequest().build();
        }

        for (PositionDto vertex : vertices) {
            if (vertex.getLng() == null || vertex.getLat() == null) {
                return ResponseEntity.badRequest().build();
            }
        }

        // Validate that first and last vertices are the same
        PositionDto firstVertex = vertices.getFirst();
        PositionDto lastVertex = vertices.getLast();
        double lngDiff = Math.abs(firstVertex.getLng() - lastVertex.getLng());
        double latDiff = Math.abs(firstVertex.getLat() - lastVertex.getLat());
        if (lngDiff > EPSILON || latDiff > EPSILON) {
            return ResponseEntity.badRequest().build();
        }

        // Determine whether position is inside region
        boolean inside = ilpService.isPointInPolygon(inRegionRequest);
        return ResponseEntity.ok(inside);
    }
}
