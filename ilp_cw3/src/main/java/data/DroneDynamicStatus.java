package data;

import lombok.Data;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Data
public class DroneDynamicStatus {
    private final String droneId;
    private PositionDto currentPosition;
    private Queue<DeliveryTask> taskQueue = new ConcurrentLinkedQueue<>();
    private boolean isBusy = false;
    private long totalMoves = 0;
    private double totalCost = 0.0;

    public DroneDynamicStatus(String droneId, PositionDto initialPosition) {
        this.droneId = droneId;
        this.currentPosition = initialPosition;
    }

    public void incrementTotalMoves() {
        this.totalMoves++;
    }

    public void addTotalCost(double cost) {
        this.totalCost += cost;
    }
}