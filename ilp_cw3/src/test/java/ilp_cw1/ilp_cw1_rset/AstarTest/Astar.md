# Pathfinding Test Cases

## [PathfindingPerformanceTest]
**Location:** `ilp_cw1.ilp_cw1_rset.AstarTest.PathfindingPerformanceTest.java`

- **testPathfindingCompletesWithinTimeBudget** – Verify the performance of the A* algorithm in the presence of multiple obstacles  
  **Check:** Path computation completes within 2 seconds  
  **Scenario:** The start and goal points are very close, with 10 small non-blocking obstacles around

---

## [AStarAlgorithmEdgeCaseTest]
**Location:** `ilp_cw1.ilp_cw1_rset.AstarTest.AStarAlgorithmEdgeCaseTest.java`

- **testStartEqualsGoal_ReturnsSinglePoint** – Ensure that when the start equals the goal, a single-point path is returned  
  **Check:** The returned path contains only one point, matching the start coordinates  
  **Scenario:** Start and goal coordinates are exactly the same

- **testNoPathExists_ReturnsEmptyList** – Verify correct handling when no path exists  
  **Check:** Algorithm returns an empty list instead of throwing an exception or entering an infinite loop  
  **Scenario:** Obstacles completely block all possible paths between the start and goal

---

## [BoundaryValuePathTest]
**Location:** `ilp_cw1.ilp_cw1_rset.AstarTest.BoundaryValuePathTest.java`

- **testGoalExactlyOnObstacleBoundary_IsBlocked** – Test behavior when the goal lies on an obstacle boundary  
  **Check:** The goal is considered unreachable when located on the boundary of an obstacle  
  **Scenario:** Goal coordinates are exactly at a corner of a square obstacle

---

## [PathfindingCombinatorialTest]
**Location:** `ilp_cw1.ilp_cw1_rset.AstarTest.PathfindingCombinatorialTest.java`

- **testPathUnderCombination** – Verify basic functionality of the A* algorithm under combinations of start positions and obstacle counts  
  **Check:** Algorithm finds a valid path in all combinations for obstacle-free scenarios  
  **Scenario:** Parameterized test covering "corner/center" start positions with 0/1 obstacle combinations  
