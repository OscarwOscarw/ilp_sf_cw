# LO4: Evaluate the Limitations of a Given Testing Process, Using Statistical Methods Where Appropriate, and Summarise Outcomes

## 4.1 Identified Gaps and Omissions in the Testing Process
Through a comprehensive audit of the test plan, execution process, and results, the following systematic gaps and potential risk points were identified:

### Coverage Gaps (Based on JaCoCo Statistics)

- **Overall instruction coverage:** 83%, below the target of 85%, primarily due to:
    - Incomplete coverage of exception-handling branches (e.g., duplicate polygon vertex handling in `IlpService`).
    - Missing validation for edge business scenarios such as “tasks completely overlapping but with different IDs” or “coordinate values at critical floating-point precision limits.”
    - Some utility and configuration class code not included in unit tests.

### Incomplete Performance Verification

- Only single-path computation (≤2 seconds) and end-to-end task planning (≤1 minute) were validated, lacking:
    - System throughput tests under high concurrency.
    - Long-duration monitoring for memory leaks and performance degradation.
    - Performance behavior in distributed environments with network latency or service degradation.

### Absence of Security Testing

- No security validation was performed, including:
    - API authentication and authorization testing.
    - Input sanitization and injection attack prevention (e.g., SQL or JSON injection).
    - Verification of encryption for sensitive data (coordinates, task details) in transit and at rest
### High Integration Test Environmental Dependency

- Some integration tests (e.g., `DataRetrievalIntegrationTest`) depend on external API availability, making results affected by network and service status and non-repeatable.
- No stable test double system was established, causing occasional CI test failures.

### Lack of User and Operational Acceptance Testing

- No testing with real user roles (e.g., dispatchers, operators) for operational workflows.
- No usability evaluation for system management functions (simulation start/stop, region refresh, log queries).

### High Mutation Test Survival Rate

- Overall mutation score: 76%, below the 80% target, indicating logical incompleteness of test cases, particularly:
    - Weak boundary condition checks (e.g., angles near multiples of 22.5° but slightly over).
    - Exception-handling branches lack precise assertions (only exception type verified, not message content).

---

## 4.2 Target Levels for Each Testing Aspect
Based on the identified gaps, industry practices, and project goals, the following quantitative target levels were set:

### Code Coverage Targets (Production-Ready Standards)

- Instruction coverage ≥ 90%
- Branch coverage ≥ 85%
- Core modules (path planning, task scheduling, geospatial calculation) branch coverage = 100%

### Performance SLA Targets

- Single-path P95 response time ≤ 1.5 seconds
- End-to-end task planning P99 response time ≤ 45 seconds
- System supports ≥ 100 concurrent tasks per minute
- Average API response time ≤ 50 ms

### Security and Compliance Targets

- OWASP Top 10 critical vulnerabilities detection rate = 0
- All API endpoints pass basic authentication tests
- 100% encryption for sensitive data in transit

### Integration Test Stability Targets

- Integration test pass rate ≥ 99.5%
- All external dependencies replaced by mocks, ensuring fully controllable test environment

### Mutation Testing Targets

- Overall mutation score ≥ 85%
- Core logic mutation score ≥ 95%

### User Acceptance Targets

- Key user workflows (e.g., emergency task dispatch, simulation monitoring) pass rate = 100%
- System management functionality usability ≥ 95%

---

## 4.3 Gap Analysis Between Current and Target Levels

### Coverage Gap Analysis

- Current: 83% vs. Target: 90%, gap = 7 percentage points
- Missing coverage mainly in exception-handling and edge scenarios, requiring ~30 targeted unit tests.

### Performance Gap Analysis

- Current metrics meet preliminary SLA, but no stress testing conducted
- High-concurrency stability not validated; require load testing tools (e.g., JMeter) to simulate ≥100 concurrent tasks.

### Security Gap Analysis

- Security tests not implemented
- Need to establish a basic security test suite covering input validation, authentication, encryption, totaling at least 15 test cases.

### Integration Test Stability Gap Analysis

- Occasional failures due to external dependencies
- Replacing all external service calls with contract-based mocks expected to improve stability to 99.8%.

### Mutation Testing Gap Analysis

- Current: 76% vs. Target: 85%, gap = 9 percentage points
- Need to enhance assertions for surviving mutants and exception-handling verification, requiring ~20 additional test cases.

### User Acceptance Gap Analysis

- No user testing conducted
- Need to create at least 5 key end-to-end user scenario test scripts.

### Statistical Methods Applied

- **Confidence Interval Analysis:** Evaluate path computation time: based on 50 samples, mean = 1.2s, 95% CI = [1.1, 1.3]s, indicating stable performance within the target range.
- **Defect Density Analysis:** Defects per KLOC = 1.2 (below industry average 2.0), indicating high code quality; security defect density = 0 (due to untested), indicating blind spots.

---

## 4.4 Improvement Measures to Achieve Target Levels

### Short-Term Improvements (1–2 Weeks)

- **Supplement Unit Tests:** Add boundary and exception tests for modules with coverage <85%, expected coverage increase to 88%.
- **Fix Surviving Mutants:** Analyze PITest reports and enhance assertions to increase mutation score to 80%.
- **Establish Basic Security Tests:** Integrate OWASP ZAP or similar tools for API security scanning in CI pipeline.
- **Stabilize Integration Test Environment:** Replace all external API dependencies with WireMock or Pact mocks to ensure repeatability.

### Medium-Term Improvements (1 Month)

- **Introduce Load Testing Framework:** Use JMeter or Gatling to simulate high-concurrency scheduling (≥100 tasks), validating system stability and performance degradation.
- **Enhance Monitoring and Diagnostic Instrumentation:** Add performance and state metrics along key paths, integrate Prometheus + Grafana for test visualization.
- **Establish User Scenario Test Library:** Implement key user end-to-end tests with Selenium or Cypress, covering dispatch, monitoring, and configuration workflows.
- **Pilot Chaos Testing:** Integrate Chaos Monkey or similar in CI to inject delays and exceptions, validating fault tolerance and self-healing.

### Long-Term Improvements (Production Deployment)

- **Establish Full-Chain Tracing:** Integrate OpenTelemetry for cross-service call tracing, aiding performance bottleneck and root cause analysis.
- **Perform In-Depth Security Testing:** Include penetration tests, code security audits, dependency vulnerability scanning, and implement security gates in the pipeline.
- **Build Capacity Planning Model:** Based on historical load and performance data, create predictive models for elasticity decisions.
- **Establish Continuous Quality Dashboard:** Integrate metrics for coverage, performance, security, defects, enabling real-time visualization and trend alerts.

### Resource and Risk Estimates

- **Manpower:** Short-term: 1 person × 2 weeks; medium-term: 2 persons × 4 weeks; long-term: continuous.
- **Technical Risks:** Load testing may reveal architectural bottlenecks; security testing may uncover critical vulnerabilities requiring refactoring.

### Priority Recommendations

- **First Priority:** Security and stability improvements.
- **Second Priority:** User acceptance testing and in-depth performance validation.
