package data;

import java.time.LocalDate;
import java.time.LocalTime;


public class EmergencyMedDispatchRec extends MedDispatchRec {

    private Integer emergencyLevel;

    public EmergencyMedDispatchRec(Integer id, LocalDate date, LocalTime time,
                                   Requirements requirements, Delivery delivery,
                                   Integer emergencyLevel) {
        super(id, date, time, requirements, delivery);
        this.emergencyLevel = emergencyLevel;
    }

    public EmergencyMedDispatchRec() {

    }

    // Getter & Setter
    public Integer getEmergencyLevel() {
        return emergencyLevel;
    }

    public void setEmergencyLevel(Integer emergencyLevel) {
        this.emergencyLevel = emergencyLevel;
    }
}