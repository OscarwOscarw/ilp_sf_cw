package data;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;

public class MedDispatchRec {
    private int id;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime time;
    private Requirements requirements;
    private Delivery delivery;

    public MedDispatchRec(int id, LocalDate date, LocalTime time,
                          Requirements requirements, Delivery delivery) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.requirements = requirements;
        this.delivery = delivery;
    }


    public MedDispatchRec() {
    }

    // getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getTime() { return time; }
    public void setTime(LocalTime time) { this.time = time; }

    public Requirements getRequirements() { return requirements; }
    public void setRequirements(Requirements requirements) { this.requirements = requirements; }

    public Delivery getDelivery() { return delivery; }
    public void setDelivery(Delivery delivery) { this.delivery = delivery; }

    public static class Requirements {
        private double capacity;
        private boolean cooling;
        private boolean heating;
        private double maxCost;

        // getters and setters
        public double getCapacity() { return capacity; }
        public void setCapacity(double capacity) { this.capacity = capacity; }

        public boolean isCooling() { return cooling; }
        public void setCooling(boolean cooling) { this.cooling = cooling; }

        public boolean isHeating() { return heating; }
        public void setHeating(boolean heating) { this.heating = heating; }

        public double getMaxCost() { return maxCost; }
        public void setMaxCost(double maxCost) { this.maxCost = maxCost; }
    }

    public static class Delivery {
        private double lng;
        private double lat;

        // getters and setters
        public double getLng() { return lng; }
        public void setLng(double lng) { this.lng = lng; }

        public double getLat() { return lat; }
        public void setLat(double lat) { this.lat = lat; }

        public PositionDto toPositionDto() {
            return new PositionDto(lng, lat);
        }
    }
}