package data;

import java.time.LocalDate;
import java.time.LocalTime;

// 紧急任务实体（继承普通医疗配送任务）
public class EmergencyMedDispatchRec extends MedDispatchRec {
    // 紧急程度：1-最高，5-最低
    private Integer emergencyLevel;

    // 构造器（复用普通任务字段）
    public EmergencyMedDispatchRec(Integer id, LocalDate date, LocalTime time,
                                   Requirements requirements, Delivery delivery,
                                   Integer emergencyLevel) {
        super(id, date, time, requirements, delivery);
        this.emergencyLevel = emergencyLevel;
    }

    // Getter & Setter
    public Integer getEmergencyLevel() {
        return emergencyLevel;
    }

    public void setEmergencyLevel(Integer emergencyLevel) {
        this.emergencyLevel = emergencyLevel;
    }
}