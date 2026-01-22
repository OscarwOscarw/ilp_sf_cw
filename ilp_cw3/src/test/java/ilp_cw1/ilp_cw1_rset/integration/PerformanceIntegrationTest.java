package ilp_cw1.ilp_cw1_rset.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import data.*;
import ilp_cw1.ilp_cw1_rset.Droneservice.droneService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTimeout;

@SpringBootTest
public class PerformanceIntegrationTest {

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
    @DisplayName("Performance test: drone path planning should complete within 1 minute")
    void testPathPlanningPerformance() {
        assertTimeout(Duration.ofMinutes(1), () -> {
            Map<Integer, PositionDto> taskLocations = droneService.assignTaskLocations(tasks);

            DeliveryPathResponse response = droneService.calculateOptimizedMultiDroneSolution(
                    drones,
                    tasks,
                    taskLocations,
                    droneInfo,
                    restrictedAreas
            );

            String geoJsonStr = droneService.convertMultipleDronesToGeoJson(response.getDronePaths(), restrictedAreas);
        });
    }
}
