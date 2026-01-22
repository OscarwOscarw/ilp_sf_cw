package ilp_cw1.ilp_cw1_rset.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import data.*;
import ilp_cw1.ilp_cw1_rset.Droneservice.droneService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class GeoJsonConversionIntegrationTest {

    @Autowired
    private droneService droneService;

    @Autowired
    private ObjectMapper objectMapper;

    private List<Drone> drones;
    private List<DroneForServicePoint> droneInfo;
    private List<MedDispatchRec> tasks;
    private List<ServicePoint> servicePoints;
    private List<RestrictedArea> restrictedAreas;

    @BeforeEach
    void setup() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/test-data/01_drones-normal.json")) {
            drones = objectMapper.readValue(is, new TypeReference<List<Drone>>() {});
        }
        try (InputStream is = getClass().getResourceAsStream("/test-data/02_drones-for-service-points.json")) {
            droneInfo = objectMapper.readValue(is, new TypeReference<List<DroneForServicePoint>>() {});
        }
        try (InputStream is = getClass().getResourceAsStream("/test-data/04_tasks-normal.json")) {
            tasks = objectMapper.readValue(is, new TypeReference<List<MedDispatchRec>>() {});
        }
        try (InputStream is = getClass().getResourceAsStream("/test-data/06_service-points.json")) {
            servicePoints = objectMapper.readValue(is, new TypeReference<List<ServicePoint>>() {});
        }
        try (InputStream is = getClass().getResourceAsStream("/test-data/07_restricted-areas.json")) {
            restrictedAreas = objectMapper.readValue(is, new TypeReference<List<RestrictedArea>>() {});
        }
    }

    @Test
    @DisplayName("Verify GeoJSON conversion for drones and restricted areas")
    void testGeoJsonConversion() throws Exception {
        Map<Integer, PositionDto> taskLocations = droneService.assignTaskLocations(tasks);
        DeliveryPathResponse response = droneService.calculateOptimizedMultiDroneSolution(
                drones,
                tasks,
                taskLocations,
                droneInfo,
                restrictedAreas
        );

        String geoJsonStr = droneService.convertMultipleDronesToGeoJson(response.getDronePaths(), restrictedAreas);
        assertNotNull(geoJsonStr, "GeoJSON should not be null");

        JsonNode geoJson = objectMapper.readTree(geoJsonStr);
        assertEquals("FeatureCollection", geoJson.get("type").asText(), "GeoJSON must be FeatureCollection");
        JsonNode features = geoJson.get("features");
        assertTrue(features.isArray(), "GeoJSON features must be an array");

        for (DeliveryPathResponse.DronePath dronePath : response.getDronePaths()) {
            boolean foundDroneFeature = false;
            for (JsonNode feature : features) {
                if (!"drone_path".equals(feature.get("properties").get("type").asText())) continue;
                String droneId = feature.get("properties").get("droneId").asText();
                if (!droneId.equals(dronePath.getDroneId())) continue;

                foundDroneFeature = true;

                JsonNode coords = feature.get("geometry").get("coordinates");
                assertNotNull(coords, "Drone feature coordinates should not be null");

                List<PositionDto> fullPath = new ArrayList<>();
                for (DeliveryPathResponse.Delivery delivery : dronePath.getDeliveries()) {
                    if (delivery.getFlightPath() != null) fullPath.addAll(delivery.getFlightPath());
                }

                if (fullPath.isEmpty()) continue;
                assertTrue(Math.abs(fullPath.size() - coords.size())<=2, "Coordinate count should match flight path size");
            }
            assertTrue(foundDroneFeature, "Drone feature must exist for droneId " + dronePath.getDroneId());
        }

        for (RestrictedArea area : restrictedAreas) {
            boolean foundAreaFeature = false;
            for (JsonNode feature : features) {
                if (!"restricted_area".equals(feature.get("properties").get("type").asText())) continue;
                int areaId = feature.get("properties").get("areaId").asInt();
                if (areaId != area.getId()) continue;

                foundAreaFeature = true;

                JsonNode coords = feature.get("geometry").get("coordinates").get(0);
                assertNotNull(coords, "RestrictedArea coordinates should not be null");

                PositionDto first = area.getVertices().get(0);
                JsonNode last = coords.get(coords.size() - 1);
                assertEquals(first.getLng(), last.get(0).asDouble(), 0.01, "Polygon start/end longitude must match within 0.01");
                assertEquals(first.getLat(), last.get(1).asDouble(), 0.01, "Polygon start/end latitude must match within 0.01");
            }
            assertTrue(foundAreaFeature, "RestrictedArea feature must exist for areaId " + area.getId());
        }
    }
}
