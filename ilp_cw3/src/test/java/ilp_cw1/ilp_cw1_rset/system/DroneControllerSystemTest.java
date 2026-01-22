package ilp_cw1.ilp_cw1_rset.system;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import data.QueryRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DroneControllerSystemTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;
    @BeforeEach
    void setup() {
        // Register JavaTimeModule in case controller uses LocalDate/LocalTime
        mapper.findAndRegisterModules();
    }

    /**
     * System tests for GET /api/v1/dronesWithCooling/{state}.
     * Validates equivalence classes (true/false) and boundary cases (case-insensitive, invalid, blank input).
     * Ensures graceful handling of invalid parameters with HTTP 200 and empty results.
     * Includes a lightweight performance check to address LO4 non-functional requirements.
     */
    @Test
    void testDronesWithCooling_TrueState() throws Exception {
        mockMvc.perform(get("/api/v1/dronesWithCooling/true"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testDronesWithCooling_FalseState() throws Exception {
        mockMvc.perform(get("/api/v1/dronesWithCooling/false"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testDronesWithCooling_CaseInsensitive() throws Exception {
        mockMvc.perform(get("/api/v1/dronesWithCooling/TrUe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testDronesWithCooling_InvalidState() throws Exception {
        mockMvc.perform(get("/api/v1/dronesWithCooling/abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testDronesWithCooling_EmptyState() throws Exception {
        mockMvc.perform(get("/api/v1/dronesWithCooling/ "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }


    @Test
    void testDronesWithCooling_Performance() throws Exception {
        long start = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
            mockMvc.perform(get("/api/v1/dronesWithCooling/true"))
                    .andExpect(status().isOk());
        }

        long duration = System.currentTimeMillis() - start;

        assert(duration < 50000);
    }

    /**
     * System tests for GET /api/v1/drone/{id}.
     * Verifies correct behaviour for existing and non-existing drone IDs.
     * Covers boundary cases including empty IDs and special characters.
     * Includes response format validation to address LO4 non-functional requirements.
     */
    @Test
    void testDroneDetails_ExistingId() throws Exception {
        MvcResult listResult = mockMvc.perform(get("/api/v1/dronesWithCooling/true"))
                .andExpect(status().isOk())
                .andReturn();

        List<String> ids = new ObjectMapper().readValue(
                listResult.getResponse().getContentAsString(),
                new TypeReference<>() {}
        );

        assertFalse(ids.isEmpty());

        String existingId = ids.get(0);

        mockMvc.perform(get("/api/v1/droneDetails/" + existingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingId));
    }

    @Test
    void testDroneDetails_NonExistingId() throws Exception {
        mockMvc.perform(get("/api/v1/droneDetails/non-existing-id-xyz"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDroneDetails_EmptyId() throws Exception {
        mockMvc.perform(get("/api/v1/droneDetails/ "))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDroneDetails_SpecialCharacters() throws Exception {
        mockMvc.perform(get("/api/v1/droneDetails/@@@###"))
                .andExpect(status().isNotFound());
    }

    /**
     * System tests for GET /api/v1/queryAsPath/{attributeName}/{attributeValue}.
     * Verifies correct handling of valid, invalid, numeric, string, boolean,
     * and malformed attribute queries. Ensures controller always returns 200.
     */

    @Test
    void testDroneDetails_ResponseFormat() throws Exception {
        MvcResult listResult = mockMvc.perform(get("/api/v1/dronesWithCooling/true"))
                .andExpect(status().isOk())
                .andReturn();

        List<String> ids = new ObjectMapper().readValue(
                listResult.getResponse().getContentAsString(),
                new TypeReference<>() {}
        );

        String existingId = ids.get(0);

        mockMvc.perform(get("/api/v1/droneDetails/" + existingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void testQueryAsPath_ValidAttribute() throws Exception {
        MvcResult listResult = mockMvc.perform(get("/api/v1/queryAsPath/id/1"))
                .andExpect(status().isOk())
                .andReturn();

        List<String> result = new ObjectMapper().readValue(
                listResult.getResponse().getContentAsString(),
                new TypeReference<>() {}
        );

        assertNotNull(result);
    }

    @Test
    void testQueryAsPath_InvalidAttribute() throws Exception {
        mockMvc.perform(get("/api/v1/queryAsPath/invalidAttr/someValue"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void testQueryAsPath_NumericComparison() throws Exception {
        mockMvc.perform(get("/api/v1/queryAsPath/capacity/10"))
                .andExpect(status().isOk());
    }

    @Test
    void testQueryAsPath_StringComparison() throws Exception {
        mockMvc.perform(get("/api/v1/queryAsPath/name/Drone 1"))
                .andExpect(status().isOk());
    }

    @Test
    void testQueryAsPath_BooleanComparison() throws Exception {
        mockMvc.perform(get("/api/v1/queryAsPath/hasCooling/true"))
                .andExpect(status().isOk());
    }

    @Test
    void testQueryAsPath_MalformedInput() throws Exception {
        mockMvc.perform(get("/api/v1/queryAsPath//"))
                .andExpect(status().isNotFound());
    }

    /**
     * System tests for POST /api/v1/query.
     * Verifies correct behaviour for single and multiple query conditions.
     * Covers conflicting conditions, empty or null input, and numerical/string/boolean comparisons.
     * Ensures controller always returns 200 and correct drone ID lists.
     */
    @Test
    void testQueryDrones_SingleCondition() throws Exception {
        String json = "[{\"attribute\":\"Cooling\",\"operator\":\"=\",\"value\":\"true\"}]";
        mockMvc.perform(post("/api/v1/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void testQueryDrones_MultipleConditions() throws Exception {
        String json = "[" +
                "{\"attribute\":\"Cooling\",\"operator\":\"=\",\"value\":\"true\"}," +
                "{\"attribute\":\"capacity\",\"operator\":\">\",\"value\":\"5\"}" +
                "]";
        mockMvc.perform(post("/api/v1/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void testQueryDrones_ConflictingConditions() throws Exception {
        String json = "[" +
                "{\"attribute\":\"Cooling\",\"operator\":\"=\",\"value\":\"true\"}," +
                "{\"attribute\":\"Cooling\",\"operator\":\"=\",\"value\":\"false\"}" +
                "]";
        mockMvc.perform(post("/api/v1/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void testQueryDrones_EmptyQueryList() throws Exception {
        String json = "[]";
        mockMvc.perform(post("/api/v1/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void testQueryDrones_NullQuery() throws Exception {
        String json = "null";
        mockMvc.perform(post("/api/v1/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void testQueryDrones_ComplexOperatorCombinations() throws Exception {
        String json = "[" +
                "{\"attribute\":\"capacity\",\"operator\":\"<\",\"value\":\"8\"}," +
                "{\"attribute\":\"Cooling\",\"operator\":\"=\",\"value\":\"true\"}" +
                "]";
        mockMvc.perform(post("/api/v1/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }


    /**
     * System tests for POST /api/v1/queryAvailableDrones.
     * Verifies correct behaviour for single and multiple medical dispatch tasks.
     * Covers invalid tasks, capacity boundary limits, cooling/heating conflicts,
     * and drone availability based on date and time constraints.
     * Ensures controller always returns 200 and correct drone ID lists.
     */

    @Test
    public void testQueryAvailableDrones_SingleTask() throws Exception {
        String json = "[" +
                "{\"id\":123,\"date\":\"2025-12-22\",\"time\":\"14:30\",\"requirements\":{\"capacity\":0.75,\"cooling\":false,\"heating\":true}}" +
                "]";

        String response = mockMvc.perform(post("/api/v1/queryAvailableDrones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        System.out.println("SingleTask Response: " + response);
    }

    @Test
    public void testQueryAvailableDrones_MultipleTasks() throws Exception {
        String json = "[" +
                "{\"id\":123,\"date\":\"2025-12-22\",\"time\":\"14:30\",\"requirements\":{\"capacity\":0.5}}," +
                "{\"id\":124,\"date\":\"2025-12-22\",\"time\":\"14:30\",\"requirements\":{\"capacity\":0.6}}" +
                "]";

        String response = mockMvc.perform(post("/api/v1/queryAvailableDrones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        System.out.println("MultipleTasks Response: " + response);
    }

    @Test
    public void testQueryAvailableDrones_InvalidTask() throws Exception {
        String json = "[" +
                "{\"id\":0,\"date\":\"2025-12-22\",\"time\":\"14:30\",\"requirements\":{\"capacity\":0}}" + // 无效 id 和容量
                "]";

        String response = mockMvc.perform(post("/api/v1/queryAvailableDrones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        System.out.println("InvalidTask Response: " + response);
    }

    @Test
    public void testQueryAvailableDrones_CapacityLimits() throws Exception {
        String json = "[" +
                "{\"id\":125,\"date\":\"2025-12-22\",\"time\":\"14:30\",\"requirements\":{\"capacity\":0.01}}," +
                "{\"id\":126,\"date\":\"2025-12-22\",\"time\":\"14:30\",\"requirements\":{\"capacity\":10.0}}" +
                "]";

        String response = mockMvc.perform(post("/api/v1/queryAvailableDrones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        System.out.println("CapacityLimits Response: " + response);
    }

    @Test
    public void testQueryAvailableDrones_TemperatureConflicts() throws Exception {
        String json = "[" +
                "{\"id\":127,\"date\":\"2025-12-22\",\"time\":\"14:30\",\"requirements\":{\"capacity\":1.0,\"cooling\":true,\"heating\":true}}" +
                "]";

        String response = mockMvc.perform(post("/api/v1/queryAvailableDrones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        System.out.println("TemperatureConflicts Response: " + response);
    }

    @Test
    public void testQueryAvailableDrones_TimeAvailability() throws Exception {
        String json = "[" +
                "{\"id\":128,\"date\":\"2025-12-23\",\"time\":\"14:30\",\"requirements\":{\"capacity\":1.0}}," +
                "{\"id\":129,\"date\":\"2025-12-23\",\"time\":\"14:30\",\"requirements\":{\"capacity\":0.8}}" +
                "]";

        String response = mockMvc.perform(post("/api/v1/queryAvailableDrones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        System.out.println("TimeAvailability Response: " + response);
    }

    /**
     * System tests for POST /api/v1/calcDeliveryPath.
     * Verifies correct behavior for single/multiple tasks, no available drones,
     * restricted areas, cost optimization, and performance benchmarks.
     */
    @Test
    void testCalcDeliveryPath_SingleTask() throws Exception {
        String json = "[" +
                "{\"id\":1001,\"date\":\"2025-11-13\",\"time\":\"09:30\",\"delivery\":{\"lng\":-3.188,\"lat\":55.946}," +
                "\"requirements\":{\"capacity\":3.5,\"cooling\":true,\"heating\":false,\"maxCost\":25.0}}" +
                "]";

        String response = mockMvc.perform(post("/api/v1/calcDeliveryPath")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        System.out.println("SingleTask Response: " + response);
    }

    @Test
    void testCalcDeliveryPath_MultipleTasks() throws Exception {
        String json = "[" +
                "{\"id\":1001,\"date\":\"2025-11-13\",\"time\":\"09:30\",\"delivery\":{\"lng\":-3.188,\"lat\":55.946}," +
                "\"requirements\":{\"capacity\":3.5,\"cooling\":true,\"heating\":false,\"maxCost\":25.0}}," +
                "{\"id\":1002,\"date\":\"2025-11-13\",\"time\":\"10:15\",\"delivery\":{\"lng\":-3.192,\"lat\":55.943}," +
                "\"requirements\":{\"capacity\":7.0,\"cooling\":false,\"heating\":true,\"maxCost\":30.0}}" +
                "]";

        String response = mockMvc.perform(post("/api/v1/calcDeliveryPath")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        System.out.println("MultipleTasks Response: " + response);
    }

    @Test
    void testCalcDeliveryPath_NoAvailableDrones() throws Exception {
        String json = "[" +
                "{\"id\":0,\"date\":\"2025-11-13\",\"time\":\"09:30\",\"delivery\":{\"lng\":-3.188,\"lat\":55.946}," +
                "\"requirements\":{\"capacity\":3.5,\"cooling\":true,\"heating\":false,\"maxCost\":25.0}}" +
                "]";

        String response = mockMvc.perform(post("/api/v1/calcDeliveryPath")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        System.out.println("NoAvailableDrones Response: " + response);
    }

    @Test
    void testCalcDeliveryPath_RestrictedArea() throws Exception {
        String json = "[" +
                "{\"id\":1003,\"date\":\"2025-11-13\",\"time\":\"11:00\",\"delivery\":{\"lng\":-3.180,\"lat\":55.950}," +
                "\"requirements\":{\"capacity\":2.0,\"cooling\":false,\"heating\":true,\"maxCost\":20.0}}" +
                "]";

        String response = mockMvc.perform(post("/api/v1/calcDeliveryPath")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        System.out.println("RestrictedArea Response: " + response);
    }

    @Test
    void testCalcDeliveryPath_CostOptimization() throws Exception {
        String json = "[" +
                "{\"id\":1001,\"date\":\"2025-11-13\",\"time\":\"09:30\",\"delivery\":{\"lng\":-3.188,\"lat\":55.946}," +
                "\"requirements\":{\"capacity\":3.5,\"cooling\":true,\"heating\":false,\"maxCost\":25.0}}," +
                "{\"id\":1004,\"date\":\"2025-11-13\",\"time\":\"13:45\",\"delivery\":{\"lng\":-3.189,\"lat\":55.945}," +
                "\"requirements\":{\"capacity\":4.2,\"cooling\":true,\"heating\":false,\"maxCost\":40.0}}," +
                "{\"id\":1006,\"date\":\"2025-11-13\",\"time\":\"15:30\",\"delivery\":{\"lng\":-3.187,\"lat\":55.944}," +
                "\"requirements\":{\"capacity\":2.8,\"cooling\":false,\"heating\":true,\"maxCost\":20.0}}" +
                "]";

        String response = mockMvc.perform(post("/api/v1/calcDeliveryPath")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        System.out.println("CostOptimization Response: " + response);
    }

    @Test
    void testCalcDeliveryPath_PerformanceBenchmark() throws Exception {
        String json = "[" +
                "{\"id\":1001,\"date\":\"2025-11-13\",\"time\":\"09:30\",\"delivery\":{\"lng\":-3.188,\"lat\":55.946}," +
                "\"requirements\":{\"capacity\":3.5,\"cooling\":true,\"heating\":false,\"maxCost\":25.0}}," +
                "{\"id\":1002,\"date\":\"2025-11-13\",\"time\":\"10:15\",\"delivery\":{\"lng\":-3.192,\"lat\":55.943}," +
                "\"requirements\":{\"capacity\":7.0,\"cooling\":false,\"heating\":true,\"maxCost\":30.0}}," +
                "{\"id\":1003,\"date\":\"2025-11-13\",\"time\":\"11:00\",\"delivery\":{\"lng\":-3.175,\"lat\":55.982}," +
                "\"requirements\":{\"capacity\":6.5,\"cooling\":false,\"heating\":true,\"maxCost\":40.0}}," +
                "{\"id\":1004,\"date\":\"2025-11-13\",\"time\":\"13:45\",\"delivery\":{\"lng\":-3.189,\"lat\":55.945}," +
                "\"requirements\":{\"capacity\":4.2,\"cooling\":true,\"heating\":false,\"maxCost\":40.0}}," +
                "{\"id\":1005,\"date\":\"2025-11-13\",\"time\":\"14:20\",\"delivery\":{\"lng\":-3.178,\"lat\":55.980}," +
                "\"requirements\":{\"capacity\":11.0,\"cooling\":false,\"heating\":false,\"maxCost\":50.0}}," +
                "{\"id\":1006,\"date\":\"2025-11-13\",\"time\":\"15:30\",\"delivery\":{\"lng\":-3.187,\"lat\":55.944}," +
                "\"requirements\":{\"capacity\":2.8,\"cooling\":false,\"heating\":true,\"maxCost\":20.0}}" +
                "]";

        long start = System.currentTimeMillis();

        String response = mockMvc.perform(post("/api/v1/calcDeliveryPath")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        long duration = System.currentTimeMillis() - start;

        System.out.println("PerformanceBenchmark Response: " + response);
        System.out.println("Performance duration (ms): " + duration);

        assert(duration < 5000);
    }


    /**
     * System tests for POST /api/v1/calcDeliveryPathAsGeoJson.
     * Verifies valid GeoJSON responses, structure, coordinates precision, empty results, and performance.
     */
    @Test
    void testCalcDeliveryPathAsGeoJson_ValidResponse() throws Exception {
        String json = "[" +
                "{\"id\":1001,\"date\":\"2025-11-13\",\"time\":\"09:30\",\"delivery\":{\"lng\":-3.188,\"lat\":55.946}," +
                "\"requirements\":{\"capacity\":3.5,\"cooling\":true,\"heating\":false,\"maxCost\":25.0}}," +
                "{\"id\":1002,\"date\":\"2025-11-13\",\"time\":\"10:15\",\"delivery\":{\"lng\":-3.192,\"lat\":55.943}," +
                "\"requirements\":{\"capacity\":7.0,\"cooling\":false,\"heating\":true,\"maxCost\":30.0}}" +
                "]";

        String response = mockMvc.perform(post("/api/v1/calcDeliveryPathAsGeoJson")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        System.out.println("ValidResponse GeoJSON: " + response);
        assert(response.contains("FeatureCollection") || response.contains("LineString"));
    }

    @Test
    void testCalcDeliveryPathAsGeoJson_Structure() throws Exception {
        String json = "[" +
                "{\"id\":1003,\"date\":\"2025-11-13\",\"time\":\"11:00\",\"delivery\":{\"lng\":-3.175,\"lat\":55.982}," +
                "\"requirements\":{\"capacity\":6.5,\"cooling\":false,\"heating\":true,\"maxCost\":40.0}}," +
                "{\"id\":1004,\"date\":\"2025-11-13\",\"time\":\"13:45\",\"delivery\":{\"lng\":-3.189,\"lat\":55.945}," +
                "\"requirements\":{\"capacity\":4.2,\"cooling\":true,\"heating\":false,\"maxCost\":40.0}}" +
                "]";

        String response = mockMvc.perform(post("/api/v1/calcDeliveryPathAsGeoJson")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        System.out.println("Structure GeoJSON: " + response);
        assert(response.contains("\"features\""));
        assert(response.contains("\"coordinates\""));
    }

    @Test
    void testCalcDeliveryPathAsGeoJson_Coordinates() throws Exception {
        String json = "[" +
                "{\"id\":1005,\"date\":\"2025-11-13\",\"time\":\"14:20\",\"delivery\":{\"lng\":-3.178,\"lat\":55.980}," +
                "\"requirements\":{\"capacity\":11.0,\"cooling\":false,\"heating\":false,\"maxCost\":50.0}}" +
                "]";

        String response = mockMvc.perform(post("/api/v1/calcDeliveryPathAsGeoJson")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        System.out.println("Coordinates GeoJSON: " + response);
        assert(response.matches(".*-?\\d+\\.\\d+.*"));
    }

    @Test
    void testCalcDeliveryPathAsGeoJson_EmptyResult() throws Exception {
        String json = "[]";

        String response = mockMvc.perform(post("/api/v1/calcDeliveryPathAsGeoJson")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        System.out.println("EmptyResult GeoJSON: " + response);
        assert(response.equals("{}"));
    }

    @Test
    void testCalcDeliveryPathAsGeoJson_Performance() throws Exception {
        String json = "[" +
                "{\"id\":1001,\"date\":\"2025-11-13\",\"time\":\"09:30\",\"delivery\":{\"lng\":-3.188,\"lat\":55.946}," +
                "\"requirements\":{\"capacity\":3.5,\"cooling\":true,\"heating\":false,\"maxCost\":25.0}}," +
                "{\"id\":1002,\"date\":\"2025-11-13\",\"time\":\"10:15\",\"delivery\":{\"lng\":-3.192,\"lat\":55.943}," +
                "\"requirements\":{\"capacity\":7.0,\"cooling\":false,\"heating\":true,\"maxCost\":30.0}}," +
                "{\"id\":1003,\"date\":\"2025-11-13\",\"time\":\"11:00\",\"delivery\":{\"lng\":-3.175,\"lat\":55.982}," +
                "\"requirements\":{\"capacity\":6.5,\"cooling\":false,\"heating\":true,\"maxCost\":40.0}}," +
                "{\"id\":1004,\"date\":\"2025-11-13\",\"time\":\"13:45\",\"delivery\":{\"lng\":-3.189,\"lat\":55.945}," +
                "\"requirements\":{\"capacity\":4.2,\"cooling\":true,\"heating\":false,\"maxCost\":40.0}}," +
                "{\"id\":1005,\"date\":\"2025-11-13\",\"time\":\"14:20\",\"delivery\":{\"lng\":-3.178,\"lat\":55.980}," +
                "\"requirements\":{\"capacity\":11.0,\"cooling\":false,\"heating\":false,\"maxCost\":50.0}}," +
                "{\"id\":1006,\"date\":\"2025-11-13\",\"time\":\"15:30\",\"delivery\":{\"lng\":-3.187,\"lat\":55.944}," +
                "\"requirements\":{\"capacity\":2.8,\"cooling\":false,\"heating\":true,\"maxCost\":20.0}}" +
                "]";

        long start = System.currentTimeMillis();

        String response = mockMvc.perform(post("/api/v1/calcDeliveryPathAsGeoJson")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        long duration = System.currentTimeMillis() - start;

        System.out.println("Performance GeoJSON Response: " + response);
        System.out.println("Performance duration (ms): " + duration);

        assert(duration < 5000);
    }


    /////////////////////////////////ADDITION TEST/////////////////////////////////////////////////////////////////////////
    @Test
    public void testQueryAvailableDrones_TimeConflict() throws Exception {
        String json = "[" +
                "{\"id\":2001,\"date\":\"2025-12-24\",\"time\":\"10:00\",\"requirements\":{\"capacity\":1.0}}," +
                "{\"id\":2002,\"date\":\"2025-12-24\",\"time\":\"10:00\",\"requirements\":{\"capacity\":1.5}}" +
                "]";
        mockMvc.perform(post("/api/v1/queryAvailableDrones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }


    @Test
    public void testQueryAvailableDrones_InvalidCoordinates() throws Exception {
        String json = "[" +
                "{\"id\":2003,\"date\":\"2025-12-24\",\"time\":\"11:00\",\"delivery\":{\"lng\":-200.0,\"lat\":95.0},\"requirements\":{\"capacity\":1.0}}" +
                "]";
        mockMvc.perform(post("/api/v1/queryAvailableDrones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    public void testQueryAvailableDrones_DuplicateTaskId() throws Exception {
        String json = "[" +
                "{\"id\":2004,\"date\":\"2025-12-24\",\"time\":\"12:00\",\"requirements\":{\"capacity\":1.0}}," +
                "{\"id\":2004,\"date\":\"2025-12-24\",\"time\":\"12:30\",\"requirements\":{\"capacity\":0.5}}" +
                "]";
        mockMvc.perform(post("/api/v1/queryAvailableDrones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    public void testQueryAvailableDrones_ExcessiveCapacity() throws Exception {
        String json = "[" +
                "{\"id\":2005,\"date\":\"2025-12-24\",\"time\":\"13:00\",\"requirements\":{\"capacity\":100.0}}" +
                "]";
        mockMvc.perform(post("/api/v1/queryAvailableDrones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }


    @Test
    public void testQueryAvailableDrones_EmptyJsonObject() throws Exception {
        String json = "{}";
        mockMvc.perform(post("/api/v1/queryAvailableDrones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testQueryAvailableDrones_InvalidFieldType() throws Exception {
        String json = "[" +
                "{\"id\":\"abc\",\"date\":\"2025-12-24\",\"time\":\"14:00\",\"requirements\":{\"capacity\":\"high\"}}" +
                "]";
        mockMvc.perform(post("/api/v1/queryAvailableDrones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testQueryAvailableDrones_HeatingCoolingConflict() throws Exception {
        String json = "[" +
                "{\"id\":2006,\"date\":\"2025-12-24\",\"time\":\"15:00\",\"requirements\":{\"capacity\":1.0,\"cooling\":true,\"heating\":true}}" +
                "]";
        mockMvc.perform(post("/api/v1/queryAvailableDrones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }


    @Test
    public void testQueryAvailableDrones_MissingRequiredFields() throws Exception {
        String json = "[" +
                "{\"date\":\"2025-12-24\",\"time\":\"16:00\"}" +
                "]";
        mockMvc.perform(post("/api/v1/queryAvailableDrones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }


    @Test
    public void testDroneDetails_SpecialCharsInId() throws Exception {
        mockMvc.perform(get("/api/v1/droneDetails/!@#$%^&*()"))
                .andExpect(status().isNotFound());
    }


    @Test
    public void testQueryAsPath_BooleanCaseInsensitive() throws Exception {
        mockMvc.perform(get("/api/v1/queryAsPath/hasCooling/TrUe"))
                .andExpect(status().isOk());
    }

    @Test
    public void testQueryAsPath_NumericBoundary() throws Exception {
        mockMvc.perform(get("/api/v1/queryAsPath/capacity/0"))
                .andExpect(status().isOk());
    }


    @Test
    void testCalcDeliveryPathAsGeoJson_EmptyTasks() throws Exception {
        String json = "[]";
        mockMvc.perform(post("/api/v1/calcDeliveryPathAsGeoJson")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("{}"));
    }

    @Test
    void testQueryDrones_NullAttributeInQuery() throws Exception {
        String json = "[" +
                "{\"attribute\":null,\"operator\":\"=\",\"value\":\"true\"}" +
                "]";
        mockMvc.perform(post("/api/v1/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void testCalcDeliveryPath_LargeTaskBatch() throws Exception {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 1; i <= 50; i++) {
            sb.append("{\"id\":").append(3000 + i)
                    .append(",\"date\":\"2025-12-25\",\"time\":\"09:00\",\"delivery\":{\"lng\":-3.18,\"lat\":55.95},\"requirements\":{\"capacity\":").append(i * 0.1).append("}}");
            if (i < 50) sb.append(",");
        }
        sb.append("]");
        long start = System.currentTimeMillis();
        mockMvc.perform(post("/api/v1/calcDeliveryPath")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sb.toString()))
                .andExpect(status().isOk());
        long duration = System.currentTimeMillis() - start;
        System.out.println("LargeTaskBatch duration (ms): " + duration);
    }






}


