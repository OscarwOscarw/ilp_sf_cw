package ilp_cw1.ilp_cw1_rset.integration;

import data.Drone;
import data.ServicePoint;
import ilp_cw1.ilp_cw1_rset.Droneservice.droneService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class QueryComparisonIntegrationTest {

    @Autowired
    private droneService droneService;

    @Test
    @DisplayName("Retrieve all drones and filter by IDs")
    void testGetDronesByIds() {
        List<Drone> allDrones = droneService.getAllDrones();
        assertNotNull(allDrones);
        assertTrue(allDrones.size() > 0, "There should be some drones in test data");

        List<String> ids = allDrones.stream().limit(2).map(Drone::getId).collect(Collectors.toList());

        List<Drone> filtered = droneService.getDronesByIds(ids);
        assertNotNull(filtered);
        assertEquals(2, filtered.size());
        for (Drone d : filtered) {
            assertTrue(ids.contains(d.getId()));
        }
    }

    @Test
    @DisplayName("Retrieve all service points")
    void testGetServicePoints() {
        List<ServicePoint> sps = droneService.getServicePoints();
        assertNotNull(sps);
        assertTrue(sps.size() > 0, "There should be service points in test data");
    }

    @Test
    @DisplayName("Compare filtering results")
    void testCompareFiltering() {
        List<Drone> allDrones = droneService.getAllDrones();
        List<String> ids = allDrones.stream().limit(3).map(Drone::getId).collect(Collectors.toList());

        List<Drone> filtered = droneService.getDronesByIds(ids);

        List<String> allIds = allDrones.stream().map(Drone::getId).collect(Collectors.toList());
        for (Drone d : filtered) {
            assertTrue(allIds.contains(d.getId()), "Filtered drone must exist in all drones list");
        }
    }

    @Test
    @DisplayName("Filter with empty ID list should return empty result")
    void testFilterEmptyIds() {
        List<Drone> filtered = droneService.getDronesByIds(List.of());
        assertNotNull(filtered);
        assertTrue(filtered.isEmpty(), "Filtering with empty IDs should return empty list");
    }

    @Test
    @DisplayName("Filter with non-existent IDs should return empty result")
    void testFilterNonExistentIds() {
        List<Drone> filtered = droneService.getDronesByIds(List.of("NON_EXISTENT_1", "NON_EXISTENT_2"));
        assertNotNull(filtered);
        assertTrue(filtered.isEmpty(), "Filtering with unknown IDs should return empty list");
    }

    @Test
    @DisplayName("Get all drones should include drones with capabilities")
    void testDroneCapabilitiesPresent() {
        List<Drone> allDrones = droneService.getAllDrones();
        assertNotNull(allDrones);
        boolean hasCapabilities = allDrones.stream()
                .anyMatch(d -> d.getCapability() != null && (d.hasCooling() || d.hasHeating()));
        assertTrue(hasCapabilities, "At least one drone should have cooling or heating capability");
    }

    @Test
    @DisplayName("Service points should have valid locations")
    void testServicePointsLocations() {
        List<ServicePoint> sps = droneService.getServicePoints();
        assertNotNull(sps);
        boolean allHaveLocations = sps.stream().allMatch(sp -> sp.getLocation() != null);
        assertTrue(allHaveLocations, "All service points must have a location");
    }
}
