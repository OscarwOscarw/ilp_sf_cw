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
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CostEstimationIntegrationTest {

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
    @DisplayName("Verify cost estimation for multi-drone assignment")
    void testCostEstimation() {
        assertFalse(drones.isEmpty(), "Drones should not be empty");
        assertFalse(tasks.isEmpty(), "Tasks should not be empty");
        assertFalse(servicePoints.isEmpty(), "ServicePoints should not be empty");

        Map<Integer, PositionDto> taskLocations = droneService.assignTaskLocations(tasks);
        DeliveryPathResponse resp = droneService.calculateOptimizedMultiDroneSolution(
                drones,
                tasks,
                taskLocations,
                droneInfo,
                restrictedAreas
        );

        assertNotNull(resp, "Response should not be null");
        assertTrue(resp.getTotalCost() >= 0, "Total cost should be non-negative");
        assertTrue(resp.getTotalMoves() >= 0, "Total moves should be non-negative");

        for (DeliveryPathResponse.DronePath dp : resp.getDronePaths()) {
            assertNotNull(dp.getDroneId(), "Drone ID should not be null");
            if (dp.getDeliveries() != null) {
                for (DeliveryPathResponse.Delivery delivery : dp.getDeliveries()) {
                    assertNotNull(delivery.getFlightPath(), "Flight path should not be null");
                    assertFalse(delivery.getFlightPath().isEmpty(), "Flight path should have at least one position");
                }
            }
        }
        if (tasks.size() >= 2) {
            DeliveryPathResponse respSubset = droneService.calculateOptimizedMultiDroneSolution(
                    drones,
                    List.of(tasks.get(0)),
                    Map.of(tasks.get(0).getId(), taskLocations.get(tasks.get(0).getId())),
                    droneInfo,
                    restrictedAreas
            );
            assertTrue(resp.getTotalCost() >= respSubset.getTotalCost(),
                    "Adding more tasks should not decrease total cost");
        }

        double maxReasonableCost = drones.stream()
                .mapToDouble(d -> d.getCapability().getCostInitial() + d.getCapability().getCostFinal())
                .sum() * tasks.size();
        assertTrue(resp.getTotalCost() <= maxReasonableCost,
                "Total cost exceeds maximum reasonable limit");
    }
}
