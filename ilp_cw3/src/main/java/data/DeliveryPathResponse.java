package data;

import java.util.List;

public class DeliveryPathResponse {
    private double totalCost;
    private int totalMoves;
    private List<DronePath> dronePaths;

    // getters and setters
    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    public int getTotalMoves() { return totalMoves; }
    public void setTotalMoves(int totalMoves) { this.totalMoves = totalMoves; }

    public List<DronePath> getDronePaths() { return dronePaths; }
    public void setDronePaths(List<DronePath> dronePaths) { this.dronePaths = dronePaths; }

    public static class DronePath {
        private String droneId;
        private List<Delivery> deliveries;

        // getters and setters
        public String getDroneId() { return droneId; }
        public void setDroneId(String droneId) { this.droneId = droneId; }

        public List<Delivery> getDeliveries() { return deliveries; }
        public void setDeliveries(List<Delivery> deliveries) { this.deliveries = deliveries; }
    }

    public static class Delivery {
        private Integer deliveryId;
        private List<PositionDto> flightPath;

        public Integer getDeliveryId() {
            return deliveryId;
        }

        public void setDeliveryId(Integer deliveryId) {
            this.deliveryId = deliveryId;
        }

        public List<PositionDto> getFlightPath() {
            return flightPath;
        }

        public void setFlightPath(List<PositionDto> flightPath) {
            this.flightPath = flightPath;
        }
    }
}