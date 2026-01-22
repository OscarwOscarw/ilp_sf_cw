package ilp_cw1.ilp_cw1_rset.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import data.*;
import ilp_cw1.ilp_cw1_rset.Droneservice.droneService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ErrorHandlingIntegrationTest {

    @Autowired
    private droneService droneService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Handle null input gracefully")
    void testNullInputHandling() {
        assertDoesNotThrow(() -> {
            // null drones, tasks, info, restrictedAreas
            DeliveryPathResponse resp = droneService.calculateOptimizedMultiDroneSolution(
                    null, null, null, null, null
            );
            assertNotNull(resp, "Response should not be null even with null input");
            assertEquals(0, resp.getTotalCost(), "Total cost should be 0 for empty input");
            assertEquals(0, resp.getTotalMoves(), "Total moves should be 0 for empty input");
            assertTrue(resp.getDronePaths().isEmpty(), "Drone paths should be empty");
        });
    }

    @Test
    @DisplayName("Handle empty lists gracefully")
    void testEmptyInputHandling() {
        assertDoesNotThrow(() -> {
            DeliveryPathResponse resp = droneService.calculateOptimizedMultiDroneSolution(
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyMap(),
                    Collections.emptyList(),
                    Collections.emptyList()
            );
            assertNotNull(resp, "Response should not be null with empty lists");
            assertEquals(0, resp.getTotalCost());
            assertEquals(0, resp.getTotalMoves());
            assertTrue(resp.getDronePaths().isEmpty());
        });
    }

    @Test
    @DisplayName("Convert empty GeoJSON gracefully")
    void testEmptyGeoJsonHandling() {
        assertDoesNotThrow(() -> {
            String geoJson = droneService.convertMultipleDronesToGeoJson(
                    null, null
            );
            assertNotNull(geoJson, "GeoJSON should not be null with null input");
            assertTrue(geoJson.contains("\"features\":[]"), "GeoJSON should be empty features");
        });

        assertDoesNotThrow(() -> {
            String geoJson = droneService.convertMultipleDronesToGeoJson(
                    Collections.emptyList(), Collections.emptyList()
            );
            assertNotNull(geoJson);
            assertTrue(geoJson.contains("\"features\":[]"));
        });
    }

    @Test
    @DisplayName("Handle invalid restricted areas gracefully")
    void testInvalidRestrictedAreas() {
        RestrictedArea invalidArea = new RestrictedArea();
        invalidArea.setId(1L);
        invalidArea.setVertices(Collections.emptyList()); // invalid polygon
        assertDoesNotThrow(() -> {
            String geoJson = droneService.convertMultipleDronesToGeoJson(
                    Collections.emptyList(),
                    Collections.singletonList(invalidArea)
            );
            assertNotNull(geoJson, "GeoJSON should not be null for invalid area");
            assertTrue(geoJson.contains("\"features\":[]") || geoJson.contains("\"restricted_area\"") == false,
                    "Invalid restricted area should be ignored in GeoJSON");
        });
    }
}
