package data;

import lombok.Data;
import java.util.List;

@Data
public class DeliveryTask {
    private final int deliveryId;
    private final List<PositionDto> flightPath;
}