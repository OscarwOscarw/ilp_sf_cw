# Drone Service Test Cases

## [GeoJSONConverterTest]
**Location:** `ilp_cw1.ilp_cw1_rset.Droneservice.GeoJSONConverterTest.java`

- **GEO_001_emptyData_returnsEmptyFeatureCollection** – Verify correct GeoJSON structure with empty data  
  **Check:** Returns an empty `FeatureCollection` with no geographic features  
  **Scenario:** Empty drone path list and empty restricted area list

- **GEO_002_singleDronePath_returnsLineStringFeature** – Verify conversion of a single drone path to a LineString feature  
  **Check:** GeoJSON contains a `LineString` feature with the correct drone ID attribute  
  **Scenario:** Single drone with a path consisting of two points

- **GEO_003_multipleDronePaths_returnsMultipleLineStringFeatures** – Verify batch conversion for multiple drone paths  
  **Check:** GeoJSON contains a number of LineString features matching the input paths  
  **Scenario:** Three drones' paths converted simultaneously

- **GEO_004_withRestrictedArea_returnsPolygonFeature** – Verify conversion of restricted areas to Polygon features  
  **Check:** GeoJSON contains both LineString (paths) and Polygon (restricted areas) features  
  **Scenario:** Drone paths and square restricted areas exist simultaneously

- **GEO_005_invalidPathData_skippedInGeoJson** – Verify handling of invalid path data  
  **Check:** Single-point paths are skipped, returning an empty feature set  
  **Scenario:** Drone path contains only one point (not a valid line)

- **GEO_006_largeData_performanceTest** – Verify performance with large datasets  
  **Check:** Conversion of a 100-point path completes within 100 ms  
  **Scenario:** Performance test for generating GeoJSON from long paths

---

## [HandleTaskTest]
**Location:** `ilp_cw1.ilp_cw1_rset.Droneservice.HandleTaskTest.java`

- **canDroneHandleTask_AllRequirementsMet_ReturnsTrue** – Verify correct handling when all task requirements are met  
  **Check:** Returns `true` when capacity, temperature control, and availability are sufficient  
  **Scenario:** Drone has cooling capability, enough capacity, and is available during task time

- **canDroneHandleTask_CapacityExceeded_ReturnsFalse** – Verify rejection when capacity is insufficient  
  **Check:** Returns `false` if drone capacity is less than task requirement  
  **Scenario:** Drone capacity 10kg, task requirement 15kg

- **canDroneHandleTask_TemperatureMismatch_ReturnsFalse** – Verify handling when temperature control requirement is unmet  
  **Check:** Returns `false` if drone lacks required cooling or heating  
  **Scenario:** Task requires cooling but drone does not have it

- **canDroneHandleTask_DroneNotAvailableAtTime_ReturnsFalse** – Verify handling when drone is unavailable at task time  
  **Check:** Returns `false` if drone is not available during task time  
  **Scenario:** Drone schedule does not match task time

- **canDroneHandleTaskWithMoves_ExceedsMaxMoves_ReturnsFalse** – Verify handling when maximum moves exceeded  
  **Check:** Returns `false` if path distance exceeds drone's maximum moves  
  **Scenario:** Task location is too far, exceeding drone movement capability

---

## [MultiTaskAssignTest]
**Location:** `ilp_cw1.ilp_cw1_rset.Droneservice.MultiTaskAssignTest.java`

- **MTA_001_emptyInput_returnsEmptyResponse** – Verify handling of empty input  
  **Check:** Returns an empty response object when all inputs are empty  
  **Scenario:** Empty drone list, task list, location mapping, and availability info

- **MTA_002_singleDrone_singleTask_assigned** – Verify basic single-drone single-task assignment  
  **Check:** Single task is successfully assigned to a single drone  
  **Scenario:** One drone and one matching task

- **MTA_003_multiDrone_singleTask_assignToCapable** – Verify selection of capable drone among multiple drones  
  **Check:** Task assigned to drone with sufficient capacity, not insufficient ones  
  **Scenario:** Two drones (5kg and 10kg capacity), task requires 8kg

- **MTA_004_multiDrone_multiTask_allAssigned** – Verify complex multi-drone multi-task assignment  
  **Check:** All tasks correctly assigned to drones according to capability  
  **Scenario:** Two drones with different temperature control, three tasks with different requirements

- **MTA_005_capacityConstraint_unassignable_returnsEmpty** – Verify handling when no drone can fulfill task  
  **Check:** Returns empty result if no drone meets task requirements  
  **Scenario:** Drone capacity 5kg, task requirement 10kg

---

## [SingleDronePathTest]
**Location:** `ilp_cw1.ilp_cw1_rset.Droneservice.SingleDronePathTest.java`

- **calculateSingleDronePath_SingleTask_ReturnsValidPath** – Verify path calculation for single drone and single task  
  **Check:** Generated path includes valid round-trip route between service point and task location  
  **Scenario:** Drone flies from service point to task location and returns

- **calculateSingleDronePath_ExceedsMaxMoves_ThrowsException** – Verify exception handling when moves exceed limit  
  **Check:** Throws exception if path length exceeds drone's maximum moves  
  **Scenario:** Calculated path has too many points, exceeding drone movement limit

---

## [APItest]
**Location:** `ilp_cw1.ilp_cw1_rset.Droneservice.APItest.java`

- **getAllDrones_NormalCase_ReturnsDronesList** – Verify normal API call returns correct drone list  
  **Check:** Successfully retrieves and parses drone data from API  
  **Scenario:** API returns valid data for two drones

- **getAllDrones_EmptyResponse_ReturnsEmptyList** – Verify handling of empty API response  
  **Check:** Returns empty list if API returns an empty array  
  **Scenario:** API returns empty drone array

- **getAllDrones_ApiException_ReturnsEmptyList** – Verify fault tolerance on API exception  
  **Check:** Returns empty list instead of throwing an exception if API fails  
  **Scenario:** API connection fails or service unavailable

---

## [CompareTest]
**Location:** `ilp_cw1.ilp_cw1_rset.Droneservice.CompareTest.java`

- **getAttributeValue_VariousAttributes_ReturnsCorrectValues** – Verify extraction of drone attributes  
  **Check:** Correctly retrieves different types of attributes (ID, capacity, cooling capability)  
  **Scenario:** Query multiple attribute fields from drone object

- **compareByType_DifferentDataTypes_ComparesCorrectly** – Verify correctness of cross-type comparisons  
  **Check:** Numbers, booleans, and strings are correctly compared across types  
  **Scenario:** Equality checks between numbers and strings, booleans and strings

- **compareWithOperator_AllOperators_WorksCorrectly** – Verify functionality of all comparison operators  
  **Check:** Greater than, less than, equal, not equal, etc., all work correctly  
  **Scenario:** Different data types compared using various operators  
