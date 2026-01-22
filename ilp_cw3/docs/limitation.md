# Testing Limitations Analysis

## LO1: Testing Approach Limitations

### 1. Testing Coverage Limitations

**Optimistic Inaccuracy of Testing**
- Testing can only check specific cases, not all possible behaviors
- Example: A* algorithm tests verify known scenarios but may miss novel edge cases
- Impact: Some calculation errors may remain undetected

**Static Analysis Limitations**
- Automated tools may produce false positives (pessimistic inaccuracy)
- Security scanners might miss novel vulnerability patterns
- Impact: Security assurance is limited and may include false alarms

### 2. Environmental Limitations

**Simulation vs Reality Gap**
- All testing uses simulated drone behavior
- Real-world factors (weather, interference) not fully represented
- Impact: Performance in deployment may differ from test results

**Resource Constraints**
- Limited time for exhaustive testing (50-hour coursework constraint)
- Cannot implement all desired test types
- Impact: Test coverage is necessarily selective

### 3. Prototype-Specific Limitations

**Security Testing Gaps**
- No full penetration testing performed
- Limited security validation due to academic prototype context
- Justification: Focus on core functionality validation

**Hardware Abstraction**
- Testing abstracted from real drone hardware
- Hardware-specific failures not detectable
- Acceptance: Appropriate for algorithm validation phase

### 4. Overall Appropriateness Assessment

**Strengths of Current Approach**
- Provides early fault detection through requirement analysis
- Combines multiple test techniques for broader coverage
- Focuses effort on most critical constraints
- Appropriate for prototype development phase

**Acknowledged Gaps**
- Cannot guarantee absence of all faults
- Security validation is incomplete
- Real-world performance uncertainty remains

**Conclusion**
The testing approach is appropriate for its context: an academic prototype focused on algorithm validation. It provides reasonable assurance for core functionality while acknowledging practical limitations. This aligns with realistic software testing practice where perfect verification is impossible, and we accept "approximately correct" systems with documented limitations.

## Mitigation Strategies

### For Current Project
- Clear documentation of all known limitations
- Focused testing on highest-risk areas (medical emergencies)
- Conservative safety margins in performance requirements

### For Production System
- Additional security testing would be required
- Hardware-in-the-loop testing needed
- Regulatory compliance testing for medical applications