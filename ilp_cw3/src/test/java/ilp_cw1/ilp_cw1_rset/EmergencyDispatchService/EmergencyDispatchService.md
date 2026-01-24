# Emergency Dispatch Service Test Cases

---

## PathCalculationTest

**Location:**  
`ilp_cw1.ilp_cw1_rset.EmergencyDispatchService.PathCalculationTest.java`

### PATH_001_shouldBypassRestrictedAreas_Level5Plus_ReturnsTrue
- **Verify:** Emergency levels 5 and above should bypass restricted areas
- **Check:** Tasks with emergency levels 5, 6, and 10 return true, indicating restricted areas should be bypassed
- **Scenario:** Bypass check invoked for tasks with emergency levels 5, 6, and 10

### PATH_002_shouldBypassRestrictedAreas_LevelBelow5_ReturnsFalse
- **Verify:** Emergency levels below 5 should not bypass restricted areas
- **Check:** Tasks with emergency levels 1–4 return false, indicating restricted areas must be respected
- **Scenario:** Bypass check invoked for low emergency level tasks (1–4)

### PATH_003_calculateDirectPath_ValidPoints_ReturnsInterpolatedPath
- **Verify:** Direct path calculation between two distinct points
- **Check:** Returns a path containing start and end points with interpolated points in between (more than two points)
- **Scenario:** Calculate direct flight path between two different coordinates

### PATH_004_calculateDirectPath_SamePoints_ReturnsPathWithTwoPoints
- **Verify:** Direct path calculation for identical points
- **Check:** Returns a path containing duplicated start and end points
- **Scenario:** Calculate a flight path from a point to itself

### PATH_005_calculateDirectPath_VeryClosePoints_ReturnsPath
- **Verify:** Path calculation for very close points
- **Check:** Generates a valid path even when the distance between points is extremely small
- **Scenario:** Calculate a flight path between two very close coordinates

### PATH_006_isPathReachable_ReachablePath_ReturnsTrue
- **Verify:** Reachable path detection
- **Check:** Returns true when the path contains the target point
- **Scenario:** Check reachability of a path that reaches the target

### PATH_007_isPathReachable_UnreachablePath_ReturnsFalse
- **Verify:** Unreachable path detection
- **Check:** Returns false when the path does not reach the target point
- **Scenario:** Check reachability of a path that does not reach the target

### PATH_008_isPathReachable_EmptyPath_ReturnsFalse
- **Verify:** Empty path handling
- **Check:** Returns false for an empty path
- **Scenario:** Check reachability for an empty path

### PATH_009_calculateDistance_TwoPoints_ReturnsEuclideanDistance
- **Verify:** Euclidean distance calculation
- **Check:** Correctly calculates distance using the Pythagorean theorem
- **Scenario:** Calculate distance between points (0,0) and (3,4)

### PATH_010_calculateDistance_SamePoint_ReturnsZero
- **Verify:** Distance calculation for identical points
- **Check:** Returns zero distance for the same point
- **Scenario:** Calculate distance from a point to itself

### PATH_011_getBlockingRestrictedAreaName_IntersectsArea_ReturnsName
- **Verify:** Detection of blocking restricted area
- **Check:** Returns the restricted area name when the path intersects a restricted area
- **Scenario:** Path segment intersects a restricted area

### PATH_012_getBlockingRestrictedAreaName_NoIntersection_ReturnsUnknown
- **Verify:** No blocking restricted area detection
- **Check:** Returns `"Unknown restricted area"` when no restricted area intersects the path
- **Scenario:** Path does not intersect any restricted areas

### PATH_013_getBlockingRestrictedAreaName_NullAreaName_ReturnsUnknown
- **Verify:** Handling of restricted area with null name
- **Check:** Returns `"Unknown restricted area"` when the restricted area name is null
- **Scenario:** Path intersects a restricted area without a name

### PATH_014_initializeIdleDroneState_SimulationRunning_AddsDroneState
- **Verify:** Idle drone state initialization during running simulation
- **Check:** Calls `addDroneState` when initializing an idle drone while simulation is running
- **Scenario:** Initialize idle drone state during an active simulation

### PATH_015_initializeIdleDroneState_SimulationNotRunning_StartsSimulation
- **Verify:** Idle drone initialization when simulation is not running
- **Check:** Starts a new simulation when initializing an idle drone with no active simulation
- **Scenario:** Initialize idle drone when no simulation is running

### PATH_016_calculateEmergencyDeliveryPath_Level5_BypassesRestrictedAreas
- **Verify:** Emergency level 5 bypasses restricted areas
- **Check:** Uses direct path calculation without invoking A* algorithm
- **Scenario:** Calculate delivery path for high emergency level task

### PATH_017_calculateEmergencyDeliveryPath_Level3_RespectsRestrictedAreas
- **Verify:** Emergency level 3 respects restricted areas
- **Check:** Invokes A* algorithm to avoid restricted areas
- **Scenario:** Calculate delivery path for low emergency level task

### PATH_018_calculateEmergencyDeliveryPath_ForceBypass_IgnoresLevel
- **Verify:** Force bypass flag handling
- **Check:** Uses direct path calculation when `forceBypass` is true, ignoring emergency level
- **Scenario:** Calculate delivery path with force bypass enabled

---

## BlockageDetectionTest

**Location:**  
`ilp_cw1.ilp_cw1_rset.EmergencyDispatchService.BlockageDetectionTest.java`

### BLOCK_001_calculateEmergencyDeliveryPath_BlockedPath_ThrowsException
- **Verify:** Exception thrown for fully blocked path
- **Check:** Throws `RestrictedAreaBlockageException` when A* returns an empty path
- **Scenario:** Path completely blocked by restricted areas

### BLOCK_002_calculateEmergencyDeliveryPath_UnreachablePath_ThrowsException
- **Verify:** Exception thrown for unreachable path
- **Check:** Throws exception when the returned path does not reach the target point
- **Scenario:** Path does not reach target location

### BLOCK_003_calculateEmergencyDeliveryPath_PathOnBoundary_ThrowsException
- **Verify:** Boundary condition handling
- **Check:** Throws exception when path points lie inside restricted areas
- **Scenario:** Path points lie on restricted area boundary

### BLOCK_004_calculateEmergencyDeliveryPath_Level1to4_RequiresHumanConfirmation
- **Verify:** Human confirmation required for low emergency levels
- **Check:** Blockage exceptions for emergency levels 1–4 are marked as requiring human confirmation
- **Scenario:** Blocked paths for different emergency levels

### BLOCK_005_calculateEmergencyDeliveryPath_Level5Plus_UsesDirectPath
- **Verify:** High emergency levels use direct path
- **Check:** Emergency levels 5 and above use direct path without invoking A*
- **Scenario:** High emergency level task encounters restricted area

### BLOCK_006_buildAndDispatchResult_RestrictedAreaException_ReturnsFailure
- **Verify:** Restricted area exception handling
- **Check:** Returns failure result when path calculation throws an exception
- **Scenario:** Path blockage occurs during dispatch result construction

### BLOCK_007_buildAndDispatchResultWithBypass_RestrictedAreaException_ReturnsFailure
- **Verify:** Restricted area exception handling with bypass disabled
- **Check:** Returns failure result when bypass is false and path calculation fails
- **Scenario:** Path blockage without bypass enabled

### BLOCK_008_buildAndDispatchResultWithBypass_BypassTrue_Succeeds
- **Verify:** Successful dispatch with forced bypass
- **Check:** Successfully builds dispatch result when bypass is true
- **Scenario:** Dispatch with forced bypass despite restricted areas

### BLOCK_009_initializeIdleDroneState_WorksCorrectly
- **Verify:** Idle drone state initialization
- **Check:** Calls `startSimulation` when initializing idle drone state
- **Scenario:** Initialize idle drone state

---

## CostCalculationTest

**Location:**  
`ilp_cw1.ilp_cw1_rset.EmergencyDispatchService.CostCalculationTest.java`

### COST_001_calculateInterruptCostsForCandidates_MovingDrone_CalculatesCost
- **Verify:** Interrupt cost calculation for moving drones
- **Check:** Correctly calculates interrupt cost for drones in MOVING state
- **Scenario:** Calculate interrupt costs for moving candidate drones

### COST_002_calculateInterruptCostsForCandidates_IdleDrone_ReturnsEmpty
- **Verify:** Idle drone interrupt cost handling
- **Check:** Returns empty cost map for idle drones
- **Scenario:** Calculate interrupt costs for idle drones

### COST_003_calculateInterruptCostsForCandidates_DroneNotMoving_Skips
- **Verify:** Skip non-moving drones in cost calculation
- **Check:** Skips interrupt cost calculation for drones in READY state
- **Scenario:** Calculate costs for non-moving drones

### COST_004_calculateInterruptCostsForCandidates_ExceptionHandling_ReturnsDefaultCost
- **Verify:** Exception handling in cost calculation
- **Check:** Returns default cost (1500.0) when exception occurs
- **Scenario:** Exception occurs during cost calculation

### COST_005_calculateInterruptCostsForCandidates_InvalidCost_ReturnsDefault
- **Verify:** Invalid cost value handling
- **Check:** Returns default cost (1000.0) when cost is NaN
- **Scenario:** Cost calculation returns invalid value

### COST_006_calculateReassignCosts_ReturnsMultipliedCosts
- **Verify:** Reassign cost calculation
- **Check:** Reassign costs are calculated as interrupt cost multiplied by 1.5
- **Scenario:** Calculate reassign costs for drones

### COST_007_calculateReassignCosts_EmptyInput_ReturnsEmpty
- **Verify:** Empty input handling for reassign cost calculation
- **Check:** Returns empty map when interrupt cost input is empty
- **Scenario:** No interrupt costs provided

### COST_008_estimateInterruptCost_ValidParameters_CalculatesCost
- **Verify:** Interrupt cost estimation
- **Check:** Correctly calculates interrupt cost using defined formula
- **Scenario:** Detailed interrupt cost estimation for moving drone

### COST_009_estimateInterruptCost_NullCapability_UsesDefaultCostPerMove
- **Verify:** Default cost handling for null capability
- **Check:** Uses default cost per move (1.0) when drone capability is null
- **Scenario:** Cost estimation for drone without capability information

### COST_010_selectOptimalDrone_ViableDrone_ReturnsLowestCost
- **Verify:** Optimal drone selection
- **Check:** Returns the viable drone with the lowest reassign cost
- **Scenario:** Select optimal drone from multiple candidates

### COST_011_selectOptimalDrone_NoViable_ReturnsNull
- **Verify:** No viable drone selection
- **Check:** Returns null when all reassign costs exceed constraints
- **Scenario:** No viable drones available

### COST_012_selectOptimalDrone_EmptyCandidates_ReturnsNull
- **Verify:** Empty candidate handling
- **Check:** Returns null when candidate list is empty
- **Scenario:** No candidate drones provided

### COST_013_selectOptimalDrone_DroneMissingFromCostMaps_Skips
- **Verify:** Missing cost map handling
- **Check:** Skips drones missing from cost maps during selection
- **Scenario:** Some drones lack cost information

---

## DroneCandidateSelectionTest

**Location:**  
`ilp_cw1.ilp_cw1_rset.EmergencyDispatchService.DroneCandidateSelectionTest.java`

### CAND_001_findCandidateDrones_MeetsRequirements_ReturnsList
- **Verify:** Candidate drone filtering based on requirements
- **Check:** Returns a list of drones that meet emergency task requirements
- **Scenario:** Filter candidates from drones with mixed capabilities

### CAND_002_findCandidateDrones_AllMeetRequirements_ReturnsAll
- **Verify:** All drones meet requirements
- **Check:** Returns all drones when all meet task requirements
- **Scenario:** All drones satisfy emergency task requirements

### CAND_003_findCandidateDrones_NoneMeetRequirements_ReturnsEmpty
- **Verify:** No drones meet requirements
- **Check:** Returns empty list when no drones meet task requirements
- **Scenario:** All drones fail to meet emergency task requirements

### CAND_004_canDroneHandleEmergency_CapabilityMatch_ReturnsTrue
- **Verify:** Capability matching
- **Check:** Returns true when drone capabilities fully match task requirements
- **Scenario:** Drone with matching capabilities

### CAND_005_canDroneHandleEmergency_CapacityExceeded_ReturnsFalse
- **Verify:** Capacity constraint handling
- **Check:** Returns false when drone capacity is insufficient
- **Scenario:** Drone capacity lower than task requirement

### CAND_006_canDroneHandleEmergency_TemperatureMismatch_ReturnsFalse
- **Verify:** Temperature control mismatch handling
- **Check:** Returns false when drone lacks required temperature control
- **Scenario:** Cooling/heating mismatch

### CAND_007_canDroneHandleEmergency_DroneWithoutCapability_ReturnsFalse
- **Verify:** Missing capability handling
- **Check:** Returns false when drone has no capability information
- **Scenario:** Drone without capability data

### CAND_008_isDroneAvailable_AvailableDrone_ReturnsTrue
- **Verify:** Drone availability check
- **Check:** Returns true for drones with available time slots
- **Scenario:** Available drone

### CAND_009_isDroneAvailable_UnavailableDrone_ReturnsFalse
- **Verify:** Unavailable drone handling
- **Check:** Returns false for unavailable or non-existent drones
- **Scenario:** Unavailable drone

### CAND_010_isDroneAvailable_EmptyAvailability_ReturnsFalse
- **Verify:** Empty availability handling
- **Check:** Returns false when availability list is empty
- **Scenario:** Drone with no availability

### CAND_011_findIdleDrone_IdleExists_ReturnsDrone
- **Verify:** Idle drone identification
- **Check:** Finds a drone not currently assigned tasks
- **Scenario:** Mixed busy and idle drones

### CAND_012_findIdleDrone_AllBusy_ReturnsNull
- **Verify:** All drones busy
- **Check:** Returns null when all candidate drones are busy
- **Scenario:** All drones busy

### CAND_013_findIdleDrone_EmptyCandidates_ReturnsNull
- **Verify:** Empty candidate handling
- **Check:** Returns null when candidate list is empty
- **Scenario:** No candidate drones

---

## EmergencyProcessingTest

**Location:**  
`ilp_cw1.ilp_cw1_rset.EmergencyDispatchService.EmergencyProcessingTest.java`

### EMER_PROC_001_processEmergencyRequest_ValidRequest_ReturnsResult
- **Verify:** Processing of valid emergency request
- **Check:** Returns success result containing drone ID and cost
- **Scenario:** Process a valid emergency task request

### EMER_PROC_002_processEmergencyRequest_EmptyTasks_ReturnsError
- **Verify:** Empty emergency task list handling
- **Check:** Returns failure result with error message containing `"No emergency tasks"`
- **Scenario:** Process request with no emergency tasks

### EMER_PROC_003_processEmergencyRequest_NullRequest_ThrowsException
- **Verify:** Null request handling
- **Check:** Throws `NullPointerException` for null request
- **Scenario:** Process null emergency request

### EMER_PROC_004_processEmergencyRequest_NoCandidateDrones_ReturnsError
- **Verify:** No candidate drones handling
- **Check:** Returns failure result with error message containing `"No drones found"`
- **Scenario:** Process emergency request with no available drones

### EMER_PROC_005_processEmergencyRequest_MultipleTasks_ProcessesFirst
- **Verify:** Multiple emergency tasks handling
- **Check:** Processes only the first emergency task
- **Scenario:** Process request containing multiple emergency tasks

### EMER_PROC_006_processEmergencyRequestWithBypass_BypassFlag_Respected
- **Verify:** Bypass flag handling
- **Check:** Successfully processes request when bypass flag is true
- **Scenario:** Emergency request with bypass enabled

### EMER_PROC_007_processEmergencyRequestWithBypass_NoBypass_NormalProcessing
- **Verify:** Normal processing without bypass
- **Check:** Processes emergency request normally when bypass flag is false
- **Scenario:** Emergency request without bypass
