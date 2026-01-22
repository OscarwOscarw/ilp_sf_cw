# DynamicDispatchControllerSystemTest

**Location:** `ilp_cw1.ilp_cw1_rset.system`

- **testSimulateDynamicPath_ValidRequest** – Verify that dynamic path simulation can successfully handle valid multi-task requests  
  **Validation:** Controller accepts compliant JSON and returns success response  
  **Scenario:** Standard request containing multiple complete task details

- **testSimulateDynamicPath_EmptyRequest** – Ensure empty task lists are properly rejected  
  **Validation:** Empty array triggers 400 error with specific message  
  **Scenario:** Boundary case with empty task array submission

- **testSimulateDynamicPath_MalformedJson** – Protect system from malformed JSON  
  **Validation:** Malformed JSON triggers 400 error  
  **Scenario:** JSON syntax errors or truncated data

- **testSimulateDynamicPath_LargeTaskList** – Test system stability with multiple tasks  
  **Validation:** Processing 6 tasks does not trigger errors  
  **Scenario:** Medium-scale task batch performance

- **testSimulateDynamicPath_TimeConflict** – Verify system accepts tasks at the same time  
  **Validation:** Tasks with overlapping times do not fail  
  **Scenario:** Fully overlapping task scheduling

- **testSimulateDynamicPath_InvalidCoordinates** – Ensure invalid geo-coordinates are rejected  
  **Validation:** Out-of-range latitude/longitude triggers 400 error  
  **Scenario:** Clearly invalid location data

- **testSimulateDynamicPath_DuplicateTaskId** – Prevent data conflicts from duplicate task IDs  
  **Validation:** Tasks with same ID trigger 400 error  
  **Scenario:** Submission with conflicting task IDs

- **testSimulateDynamicPath_ExcessiveCapacity** – Test error handling for unrealistic capacity requirements  
  **Validation:** Unreasonably high capacity triggers 500 error  
  **Scenario:** Capacity value far exceeds normal range

- **testSimulateDynamicPath_CrossDateTask** – Verify tasks with future dates are accepted  
  **Validation:** Dates from different years do not block processing  
  **Scenario:** Cross-year task scheduling

- **testSimulateDynamicPath_MissingDeliveryField** – Ensure missing required fields are detected  
  **Validation:** Missing `delivery` field triggers 400 error  
  **Scenario:** Incomplete task data structure

- **testTaskAfterClearingRestrictedAreas** – Test task processing after clearing restricted areas  
  **Validation:** Tasks execute normally after restrictions are cleared  
  **Scenario:** Continuity after system configuration changes

- **testHandleEmergencyOrder_NoSimulationRunning** – Verify emergency order handling when no simulation is running  
  **Validation:** System handles urgent requests independently  
  **Scenario:** Emergency scheduling independent of normal simulation

- **testHandleEmergencyOrder_EmptyTaskList** – Prevent empty emergency task lists  
  **Validation:** Empty emergency task array triggers 400 error  
  **Scenario:** Boundary case in emergency scheduling

- **testHandleEmergencyOrder_ValidTasks** – Test multi-emergency task handling  
  **Validation:** Multiple emergency tasks accepted (including failure verification)  
  **Scenario:** Compound emergency response

- **testHandleEmergencyOrder_LargeTaskList** – Test handling of large emergency task batches  
  **Validation:** 10 emergency tasks processed correctly  
  **Scenario:** Large-scale emergency events

- **testStopSimulation_Success** – Verify simulation stop functionality  
  **Validation:** Stop endpoint returns success message  
  **Scenario:** Normal stop flow

- **testStopSimulation_InternalError** – Test error recovery during stop operation  
  **Validation:** Stop operation does not fail due to internal state  
  **Scenario:** Repeated or abnormal stop attempts

- **testGetDronesStatus_AfterSimulation** – Verify drone status retrieval after simulation  
  **Validation:** Specific drone status can be queried after simulation (even if returns 404)  
  **Scenario:** Timing dependency of status queries

- **testGetDroneStatus_NonExistingDrone** – Test error handling for non-existing drone  
  **Validation:** Invalid drone ID returns 404  
  **Scenario:** Querying non-existent resource

- **testGetDroneStatus_InternalError** – Verify error recovery for status queries  
  **Validation:** Invalid ID triggers appropriate response  
  **Scenario:** Internal system error handling

- **testGetAllDronesStatus_AfterSimulation** – Test batch drone status retrieval  
  **Validation:** Status of all drones accessible after simulation  
  **Scenario:** Global status monitoring

- **testGetSimulationState_AfterSimulation** – Verify simulation state endpoint  
  **Validation:** Simulation state endpoint functions correctly  
  **Scenario:** System operational status check

- **testGetRestrictedAreas** – Verify retrieval of restricted areas  
  **Validation:** Restricted areas list successfully retrieved  
  **Scenario:** Area configuration query

- **testAddRestrictedAreas** – Test addition of restricted areas  
  **Validation:** New restricted areas successfully added  
  **Scenario:** Dynamic area configuration

- **testClearAllRestrictedAreas** – Verify clearing of restricted areas  
  **Validation:** All restricted areas can be cleared  
  **Scenario:** Configuration reset

- **testRefreshRestrictedAreas** – Test refresh mechanism for restricted areas  
  **Validation:** Refresh operation executes successfully  
  **Scenario:** Configuration update process

- **testHandleEmergencyOrder** – Verify complete emergency order handling  
  **Validation:** Emergency task inserted during running simulation  
  **Scenario:** Real-time scheduling interruption handling

- **testHandleEmergencyOrderWithBypass** – Test emergency scheduling bypassing restricted areas  
  **Validation:** Emergency request with bypass flag accepted  
  **Scenario:** Special-permission emergency operation


# ilpControllerSpecTest

**Location:** `ilp_cw1.ilp_cw1_rset.system`

- **testHealthEndpoint** – Verify application health monitoring  
  **Validation:** Health check endpoint returns UP  
  **Scenario:** Basic system availability

- **testUidEndpoint** – Ensure correct student UID is returned  
  **Validation:** UID endpoint returns preset identifier  
  **Scenario:** Basic authentication

- **testDistanceTo_ValidInput** – Verify core distance calculation  
  **Validation:** Valid coordinates return numeric distance  
  **Scenario:** Standard geodistance calculation

- **testDistanceTo_NullBody** – Prevent system error on empty request  
  **Validation:** Null request body triggers 400 error  
  **Scenario:** Missing request body handling

- **testDistanceTo_NullPosition1** – Ensure required coordinate validation  
  **Validation:** Missing first position triggers 400 error  
  **Scenario:** Incomplete coordinate data

- **testDistanceTo_NullPosition2** – Validate second coordinate requirement  
  **Validation:** Missing second position triggers 400 error  
  **Scenario:** Coordinate pair integrity check

- **testDistanceTo_Position1MissingLatitude** – Test coordinate completeness validation  
  **Validation:** Missing latitude triggers 400 error  
  **Scenario:** Partial coordinate handling

- **testDistanceTo_Position1MissingLongitude** – Validate longitude requirement  
  **Validation:** Missing longitude triggers 400 error  
  **Scenario:** Coordinate field integrity

- **testIsCloseTo_ValidInput** – Verify proximity check functionality  
  **Validation:** Valid input returns boolean result  
  **Scenario:** Standard proximity check

- **testIsCloseTo_NullBody** – Prevent empty proximity request  
  **Validation:** Null request body triggers 400 error  
  **Scenario:** Empty request handling

- **testIsCloseTo_EmptyBody** – Validate empty object handling  
  **Validation:** Empty JSON object triggers 400 error  
  **Scenario:** Invalid request format

- **testIsCloseTo_BothPositionsNull** – Test double null coordinate handling  
  **Validation:** Both positions null triggers 400 error  
  **Scenario:** Completely missing data

- **testNextPosition_ValidAngle** – Verify valid angle movement calculation  
  **Validation:** Multiples of 22.5° return new position  
  **Scenario:** Standard directional movement

- **testNextPosition_InvalidAngle** – Prevent invalid angles  
  **Validation:** Non-multiples of 22.5° trigger 400 error  
  **Scenario:** Illegal input validation

- **testNextPosition_NullStartPosition** – Ensure starting position is required  
  **Validation:** Null start position triggers 400 error  
  **Scenario:** Missing required parameter

- **testNextPosition_StartMissingLongitude** – Validate starting coordinate completeness  
  **Validation:** Missing starting longitude triggers 400 error  
  **Scenario:** Partial start data

- **testIsInRegion_ValidClosedPolygon** – Verify point-in-region functionality  
  **Validation:** Point inside closed polygon returns true  
  **Scenario:** Standard area detection

- **testIsInRegion_PositionWithNullLongitude** – Prevent null longitude coordinates  
  **Validation:** Null longitude triggers 400 error  
  **Scenario:** Coordinate validation

- **testIsInRegion_PositionWithNullLatitude** – Prevent null latitude coordinates  
  **Validation:** Null latitude triggers 400 error  
  **Scenario:** Coordinate completeness check

- **testIsInRegion_PositionWithBothNullCoordinates** – Test completely null coordinates  
  **Validation:** Null lat/lon triggers 400 error  
  **Scenario:** Fully invalid data

- **testIsInRegion_OpenPolygon** – Ensure polygon closure validation  
  **Validation:** Open polygon triggers 400 error  
  **Scenario:** Geometry integrity

- **testIsInRegion_NullBody** – Prevent null region request body  
  **Validation:** Null request body triggers 400 error  
  **Scenario:** Missing request data

- **testIsInRegion_NullPosition** – Ensure position for check is required  
  **Validation:** Null position triggers 400 error  
  **Scenario:** Required parameter check

- **testIsInRegion_NullRegion** – Prevent null region definition  
  **Validation:** Null region object triggers 400 error  
  **Scenario:** Missing region data

- **testIsInRegion_RegionMissingVertices** – Validate vertices required for region  
  **Validation:** Empty vertex list triggers 400 error  
  **Scenario:** Polygon integrity

- **testIsInRegion_InsufficientVertices** – Ensure minimum vertex count  
  **Validation:** Less than 4 vertices triggers 400 error  
  **Scenario:** Polygon complexity check

- **testIsInRegion_VertexMissingLongitude** – Validate vertex coordinate completeness  
  **Validation:** Missing longitude triggers 400 error  
  **Scenario:** Vertex data validation

- **testNextPosition_ValidAngleMultipleOf22_5** – Accept exact multiples of 22.5°  
  **Validation:** Accepted  
  **Scenario:** Boundary angle value

- **testNextPosition_AngleNormalizesToValid** – Test angle normalization  
  **Validation:** Angles >360° normalized and accepted  
  **Scenario:** Angle wrapping

- **testNextPosition_NegativeAngleNormalizesToValid** – Verify negative angle normalization  
  **Validation:** Negative angles normalized and accepted  
  **Scenario:** Negative angle handling

- **testNextPosition_AngleJustBelowValidWithEpsilon** – Test floating-point tolerance lower boundary  
  **Validation:** Near-valid value within tolerance accepted  
  **Scenario:** Numeric precision handling

- **testNextPosition_AngleJustAboveValidWithEpsilon** – Test floating-point tolerance upper boundary  
  **Validation:** Slightly above valid value within tolerance accepted  
  **Scenario:** Precision upper tolerance

- **testNextPosition_InvalidAngleNotMultiple** – Prevent non-multiple angles  
  **Validation:** Non-22.5° multiple rejected  
  **Scenario:** Invalid input detection

- **testNextPosition_AngleOutsideEpsilonTolerance** – Strict tolerance enforcement  
  **Validation:** Angle outside tolerance rejected  
  **Scenario:** Precision boundary enforcement

- **testNextPosition_NormalizedAngleInvalid** – Verify normalized angle invalidity  
  **Validation:** Still invalid after normalization rejected  
  **Scenario:** Angle transformation validation


# DroneControllerSystemTest

**Location:** `ilp_cw1.ilp_cw1_rset.system`

- **testDronesWithCooling_TrueState** – Verify drones with cooling query  
  **Validation:** True parameter returns valid drone list  
  **Scenario:** Filter by cooling attribute

- **testDronesWithCooling_FalseState** – Test non-cooling drone query  
  **Validation:** False parameter returns appropriate list  
  **Scenario:** Attribute negation filter

- **testDronesWithCooling_CaseInsensitive** – Ensure parameter case-insensitive  
  **Validation:** Mixed-case parameter parsed correctly  
  **Scenario:** Input flexibility

- **testDronesWithCooling_InvalidState** – Test recovery from invalid parameter  
  **Validation:** Invalid parameter returns empty array, not error  
  **Scenario:** Robustness boundary

- **testDronesWithCooling_EmptyState** – Validate empty parameter handling  
  **Validation:** Empty string returns empty array  
  **Scenario:** Edge input value

- **testDronesWithCooling_Performance** – Ensure query performance  
  **Validation:** 100 queries complete within 50 seconds  
  **Scenario:** Load performance benchmark

- **testDroneDetails_ExistingId** – Verify drone details retrieval  
  **Validation:** Existing ID returns full drone information  
  **Scenario:** Standard details query

- **testDroneDetails_NonExistingId** – Prevent system error on invalid ID  
  **Validation:** Non-existent ID returns 404 error  
  **Scenario:** Resource not found handling

- **testDroneDetails_EmptyId** – Test empty ID parameter  
  **Validation:** Empty ID triggers 400 error  
  **Scenario:** Required parameter validation

- **testDroneDetails_SpecialCharacters** – Verify handling of special character IDs  
  **Validation:** Special char ID returns 404, not 400  
  **Scenario:** Exceptional input handling

- **testDroneDetails_ResponseFormat** – Ensure response format consistency  
  **Validation:** Detail response contains standard fields  
  **Scenario:** API contract compliance

- **testQueryAsPath_ValidAttribute** – Verify basic path query  
  **Validation:** Valid attribute returns results  
  **Scenario:** Attribute filter

- **testQueryAsPath_InvalidAttribute** – Test recovery from invalid attribute  
  **Validation:** Invalid attribute returns empty array  
  **Scenario:** Unknown attribute handling

- **testQueryAsPath_NumericComparison** – Verify numeric comparison query  
  **Validation:** Numeric attribute comparison works  
  **Scenario:** Numeric filtering

- **testQueryAsPath_StringComparison** – Test string attribute query  
  **Validation:** String comparison works  
  **Scenario:** Text filtering

- **testQueryAsPath_BooleanComparison** – Verify boolean attribute query  
  **Validation:** Boolean filtering works  
  **Scenario:** Logical attribute filtering

- **testQueryAsPath_MalformedInput** – Prevent malformed path parameter  
  **Validation:** Empty path segment triggers 404  
  **Scenario:** URL format validation

- **testQueryDrones_SingleCondition** – Verify single-condition query  
  **Validation:** Single query condition returns result  
  **Scenario:** Basic composite query

- **testQueryDrones_MultipleConditions** – Test multiple-condition queries  
  **Validation:** Multiple conditions return results  
  **Scenario:** Complex filter logic

- **testQueryDrones_ConflictingConditions** – Ensure handling of conflicting conditions  
  **Validation:** Conflicting conditions return empty result  
  **Scenario:** Logical consistency check

- **testQueryDrones_EmptyQueryList** – Test empty query list  
  **Validation:** Empty array returns empty result  
  **Scenario:** Boundary query condition

- **testQueryDrones_NullQuery** – Prevent null query errors  
  **Validation:** Null query returns empty array  
  **Scenario:** Robustness on null input

- **testQueryDrones_ComplexOperatorCombinations** – Verify complex operator combinations  
  **Validation:** Mixed operator query works  
  **Scenario:** Advanced query

- **testQueryAvailableDrones_SingleTask** – Verify single-task drone availability  
  **Validation:** Returns available drones list  
  **Scenario:** Basic scheduling preparation

- **testQueryAvailableDrones_MultipleTasks** – Test multi-task concurrency  
  **Validation:** Multiple tasks return available drones  
  **Scenario:** Batch scheduling assessment

- **testQueryAvailableDrones_InvalidTask** – Ensure invalid task recovery  
  **Validation:** Invalid task parameters do not break system  
  **Scenario:** Data validation robustness

- **testQueryAvailableDrones_CapacityLimits** – Test capacity boundary handling  
  **Validation:** Extreme capacity handled correctly  
  **Scenario:** Numeric range boundary

- **testQueryAvailableDrones_TemperatureConflicts** – Verify heating/cooling conflict detection  
  **Validation:** Tasks requiring both handled  
  **Scenario:** Requirement conflict handling

- **testQueryAvailableDrones_TimeAvailability** – Test time conflict handling  
  **Validation:** Tasks at same time do not affect availability  
  **Scenario:** Scheduling logic

- **testCalcDeliveryPath_SingleTask** – Verify single-task path calculation  
  **Validation:** Single task returns delivery path  
  **Scenario:** Basic path planning

- **testCalcDeliveryPath_MultipleTasks** – Test multi-task path optimization  
  **Validation:** Multiple tasks return optimized path  
  **Scenario:** Batch path planning

- **testCalcDeliveryPath_NoAvailableDrones** – Handle no available drones gracefully  
  **Validation:** Returns appropriate response  
  **Scenario:** Resource shortage

- **testCalcDeliveryPath_RestrictedArea** – Verify restricted area path planning  
  **Validation:** Restricted areas affect path calculation  
  **Scenario:** Geographic constraints

- **testCalcDeliveryPath_CostOptimization** – Test cost optimization algorithm  
  **Validation:** Multi-task returns cost-optimized plan  
  **Scenario:** Economic planning verification

- **testCalcDeliveryPath_PerformanceBenchmark** – Ensure path calculation performance  
  **Validation:** 6 tasks computed within 5 seconds  
  **Scenario:** Performance benchmark

- **testCalcDeliveryPathAsGeoJson_ValidResponse** – Verify GeoJSON output  
  **Validation:** Returns valid GeoJSON structure  
  **Scenario:** Geographic data compatibility

- **testCalcDeliveryPathAsGeoJson_Structure** – Test GeoJSON structure integrity  
  **Validation:** Response contains standard GeoJSON fields  
  **Scenario:** Data contract verification

- **testCalcDeliveryPathAsGeoJson_Coordinates** – Ensure coordinate accuracy  
  **Validation:** Coordinates formatted correctly and precise  
  **Scenario:** Geographic precision

- **testCalcDeliveryPathAsGeoJson_EmptyResult** – Verify empty result handling  
  **Validation:** Empty tasks return empty JSON  
  **Scenario:** No-data boundary

- **testCalcDeliveryPathAsGeoJson_Performance** – Test GeoJSON generation performance  
  **Validation:** Batch GeoJSON generated within 5 seconds  
  **Scenario:** Formatting performance benchmark

- **testQueryAvailableDrones_TimeConflict** – Verify tasks with same time accepted  
  **Validation:** Fully overlapping tasks accepted  
  **Scenario:** Strict time overlap

- **testQueryAvailableDrones_InvalidCoordinates** – Test invalid coordinate recovery  
  **Validation:** Invalid coordinates do not crash system  
  **Scenario:** Geographic data validation

- **testQueryAvailableDrones_DuplicateTaskId** – Prevent duplicate task IDs  
  **Validation:** Duplicate ID tasks handled correctly  
  **Scenario:** ID conflict handling

- **testQueryAvailableDrones_ExcessiveCapacity** – Test extreme capacity requirements  
  **Validation:** Extreme capacity handled without crash  
  **Scenario:** Numeric overflow protection

- **testQueryAvailableDrones_EmptyJsonObject** – Prevent empty object requests  
  **Validation:** Empty JSON triggers 400  
  **Scenario:** Request format validation

- **testQueryAvailableDrones_InvalidFieldType** – Test field type validation  
  **Validation:** Wrong type triggers 400  
  **Scenario:** Data format strictness

- **testQueryAvailableDrones_HeatingCoolingConflict** – Verify heating/cooling conflict handling  
  **Validation:** Tasks needing both handled  
  **Scenario:** Requirement logic validation

- **testQueryAvailableDrones_MissingRequiredFields** – Test required field missing  
  **Validation:** Missing fields handled appropriately  
  **Scenario:** Data integrity check

- **testDroneDetails_SpecialCharsInId** – Verify special character IDs  
  **Validation:** Special char ID returns 404, not error  
  **Scenario:** Input sanitization

- **testQueryAsPath_BooleanCaseInsensitive** – Ensure boolean case-insensitive  
  **Validation:** Mixed-case boolean parsed correctly  
  **Scenario:** Input normalization

- **testQueryAsPath_NumericBoundary** – Test numeric boundary query  
  **Validation:** Zero-value query returns results  
  **Scenario:** Boundary value handling

- **testCalcDeliveryPathAsGeoJson_EmptyTasks** – Verify empty task GeoJSON  
  **Validation:** Empty array returns empty JSON  
  **Scenario:** Empty input formatting

- **testQueryDrones_NullAttributeInQuery** – Test null attribute in query  
  **Validation:** Null attribute returns empty result  
  **Scenario:** Query data completeness

- **testCalcDeliveryPath_LargeTaskBatch** – Ensure large batch performance  
  **Validation:** 50 tasks processed within reasonable time  
  **Scenario:** Scalability stress test
