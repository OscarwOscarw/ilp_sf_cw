package data;

public class Drone {
    private String name;
    private String id;
    private DroneCapability capability;

    public Drone() {}

    public Drone(String name, String id, DroneCapability capability) {
        this.name = name;
        this.id = id;
        this.capability = capability;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DroneCapability getCapability() {
        return capability;
    }

    public void setCapability(DroneCapability capability) {
        this.capability = capability;
    }

    public boolean hasCooling() {
        return capability != null && capability.getCooling();
    }

    public boolean hasHeating() {
        return capability != null && capability.getHeating();
    }

    public static class DroneCapability {
        private Boolean cooling;
        private Boolean heating;
        private Double capacity;
        private Integer maxMoves;
        private Double costPerMove;
        private Double costInitial;
        private Double costFinal;

        public DroneCapability() {}

        public DroneCapability(Boolean cooling, Boolean heating, Double capacity,
                               Integer maxMoves, Double costPerMove, Double costInitial, Double costFinal) {
            this.cooling = cooling;
            this.heating = heating;
            this.capacity = capacity;
            this.maxMoves = maxMoves;
            this.costPerMove = costPerMove;
            this.costInitial = costInitial;
            this.costFinal = costFinal;
        }

        public Boolean getCooling() {
            return cooling != null ? cooling : false;
        }

        public void setCooling(Boolean cooling) {
            this.cooling = cooling;
        }

        public Boolean getHeating() {
            return heating != null ? heating : false;
        }

        public void setHeating(Boolean heating) {
            this.heating = heating;
        }

        public Double getCapacity() {
            return capacity;
        }

        public void setCapacity(Double capacity) {
            this.capacity = capacity;
        }

        public Integer getMaxMoves() {
            return maxMoves;
        }

        public void setMaxMoves(Integer maxMoves) {
            this.maxMoves = maxMoves;
        }

        public Double getCostPerMove() {
            return costPerMove;
        }

        public void setCostPerMove(Double costPerMove) {
            this.costPerMove = costPerMove;
        }

        public Double getCostInitial() {
            return costInitial;
        }

        public void setCostInitial(Double costInitial) {
            this.costInitial = costInitial;
        }

        public Double getCostFinal() {
            return costFinal;
        }

        public void setCostFinal(Double costFinal) {
            this.costFinal = costFinal;
        }
    }
}