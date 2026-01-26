# LO2: Design and Implement Comprehensive Test Plans with Instrumented Code

## 2.1 Construction of Test Plans
The test plan is designed with a layered, risk-driven approach, integrated throughout the Test-Driven Development (TDD) process to ensure tests evolve in sync with development.

### Layered Testing Structure

**Unit Testing Layer:**  
Focuses on core algorithms and business logic, such as the A* pathfinding algorithm, distance calculations, and area checks. Methods include boundary value analysis, equivalence partitioning, and parameterized tests to ensure the correctness of basic components.

**Integration Testing Layer:**  
Focuses on module interactions and data flows, such as collaboration between drone services and dynamic scheduling services, and consistency verification of geographic data conversion APIs. Mock objects are used to isolate external dependencies and ensure reliable integration logic.

**System Testing Layer:**  
Simulates real user scenarios and business workflows, covering dynamic path simulations, emergency task insertions, and multi-drone collaborative scheduling. Scenario testing and contract testing verify whether the system behavior meets requirements end-to-end.

### Risk-Driven Test Prioritization

- High-priority tests focus on critical and complex modules such as path planning algorithms, emergency task scheduling, and cost calculation.
- Test plans are dynamically adjusted as the project evolves. For example, mid-project, full-system load testing and SLA verification scenarios were added when performance validation was identified as insufficient.

### Documentation Support

- Test strategy documents define the objectives, tools, and entry/exit criteria for each test layer.
- Test case libraries are recorded in a structured Markdown format for traceability and review.

---

## 2.2 Quality Evaluation of Test Plans

### Strengths

- **Immediate Feedback Mechanism:** TDD ensures every new functionality is accompanied by corresponding tests, reducing regression defects.
- **Multi-Level Coverage:** Creates a 3D verification network (unit, integration, system) covering both depth and breadth.
- **Scenario Completeness:** Covers normal workflows, exception handling, boundary conditions, and performance benchmarks.
- **Automated Integration:** All tests are integrated into CI/CD pipelines, enabling continuous verification and quality gates.

### Identified Limitations

- **Insufficient Performance Testing:** Early stages only set time budgets for individual path calculations, not full-system behavior under high concurrency or large data loads.
- **Missing Security & Compliance Tests:** Authentication, data encryption, and input sanitization tests were not included.
- **User Acceptance Testing Not Covered:** Lack of simulated real-user interactions for acceptance testing.
- **High Environmental Dependency:** Some integration tests assume external services are always available, making tests fragile.

### Continuous Improvement Practices

- Introduced system-level performance tests mid-project, extending benchmarks from single algorithms to end-to-end workflows.
- Enhanced robustness verification through exception-handling integration tests, covering service degradation and fault-tolerance scenarios.

---

## 2.3 Implementation of Code Instrumentation

Instrumentation strategy aims to improve observability, debuggability, and verifiability across core business modules and critical execution paths.

### Assertion Instrumentation

- Insert preconditions and postconditions at key algorithm entry points and decision branches to ensure data states meet expectations.
- Example: Validate input coordinates in the A* algorithm, verify non-negative capacity in task allocation.

### Structured Logging Instrumentation

- Output structured logs at key events, such as state machine transitions, scheduling decisions, and exception handling.
- Logs include timestamps, context IDs, operation types, and result status to support traceability and post-mortem analysis.

### Performance Probe Instrumentation

- Insert high-precision timers at the start and end of performance-sensitive paths (e.g., path planning, cost calculation, geographic conversion).
- Collect metrics like execution time and call frequency for performance benchmarking and bottleneck analysis.

### Coverage Tool Integration

- Integrate JaCoCo to collect coverage metrics, configuring instruction and branch coverage thresholds.
- Automatically generate and publish coverage reports during the build process to identify untested code areas.

### Custom Monitoring Probes

- Embed state snapshots and change event records in key state objects (e.g., drone status, simulation engine, task queue).
- Support real-time state queries through diagnostic interfaces for testing validation and troubleshooting.

---

## 2.4 Evaluation of Code Instrumentation

### Effectiveness of Instrumentation

- **Accelerated Issue Diagnosis:** Logs and assertions help quickly identify the layer and root cause of issues in scenarios like JSON parsing errors, coordinate overflow, or task conflicts.
- **Performance Bottleneck Visualization:** Performance probe data revealed excessive A* algorithm calls in multi-task path planning, prompting caching and result reuse optimizations.
- **Coverage-Guided Test Supplementation:** Coverage reports highlighted insufficient testing for polygon point checks and angle normalization, guiding additional targeted test cases.
- **State Consistency Verification:** Custom state probes validated consistency between drone state machine transitions and task scheduling logic in integration tests.

### Existing Limitations and Blind Spots

- **No End-to-End Emergency Traceability:** Lack of unified tracking ID and timing statistics from emergency task reception to dispatch.
- **Resource Consumption Monitoring Missing:** Memory usage, thread pool utilization, and network I/O are not instrumented.
- **Opaque Cross-Service Call Chains:** No context or performance metric propagation between services, making distributed bottleneck analysis difficult.
- **Insufficient Fault Recovery Path Verification:** Existing instrumentation focuses on normal paths with limited support for fault injection and recovery mechanism testing.

### Lessons Learned and Future Improvements

- Align instrumentation with SLA: Key business metrics (e.g., emergency response time, path computation delay) should have instrumentation points with alert thresholds.
- Adopt standardized observability frameworks (e.g., OpenTelemetry) for tracing, metrics collection, and log correlation.
- Expose health checks and readiness probes for system health, service connectivity, and internal queue depth.
- Support chaos engineering instrumentation: Integrate fault injection tests in CI to verify system behavior and self-healing under abnormal conditions.

### Practical Example

During restricted area blockage detection tests, exception instrumentation and state logging verified that the system correctly identified blocked paths and distinguished handling strategies based on emergency level (e.g., low-level requires manual confirmation, high-level allows forced rerouting). This demonstrates the value of instrumentation in validating complex business logic.
