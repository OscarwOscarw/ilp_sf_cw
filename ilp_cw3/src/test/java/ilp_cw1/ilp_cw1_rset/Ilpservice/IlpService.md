# ILP Service Test Cases

## [closeTest]
**Location:** `ilp_cw1.ilp_cw1_rset.Ilpservice.closeTest.java`

- **testIsCloseTo_ClosePositions_ReturnsTrue** – Verify correct detection when two points are closer than the threshold  
  **Check:** Returns `true` and 200 status if distance < 0.00015  
  **Scenario:** Two points 0.0001 apart (below threshold)

- **testIsCloseTo_FarPositions_ReturnsFalse** – Verify correct detection when two points are farther than the threshold  
  **Check:** Returns `false` and 200 status if distance > 0.00015  
  **Scenario:** Two points 0.0002 apart (above threshold)

- **testIsCloseTo_ExactThresholdDistance_ReturnsFalse** – Verify boundary behavior when distance equals threshold  
  **Check:** Returns `false` at distance = 0.00015 (threshold not inclusive)  
  **Scenario:** Two points exactly at threshold distance

- **testIsCloseTo_NullRequest_ReturnsBadRequest** – Verify handling of null request  
  **Check:** Returns 400 status if request is null  
  **Scenario:** Request object is null

- **testIsCloseTo_NullPosition1_ReturnsBadRequest** – Verify validation when first position is missing  
  **Check:** Returns 400 status if `position1` is null  
  **Scenario:** First position object in distance request is null

- **testIsCloseTo_NullPosition2_ReturnsBadRequest** – Verify validation when second position is missing  
  **Check:** Returns 400 status if `position2` is null  
  **Scenario:** Second position object in distance request is null

- **testIsCloseTo_Position1MissingLatitude_ReturnsBadRequest** – Verify validation for incomplete coordinates  
  **Check:** Returns 400 if position1 lacks latitude  
  **Scenario:** Position object has longitude only, missing latitude

- **testIsCloseTo_Position2MissingLongitude_ReturnsBadRequest** – Verify validation for incomplete coordinates  
  **Check:** Returns 400 if position2 lacks longitude  
  **Scenario:** Position object has latitude only, missing longitude

---

## [distanceTest]
**Location:** `ilp_cw1.ilp_cw1_rset.Ilpservice.distanceTest.java`

- **testDistanceTo_WithProvidedCoordinates_ReturnsServiceResult** – Verify normal API distance computation  
  **Check:** Controller returns service-computed distance with 200 status  
  **Scenario:** Compute distance between two real coordinates

- **testDistanceTo_WithNullRequest_ReturnsBadRequest** – Verify handling of null request  
  **Check:** Returns 400 status if request is null  
  **Scenario:** Distance request object is null

- **testDistanceTo_WithNullPosition2_ReturnsBadRequest** – Verify handling when second position is missing  
  **Check:** Returns 400 if `position2` is null  
  **Scenario:** Second position in distance request is null

- **testDistanceTo_WithPosition1MissingLongitude_ReturnsBadRequest** – Verify handling of incomplete coordinates  
  **Check:** Returns 400 if position1 lacks longitude  
  **Scenario:** First coordinate missing longitude field

- **testDistanceTo_WithPosition2MissingLatitude_ReturnsBadRequest** – Verify handling of incomplete coordinates  
  **Check:** Returns 400 if position2 lacks latitude  
  **Scenario:** Second coordinate missing latitude field

---

## [ilpServiceTest]
**Location:** `ilp_cw1.ilp_cw1_rset.Ilpservice.ilpServiceTest.java`

- **testDistanceCalculate_Valid** – Verify correctness of distance calculation service  
  **Check:** Computes Euclidean distance correctly  
  **Scenario:** Calculate known distance using real coordinates

- **testDistanceCalculate_NullPositions** – Verify handling of null position parameters  
  **Check:** Throws `NullPointerException` if both positions are null  
  **Scenario:** Distance request with null positions

- **testMovementCalculate_Valid** – Verify correctness of movement calculation service  
  **Check:** Computes next position based on start and angle correctly  
  **Scenario:** Move 0.00015 units at 45° from start point

- **testMovementCalculate_NullStart** – Verify handling of null start position  
  **Check:** Throws `NullPointerException` if start position is null  
  **Scenario:** Movement request with null start position

- **testIsPointInPolygon_Inside** – Verify correct detection of point inside polygon  
  **Check:** Returns `true` for points inside a closed polygon  
  **Scenario:** Test point inside rectangular area

- **testIsPointInPolygon_Outside** – Verify correct detection of point outside polygon  
  **Check:** Returns `false` for points outside polygon  
  **Scenario:** Test point outside rectangular area

- **testIsPointInPolygon_OpenPolygon** – Verify detection for point in open polygon  
  **Check:** Handles points in polygons where first and last vertices are not equal  
  **Scenario:** Polygon not closed

---

## [moveTest]
**Location:** `ilp_cw1.ilp_cw1_rset.Ilpservice.moveTest.java`

- **testCalculateNextPosition_ValidRequest_ReturnsNextPosition** – Verify normal functionality of movement calculation API  
  **Check:** Returns computed next position with 200 status  
  **Scenario:** Move from start point at 0° (north)

- **testCalculateNextPosition_ValidAngleMultiple_ReturnsPosition** – Verify handling of valid angle multiples  
  **Check:** Accepts angles that are multiples of 22.5°  
  **Scenario:** Move with 90° angle (multiple of 22.5)

- **testCalculateNextPosition_AngleNormalization_ReturnsPosition** – Verify angle normalization  
  **Check:** Angles >360° are normalized correctly  
  **Scenario:** 382.5° normalized to 22.5°

- **testCalculateNextPosition_InvalidAngle_ReturnsBadRequest** – Verify invalid angle handling  
  **Check:** Returns 400 if angle is not a multiple of 22.5°  
  **Scenario:** Move with 10° angle

- **testCalculateNextPosition_NullRequest_ReturnsBadRequest** – Verify null request handling  
  **Check:** Returns 400 if request is null  
  **Scenario:** Movement request object is null

- **testCalculateNextPosition_NullStartPosition_ReturnsBadRequest** – Verify null start handling  
  **Check:** Returns 400 if start position is null  
  **Scenario:** Start position in movement request is null

- **testCalculateNextPosition_StartMissingLongitude_ReturnsBadRequest** – Verify handling of incomplete start coordinates  
  **Check:** Returns 400 if start longitude is missing  
  **Scenario:** Start coordinate missing longitude

- **testCalculateNextPosition_StartMissingLatitude_ReturnsBadRequest** – Verify handling of incomplete start coordinates  
  **Check:** Returns 400 if start latitude is missing  
  **Scenario:** Start coordinate missing latitude

---

## [regionTest]
**Location:** `ilp_cw1.ilp_cw1_rset.Ilpservice.regionTest.java`

- **testIsInRegion_PointInside_ReturnsTrue** – Verify correct detection for points inside region  
  **Check:** Returns `true` and 200 status for points inside polygon  
  **Scenario:** Point inside square area

- **testIsInRegion_PointOutside_ReturnsFalse** – Verify correct detection for points outside region  
  **Check:** Returns `false` and 200 status for points outside polygon  
  **Scenario:** Point outside square area

- **testIsInRegion_PointOnEdge_ReturnsTrue** – Verify detection for points on region edge  
  **Check:** Returns `true` according to service logic  
  **Scenario:** Point on edge of square area

- **testIsInRegion_NullRequest_ReturnsBadRequest** – Verify null request handling  
  **Check:** Returns 400 if request is null  
  **Scenario:** Region check request is null

- **testIsInRegion_NullPosition_ReturnsBadRequest** – Verify null position handling  
  **Check:** Returns 400 if position is null  
  **Scenario:** Position object in region request is null

- **testIsInRegion_NullRegion_ReturnsBadRequest** – Verify null region handling  
  **Check:** Returns 400 if region is null  
  **Scenario:** Region object in request is null

- **testIsInRegion_InsufficientVertices_ReturnsBadRequest** – Verify handling for polygons with too few vertices  
  **Check:** Returns 400 if vertices <4  
  **Scenario:** Region has only 3 vertices

- **testIsInRegion_NullVertices_ReturnsBadRequest** – Verify handling for null vertex list  
  **Check:** Returns 400 if vertices list is null  
  **Scenario:** Region vertices list is null

- **testIsInRegion_UnnamedRegion_ReturnsBadRequest** – Verify handling for unnamed region  
  **Check:** Returns 400 if region name is null  
  **Scenario:** Region name field is null

- **testIsInRegion_PositionMissingLongitude_ReturnsBadRequest** – Verify handling for incomplete position  
  **Check:** Returns 400 if longitude missing  
  **Scenario:** Test point missing longitude

- **testIsInRegion_VertexMissingLatitude_ReturnsBadRequest** – Verify handling for incomplete vertex coordinates  
  **Check:** Returns 400 if vertex latitude missing  
  **Scenario:** Region vertex missing latitude

- **testIsInRegion_UnclosedRegion_ReturnsBadRequest** – Verify handling for unclosed polygons  
  **Check:** Returns 400 if first and last vertices are not equal  
  **Scenario:** Region vertices not matching at start and end

---

## [uidTest]
**Location:** `ilp_cw1.ilp_cw1_rset.Ilpservice.uidTest.java`

- **testUidReturnsCorrectValue** – Verify UID API returns correct student ID  
  **Check:** Returned string matches expected `"s2488412"`  
  **Scenario:** Call `uid()` method to get student identifier

- **testUidIsImmutable** – Verify immutability of UID return value  
  **Check:** Returns constant string, not mutable object  
  **Scenario:** Check if returned string matches constant reference  
