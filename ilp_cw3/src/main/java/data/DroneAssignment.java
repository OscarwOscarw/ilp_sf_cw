package data;

import java.util.List;

public class DroneAssignment {
    private Drone drone;
    private List<MedDispatchRec> tasks;
    private PositionDto servicePoint;

    public DroneAssignment(Drone drone, List<MedDispatchRec> tasks, PositionDto servicePoint) {
        this.drone = drone;
        this.tasks = tasks;
        this.servicePoint = servicePoint;
    }

    // getters
    public Drone getDrone() {
        return drone;
    }

    public List<MedDispatchRec> getTasks() {
        return tasks;
    }

    public PositionDto getServicePoint() {
        return servicePoint;
    }
}