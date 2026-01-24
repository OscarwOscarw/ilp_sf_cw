# Dynamic Dispatch Service Test Cases

---

## SimulationManagementTest

**Location:**  
`ilp_cw1.ilp_cw1_rset.DynamicDispatchService.SimulationManagementTest.java`

### SIM_001_startSimulation_ValidResponse_InitializesStates
- **Verify:** Correct state initialization when starting the simulation
- **Check:** After starting the simulation with a valid response, the simulation status is running and drone states are initialized
- **Scenario:** Start simulation with a valid `DeliveryPathResponse`

### SIM_002_startSimulation_EmptyResponse_ThrowsException
- **Verify:** Exception handling for empty response
- **Check:** Throws `IllegalArgumentException` when the drone path list in the response is empty
- **Scenario:** `DeliveryPathResponse` with empty `DronePaths`

### SIM_003_startSimulation_Idempotent_CallsStopFirst
- **Verify:** Idempotency of simulation start
- **Check:** When starting the simulation repeatedly, the current simulation is stopped before a new one starts, ensuring correct state replacement
- **Scenario:** Start a simulation, then start another one and verify new drone states exist while old states are cleared

### SIM_004_stopSimulation_StopsSchedulerAndClearsStates
- **Verify:** Simulation stop functionality
- **Check:** After stopping the simulation, the simulation status becomes not running and the task scheduler is stopped
- **Scenario:** Start the simulation and then execute the stop operation, verifying state cleanup

---

## StatusRetrievalTest

**Location:**  
`ilp_cw1.ilp_cw1_rset.DynamicDispatchService.StatusRetrievalTest.java`

### STATUS_001_getOngoingTasks_ReturnsCorrectTaskMapping
- **Verify:** Correct mapping of ongoing tasks
- **Check:** Returned task mapping contains correct drone-to-task relationships, with matching task counts and IDs
- **Scenario:** Simulation where a single drone has three tasks

### STATUS_002_getOngoingTasks_WithEmergencyTask_IncludesEmergency
- **Verify:** Inclusion of emergency tasks in task list
- **Check:** After inserting an emergency task, the task list contains the emergency task with correct priority
- **Scenario:** Insert an emergency task during simulation and verify it appears at the front of the task list

### STATUS_003_getOngoingTasks_MultipleDrones_ReturnsAll
- **Verify:** Retrieval of tasks for multiple drones
- **Check:** Returns task mappings for all drones in a multi-drone simulation
- **Scenario:** Simulation with two drones, each having different tasks

### STATUS_004_getOngoingTasks_NoSimulation_ReturnsEmptyMap
- **Verify:** Behavior when no simulation is running
- **Check:** Returns an empty map when retrieving ongoing tasks without an active simulation
- **Scenario:** Call task retrieval API when no simulation is running

### STATUS_005_estimateInterruptCost_ValidDrone_ReturnsCost
- **Verify:** Interrupt cost estimation for valid drone
- **Check:** Returns a reasonable cost value (between 0 and 1) for a valid drone ID
- **Scenario:** Estimate interrupt cost for a drone that currently has tasks

### STATUS_006_estimateInterruptCost_InvalidDrone_ReturnsZero
- **Verify:** Interrupt cost handling for invalid drone
- **Check:** Returns zero cost for an invalid drone ID
- **Scenario:** Estimate interrupt cost for a non-existent drone

### STATUS_007_isSimulationRunning_Initial_False
- **Verify:** Initial simulation state
- **Check:** Simulation state is not running after service initialization
- **Scenario:** Check simulation state immediately after service creation

### STATUS_008_isSimulationRunning_AfterStart_True
- **Verify:** Simulation state after start
- **Check:** Simulation state becomes running after starting the simulation
- **Scenario:** Start simulation and immediately check status

### STATUS_009_isSimulationRunning_AfterStop_False
- **Verify:** Simulation state after stop
- **Check:** Simulation state becomes not running after stopping the simulation
- **Scenario:** Start simulation, stop it, and verify state change

---

## DroneStateManagementTest

**Location:**  
`ilp_cw1.ilp_cw1_rset.DynamicDispatchService.DroneStateManagementTest.java`

### STATE_001_addDroneState_IdleDrone_AddsSuccessfully
- **Verify:** Adding idle drone state
- **Check:** Successfully adds a new drone state during a running simulation and the new state is retrievable
- **Scenario:** Add a new idle drone state while a simulation is running

### STATE_002_addDroneState_ExistingDrone_NoDuplication
- **Verify:** Handling duplicate drone state addition
- **Check:** Does not create duplicate states when adding an existing drone ID
- **Scenario:** Attempt to add a drone state that already exists

### STATE_003_addDroneState_SimulationNotRunning_StartsSimulation
- **Verify:** Automatic simulation start when adding drone state
- **Check:** Automatically starts the simulation and adds the drone state when no simulation is running
- **Scenario:** Add a drone state while simulation is not running

### STATE_004_getCurrentDroneStatus_ValidDrone_ReturnsStatus
- **Verify:** Retrieval of valid drone status
- **Check:** Returns complete status information including position and state
- **Scenario:** Query the status of an existing drone during simulation

### STATE_005_getCurrentDroneStatus_InvalidDrone_ReturnsNull
- **Verify:** Retrieval of invalid drone status
- **Check:** Returns null for an invalid drone ID
- **Scenario:** Query the status of a non-existent drone

### STATE_006_getAllCurrentDroneStatuses_ReturnsAllStatuses
- **Verify:** Retrieval of all drone statuses
- **Check:** Returned list contains status information for all drones
- **Scenario:** Retrieve all drone statuses in a multi-drone simulation

### STATE_007_getAllCurrentDroneStatuses_EmptySimulation_ReturnsEmptyList
- **Verify:** Status retrieval with no simulation
- **Check:** Returns an empty list when no simulation is running
- **Scenario:** Call status retrieval API without starting simulation

---

## EmergencyTaskInsertionTest

**Location:**  
`ilp_cw1.ilp_cw1_rset.DynamicDispatchService.EmergencyTaskInsertionTest.java`

### EMER_001_insertEmergencyTask_ValidDrone_TaskInserted
- **Verify:** Inserting emergency task into valid drone
- **Check:** Emergency task is successfully inserted and the drone state is marked as handling an emergency
- **Scenario:** Insert an emergency task into a running drone

### EMER_002_insertEmergencyTask_InvalidDrone_ThrowsException
- **Verify:** Emergency task insertion for invalid drone
- **Check:** Throws `NoSuchElementException` when inserting an emergency task into a non-existent drone
- **Scenario:** Insert emergency task into a non-existent drone

### EMER_003_insertEmergencyTask_NullTask_ThrowsException
- **Verify:** Handling of null task insertion
- **Check:** Throws `IllegalArgumentException` when inserting a null task
- **Scenario:** Insert a null task into a drone

### EMER_004_insertEmergencyTask_NullTaskId_ThrowsException
- **Verify:** Handling of emergency task without ID
- **Check:** Throws `IllegalArgumentException` when task ID is null
- **Scenario:** Insert an emergency task with a null ID

### EMER_005_insertEmergencyTask_MultipleDrones_OnlyAffectsTarget
- **Verify:** Emergency task isolation in multi-drone simulation
- **Check:** Emergency task affects only the target drone, while other drones remain unchanged
- **Scenario:** Insert emergency task for one drone in a two-drone simulation

---

## RestrictedAreaTest

**Location:**  
`ilp_cw1.ilp_cw1_rset.DynamicDispatchService.RestrictedAreaTest.java`

### AREA_001_refreshRestrictedAreas_SuccessfulRefresh
- **Verify:** Successful restricted area refresh
- **Check:** Refresh operation invokes service API and updates restricted area data
- **Scenario:** Refresh restricted areas with valid service response

### AREA_002_refreshRestrictedAreas_Exception_FallbackEmpty
- **Verify:** Fallback handling on service exception
- **Check:** Does not throw exception and falls back to using an empty list
- **Scenario:** Refresh restricted areas when service API is unavailable

### AREA_003_refreshRestrictedAreas_NullResponse_FallbackEmpty
- **Verify:** Handling of null service response
- **Check:** Processes null response gracefully and uses an empty list
- **Scenario:** Refresh restricted areas when service returns null

### AREA_004_isPointInPolygon_PointInside_ReturnsTrue
- **Verify:** Point-in-polygon detection
- **Check:** Returns true when the point lies inside the polygon (tested via public API or reflection)
- **Scenario:** Point located inside a square polygon

### AREA_005_isPointInPolygon_PointOutside_ReturnsFalse
- **Verify:** Point-outside-polygon detection
- **Check:** Returns false when the point lies outside the polygon
- **Scenario:** Point located outside a square polygon

### AREA_006_isPointOnSegment_PointOnLine_ReturnsTrue
- **Verify:** Point-on-segment detection
- **Check:** Returns true when the point lies on the line segment
- **Scenario:** Point located on the middle of a line segment

### AREA_007_ServiceInitialization_LoadsRestrictedAreas
- **Verify:** Restricted area loading during service initialization
- **Check:** Restricted area data is automatically loaded when the service is created
- **Scenario:** Verify restricted area loading after service instantiation

### AREA_008_MultipleRefreshCalls_WorksCorrectly
- **Verify:** Multiple refresh operations
- **Check:** Multiple refresh calls work correctly and invoke the service API each time
- **Scenario:** Refresh restricted areas twice and verify correct service call count
