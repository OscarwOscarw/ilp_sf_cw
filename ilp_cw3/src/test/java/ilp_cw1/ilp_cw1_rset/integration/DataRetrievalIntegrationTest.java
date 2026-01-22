package ilp_cw1.ilp_cw1_rset.integration;

import data.Drone;
import data.DroneForServicePoint;
import data.ServicePoint;
import data.RestrictedArea;
import ilp_cw1.ilp_cw1_rset.Droneservice.droneService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("integration")
class DataRetrievalIntegrationTest {

    @Autowired
    private droneService droneService;

    @Test
    @DisplayName("Retrieve all drones from live API")
    void shouldRetrieveAllDrones() {
        List<Drone> drones = droneService.getAllDrones();
        assertNotNull(drones);
    }

    @Test
    @DisplayName("Retrieve drones by ID subset")
    void shouldRetrieveDronesByIds() {
        List<Drone> allDrones = droneService.getAllDrones();
        assertNotNull(allDrones);

        if (allDrones.isEmpty()) {
            return;
        }

        String id = allDrones.get(0).getId();
        List<Drone> filtered = droneService.getDronesByIds(List.of(id));

        assertNotNull(filtered);
        assertTrue(
                filtered.stream().allMatch(d -> id.equals(d.getId()))
        );
    }

    @Test
    @DisplayName("Retrieve all service points")
    void shouldRetrieveServicePoints() {
        List<ServicePoint> servicePoints = droneService.getServicePoints();
        assertNotNull(servicePoints);
    }

    @Test
    @DisplayName("Retrieve available drones at service points")
    void shouldRetrieveAvailableDronesForServicePoints() {
        List<DroneForServicePoint> availableDrones = droneService.readAvailableDrones();
        assertNotNull(availableDrones);
    }

    @Test
    @DisplayName("Retrieve restricted areas from API and dynamic sources")
    void shouldRetrieveRestrictedAreas() {
        List<RestrictedArea> restrictedAreas = droneService.getRestrictedAreas();
        assertNotNull(restrictedAreas);
    }

    @Test
    @DisplayName("Handle empty drone ID list gracefully")
    void shouldHandleEmptyDroneIdList() {
        List<Drone> result = droneService.getDronesByIds(List.of());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Data retrieval should not throw exceptions under normal conditions")
    void dataRetrievalShouldNotThrow() {
        assertDoesNotThrow(() -> droneService.getAllDrones());
        assertDoesNotThrow(() -> droneService.getServicePoints());
        assertDoesNotThrow(() -> droneService.readAvailableDrones());
        assertDoesNotThrow(() -> droneService.getRestrictedAreas());
    }
}
