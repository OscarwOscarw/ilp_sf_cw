// Global variables
let map;
let droneMarkers = {};
let taskMarkers = {};
let emergencyTaskMarkers = {};
let emergencyDestinationMarkers = {};
let pathLayers = {};
let dronePaths = {};
let servicePointPosition = [55.9533, -3.1883];
let simulationInterval;
let baseUrl = "http://localhost:8080";
let taskStatus = {};
let taskProgress = {};
let droneStatusHistory = {};
let restrictedAreas = {};
let areaDrawControl;
let currentDrawingArea = null;
let drawModeActive = false;
let pendingEmergencyRequest = null;
let bypassConfirmationModal = null;

// Predefined task data
const predefinedNormalTasks = [
    {
        "id": 1001,
        "date": "2025-11-13",
        "time": "09:30",
        "delivery": {
            "lng": -3.188,
            "lat": 55.946
        },
        "requirements": {
            "capacity": 3.5,
            "cooling": true,
            "heating": false,
            "maxCost": 25.0
        }
    },
    {
        "id": 1002,
        "date": "2025-11-13",
        "time": "10:15",
        "delivery": {
            "lng": -3.192,
            "lat": 55.943
        },
        "requirements": {
            "capacity": 7.0,
            "cooling": false,
            "heating": true,
            "maxCost": 30.0
        }
    },
    {
        "id": 1003,
        "date": "2025-11-13",
        "time": "11:00",
        "delivery": {
            "lng": -3.175,
            "lat": 55.982
        },
        "requirements": {
            "capacity": 6.5,
            "cooling": false,
            "heating": true,
            "maxCost": 40.0
        }
    }
];

const predefinedEmergencyTasks = {
    "emergencyTasks": [
        {
            "id": 2001,
            "emergencyLevel": 1,
            "date": "2025-11-13",
            "time": "12:00",
            "requirements": {
                "capacity": 5,
                "maxCost": 200
            },
            "delivery": {
                "lng": -3.187,
                "lat": 55.944
            }
        }
    ]
};

// Initialization functions
document.addEventListener('DOMContentLoaded', function() {
    initMap();
    initEventListeners();
    checkSimulationStatus();
    loadRestrictedAreasFromBackend();

    document.getElementById('normal-tasks-json').value = JSON.stringify(predefinedNormalTasks, null, 2);
    document.getElementById('emergency-tasks-json').value = JSON.stringify(predefinedEmergencyTasks, null, 2);

    bypassConfirmationModal = new bootstrap.Modal(document.getElementById('bypassConfirmationModal'));
});

function initMap() {
    map = L.map('map', {
        center: servicePointPosition,
        zoom: 13,
        preferCanvas: true,
        fadeAnimation: false,
        markerZoomAnimation: false
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; OpenStreetMap contributors'
    }).addTo(map);

    L.marker(servicePointPosition, {
        icon: L.divIcon({
            className: 'service-point-marker',
            html: '<i class="fas fa-home" style="color: white; font-size: 12px;"></i>',
            iconSize: [25, 25]
        })
    }).addTo(map).bindPopup('Service Point');

    initDrawingControls();
}

function initDrawingControls() {
    areaDrawControl = new L.Control.Draw({
        position: 'topright',
        draw: {
            polygon: {
                allowIntersection: false,
                showArea: true,
                shapeOptions: {color: '#e74c3c', fillColor: '#e74c3c', fillOpacity: 0.3, weight: 2}
            },
            polyline: false, circle: false, rectangle: false, marker: false, circlemarker: false
        },
        edit: false
    });

    map.on(L.Draw.Event.CREATED, function (e) {
        const layer = e.layer;
        currentDrawingArea = {
            id: 'area_' + Date.now(),
            layer: layer,
            vertices: layer.getLatLngs()[0].map(latlng => ({lat: latlng.lat, lng: latlng.lng}))
        };

        layer.setStyle({color: '#f39c12', fillColor: '#f39c12', fillOpacity: 0.5, weight: 3});
        map.addLayer(layer);
        document.getElementById('save-current-area').disabled = false;
        showAlert('Restricted area drawing completed, please fill in information and save', 'warning');
    });

    map.on(L.Draw.Event.DRAWSTOP, function() { drawModeActive = false; });
}

function initEventListeners() {
    document.getElementById('start-simulation').addEventListener('click', startSimulation);
    document.getElementById('stop-simulation').addEventListener('click', stopSimulation);
    document.getElementById('refresh-status').addEventListener('click', refreshAllStatus);
    document.getElementById('load-predefined-tasks').addEventListener('click', loadPredefinedTasks);
    document.getElementById('send-emergency').addEventListener('click', sendDefaultEmergency);
    document.getElementById('clear-all-tasks').addEventListener('click', clearAllTasks);
    document.getElementById('show-all-paths').addEventListener('click', showAllPaths);
    document.getElementById('hide-all-paths').addEventListener('click', hideAllPaths);
    document.getElementById('clear-all-paths').addEventListener('click', clearAllPaths);
    document.getElementById('toggle-paths').addEventListener('click', togglePaths);
    document.getElementById('toggle-task-markers').addEventListener('click', toggleTaskMarkers);
    document.getElementById('toggle-restricted-areas').addEventListener('click', toggleRestrictedAreas);
    document.getElementById('normal-task-form').addEventListener('submit', createNormalTask);
    document.getElementById('emergency-task-form').addEventListener('submit', createEmergencyTask);
    document.getElementById('start-drawing-area').addEventListener('click', startDrawingArea);
    document.getElementById('clear-all-areas').addEventListener('click', clearAllRestrictedAreas);
    document.getElementById('load-backend-areas').addEventListener('click', loadRestrictedAreasFromBackend);
    document.getElementById('create-area-from-coords').addEventListener('click', createAreaFromCoordinates);
    document.getElementById('create-area-from-json').addEventListener('click', createAreaFromJSON);
    document.getElementById('save-current-area').addEventListener('click', saveRestrictedArea);
    document.getElementById('import-normal-tasks').addEventListener('click', importNormalTasksFromJSON);
    document.getElementById('import-emergency-tasks').addEventListener('click', importEmergencyTasksFromJSON);
    document.getElementById('confirmBypass').addEventListener('click', confirmBypassRestrictedArea);
}

// Task management functions
function importNormalTasksFromJSON() {
    const jsonText = document.getElementById('normal-tasks-json').value.trim();

    if (!jsonText) {
        showAlert('Please enter JSON data', 'warning');
        return;
    }

    try {
        const tasks = JSON.parse(jsonText);

        if (!Array.isArray(tasks)) {
            throw new Error('JSON format error: must be a task array');
        }

        let successCount = 0;
        let errorCount = 0;

        tasks.forEach(task => {
            if (!task.id || !task.delivery || !task.requirements) {
                console.warn('Task format error, missing required fields:', task);
                errorCount++;
                return;
            }

            if (!task.date) {
                const now = new Date();
                task.date = now.toISOString().split('T')[0];
            }
            if (!task.time) {
                const now = new Date();
                task.time = now.toTimeString().split(' ')[0].substring(0, 5);
            }

            if (addTaskToStorage(task)) {
                taskStatus[task.id] = 'pending';
                taskProgress[task.id] = 0;
                successCount++;
            } else {
                errorCount++;
            }
        });

        updateTaskList();
        addTaskMarkersToMap();

        let message = `Successfully imported ${successCount} normal tasks`;
        if (errorCount > 0) {
            message += `, ${errorCount} tasks failed to import (possibly duplicate IDs or format errors)`;
        }
        showAlert(message, successCount > 0 ? 'success' : 'warning');

    } catch (error) {
        showAlert('JSON parsing error: ' + error.message, 'danger');
    }
}

function importEmergencyTasksFromJSON() {
    const jsonText = document.getElementById('emergency-tasks-json').value.trim();

    if (!jsonText) {
        showAlert('Please enter JSON data', 'warning');
        return;
    }

    try {
        const emergencyRequest = JSON.parse(jsonText);

        if (!emergencyRequest.emergencyTasks || !Array.isArray(emergencyRequest.emergencyTasks)) {
            throw new Error('JSON format error: must contain emergencyTasks array');
        }

        sendEmergencyTask(emergencyRequest);

        emergencyRequest.emergencyTasks.forEach(task => {
            if (addTaskToStorage(task)) {
                taskStatus[task.id] = 'assigned';
                taskProgress[task.id] = 0;
            }
        });

        updateEmergencyTaskList();
        addTaskMarkersToMap();

    } catch (error) {
        showAlert('JSON parsing error: ' + error.message, 'danger');
    }
}

function loadPredefinedTasks() {
    predefinedNormalTasks.forEach(task => {
        addTaskToStorage(task);
        taskStatus[task.id] = 'pending';
        taskProgress[task.id] = 0;
    });
    updateTaskList();
    updateEmergencyTaskList();
    showAlert(`Loaded ${predefinedNormalTasks.length} built-in tasks`, 'success');
    addTaskMarkersToMap();
}

function createNormalTask(e) {
    e.preventDefault();
    const taskId = parseInt(document.getElementById('normal-task-id').value);
    const capacity = parseFloat(document.getElementById('normal-task-capacity').value);
    const location = document.getElementById('normal-task-location').value;

    if (!taskId || isNaN(capacity) || !location) {
        showAlert('Please fill in all required fields', 'warning');
        return;
    }

    const [lng, lat] = location.split(',').map(coord => parseFloat(coord.trim()));

    const now = new Date();
    const date = now.toISOString().split('T')[0];
    const time = now.toTimeString().split(' ')[0].substring(0, 5);

    const task = {
        id: taskId,
        date: date,
        time: time,
        requirements: {
            capacity: capacity,
            cooling: false,
            heating: false,
            maxCost: 35.0
        },
        delivery: {
            lng: lng,
            lat: lat
        }
    };

    if (addTaskToStorage(task)) {
        taskStatus[taskId] = 'pending';
        taskProgress[taskId] = 0;
        updateTaskList();
        addTaskMarkersToMap();
        showAlert(`Normal task ${taskId} added`, 'success');
        document.getElementById('normal-task-id').value = taskId + 1;
    }
}

function createEmergencyTask(e) {
    e.preventDefault();
    const taskId = parseInt(document.getElementById('emergency-task-id').value);
    const emergencyLevel = parseInt(document.getElementById('emergency-level').value);
    const capacity = parseFloat(document.getElementById('emergency-capacity').value);
    const location = document.getElementById('emergency-location').value;

    if (!taskId || isNaN(capacity) || !location) {
        showAlert('Please fill in all required fields', 'warning');
        return;
    }

    const [lng, lat] = location.split(',').map(coord => parseFloat(coord.trim()));

    const now = new Date();
    const date = now.toISOString().split('T')[0];
    const time = now.toTimeString().split(' ')[0].substring(0, 5);

    const task = {
        id: taskId,
        emergencyLevel: emergencyLevel,
        date: date,
        time: time,
        requirements: {capacity: capacity, maxCost: 200},
        delivery: {lng: lng, lat: lat}
    };
    const emergencyRequest = {emergencyTasks: [task]};
    sendEmergencyTask(emergencyRequest);
}

// Emergency task processing functions
function sendEmergencyTask(emergencyRequest) {
    const emergencyTask = emergencyRequest.emergencyTasks[0];
    if (emergencyTask.emergencyLevel >= 5) {
        emergencyRequest.bypassRestrictedAreas = true;
        console.log("Emergency level 5 or above, automatically bypass restricted areas");
    }

    fetch(`${baseUrl}/api/dispatch/emergency`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(emergencyRequest)
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showAlert(`Emergency task sent and assigned: ${data.message}`, 'success');
                const emergencyTask = emergencyRequest.emergencyTasks[0];
                const lat = emergencyTask.delivery.lat;
                const lng = emergencyTask.delivery.lng;
                const destinationMarker = L.marker([lat, lng], {
                    icon: L.divIcon({
                        className: 'emergency-destination-marker',
                        html: '<i class="fas fa-bolt" style="color: white; font-size: 12px;"></i>',
                        iconSize: [28, 28]
                    })
                }).addTo(map).bindPopup(`<b>Emergency Task ${emergencyTask.id} Destination</b><br>Location: ${lng.toFixed(4)}, ${lat.toFixed(4)}<br><span style="color: red">Emergency Delivery Point</span>`);
                emergencyDestinationMarkers[emergencyTask.id] = destinationMarker;
                taskStatus[emergencyTask.id] = 'assigned';
                taskProgress[emergencyTask.id] = 0;
                updateEmergencyTaskList();
            } else {
                if (data.message && data.message.includes("restricted area")) {
                    pendingEmergencyRequest = emergencyRequest;
                    showBypassConfirmationDialog(emergencyTask.id, data.message);
                } else {
                    showAlert(`Emergency task assignment failed: ${data.message}`, 'danger');
                }
            }
        })
        .catch(error => {
            console.error('Failed to send emergency task:', error);
            showAlert('Failed to send emergency task: ' + error.message, 'danger');
        });
}

function showBypassConfirmationDialog(taskId, errorMessage) {
    const areaNameMatch = errorMessage.match(/restricted area[:\s]*([^，。\s]+)/);
    const areaName = areaNameMatch ? areaNameMatch[1] : "Unknown restricted area";

    document.getElementById('blocked-task-id').textContent = taskId;
    document.getElementById('blocked-area-name').textContent = areaName;

    bypassConfirmationModal.show();
}

function confirmBypassRestrictedArea() {
    if (!pendingEmergencyRequest) {
        showAlert('Error: No pending emergency task', 'danger');
        bypassConfirmationModal.hide();
        return;
    }

    pendingEmergencyRequest.bypassRestrictedAreas = true;
    bypassConfirmationModal.hide();

    fetch(`${baseUrl}/api/dispatch/emergency/confirm-bypass`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
            taskId: pendingEmergencyRequest.emergencyTasks[0].id,
            confirmed: true,
            restrictedAreaName: document.getElementById('blocked-area-name').textContent,
            originalRequest: pendingEmergencyRequest
        })
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showAlert(`Emergency task successfully assigned! ${data.message}`, 'success');
                const emergencyTask = pendingEmergencyRequest.emergencyTasks[0];
                const lat = emergencyTask.delivery.lat;
                const lng = emergencyTask.delivery.lng;
                const destinationMarker = L.marker([lat, lng], {
                    icon: L.divIcon({
                        className: 'emergency-destination-marker',
                        html: '<i class="fas fa-bolt" style="color: white; font-size: 12px;"></i>',
                        iconSize: [28, 28]
                    })
                }).addTo(map).bindPopup(`<b>Emergency Task ${emergencyTask.id} Destination</b><br>Location: ${lng.toFixed(4)}, ${lat.toFixed(4)}<br><span style="color: red">Emergency Delivery Point (Bypassed Restricted Area)</span>`);
                emergencyDestinationMarkers[emergencyTask.id] = destinationMarker;
                taskStatus[emergencyTask.id] = 'assigned';
                taskProgress[emergencyTask.id] = 0;
                updateEmergencyTaskList();
            } else {
                showAlert(`Emergency task assignment failed: ${data.message}`, 'danger');
            }

            pendingEmergencyRequest = null;
        })
        .catch(error => {
            console.error('Failed to confirm restricted area bypass:', error);
            showAlert('Failed to confirm restricted area bypass: ' + error.message, 'danger');
            pendingEmergencyRequest = null;
        });
}

// Restricted area management functions
function startDrawingArea() {
    if (drawModeActive) return;
    map.addControl(areaDrawControl);
    new L.Draw.Polygon(map, areaDrawControl.options.draw.polygon).enable();
    drawModeActive = true;
    showAlert('Please draw a restricted area polygon on the map', 'info');
}

function createAreaFromCoordinates() {
    const name = document.getElementById('area-name-coords').value || `Restricted_Area_${Date.now()}`;
    const coordsText = document.getElementById('area-coordinates').value.trim();

    if (!coordsText) {
        showAlert('Please enter coordinate data', 'warning');
        return;
    }

    try {
        const vertices = [];
        const lines = coordsText.split('\n');

        for (const line of lines) {
            const trimmedLine = line.trim();
            if (!trimmedLine) continue;

            const parts = trimmedLine.split(',');
            if (parts.length !== 2) {
                throw new Error(`Coordinate format error: ${trimmedLine}`);
            }

            const lng = parseFloat(parts[0].trim());
            const lat = parseFloat(parts[1].trim());

            if (isNaN(lng) || isNaN(lat)) {
                throw new Error(`Coordinate value error: ${trimmedLine}`);
            }

            vertices.push({lng, lat});
        }

        if (vertices.length < 3) {
            throw new Error('At least 3 vertices are required to create a polygon');
        }

        const areaData = {
            name: name,
            vertices: vertices
        };

        saveRestrictedAreaToBackend(areaData);

    } catch (error) {
        showAlert('Coordinate parsing error: ' + error.message, 'danger');
    }
}

function createAreaFromJSON() {
    const jsonText = document.getElementById('area-json').value.trim();

    if (!jsonText) {
        showAlert('Please enter JSON data', 'warning');
        return;
    }

    try {
        const areaData = JSON.parse(jsonText);

        if (!areaData.name || !areaData.vertices || !Array.isArray(areaData.vertices)) {
            throw new Error('JSON format error: must contain name and vertices array');
        }

        if (areaData.vertices.length < 3) {
            throw new Error('At least 3 vertices are required to create a polygon');
        }

        areaData.vertices.forEach((vertex, index) => {
            if (typeof vertex.lng !== 'number' || typeof vertex.lat !== 'number') {
                throw new Error(`Vertex ${index} format error: must contain lng and lat numbers`);
            }
        });

        saveRestrictedAreaToBackend(areaData);

    } catch (error) {
        showAlert('JSON parsing error: ' + error.message, 'danger');
    }
}

function saveRestrictedArea() {
    if (!currentDrawingArea) {
        showAlert('Please draw a restricted area first', 'warning');
        return;
    }

    const name = document.getElementById('area-name').value || `Restricted_Area_${Date.now()}`;

    const areaData = {
        name: name,
        vertices: currentDrawingArea.vertices
    };

    fetch(`${baseUrl}/api/dispatch/restricted-areas`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(areaData)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Save failed');
            }
            return response.json();
        })
        .then(savedArea => {
            showAlert(`Restricted area "${name}" saved to backend`, 'success');

            currentDrawingArea.layer.setStyle({
                color: '#e74c3c',
                fillColor: '#e74c3c',
                fillOpacity: 0.3,
                weight: 2
            });

            currentDrawingArea.layer.bindPopup(`
                <b>${name}</b><br>
                <button class="btn btn-sm btn-danger mt-1" onclick="deleteArea(${savedArea.id}, '${savedArea.name.replace(/'/g, "\\'")}')">Delete</button>
            `);

            restrictedAreas[savedArea.id] = {
                ...savedArea,
                layer: currentDrawingArea.layer
            };

            document.getElementById('area-name').value = '';
            document.getElementById('save-current-area').disabled = true;
            currentDrawingArea = null;

            updateRestrictedAreasList();
        })
        .catch(error => {
            console.error('Failed to save restricted area:', error);
            showAlert('Failed to save restricted area: ' + error.message, 'danger');
        });
}

function saveRestrictedAreaToBackend(areaData) {
    fetch(`${baseUrl}/api/dispatch/restricted-areas`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(areaData)
    })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => { throw new Error(text); });
            }
            return response.json();
        })
        .then(savedArea => {
            showAlert(`Restricted area "${savedArea.name}" saved to backend`, 'success');

            document.getElementById('area-name-coords').value = '';
            document.getElementById('area-coordinates').value = '';
            document.getElementById('area-json').value = '';

            loadRestrictedAreasFromBackend();
        })
        .catch(error => {
            console.error('Failed to save restricted area:', error);
            showAlert('Failed to save restricted area: ' + error.message, 'danger');
        });
}

function deleteArea(areaId, areaName) {
    if (!confirm(`Are you sure you want to delete restricted area "${areaName}"?`)) return;

    console.log('=== Delete restricted area by name ===');
    console.log('areaId:', areaId);
    console.log('areaName:', areaName);

    const encodedName = encodeURIComponent(areaName);
    const url = `${baseUrl}/api/dispatch/restricted-areas/by-name/${encodedName}`;
    console.log('Request URL:', url);

    fetch(url, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            console.log('Response status:', response.status);
            if (!response.ok) {
                return response.text().then(text => {
                    console.log('Error response:', text);
                    throw new Error(`HTTP ${response.status}: ${text}`);
                });
            }
            return response.text();
        })
        .then(message => {
            console.log('Delete successful:', message);
            if (restrictedAreas[areaId]) {
                map.removeLayer(restrictedAreas[areaId].layer);
                delete restrictedAreas[areaId];
                updateRestrictedAreasList();
                showAlert(`Restricted area "${areaName}" deleted`, 'success');
            }
        })
        .catch(error => {
            console.error('Delete failed:', error);
            showAlert('Failed to delete restricted area: ' + error.message, 'danger');
        });
}

function clearAllRestrictedAreas() {
    if (!confirm('Are you sure you want to clear all restricted areas?')) return;

    fetch(`${baseUrl}/api/dispatch/restricted-areas`, {method: 'DELETE'})
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => { throw new Error(text); });
            }
            return response.text();
        })
        .then(message => {
            Object.values(restrictedAreas).forEach(area => map.removeLayer(area.layer));
            restrictedAreas = {};
            updateRestrictedAreasList();
            showAlert('All restricted areas cleared', 'info');
        })
        .catch(error => {
            showAlert('Failed to clear restricted areas: ' + error, 'danger');
        });
}

function loadRestrictedAreasFromBackend() {
    fetch(`${baseUrl}/api/dispatch/restricted-areas`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Load failed');
            }
            return response.json();
        })
        .then(areas => {
            Object.values(restrictedAreas).forEach(area => map.removeLayer(area.layer));
            restrictedAreas = {};

            areas.forEach(areaData => {
                const polygon = L.polygon(areaData.vertices.map(v => [v.lat, v.lng]), {
                    color: '#e74c3c', fillColor: '#e74c3c', fillOpacity: 0.3, weight: 2
                }).addTo(map);

                polygon.bindPopup(`
                    <b>${areaData.name}</b><br>
                    <button class="btn btn-sm btn-danger mt-1" onclick="deleteArea(${areaData.id}, '${areaData.name.replace(/'/g, "\\'")}')">
                        Delete
                    </button>
                `);

                restrictedAreas[areaData.id] = {
                    ...areaData,
                    layer: polygon
                };
            });

            updateRestrictedAreasList();
            showAlert(`Loaded ${areas.length} restricted areas from backend`, 'success');
        })
        .catch(error => {
            showAlert('Failed to load restricted areas: ' + error, 'danger');
        });
}

function updateRestrictedAreasList() {
    const container = document.getElementById('restricted-areas-list');
    const countElement = document.getElementById('restricted-area-count');
    const areas = Object.values(restrictedAreas);

    countElement.textContent = `${areas.length} areas`;

    if (areas.length === 0) {
        container.innerHTML = '<p class="text-muted p-3">No restricted areas</p>';
        return;
    }

    let html = '';
    areas.forEach(area => {
        html += `<div class="restricted-area-item">
            <div class="d-flex justify-content-between align-items-center">
                <div><span class="fw-bold">${area.name}</span></div>
                <div class="small text-muted">${area.vertices.length} vertices</div>
            </div>
            <div class="area-actions">
                <button class="btn btn-outline-danger btn-sm" onclick="deleteArea(${area.id}, '${area.name.replace(/'/g, "\\'")}')">
                    <i class="fas fa-trash"></i> Delete
                </button>
                <button class="btn btn-outline-primary btn-sm" onclick="focusOnArea(${area.id})">
                    <i class="fas fa-search"></i> View
                </button>
            </div>
        </div>`;
    });
    container.innerHTML = html;
}

function focusOnArea(areaId) {
    const area = restrictedAreas[areaId];
    if (area && area.layer) {
        map.fitBounds(area.layer.getBounds());
        area.layer.openPopup();
    }
}

function toggleRestrictedAreas() {
    const hasVisibleAreas = Object.values(restrictedAreas).some(area => map.hasLayer(area.layer));
    if (hasVisibleAreas) {
        Object.values(restrictedAreas).forEach(area => map.removeLayer(area.layer));
        showAlert('Restricted areas hidden', 'info');
    } else {
        Object.values(restrictedAreas).forEach(area => map.addLayer(area.layer));
        showAlert('Restricted areas shown', 'info');
    }
}

// Simulation control functions
function checkSimulationStatus() {
    fetch(`${baseUrl}/api/dispatch/simulation-state`)
        .then(response => response.json())
        .then(data => updateSimulationStatus(data === true || data === 'true'))
        .catch(error => {
            console.error('Failed to check simulation status:', error);
            showAlert('Unable to connect to backend service', 'danger');
            updateSimulationStatus(false);
        });
}

function updateSimulationStatus(isRunning) {
    const statusElement = document.getElementById('simulation-status');
    const startButton = document.getElementById('start-simulation');
    const stopButton = document.getElementById('stop-simulation');

    if (isRunning) {
        statusElement.textContent = 'Running';
        statusElement.className = 'badge bg-success';
        startButton.disabled = true;
        stopButton.disabled = false;
        if (!simulationInterval) simulationInterval = setInterval(updateSimulationData, 1000);
    } else {
        statusElement.textContent = 'Not Running';
        statusElement.className = 'badge bg-secondary';
        startButton.disabled = false;
        stopButton.disabled = true;
        if (simulationInterval) {
            clearInterval(simulationInterval);
            simulationInterval = null;
        }
    }
}

function updateSimulationData() {
    refreshDronesStatus();
    updateMapMarkers();
}

function startSimulation() {
    const tasks = getTasksFromStorage();
    if (tasks.length === 0) {
        showAlert('Please add tasks before starting simulation', 'warning');
        return;
    }

    fetch(`${baseUrl}/api/dispatch/simulate`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(tasks)
    })
        .then(response => response.ok ? response.text() : response.text().then(text => { throw new Error(text); }))
        .then(data => {
            showAlert('Simulation started successfully', 'success');
            checkSimulationStatus();
            tasks.forEach(task => {
                taskStatus[task.id] = 'assigned';
                taskProgress[task.id] = 0;
            });
            updateTaskList();
            updateEmergencyTaskList();
        })
        .catch(error => {
            console.error('Failed to start simulation:', error);
            showAlert('Failed to start simulation: ' + error.message, 'danger');
        });
}

function stopSimulation() {
    fetch(`${baseUrl}/api/dispatch/stop`, {method: 'POST'})
        .then(response => response.ok ? response.text() : Promise.reject('Failed to stop simulation'))
        .then(data => {
            showAlert('Simulation stopped', 'info');
            checkSimulationStatus();
            Object.values(droneMarkers).forEach(marker => map.removeLayer(marker));
            droneMarkers = {};

            Object.keys(taskStatus).forEach(taskId => {
                taskStatus[taskId] = 'pending';
                taskProgress[taskId] = 0;
            });

            try {
                updateTaskList();
                updateEmergencyTaskList();
            } catch (error) {
                console.warn('Error updating task list:', error);
            }

            const pathInfoContainer = document.getElementById('path-info');
            if (pathInfoContainer) pathInfoContainer.remove();
        })
        .catch(error => {
            console.error('Failed to stop simulation:', error);
            showAlert('Failed to stop simulation: ' + error.message, 'danger');
        });
}

function sendDefaultEmergency() {
    sendEmergencyTask(predefinedEmergencyTasks);
}

function refreshAllStatus() {
    checkSimulationStatus();
    refreshDronesStatus();
}

// Task display functions
function updateTaskList() {
    try {
        const container = document.getElementById('task-list-container');
        const taskCount = document.getElementById('task-count');

        if (!container || !taskCount) {
            console.warn('Task list elements not found');
            return;
        }

        const tasks = getTasksFromStorage().filter(task => !task.emergencyLevel);
        taskCount.textContent = `${tasks.length} tasks`;

        if (tasks.length === 0) {
            container.innerHTML = '<p class="text-muted p-3">No tasks</p>';
            return;
        }

        let html = '';
        tasks.forEach(task => {
            const status = taskStatus[task.id] || 'pending';
            const progress = taskProgress[task.id] || 0;
            const statusText = getStatusText(status);
            const statusClass = getStatusClass(status);
            html += `<div class="task-item">
            <div class="d-flex justify-content-between align-items-center">
                <div><span class="task-id">${task.id}</span><span class="task-status ${statusClass}">${statusText}</span></div>
            </div>
            <div class="small text-muted">Location: ${task.delivery.lng.toFixed(4)}, ${task.delivery.lat.toFixed(4)}</div>
            ${status !== 'pending' ? `<div class="progress-container"><div class="progress"><div class="progress-bar" role="progressbar" style="width: ${progress}%"></div></div><div class="progress-text">Completion: ${progress}%</div></div>` : ''}
            <div class="task-actions">
                <button class="btn btn-outline-danger btn-sm" onclick="removeTask(${task.id})"><i class="fas fa-trash"></i> Delete</button>
                <button class="btn btn-outline-primary btn-sm" onclick="viewTaskOnMap(${task.id})"><i class="fas fa-map-marker-alt"></i> View</button>
            </div>
        </div>`;
        });
        container.innerHTML = html;
    } catch (error) {
        console.error('Error updating task list:', error);
    }
}

function updateEmergencyTaskList() {
    try {
        const container = document.getElementById('emergency-task-list-container');
        const taskCount = document.getElementById('emergency-task-count');

        if (!container || !taskCount) {
            console.warn('Emergency task list elements not found');
            return;
        }

        const tasks = getTasksFromStorage().filter(task => task.emergencyLevel);
        taskCount.textContent = `${tasks.length} emergency tasks`;

        const activeCount = tasks.filter(task => taskStatus[task.id] === 'assigned').length;
        const completedCount = tasks.filter(task => taskStatus[task.id] === 'completed').length;

        const activeElement = document.getElementById('active-emergency-count');
        const completedElement = document.getElementById('completed-emergency-count');
        if (activeElement) activeElement.textContent = activeCount;
        if (completedElement) completedElement.textContent = completedCount;

        if (tasks.length === 0) {
            container.innerHTML = '<p class="text-muted p-3">No emergency tasks</p>';
            return;
        }

        let html = '';
        tasks.forEach(task => {
            const status = taskStatus[task.id] || 'pending';
            const progress = taskProgress[task.id] || 0;
            const statusText = getStatusText(status);
            const statusClass = getStatusClass(status);
            html += `<div class="task-item task-emergency">
            <div class="d-flex justify-content-between align-items-center">
                <div><span class="task-id">${task.id}</span><span class="badge bg-danger">Emergency ${task.emergencyLevel}</span><span class="task-status ${statusClass}">${statusText}</span></div>
            </div>
            <div class="small text-muted">Location: ${task.delivery.lng.toFixed(4)}, ${task.delivery.lat.toFixed(4)}</div>
            ${status !== 'pending' ? `<div class="progress-container"><div class="progress"><div class="progress-bar bg-danger" role="progressbar" style="width: ${progress}%"></div></div><div class="progress-text">Completion: ${progress}%</div></div>` : ''}
            <div class="task-actions">
                <button class="btn btn-outline-danger btn-sm" onclick="removeTask(${task.id})"><i class="fas fa-trash"></i> Delete</button>
                <button class="btn btn-outline-primary btn-sm" onclick="viewTaskOnMap(${task.id})"><i class="fas fa-map-marker-alt"></i> View</button>
            </div>
        </div>`;
        });
        container.innerHTML = html;
    } catch (error) {
        console.error('Error updating emergency task list:', error);
    }
}

function getStatusText(status) {
    const statusMap = {
        'pending': 'Pending',
        'assigned': 'Assigned',
        'completed': 'Completed',
        'delivered': 'Delivered'
    };
    return statusMap[status] || 'Unknown';
}

function getStatusClass(status) {
    const classMap = {
        'pending': 'status-pending',
        'assigned': 'status-assigned',
        'completed': 'status-completed',
        'delivered': 'status-delivered'
    };
    return classMap[status] || 'status-pending';
}

function viewTaskOnMap(taskId) {
    const tasks = getTasksFromStorage();
    const task = tasks.find(t => t.id === taskId);
    if (task) {
        map.setView([task.delivery.lat, task.delivery.lng], 15);
        if (taskMarkers[taskId]) taskMarkers[taskId].openPopup();
        if (emergencyTaskMarkers[taskId]) emergencyTaskMarkers[taskId].openPopup();
        if (emergencyDestinationMarkers[taskId]) emergencyDestinationMarkers[taskId].openPopup();
    }
}

function addTaskMarkersToMap() {
    const tasks = getTasksFromStorage();
    Object.values(taskMarkers).forEach(marker => map.removeLayer(marker));
    Object.values(emergencyTaskMarkers).forEach(marker => map.removeLayer(marker));
    taskMarkers = {};
    emergencyTaskMarkers = {};

    tasks.forEach(task => {
        const isEmergency = task.emergencyLevel;
        const marker = L.marker([task.delivery.lat, task.delivery.lng], {
            icon: L.divIcon({
                className: isEmergency ? 'emergency-task-marker' : 'task-marker',
                html: isEmergency ? '<i class="fas fa-bolt" style="color: white; font-size: 10px;"></i>' : '',
                iconSize: isEmergency ? [24, 24] : [20, 20]
            })
        }).addTo(map).bindPopup(`<b>${isEmergency ? 'Emergency' : 'Normal'} Task ${task.id}</b><br>Location: ${task.delivery.lng.toFixed(4)}, ${task.delivery.lat.toFixed(4)}<br>Status: ${getStatusText(taskStatus[task.id] || 'pending')}`);

        if (isEmergency) emergencyTaskMarkers[task.id] = marker;
        else taskMarkers[task.id] = marker;
    });
}

function getTasksFromStorage() {
    const tasksJson = localStorage.getItem('droneTasks');
    return tasksJson ? JSON.parse(tasksJson) : [];
}

function addTaskToStorage(task) {
    const tasks = getTasksFromStorage();
    if (tasks.some(t => t.id === task.id)) {
        showAlert(`Task ID ${task.id} already exists, please use a different ID`, 'warning');
        return false;
    }
    tasks.push(task);
    localStorage.setItem('droneTasks', JSON.stringify(tasks));
    return true;
}

function removeTask(taskId) {
    const tasks = getTasksFromStorage();
    const taskIndex = tasks.findIndex(task => task.id === taskId);
    if (taskIndex === -1) return;

    tasks.splice(taskIndex, 1);
    localStorage.setItem('droneTasks', JSON.stringify(tasks));
    updateTaskList();
    updateEmergencyTaskList();

    if (taskMarkers[taskId]) {
        map.removeLayer(taskMarkers[taskId]);
        delete taskMarkers[taskId];
    }
    if (emergencyTaskMarkers[taskId]) {
        map.removeLayer(emergencyTaskMarkers[taskId]);
        delete emergencyTaskMarkers[taskId];
    }
    if (emergencyDestinationMarkers[taskId]) {
        map.removeLayer(emergencyDestinationMarkers[taskId]);
        delete emergencyDestinationMarkers[taskId];
    }

    delete taskStatus[taskId];
    delete taskProgress[taskId];
    showAlert(`Task ${taskId} deleted`, 'info');
}

// Drone status functions
function refreshDronesStatus() {
    fetch(`${baseUrl}/api/dispatch/status`)
        .then(response => response.json())
        .then(drones => {
            updateDronesStatusList(drones);
            updateStatusCounters(drones);
            updateTaskProgress(drones);
        })
        .catch(error => console.error('Failed to get drone status:', error));
}

function updateTaskProgress(drones) {
    drones.forEach(drone => {
        if (drone.currentTaskId) {
            const progress = Math.round((drone.completedTasksCount / drone.totalTasks) * 100);
            taskProgress[drone.currentTaskId] = progress;
            if (drone.status === 'COMPLETED') taskStatus[drone.currentTaskId] = 'completed';
            else if (drone.status === 'MOVING') taskStatus[drone.currentTaskId] = 'assigned';
        }
    });
    updateTaskList();
    updateEmergencyTaskList();
}

function updateDronesStatusList(drones) {
    const container = document.getElementById('drones-status-list');
    if (!drones || drones.length === 0) {
        container.innerHTML = '<div class="text-muted">No drone status information</div>';
        return;
    }

    let html = '';
    drones.forEach(drone => {
        const isEmergency = drone.processingEmergency;
        html += `<div class="drone-status-item ${isEmergency ? 'emergency' : ''}">
            <div class="drone-id">Drone ${drone.droneId}</div>
            <div class="drone-status"><span class="badge ${drone.status === 'MOVING' ? 'bg-success' : drone.status === 'COMPLETED' ? 'bg-secondary' : 'bg-warning'}">${drone.status}</span>${isEmergency ? '<span class="badge bg-danger ms-1">Emergency</span>' : ''}</div>
            <div class="drone-position">Position: ${drone.currentPosition.lng.toFixed(4)}, ${drone.currentPosition.lat.toFixed(4)}</div>
            <div class="drone-position">Task: ${drone.currentTaskId || 'None'} Progress: ${drone.completedTasksCount}/${drone.totalTasks}</div>
        </div>`;
    });
    container.innerHTML = html;
}

function updateStatusCounters(drones) {
    if (!drones) return;
    const activeDrones = drones.filter(d => d.status !== 'COMPLETED').length;
    const completedTasks = drones.reduce((sum, drone) => sum + drone.completedTasksCount, 0);
    const emergencyTasks = drones.filter(d => d.processingEmergency).length;
    document.getElementById('active-drones-count').textContent = activeDrones;
    document.getElementById('completed-tasks-count').textContent = completedTasks;
    document.getElementById('emergency-tasks-count').textContent = emergencyTasks;
}

function updateMapMarkers() {
    fetch(`${baseUrl}/api/dispatch/status`)
        .then(response => response.json())
        .then(drones => {
            Object.values(droneMarkers).forEach(marker => map.removeLayer(marker));
            droneMarkers = {};
            drones.forEach(drone => {
                const isEmergency = drone.processingEmergency;
                const icon = L.divIcon({
                    className: `custom-drone-marker ${isEmergency ? 'emergency' : ''}`,
                    html: drone.droneId,
                    iconSize: [40, 40]
                });
                const marker = L.marker([drone.currentPosition.lat, drone.currentPosition.lng], { icon }).addTo(map).bindPopup(`<b>Drone ${drone.droneId}</b><br>Status: ${drone.status}<br>Position: ${drone.currentPosition.lng.toFixed(4)}, ${drone.currentPosition.lat.toFixed(4)}<br>Task: ${drone.currentTaskId || 'None'}<br>Progress: ${drone.completedTasksCount}/${drone.totalTasks}${isEmergency ? '<br><span style="color:red">Processing Emergency Task: ' + drone.currentEmergencyTaskId + '</span>' : ''}`);
                droneMarkers[drone.droneId] = marker;
                updateDronePath(drone.droneId, drone.currentPosition);
            });
        })
        .catch(error => console.error('Failed to update map markers:', error));
}

// Path management functions
function updateDronePath(droneId, position) {
    if (!dronePaths[droneId]) dronePaths[droneId] = [];
    if (!droneStatusHistory[droneId]) droneStatusHistory[droneId] = [];

    const lastPoint = dronePaths[droneId][dronePaths[droneId].length - 1];
    if (!lastPoint || lastPoint[0] !== position.lat || lastPoint[1] !== position.lng) {
        dronePaths[droneId].push([position.lat, position.lng]);
        updatePathLayer(droneId);
        updatePathInfoDisplay();
    }
}

function updatePathLayer(droneId) {
    if (pathLayers[droneId]) map.removeLayer(pathLayers[droneId]);
    if (dronePaths[droneId].length > 1) {
        const colors = ['#3498db', '#e74c3c', '#2ecc71', '#f39c12', '#9b59b6', '#1abc9c'];
        const color = colors[parseInt(droneId) % colors.length];
        pathLayers[droneId] = L.polyline(dronePaths[droneId], {
            color: color,
            weight: 4,
            opacity: 0.7,
            lineJoin: 'round'
        }).addTo(map);
        pathLayers[droneId].bindPopup(`<b>Drone ${droneId} Flight Path</b><br>Path Points: ${dronePaths[droneId].length}<br>Last Update: ${new Date().toLocaleTimeString()}`);
    }
}

function updatePathInfoDisplay() {
    let pathInfoContainer = document.getElementById('path-info');
    if (!pathInfoContainer) {
        pathInfoContainer = document.createElement('div');
        pathInfoContainer.id = 'path-info';
        pathInfoContainer.className = 'path-info';
        document.querySelector('.map-container').appendChild(pathInfoContainer);
    }

    const activeDrones = Object.keys(dronePaths).filter(id => dronePaths[id].length > 0);
    let html = `<h6>Drone Path Information</h6><small class="text-muted">Active Drones: ${activeDrones.length}</small><br><br>`;

    activeDrones.forEach(droneId => {
        const path = dronePaths[droneId];
        const currentPosition = path[path.length - 1];
        const distanceToServicePoint = calculateDistance(currentPosition[0], currentPosition[1], servicePointPosition[0], servicePointPosition[1]);
        const isAtServicePoint = distanceToServicePoint < 0.1;

        html += `<div class="path-segment ${isAtServicePoint ? 'return' : ''}">
            <div class="segment-header">Drone ${droneId}</div>
            <div class="segment-details">
                Path Points: ${path.length}<br>Current Position: ${currentPosition[1].toFixed(4)}, ${currentPosition[0].toFixed(4)}<br>
                Distance to Service Point: ${distanceToServicePoint.toFixed(2)} km<br>Status: ${isAtServicePoint ? '✅ Returned to Service Point' : '🚀 Executing Task'}
            </div>
        </div>`;
    });

    if (activeDrones.length === 0) html += '<div class="text-muted">No path information</div>';
    pathInfoContainer.innerHTML = html;
}

function calculateDistance(lat1, lng1, lat2, lng2) {
    const R = 6371;
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLng = (lng2 - lng1) * Math.PI / 180;
    const a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) * Math.sin(dLng/2) * Math.sin(dLng/2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    return R * c;
}

// Utility functions
function showAlert(message, type = 'info') {
    const alertArea = document.querySelector('.alert-area');
    const alertId = 'alert-' + Date.now();
    const alert = document.createElement('div');
    alert.className = `alert alert-${type} alert-dismissible fade show`;
    alert.innerHTML = `${message}<button type="button" class="btn-close" data-bs-dismiss="alert"></button>`;
    alert.id = alertId;
    alertArea.appendChild(alert);
    setTimeout(() => {
        const el = document.getElementById(alertId);
        if (el) el.remove();
    }, 5000);
}

function clearAllTasks() {
    if (!confirm('Are you sure you want to clear all tasks?')) return;
    localStorage.removeItem('droneTasks');
    updateTaskList();
    updateEmergencyTaskList();
    Object.values(taskMarkers).forEach(marker => map.removeLayer(marker));
    Object.values(emergencyTaskMarkers).forEach(marker => map.removeLayer(marker));
    Object.values(emergencyDestinationMarkers).forEach(marker => map.removeLayer(marker));
    taskMarkers = {};
    emergencyTaskMarkers = {};
    emergencyDestinationMarkers = {};
    taskStatus = {};
    taskProgress = {};
    showAlert('All tasks cleared', 'info');
}

function showAllPaths() {
    Object.values(pathLayers).forEach(layer => map.addLayer(layer));
    showAlert('All drone paths shown', 'info');
}

function hideAllPaths() {
    Object.values(pathLayers).forEach(layer => map.removeLayer(layer));
    showAlert('All drone paths hidden', 'info');
}

function clearAllPaths() {
    Object.values(pathLayers).forEach(layer => map.removeLayer(layer));
    pathLayers = {};
    dronePaths = {};
    droneStatusHistory = {};
    const pathInfoContainer = document.getElementById('path-info');
    if (pathInfoContainer) pathInfoContainer.remove();
    showAlert('All drone paths cleared', 'info');
}

function togglePaths() {
    const hasVisiblePaths = Object.values(pathLayers).some(layer => map.hasLayer(layer));
    if (hasVisiblePaths) hideAllPaths();
    else showAllPaths();
}

function toggleTaskMarkers() {
    const hasVisibleMarkers = Object.values(taskMarkers).some(marker => map.hasLayer(marker)) ||
        Object.values(emergencyTaskMarkers).some(marker => map.hasLayer(marker)) ||
        Object.values(emergencyDestinationMarkers).some(marker => map.hasLayer(marker));

    if (hasVisibleMarkers) {
        Object.values(taskMarkers).forEach(marker => map.removeLayer(marker));
        Object.values(emergencyTaskMarkers).forEach(marker => map.removeLayer(marker));
        Object.values(emergencyDestinationMarkers).forEach(marker => map.removeLayer(marker));
        showAlert('Task markers hidden', 'info');
    } else {
        Object.values(taskMarkers).forEach(marker => map.addLayer(marker));
        Object.values(emergencyTaskMarkers).forEach(marker => map.addLayer(marker));
        Object.values(emergencyDestinationMarkers).forEach(marker => map.addLayer(marker));
        showAlert('Task markers shown', 'info');
    }
}