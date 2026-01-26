# LO3: Apply a Wide Variety of Testing Techniques and Compute Test Coverage and Yield According to a Variety of Criteria

## 3.1 Scope and Diversity of Testing Techniques
This project adopts a multi-level, multi-method testing matrix to ensure thorough verification of the system across different dimensions and granularities.

### Unit Testing Techniques

- **Equivalence Partitioning:** Partition valid and invalid input domains for coordinates, task capacity, temperature control, etc.
- **Boundary Value Analysis:** Test boundaries such as angles that must be multiples of 22.5°, coordinate ranges (-90 to 90), and non-negative capacities.
- **Parameterized Testing:** For example, in `PathfindingCombinatorialTest`, combinations of start positions and obstacle counts are tested.
- **Exception Testing:** Validate system behavior under empty inputs, invalid JSON, duplicate IDs, and other abnormal scenarios.

### Integration Testing Techniques

- **Component Interface Testing:** Verify API contracts between services, e.g., task handover between `DroneService` and `DynamicDispatchService`.
- **Mocking and Stubbing:** Use Mockito to simulate external dependencies, e.g., API tests returning empty lists or exceptions.
- **Data Flow Testing:** Trace tasks from creation to assignment to completion, ensuring state consistency.
- **Contract Testing:** Use `GeoJsonConversionIntegrationTest` to verify GeoJSON outputs comply with geospatial data standards.

### System Testing Techniques

- **End-to-End Scenario Testing:** For example, `DynamicDispatchControllerSystemTest` simulates multi-task scheduling, emergency insertion, and simulation start/stop to cover full business workflows.
- **Performance Benchmarking:** `PerformanceIntegrationTest` verifies that the system completes all tasks within one minute.
- **Fault Tolerance and Recovery Testing:** `ErrorHandlingIntegrationTest` checks degradation and recovery when faced with empty input, invalid data, or unavailable services.
- **Compatibility Testing:** Validate API response formats with third-party tools such as Leaflet maps.

### Specialized Testing Techniques

- **Time Budget Testing:** `PathfindingPerformanceTest` ensures path computation completes within 2 seconds.
- **State Machine Testing:** Verify transitions of drone states (IDLE, MOVING, EMERGENCY).
- **Geospatial Logic Testing:** For example, `IsInRegion_OpenPolygon` validates polygon closure requirements.
- **Mutation Testing:** Use PITest to test core algorithms and service logic, identifying assertion strength and missing scenarios.
    - Mutation targets include path cost calculations in the A* algorithm, comparison operators in task allocation, and exception-handling branches.

---

## 3.2 Criteria for Assessing Testing Sufficiency

### Code Coverage Standards

- **Instruction Coverage:** Target ≥85% to ensure most logic paths are executed.
- **Branch Coverage:** Target ≥80%, covering all possible condition branches.
- **Core Module Coverage:** Critical modules such as the A* algorithm, task allocation, and emergency scheduling should achieve 100% branch coverage.

### Functional Pass Rate Standards

- **Unit Tests:** 100% pass rate to ensure base components are defect-free.
- **Integration Tests:** Target 100% pass rate to ensure module interactions are correct.
- **System Tests:** Key path scenarios must pass 100%; non-critical scenarios may fail ≤5%.

### Performance Standards

- **Path Computation Response Time:** ≤2 seconds per path (`PathfindingPerformanceTest`).
- **End-to-End Task Planning Time:** ≤1 minute for full system integration (`PerformanceIntegrationTest`).
- **API Response Time:** Lightweight APIs like health checks and status queries ≤100 ms.

### Mutation Testing Standards

- **Overall Mutation Score:** Target ≥80% to ensure tests effectively identify logical defects.
- **Core Module Mutation Score:** Critical logic such as A* algorithm and cost calculations ≥90%.

### Test Deliverables Standards

- **Test Report Completeness:** Each run generates a report with pass rates, coverage, and performance metrics.
- **Defect Tracking:** All test failures must be linked to specific defect entries and track resolution status.

---

## 3.3 Overview and Analysis of Test Results

### Coverage Results (JaCoCo Reports)

- **Overall Instruction Coverage:** 83% (slightly below the 85% target).
- **Overall Branch Coverage:** 84% (above 80% target).

**Core Module Coverage:**

- **A* Pathfinding Algorithm:** 92% instruction, 88% branch.
- **Emergency Task Scheduling Service:** 87% instruction, 85% branch.
- **Geospatial Calculation Service (`IlpService`):** 95% instruction, 90% branch.

**Low Coverage Areas:** Some exception-handling branches and edge-case logic (e.g., duplicate polygon vertices) are insufficiently covered.

### Functional Testing Results

- **Unit Test Pass Rate:** 100% (142 test cases).
- **Integration Test Pass Rate:** 100% (38 test cases).
- **System Test Pass Rate:** 98.5% (2 non-critical scenario failures due to environmental dependencies).

**Failed Cases Analysis:** Mainly degradation handling when external services are unavailable; mitigated with retry mechanisms and timeout settings.

### Performance Testing Results

- **Path Computation:** Average 1.2 seconds per path, within the 2-second budget.
- **End-to-End Planning:** Average 45 seconds for all tasks, meeting the 1-minute SLA.
- **API Response:** Lightweight APIs average 65 ms, within the 100 ms requirement.

### Mutation Testing Results

- **Overall Mutation Score:** 76% (below 80% target).
- **Surviving Mutants Analysis:** Mainly edge-condition checks (near-threshold decisions), floating-point equality comparisons, and exception message formatting.
- **Improvement Directions:** Enhance boundary assertions, use delta comparisons for floating-point numbers, and supplement exception scenario verification.

### Defect Discovery and Distribution

- **Unit Test Layer:** 12 defects, mainly algorithm logic errors and boundary omissions.
- **Integration Test Layer:** 8 defects, primarily API contract inconsistencies and state synchronization issues.
- **System Test Layer:** 5 defects, mainly resource contention and order-dependency under concurrency.
- **Performance Bottlenecks:** 3 identified, including missing path computation caching, unindexed database queries, and suboptimal thread pool configuration.

---

## 3.4 Comprehensive Evaluation of Test Results

### Effectiveness of Testing

- **Functional Correctness:** Sufficiently ensured; core business processes thoroughly validated with stable performance in normal and exceptional scenarios.
- **Performance Targets:** Mostly achieved; key performance indicators meet SLA with acceptable responsiveness.
- **Code Quality Visualization:** Coverage and mutation testing results objectively reflect test intensity and potential weak points.

### Identified Limitations

- **Coverage Shortfalls:** Some exception-handling and edge logic insufficiently covered, leaving potential defects.
- **Mutation Score Below Target:** Indicates test cases require improvement in logical completeness, especially boundary and exception scenarios.
- **Security Testing Absent:** No authentication, encryption, or SQL injection tests conducted.
- **Insufficient Load/Stress Testing:** Only SLA performance validated; long-term high-concurrency and capacity planning not performed.
- **Weak User Scenario Verification:** Lacks real-user acceptance tests and UX evaluation.

### Confidence Assessment

- **Functional Confidence:** High (100% unit and integration pass rates).
- **Integration Confidence:** High (API contract and data flow validation).
- **Performance Confidence:** Medium-High (SLA met but extreme load not tested).
- **Security Confidence:** Low (no dedicated security testing).
- **Production Readiness Confidence:** Medium (core functionality stable, but security, scalability, and monitoring need enhancement).

### Improvement Priorities

- **Short-Term:** Add unit tests for low-coverage areas, fix surviving mutants, and implement basic security tests (e.g., input sanitization).
- **Medium-Term:** Introduce load testing frameworks for peak task volumes and conduct chaos testing for fault tolerance validation.
- **Long-Term:** Establish user acceptance testing processes, integrate security scanning tools, and enhance production monitoring and alerting systems.

### Overall Conclusion

The combination of testing techniques effectively supports system quality verification, particularly in functional correctness and core performance. However, gaps remain in security, scalability, and extreme scenario validation. The test results provide high confidence in functional reliability and moderate confidence in performance, but additional validation is needed for production deployment in terms of security and load handling.
