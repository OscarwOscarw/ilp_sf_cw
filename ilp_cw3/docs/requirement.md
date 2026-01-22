# Drone Medical Delivery System - Requirements

**Project:** ILP Drone Delivery System  
**For:** Software Testing Course Portfolio  
**Last Updated:** January 2025

---

## 1. Requirement Categories

### 1.1 Functional Requirements
- **A* Path Planning with No-Fly Zone Avoidance**
    - Core algorithm for optimal route calculation
    - Must avoid restricted areas for normal tasks
- **Dual-Mode Task Processing**
    - Normal delivery task handling
    - Emergency medication order processing with priority
- **Dynamic Restricted Area Management**
    - Create and modify no-fly zones
    - Support polygon drawing and JSON import

### 1.2 Measurable Quality Attributes
- **Route Calculation Performance**
    - Must complete within 30 seconds (ILP specification requirement)
    - Critical for system responsiveness
- **Emergency Response Time**
    - Allocation and dispatch within 2 seconds
    - Medical emergency critical constraint
- **Real-Time Updates**
    - Position updates every 200ms
    - Required for operator situational awareness

### 1.3 Security Requirements (Identified Gaps)
- **External Service Authentication**
    - Currently uses HTTP without authentication
    - Production system would require secure API communication
- **Input Validation**
    - Basic validation implemented
    - Full sanitization needed for production

### 1.4 Safety & Robustness Requirements
- **Life-First Priority**
    - Emergency tasks can bypass restricted areas
    - Critical for medical emergency scenarios
- **Fail-Safe Behavior**
    - Return-to-base procedures on failures
    - Essential for medical delivery reliability
- **Update Consistency**
    - Area changes immediately affect all tasks
    - Ensures system-wide compliance

---

## 2. Requirement Levels Analysis

### 2.1 System Level Requirements
- **End-to-End Medication Delivery Workflow**
    - Order receipt → drone dispatch → delivery confirmation
    - Complete business process validation
- **Emergency Response Scenarios**
    - Urgent medication delivery workflows
    - Life-critical system behavior
- **User Interface Interactions**
    - Web-based task management
    - Area definition and modification

### 2.2 Integration Level Requirements
- **External Service Communication**
    - Drone system ↔ ILP-REST service integration
    - Dynamic data retrieval and processing
- **Component Interaction**
    - Path planner ↔ area compliance checker
    - Algorithm integration validation
- **Frontend-Backend Synchronization**
    - Real-time data consistency
    - State management across layers

### 2.3 Unit Level Requirements
- **A* Path Finding Algorithm**
    - Algorithm correctness and efficiency
    - Individual component validation
- **Cost-Based Allocation Calculations**
    - Priority and resource allocation logic
    - Mathematical correctness verification
- **Coordinate Validation**
    - 0.00015° tolerance compliance
    - Precision requirement implementation

---

## 3. Traceability Information

### 3.1 Source References
- **ILP Specification**: 30-second calculation constraint
- **CW3 Implementation**: Emergency task priority system
- **CW3 Implementation**: Restricted area management features

### 3.2 Testing Implications
- Each requirement category needs different testing approach
- Different levels require appropriate test scopes
- Security gaps indicate missing test coverage areas

---

*This document provides the requirements basis for testing strategy development.*