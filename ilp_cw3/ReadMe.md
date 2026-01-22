# Drone Delivery Emergency Management System - CW3

## Project Overview
This is a **Spring Boot**-based drone delivery emergency management system. It extends the features of CW2 by adding emergency task response and dynamic no-fly zone management. The system provides a real-time interface for logistics operators to handle sudden medical delivery demands and temporary airspace restrictions.

## 🚀 Quick Start

### Requirements
- **Java 17** or higher
- **Modern web browser** (Chrome / Firefox / Safari / Edge)

## 📁 Project Structure

```
ilp_cw3/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── ilp_cw1/
│   │   │       └── ilp_cw1_rset/
│   │   │           ├── config/
│   │   │           │   ├── CorsConfig.java              # CORS configuration
│   │   │           │   └── RestTemplateConfig.java      # REST client config
│   │   │           ├── service/
│   │   │           │   ├── droneService.java            # Drone management
│   │   │           │   ├── DynamicDispatchService.java  # Task scheduling
│   │   │           │   ├── EmergencyDispatchService.java # Emergency handling
│   │   │           │   └── ilpService.java              
│   │   │           ├── droneController.java             # Drone API
│   │   │           ├── DynamicDispatchController.java   # Dispatch API
│   │   │           ├── ilpController.java               
│   │   │           └── IlpCw1RsetApplication.java       # Spring Boot entry
│   │   └── resources/
│   │       ├── static/                                  # Frontend static files
│   │       ├── templates/                               # HTML templates
│   │       ├── application.properties                   # Spring configuration
│   │       ├── app.js                                   # Frontend JavaScript
│   │       ├── drone-simulation.html                    # Main web interface
│   │       └── styles.css                               # Frontend styles
│   └── test/                                          
├── Dockerfile                                          
├── pom.xml                                            
├── ReadMe.md                                          
├── HELP.md                                            
├── ilp_submission_image.tar                           # Docker image                      # Docker image
```                         
### Steps to Run

#### 1. Start the backend service or run the .tar file in docker


#### 2. Open the frontend(.html file in resources)

#### 3. Verify the system
- Backend: Console should display `"Started Simulation"`
- Frontend: Map and control panel should be visible

## 📋 Core Features

## 1.1 Technology Stack

| Layer          | Technology           | Purpose                                         |
|----------------|--------------------|------------------------------------------------|
| Frontend       | HTML5, CSS3, JS     | User interface and interaction                |
|                | Leaflet.js          | Interactive map rendering                      |
|                | Bootstrap 5         | Responsive UI components                       |
|                | Font Awesome        | Icons and visual elements                      |
| Data Storage   | Browser LocalStorage| Client-side task persistence                   |
|                | In-memory Collections| Runtime state management                       |

- **HTML5/CSS3/JS**: Enables cross-browser interaction for real-time emergency task operations, fitting web-based drone dispatch needs.
- **Leaflet.js**: Lightweight with custom layers, ensuring smooth map rendering and accurate restricted area polygon drawing.
- **Font Awesome**: Intuitive icons reduce operational errors by enhancing feature recognition.
- **LocalStorage + In-memory Collections**: Balances task persistence (post-refresh retention) and real-time state updates.

This panel allows dynamic creation of no-fly zones for events or security needs. Zones can be added via JSON, a form, or directly on the map by drawing polygons. All active zones are listed, and normal delivery routes automatically avoid them.
Please test emergency orders during simulation. Once a restricted area is added, it will only become effective in the next simulation.

### 🚨 Emergency Task System
- Five emergency levels: Level 1 (Low) → Level 5 (Critical)
- Smart drone allocation: idle drones prioritized, busy drones recalled based on cost model
- Emergency tasks can bypass no-fly zones and fly directly to the destination

### 🚫 No-Fly Zone Management
- Multiple creation methods:
    - Draw polygons on the map
    - Import JSON data
    - Bulk coordinate input
- Regular tasks automatically avoid no-fly zones
- When using the no-fly zone drawing function, draw the dots as small as possible and zoom in on the map. Otherwise, the actual restricted area may be larger than what's on the map, causing normal tasks to fall into the no-fly zone and the path generation function to freeze. In this case, please close the webpage and restart the Spring code.

### 🎯 Task Management
- **Regular Tasks**: Follow no-fly zone restrictions
- **Emergency Tasks**: Bypass no-fly zones and have higher priority
- **Real-time Monitoring**: Visualize drone positions, status, and task progress
- Data from each test is stored locally. If too much data causes slowdown, you can choose to restart Spring and the website.
## 🔧 Usage Guide

### 1. Basic Operations
1. Import or create regular tasks in **“Task Management”**
2. Click **“Start Simulation”** to begin delivery
3. Monitor drone status and paths in real time

### 2. Handling Emergency Deliveries
1. Go to **“Emergency Tasks”**
2. Create emergency tasks (manual or JSON import)
3. The system automatically assigns drones to bypass no-fly zones

### 3. Managing No-Fly Zones
1. Go to **“Restricted Areas”**
2. Create no-fly zones via map drawing, coordinates input, or JSON import
3. Newly added zones take effect immediately and affect path planning

## 🛠️ API Endpoints

```
Simulation Control
POST   /api/dispatch/simulate          - Start delivery simulation
POST   /api/dispatch/stop              - Stop simulation
GET    /api/dispatch/simulation-state  - Check simulation state
```
```
Drone Status Monitoring
GET    /api/dispatch/status            - Get all drone statuses
GET    /api/dispatch/drone/{droneId}/status - Get specific drone status
```
```
Emergency Task Management
POST   /api/dispatch/emergency         - Submit emergency task
POST   /api/dispatch/emergency/with-bypass - Submit emergency task with restricted area bypass
```
```
Restricted Area Management
GET    /api/dispatch/restricted-areas  - Get all restricted areas
POST   /api/dispatch/restricted-areas  - Add restricted area
POST   /api/dispatch/restricted-areas/refresh - Refresh restricted areas data
```

## ⚡ Technical Features
- **200ms** position update for real-time monitoring
- Smart scheduling algorithm based on cost model
- Optimal path planning using **A\*** algorithm
- Interactive web interface with drag-and-drop no-fly zone creation
- Modular design for easy extension

## ⚠️ Features Overview

### Working Features
- ✅ Multiple drones executing tasks concurrently
- ✅ Emergency task priority scheduling
- ✅ No-fly zone creation, import, and application
- ✅ Real-time monitoring
- ✅ Regular tasks automatically avoid no-fly zones
- ✅ Emergency tasks bypass no-fly zones

### Current Limitations
- ⚠️ Task progress bar display incomplete
- ⚠️ Deleting individual no-fly zones is not yet implemented

### Actual System Behavior
- **Emergency Tasks**: Automatically bypass no-fly zones
- **Regular Tasks**: Strictly follow no-fly zone restrictions
- **Path Planning**: Optimal routes calculated using **A\*** algorithm

## 🎯 Tips & Tricks
- Use built-in test tasks to quickly verify system functionality
- Support batch import of tasks and no-fly zones via JSON
- Map controls can show/hide paths, task markers, and no-fly zones
- Periodically use **“Refresh Status”** to update drone statuses  
