# Integration Test Cases

## [CostEstimationIntegrationTest]
**Location:** `ilp_cw1.ilp_cw1_rset.integration.CostEstimationIntegrationTest.java`

- **testCostEstimation** – Verify cost estimation correctness for multi-drone task assignments  
  **Check:** Response contains valid total cost and number of moves, with cost in a reasonable range  
  **Scenario:** Use JSON test data to validate end-to-end task assignment cost estimation

---

## [DataRetrievalIntegrationTest]
**Location:** `ilp_cw1.ilp_cw1_rset.integration.DataRetrievalIntegrationTest.java`

- **shouldRetrieveAllDrones** – Verify retrieval of all drones from the live API  
  **Check:** Successfully retrieves a non-null drone list  
  **Scenario:** Connect to real API endpoint

- **shouldRetrieveDronesByIds** – Verify retrieval of drones filtered by a subset of IDs  
  **Check:** Returned list contains only drones with specified IDs  
  **Scenario:** Filter query using existing drone IDs

- **shouldRetrieveServicePoints** – Verify retrieval of all service points  
  **Check:** Successfully retrieves a non-null service point list  
  **Scenario:** Get service point data from API

- **shouldRetrieveAvailableDronesForServicePoints** – Verify retrieval of drone availability per service point  
  **Check:** Successfully retrieves drone availability for each service point  
  **Scenario:** Query service points with associated drone availability times

- **shouldRetrieveRestrictedAreas** – Verify retrieval of restricted areas  
  **Check:** Successfully retrieves a non-null list of restricted areas  
  **Scenario:** Get restricted flight area data from API and dynamic sources

- **shouldHandleEmptyDroneIdList** – Verify graceful handling of empty ID lists  
  **Check:** Returns empty result rather than throwing an exception  
  **Scenario:** Empty list used as filter

- **dataRetrievalShouldNotThrow** – Verify robustness of data retrieval operations  
  **Check:** Multiple data retrieval methods do not throw exceptions under normal use  
  **Scenario:** Sequentially call multiple data retrieval APIs

---

## [ErrorHandlingIntegrationTest]
**Location:** `ilp_cw1.ilp_cw1_rset.integration.ErrorHandlingIntegrationTest.java`

- **testNullInputHandling** – Verify graceful handling of null inputs  
  **Check:** Returns empty response with zero cost instead of throwing exception  
  **Scenario:** Call multi-drone solution calculation with all null parameters

- **testEmptyInputHandling** – Verify graceful handling of empty list inputs  
  **Check:** Returns empty response with zero cost  
  **Scenario:** Call calculation with all empty list parameters

- **testEmptyGeoJsonHandling** – Verify graceful handling of empty GeoJSON conversion  
  **Check:** Generates valid empty `FeatureCollection` for empty or null data  
  **Scenario:** Convert empty drone paths and restricted area lists to GeoJSON

- **testInvalidRestrictedAreas** – Verify graceful handling of invalid restricted areas  
  **Check:** Invalid polygons with empty vertex lists are ignored without exception  
  **Scenario:** Include invalid polygon areas in data conversion

---

## [GeoJsonConversionIntegrationTest]
**Location:** `ilp_cw1.ilp_cw1_rset.integration.GeoJsonConversionIntegrationTest.java`

- **testGeoJsonConversion** – Verify full conversion of drone paths and restricted areas to GeoJSON  
  **Check:** Generated GeoJSON conforms to spec, with correct geometry for all drones and restricted areas  
  **Scenario:** Use JSON test data to perform complete path planning and validate GeoJSON output

---

## [PathPlanningIntegrationTest]
**Location:** `ilp_cw1.ilp_cw1_rset.integration.PathPlanningIntegrationTest.java`

- **testTaskAssignmentIntegration** – Verify end-to-end multi-drone task assignment using local JSON data  
  **Check:** Path planning completes successfully with non-negative total cost and number of moves  
  **Scenario:** Full JSON test dataset validates end-to-end path planning workflow

---

## [PerformanceIntegrationTest]
**Location:** `ilp_cw1.ilp_cw1_rset.integration.PerformanceIntegrationTest.java`

- **testPathPlanningPerformance** – Verify performance requirements for path planning  
  **Check:** Complete path planning and GeoJSON conversion finish within 1 minute  
  **Scenario:** Performance stress test using full test dataset

---

## [QueryComparisonIntegrationTest]
**Location:** `ilp_cw1.ilp_cw1_rset.integration.QueryComparisonIntegrationTest.java`

- **testGetDronesByIds** – Verify query filtering drones by IDs  
  **Check:** Results contain only specified IDs with correct count  
  **Scenario:** Filter drones using two IDs

- **testGetServicePoints** – Verify retrieval of all service points  
  **Check:** Returns at least one service point  
  **Scenario:** Get all service points from test data

- **testCompareFiltering** – Verify correctness of filtered results  
  **Check:** Filtered drones exist in original drone list  
  **Scenario:** Compare filtered results with full dataset

- **testFilterEmptyIds** – Verify boundary case of empty ID list filtering  
  **Check:** Returns empty result instead of exception  
  **Scenario:** Filter with empty ID list

- **testFilterNonExistentIds** – Verify boundary case for non-existent IDs  
  **Check:** Returns empty result instead of exception  
  **Scenario:** Filter with fictitious IDs

- **testDroneCapabilitiesPresent** – Verify completeness of drone capability data  
  **Check:** At least one drone has cooling or heating capability  
  **Scenario:** Validate drone capability data

- **testServicePointsLocations** – Verify completeness of service point location data  
  **Check:** All service points have valid coordinates  
  **Scenario:** Check integrity of service point locations  
