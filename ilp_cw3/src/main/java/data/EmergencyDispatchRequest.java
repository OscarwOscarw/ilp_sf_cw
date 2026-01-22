package data;

import java.util.List;

public class EmergencyDispatchRequest {
    private List<EmergencyMedDispatchRec> emergencyTasks;
    private boolean bypassRestrictedAreas;

    // Getter & Setter
    public List<EmergencyMedDispatchRec> getEmergencyTasks() {
        return emergencyTasks;
    }

    public void setEmergencyTasks(List<EmergencyMedDispatchRec> emergencyTasks) {
        this.emergencyTasks = emergencyTasks;
    }

    public boolean isBypassRestrictedAreas() {
        return bypassRestrictedAreas;
    }

    public void setBypassRestrictedAreas(boolean bypassRestrictedAreas) {
        this.bypassRestrictedAreas = bypassRestrictedAreas;
    }
}