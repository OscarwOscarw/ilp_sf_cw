package data;

public class EmergencyHandleResult {
    private String droneId;
    private double finalCost;
    private double baseCost;
    private String message;
    private boolean success;

    public EmergencyHandleResult(String droneId, double finalCost, double baseCost, String message, boolean success) {
        this.droneId = droneId;
        this.finalCost = finalCost;
        this.baseCost = baseCost;
        this.message = message;
        this.success = success;
    }

    // Getters and Setters
    public String getDroneId() {
        return droneId;
    }

    public void setDroneId(String droneId) {
        this.droneId = droneId;
    }

    public double getFinalCost() {
        return finalCost;
    }

    public void setFinalCost(double finalCost) {
        this.finalCost = finalCost;
    }

    public double getBaseCost() {
        return baseCost;
    }

    public void setBaseCost(double baseCost) {
        this.baseCost = baseCost;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}