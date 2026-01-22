package ilp_cw1.ilp_cw1_rset.Droneservice;

import data.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service class that provides core calculation logic for distance, movement, and region detection.
 */
@Service
public class ilpService {

    /**
     * Calculates the distance between two positions.
     * @param distanceRequest contains two position objects (position1 and position2)
     * @return the Euclidean distance between the two positions
     */
    public Double distanceCalculate(DistanceRequest distanceRequest) {
        double lng1 = distanceRequest.getPosition1().getLng();
        double lat1 = distanceRequest.getPosition1().getLat();
        double lng2 = distanceRequest.getPosition2().getLng();
        double lat2 = distanceRequest.getPosition2().getLat();

        double dLn = lng1 - lng2;
        double dLat = lat1 - lat2;

        // Return Euclidean distance
        return Math.sqrt(dLn * dLn + dLat * dLat);
    }

    /**
     * Calculates the next position after moving from a start point by a given angle.
     * @param movementRequest contains the start position and angle
     * @return a new PositionDto representing the next position
     */
    public PositionDto movementCalculate(MovementRequest movementRequest) {
        double lng = movementRequest.getStart().getLng();
        double lat = movementRequest.getStart().getLat();
        double angle = movementRequest.getAngle();

        // Convert to BigDecimal for higher precision
        BigDecimal bdStartLng = BigDecimal.valueOf(lng);
        BigDecimal bdStartLat = BigDecimal.valueOf(lat);

        // Convert angle to radians
        double radians = Math.toRadians(angle);

        // Calculate cosine and sine values
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        BigDecimal bdCos = BigDecimal.valueOf(cos);
        BigDecimal bdSin = BigDecimal.valueOf(sin);

        // Move distance (fixed step)
        BigDecimal moveDistance = new BigDecimal("0.00015");

        // Calculate coordinate deltas
        BigDecimal deltaLng = moveDistance.multiply(bdCos);
        BigDecimal deltaLat = moveDistance.multiply(bdSin);

        // Add deltas to current coordinates
        BigDecimal bdnextLng = bdStartLng.add(deltaLng);
        BigDecimal bdnextLat = bdStartLat.add(deltaLat);

        // Convert results back to double
        double nextLng = bdnextLng.doubleValue();
        double nextLat = bdnextLat.doubleValue();

        // Create result position
        PositionDto result = new PositionDto();
        result.setLng(nextLng);
        result.setLat(nextLat);

        return result;
    }

    /**
     * Determines whether a given point is inside a polygonal region.
     * @param regionRequest contains the point and the region definition
     * @return true if the point is inside the region, false otherwise
     */
    public boolean isPointInPolygon(RegionRequest regionRequest) {
        PositionDto position = regionRequest.getPosition();
        Region region = regionRequest.getRegion();
        List<PositionDto> vertices = region.getVertices();

        int n = vertices.size();
        boolean inside = false;

        double pointLng = position.getLng();
        double pointLat = position.getLat();

        // Loop through each polygon edge
        for (int i = 0; i < n - 1; i++) {
            PositionDto current = vertices.get(i);
            PositionDto next = vertices.get(i + 1);

            double currLng = current.getLng();
            double currLat = current.getLat();
            double nextLng = next.getLng();
            double nextLat = next.getLat();

            // Check if point lies exactly on an edge
            if (isPointOnEdge(pointLng, pointLat, currLng, currLat, nextLng, nextLat)) {
                return true;
            }

            // Ray casting method to toggle "inside" state
            if (((currLat > pointLat) != (nextLat > pointLat)) &&
                    (pointLng < (nextLng - currLng) * (pointLat - currLat) / (nextLat - currLat) + currLng)) {
                inside = !inside;
            }
        }

        return inside;
    }

    /**
     * Checks if a point lies on the given edge.
     * @param pointLng longitude of the point
     * @param pointLat latitude of the point
     * @param edgeLng1 longitude of the first vertex
     * @param edgeLat1 latitude of the first vertex
     * @param edgeLng2 longitude of the second vertex
     * @param edgeLat2 latitude of the second vertex
     * @return true if the point lies on the edge, false otherwise
     */
    private boolean isPointOnEdge(double pointLng, double pointLat,
                                         double edgeLng1, double edgeLat1,
                                         double edgeLng2, double edgeLat2) {
        // Check if point is within the bounding box of the edge
        if (!isValueInRange(pointLat, edgeLat1, edgeLat2) ||
                !isValueInRange(pointLng, edgeLng1, edgeLng2)) {
            return false;
        }

        // Compute cross product to check collinearity
        BigDecimal dx1 = BigDecimal.valueOf(pointLng).subtract(BigDecimal.valueOf(edgeLng1));
        BigDecimal dy1 = BigDecimal.valueOf(pointLat).subtract(BigDecimal.valueOf(edgeLat1));
        BigDecimal dx2 = BigDecimal.valueOf(edgeLng2).subtract(BigDecimal.valueOf(edgeLng1));
        BigDecimal dy2 = BigDecimal.valueOf(edgeLat2).subtract(BigDecimal.valueOf(edgeLat1));

        BigDecimal crossProduct = dx1.multiply(dy2).subtract(dx2.multiply(dy1));
        return crossProduct.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Checks if a numeric value lies between two bounds.
     * @param value the number to check
     * @param a first bound
     * @param b second bound
     * @return true if value is between a and b, false otherwise
     */
    private boolean isValueInRange(double value, double a, double b) {
        if (a > b) {
            return value >= b && value <= a;
        } else {
            return value >= a && value <= b;
        }
    }
}
