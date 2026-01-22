package data;

import java.util.List;

public class DroneForServicePoint {
    private int servicePointId;
    private List<DroneAvailability> drones;

    // getters and setters
    public int getServicePointId() { return servicePointId; }
    public void setServicePointId(int servicePointId) { this.servicePointId = servicePointId; }

    public List<DroneAvailability> getDrones() { return drones; }
    public void setDrones(List<DroneAvailability> drones) { this.drones = drones; }

    public static class DroneAvailability {
        private String id;
        private List<AvailabilitySlot> availability;

        // getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public List<AvailabilitySlot> getAvailability() { return availability; }
        public void setAvailability(List<AvailabilitySlot> availability) { this.availability = availability; }

        public static class AvailabilitySlot {
            private String dayOfWeek;
            private String from;
            private String until;

            // getters and setters
            public String getDayOfWeek() { return dayOfWeek; }
            public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

            public String getFrom() { return from; }
            public void setFrom(String from) { this.from = from; }

            public String getUntil() { return until; }
            public void setUntil(String until) { this.until = until; }
        }
    }
}