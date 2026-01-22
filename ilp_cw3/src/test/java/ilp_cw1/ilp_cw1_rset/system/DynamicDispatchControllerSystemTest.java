package ilp_cw1.ilp_cw1_rset.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DynamicDispatchControllerSystemTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @BeforeEach
    void setup() {
        mapper.findAndRegisterModules();
    }

    /**
     * System tests for POST /api/dispatch/simulate
     *
     * This is a true system-level test: the JSON is sent to the controller and the controller
     * runs its full logic. No services are mocked.
     */
    @Test
    void testSimulateDynamicPath_ValidRequest() throws Exception {
        String json = "[\n" +
                "  {\n" +
                "    \"id\": 1001,\n" +
                "    \"date\": \"2025-11-13\",\n" +
                "    \"time\": \"09:30\",\n" +
                "    \"delivery\": { \"lng\": -3.188, \"lat\": 55.946 },\n" +
                "    \"requirements\": { \"capacity\": 3.5, \"cooling\": true, \"heating\": false, \"maxCost\": 25.0 }\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": 1002,\n" +
                "    \"date\": \"2025-11-13\",\n" +
                "    \"time\": \"10:15\",\n" +
                "    \"delivery\": { \"lng\": -3.192, \"lat\": 55.943 },\n" +
                "    \"requirements\": { \"capacity\": 7.0, \"cooling\": false, \"heating\": true, \"maxCost\": 30.0 }\n" +
                "  }\n" +
                "]";

        mockMvc.perform(post("/api/dispatch/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("Dynamic simulation started successfully."));
    }

    @Test
    void testSimulateDynamicPath_EmptyRequest() throws Exception {
        mockMvc.perform(post("/api/dispatch/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error: Task list cannot be empty."));
    }

    @Test
    void testSimulateDynamicPath_MalformedJson() throws Exception {
        String malformedJson = "[ { \"id\": 1001, \"date\": \"2025-11-13\" "; // truncated, invalid JSON

        mockMvc.perform(post("/api/dispatch/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSimulateDynamicPath_LargeTaskList() throws Exception {
        String json = "[\n" +
                "    {\n" +
                "        \"id\": 1001,\n" +
                "        \"date\": \"2025-11-13\",\n" +
                "        \"time\": \"09:30\",\n" +
                "        \"delivery\": {\n" +
                "            \"lng\": -3.188,\n" +
                "            \"lat\": 55.946\n" +
                "        },\n" +
                "        \"requirements\": {\n" +
                "            \"capacity\": 3.5,\n" +
                "            \"cooling\": true,\n" +
                "            \"heating\": false,\n" +
                "            \"maxCost\": 25.0\n" +
                "        }\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 1002,\n" +
                "        \"date\": \"2025-11-13\",\n" +
                "        \"time\": \"10:15\",\n" +
                "        \"delivery\": {\n" +
                "            \"lng\": -3.192,\n" +
                "            \"lat\": 55.943\n" +
                "        },\n" +
                "        \"requirements\": {\n" +
                "            \"capacity\": 7.0,\n" +
                "            \"cooling\": false,\n" +
                "            \"heating\": true,\n" +
                "            \"maxCost\": 30.0\n" +
                "        }\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 1003,\n" +
                "        \"date\": \"2025-11-13\",\n" +
                "        \"time\": \"11:00\",\n" +
                "        \"delivery\": {\n" +
                "            \"lng\": -3.175,\n" +
                "            \"lat\": 55.982\n" +
                "        },\n" +
                "        \"requirements\": {\n" +
                "            \"capacity\": 6.5,\n" +
                "            \"cooling\": false,\n" +
                "            \"heating\": true,\n" +
                "            \"maxCost\": 40.0\n" +
                "        }\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 1004,\n" +
                "        \"date\": \"2025-11-13\",\n" +
                "        \"time\": \"13:45\",\n" +
                "        \"delivery\": {\n" +
                "            \"lng\": -3.189,\n" +
                "            \"lat\": 55.945\n" +
                "        },\n" +
                "        \"requirements\": {\n" +
                "            \"capacity\": 4.2,\n" +
                "            \"cooling\": true,\n" +
                "            \"heating\": false,\n" +
                "            \"maxCost\": 40.0\n" +
                "        }\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 1005,\n" +
                "        \"date\": \"2025-11-13\",\n" +
                "        \"time\": \"14:20\",\n" +
                "        \"delivery\": {\n" +
                "            \"lng\": -3.178,\n" +
                "            \"lat\": 55.980\n" +
                "        },\n" +
                "        \"requirements\": {\n" +
                "            \"capacity\": 11.0,\n" +
                "            \"cooling\": false,\n" +
                "            \"heating\": false,\n" +
                "            \"maxCost\": 50.0\n" +
                "        }\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": 1006,\n" +
                "        \"date\": \"2025-11-13\",\n" +
                "        \"time\": \"15:30\",\n" +
                "        \"delivery\": {\n" +
                "            \"lng\": -3.187,\n" +
                "            \"lat\": 55.944\n" +
                "        },\n" +
                "        \"requirements\": {\n" +
                "            \"capacity\": 2.8,\n" +
                "            \"cooling\": false,\n" +
                "            \"heating\": true,\n" +
                "            \"maxCost\": 20.0\n" +
                "        }\n" +
                "    }\n" +
                "]";


        mockMvc.perform(post("/api/dispatch/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("Dynamic simulation started successfully."));
    }

    @Test
    void testSimulateDynamicPath_TimeConflict() throws Exception {
        String json = "["
                + "{\"id\":1010,\"date\":\"2025-11-13\",\"time\":\"10:00\",\"delivery\":{\"lng\":-3.180,\"lat\":55.945},\"requirements\":{\"capacity\":3.0,\"cooling\":false,\"heating\":true,\"maxCost\":20.0}},"
                + "{\"id\":1011,\"date\":\"2025-11-13\",\"time\":\"10:00\",\"delivery\":{\"lng\":-3.181,\"lat\":55.946},\"requirements\":{\"capacity\":2.0,\"cooling\":true,\"heating\":false,\"maxCost\":25.0}}"
                + "]";
        mockMvc.perform(post("/api/dispatch/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void testSimulateDynamicPath_InvalidCoordinates() throws Exception {
        String json = "[{\"id\":1012,\"date\":\"2025-11-13\",\"time\":\"11:00\",\"delivery\":{\"lng\":-200.0,\"lat\":95.0},\"requirements\":{\"capacity\":2.0,\"cooling\":false,\"heating\":false,\"maxCost\":10.0}}]";
        mockMvc.perform(post("/api/dispatch/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSimulateDynamicPath_DuplicateTaskId() throws Exception {
        String json = "[{\"id\":1013,\"date\":\"2025-11-13\",\"time\":\"12:00\",\"delivery\":{\"lng\":-3.182,\"lat\":55.948},\"requirements\":{\"capacity\":2.0,\"cooling\":false,\"heating\":true,\"maxCost\":15.0}},"
                + "{\"id\":1013,\"date\":\"2025-11-13\",\"time\":\"12:30\",\"delivery\":{\"lng\":-3.183,\"lat\":55.949},\"requirements\":{\"capacity\":1.5,\"cooling\":true,\"heating\":false,\"maxCost\":10.0}}]";
        mockMvc.perform(post("/api/dispatch/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSimulateDynamicPath_ExcessiveCapacity() throws Exception {
        String json = "[{\"id\":1014,\"date\":\"2025-11-13\",\"time\":\"13:00\",\"delivery\":{\"lng\":-3.184,\"lat\":55.950},\"requirements\":{\"capacity\":100.0,\"cooling\":false,\"heating\":false,\"maxCost\":500.0}}]";
        mockMvc.perform(post("/api/dispatch/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isInternalServerError());
    }


    @Test
    void testSimulateDynamicPath_CrossDateTask() throws Exception {
        String json = "[{\"id\":1016,\"date\":\"2026-12-13\",\"time\":\"09:00\",\"delivery\":{\"lng\":-3.188,\"lat\":55.954},\"requirements\":{\"capacity\":2.0,\"cooling\":false,\"heating\":true,\"maxCost\":20.0}}]";
        mockMvc.perform(post("/api/dispatch/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }


    @Test
    void testSimulateDynamicPath_MissingDeliveryField() throws Exception {
        String json = "[{\"id\":1018,\"date\":\"2025-11-13\",\"time\":\"17:00\",\"requirements\":{\"capacity\":2.0,\"cooling\":false,\"heating\":true,\"maxCost\":20.0}}]";
        mockMvc.perform(post("/api/dispatch/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testTaskAfterClearingRestrictedAreas() throws Exception {
        mockMvc.perform(delete("/api/dispatch/restricted-areas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        String json = "[{\"id\":1019,\"date\":\"2025-11-13\",\"time\":\"18:00\",\"delivery\":{\"lng\":-3.191,\"lat\":55.957},\"requirements\":{\"capacity\":1.0,\"cooling\":false,\"heating\":false,\"maxCost\":15.0}}]";
        mockMvc.perform(post("/api/dispatch/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    /**
     * System tests for POST /api/dispatch/emergency
     *
     * True system-level test: the JSON is sent to the controller and the controller
     * runs full logic.
     */

    @Test
    void testHandleEmergencyOrder_NoSimulationRunning() throws Exception {
        String json = "{\n" +
                "  \"emergencyTasks\": [\n" +
                "    {\n" +
                "      \"id\": 2002,\n" +
                "      \"emergencyLevel\": 3,\n" +
                "      \"date\": \"2025-11-13\",\n" +
                "      \"time\": \"14:30\",\n" +
                "      \"requirements\": {\n" +
                "        \"capacity\": 4.5,\n" +
                "        \"maxCost\": 200\n" +
                "      },\n" +
                "      \"delivery\": {\n" +
                "        \"lng\": -3.189,\n" +
                "        \"lat\": 55.945\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        mockMvc.perform(post("/api/dispatch/emergency")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void testHandleEmergencyOrder_EmptyTaskList() throws Exception {
        String json = "{ \"emergencyTasks\": [] }";

        mockMvc.perform(post("/api/dispatch/emergency")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testHandleEmergencyOrder_ValidTasks() throws Exception {
        String json = "{\n" +
                "  \"emergencyTasks\": [\n" +
                "    {\n" +
                "      \"id\": 2002,\n" +
                "      \"emergencyLevel\": 3,\n" +
                "      \"date\": \"2025-11-13\",\n" +
                "      \"time\": \"14:30\",\n" +
                "      \"requirements\": { \"capacity\": 4.5, \"maxCost\": 200 },\n" +
                "      \"delivery\": { \"lng\": -3.189, \"lat\": 55.945 }\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": 205,\n" +
                "      \"emergencyLevel\": 5,\n" +
                "      \"date\": \"2025-11-13\",\n" +
                "      \"time\": \"14:30\",\n" +
                "      \"requirements\": { \"capacity\": 4.5, \"maxCost\": 200 },\n" +
                "      \"delivery\": { \"lng\": -3.189, \"lat\": 55.943 }\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        mockMvc.perform(post("/api/dispatch/emergency")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testHandleEmergencyOrder_LargeTaskList() throws Exception {
        StringBuilder jsonBuilder = new StringBuilder("{\"emergencyTasks\":[");
        for (int i = 1; i <= 10; i++) {
            jsonBuilder.append("{\n")
                    .append("\"id\": ").append(2000 + i).append(",\n")
                    .append("\"emergencyLevel\": ").append((i % 5) + 1).append(",\n")
                    .append("\"date\": \"2025-11-13\",\n")
                    .append("\"time\": \"1").append(i).append(":30\",\n")
                    .append("\"requirements\": { \"capacity\": 5.0, \"maxCost\": 200 },\n")
                    .append("\"delivery\": { \"lng\": -3.18").append(i).append(", \"lat\": 55.94").append(i).append(" }\n")
                    .append("}");
            if (i != 10) jsonBuilder.append(",");
        }
        jsonBuilder.append("]}");

        mockMvc.perform(post("/api/dispatch/emergency")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBuilder.toString()))
                .andExpect(status().isBadRequest());
    }

    /**
     * System tests for POST /api/dispatch/stop
     *
     * True system-level tests: controller is called without mocking.
     */
    @Test
    void testStopSimulation_Success() throws Exception {
        mockMvc.perform(post("/api/dispatch/stop")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Dynamic simulation stopped successfully."));
    }

    @Test
    void testStopSimulation_InternalError() throws Exception {
        mockMvc.perform(post("/api/dispatch/stop")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /**
     * System tests for GET /api/dispatch/drone/{droneId}/status
     *
     * True system-level tests: call controller directly with real droneId.
     */

    @Test
    void testGetDronesStatus_AfterSimulation() throws Exception {
        String json = "[\n" +
                "  {\n" +
                "    \"id\": 1001,\n" +
                "    \"date\": \"2025-11-13\",\n" +
                "    \"time\": \"09:30\",\n" +
                "    \"delivery\": { \"lng\": -3.188, \"lat\": 55.946 },\n" +
                "    \"requirements\": { \"capacity\": 3.5, \"cooling\": true, \"heating\": false, \"maxCost\": 25.0 }\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": 1002,\n" +
                "    \"date\": \"2025-11-13\",\n" +
                "    \"time\": \"10:15\",\n" +
                "    \"delivery\": { \"lng\": -3.192, \"lat\": 55.943 },\n" +
                "    \"requirements\": { \"capacity\": 7.0, \"cooling\": false, \"heating\": true, \"maxCost\": 30.0 }\n" +
                "  }\n" +
                "]";

        mockMvc.perform(post("/api/dispatch/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        String droneId = "1";
        mockMvc.perform(get("/api/dispatch/drone/{droneId}/status", droneId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetDroneStatus_NonExistingDrone() throws Exception {
        String droneId = "nonexistent_drone";

        mockMvc.perform(get("/api/dispatch/drone/{droneId}/status", droneId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetDroneStatus_InternalError() throws Exception {
        String droneId = "drone_error";

        mockMvc.perform(get("/api/dispatch/drone/{droneId}/status", droneId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    /**
     * System tests for GET /api/dispatch/status
     *
     * Calls controller directly to retrieve all drone statuses.
     */
    @Test
    void testGetAllDronesStatus_AfterSimulation() throws Exception {
        // Start simulation first
        String json = "[\n" +
                "  {\n" +
                "    \"id\": 1001,\n" +
                "    \"date\": \"2025-11-13\",\n" +
                "    \"time\": \"09:30\",\n" +
                "    \"delivery\": { \"lng\": -3.188, \"lat\": 55.946 },\n" +
                "    \"requirements\": { \"capacity\": 3.5, \"cooling\": true, \"heating\": false, \"maxCost\": 25.0 }\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": 1002,\n" +
                "    \"date\": \"2025-11-13\",\n" +
                "    \"time\": \"10:15\",\n" +
                "    \"delivery\": { \"lng\": -3.192, \"lat\": 55.943 },\n" +
                "    \"requirements\": { \"capacity\": 7.0, \"cooling\": false, \"heating\": true, \"maxCost\": 30.0 }\n" +
                "  }\n" +
                "]";

        mockMvc.perform(post("/api/dispatch/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        // Then query all drone statuses
        mockMvc.perform(get("/api/dispatch/status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /**
     * System tests for GET /api/dispatch/simulation-state
     *
     * Calls controller to check if the simulation is running.
     */
    @Test
    void testGetSimulationState_AfterSimulation() throws Exception {
        // Start simulation first
        String json = "[\n" +
                "  {\n" +
                "    \"id\": 1001,\n" +
                "    \"date\": \"2025-11-13\",\n" +
                "    \"time\": \"09:30\",\n" +
                "    \"delivery\": { \"lng\": -3.188, \"lat\": 55.946 },\n" +
                "    \"requirements\": { \"capacity\": 3.5, \"cooling\": true, \"heating\": false, \"maxCost\": 25.0 }\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": 1002,\n" +
                "    \"date\": \"2025-11-13\",\n" +
                "    \"time\": \"10:15\",\n" +
                "    \"delivery\": { \"lng\": -3.192, \"lat\": 55.943 },\n" +
                "    \"requirements\": { \"capacity\": 7.0, \"cooling\": false, \"heating\": true, \"maxCost\": 30.0 }\n" +
                "  }\n" +
                "]";

        mockMvc.perform(post("/api/dispatch/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        // Then query simulation state
        mockMvc.perform(get("/api/dispatch/simulation-state")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /**
     * System tests for /api/dispatch/restricted-areas endpoints
     *
     * Calls controller directly to test restricted areas functionality.
     */
    @Test
    void testGetRestrictedAreas() throws Exception {
        mockMvc.perform(get("/api/dispatch/restricted-areas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testAddRestrictedAreas() throws Exception {
        String json =
                "  {\n" +
                "    \"name\": \"George Square Area\",\n" +
                "    \"id\": 1,\n" +
                "    \"limits\": { \"lower\": 0, \"upper\": -1 },\n" +
                "    \"vertices\": [\n" +
                "      {\"lng\": -3.19057881832123, \"lat\": 55.9440241257753},\n" +
                "      {\"lng\": -3.18998873233795, \"lat\": 55.9428465054091},\n" +
                "      {\"lng\": -3.1870973110199,  \"lat\": 55.9432881172426},\n" +
                "      {\"lng\": -3.18768203258514, \"lat\": 55.9444777403937},\n" +
                "      {\"lng\": -3.19057881832123, \"lat\": 55.9440241257753}\n" +
                "    ]\n" +
                "  }";

        mockMvc.perform(post("/api/dispatch/restricted-areas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void testClearAllRestrictedAreas() throws Exception {
        mockMvc.perform(delete("/api/dispatch/restricted-areas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testRefreshRestrictedAreas() throws Exception {
        mockMvc.perform(post("/api/dispatch/restricted-areas/refresh")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /**
     * System tests for /api/dispatch/emergency endpoints
     *
     * Calls controller directly to test emergency dispatch functionality.
     */

    @Test
    void testHandleEmergencyOrder() throws Exception {
        // Start simulation first (simulate endpoint can be called here if needed)
        String simulateJson = "[\n" +
                "  {\n" +
                "    \"id\": 1001,\n" +
                "    \"date\": \"2025-11-13\",\n" +
                "    \"time\": \"09:30\",\n" +
                "    \"delivery\": { \"lng\": -3.188, \"lat\": 55.946 },\n" +
                "    \"requirements\": { \"capacity\": 3.5, \"cooling\": true, \"heating\": false, \"maxCost\": 25.0 }\n" +
                "  }\n" +
                "]";
        mockMvc.perform(post("/api/dispatch/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(simulateJson))
                .andExpect(status().isOk());

        // Emergency tasks JSON
        String emergencyJson = "{\n" +
                "  \"emergencyTasks\": [\n" +
                "    {\n" +
                "      \"id\": 2002,\n" +
                "      \"emergencyLevel\": 3,\n" +
                "      \"date\": \"2025-11-13\",\n" +
                "      \"time\": \"14:30\",\n" +
                "      \"requirements\": { \"capacity\": 4.5, \"maxCost\": 200 },\n" +
                "      \"delivery\": { \"lng\": -3.189, \"lat\": 55.945 }\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": 205,\n" +
                "      \"emergencyLevel\": 5,\n" +
                "      \"date\": \"2025-11-13\",\n" +
                "      \"time\": \"14:30\",\n" +
                "      \"requirements\": { \"capacity\": 4.5, \"maxCost\": 200 },\n" +
                "      \"delivery\": { \"lng\": -3.189, \"lat\": 55.943 }\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        // Test POST /emergency
        mockMvc.perform(post("/api/dispatch/emergency")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emergencyJson))
                .andExpect(status().isOk());
    }

    @Test
    void testHandleEmergencyOrderWithBypass() throws Exception {
        // Start simulation first
        String simulateJson = "[\n" +
                "  {\n" +
                "    \"id\": 1001,\n" +
                "    \"date\": \"2025-11-13\",\n" +
                "    \"time\": \"09:30\",\n" +
                "    \"delivery\": { \"lng\": -3.188, \"lat\": 55.946 },\n" +
                "    \"requirements\": { \"capacity\": 3.5, \"cooling\": true, \"heating\": false, \"maxCost\": 25.0 }\n" +
                "  }\n" +
                "]";
        mockMvc.perform(post("/api/dispatch/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(simulateJson))
                .andExpect(status().isOk());

        // Emergency tasks JSON with bypass flag
        String bypassJson = "{\n" +
                "  \"emergencyTasks\": [\n" +
                "    {\n" +
                "      \"id\": 2002,\n" +
                "      \"emergencyLevel\": 3,\n" +
                "      \"date\": \"2025-11-13\",\n" +
                "      \"time\": \"14:30\",\n" +
                "      \"requirements\": { \"capacity\": 4.5, \"maxCost\": 200 },\n" +
                "      \"delivery\": { \"lng\": -3.189, \"lat\": 55.945 }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"bypassRestrictedAreas\": true\n" +
                "}";

        // Test POST /emergency/with-bypass
        mockMvc.perform(post("/api/dispatch/emergency/with-bypass")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bypassJson))
                .andExpect(status().isOk());
    }


}
