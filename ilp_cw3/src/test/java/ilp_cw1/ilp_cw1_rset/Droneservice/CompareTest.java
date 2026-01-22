package ilp_cw1.ilp_cw1_rset.Droneservice;

import data.Drone;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CompareTest {
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private droneService droneService;

    @Test
    public void getAttributeValue_VariousAttributes_ReturnsCorrectValues() {
        Drone.DroneCapability capability =
                new Drone.DroneCapability(
                        true,     // cooling
                        false,    // heating
                        10.0,     // capacity
                        100,      // maxMoves
                        1.5,      // costPerMove
                        5.0,      // costInitial
                        20.0      // costFinal
                );
        Drone drone = new Drone("DroneA","1",capability);

        assertEquals("1", droneService.getAttributeValue(drone, "id"));

        assertEquals(10.0, droneService.getAttributeValue(drone, "capacity"));

        assertEquals(true, droneService.getAttributeValue(drone, "cooling"));

        assertNull(droneService.getAttributeValue(drone, "nonexistent"));
    }

    @Test
    public void compareByType_DifferentDataTypes_ComparesCorrectly() {
        assertTrue(droneService.compareByType(10.5, "10.5"));
        assertFalse(droneService.compareByType(10.5, "10.6"));

        assertTrue(droneService.compareByType(100, "100"));

        assertTrue(droneService.compareByType(true, "true"));

        assertTrue(droneService.compareByType("test", "test"));

        assertFalse(droneService.compareByType(null, "value"));

        assertFalse(droneService.compareByType(10.5, "not-a-number"));
    }

    @Test
    public void compareWithOperator_AllOperators_WorksCorrectly() {
        assertTrue(droneService.compareWithOperator(10.0, "5.0", ">"));
        assertTrue(droneService.compareWithOperator(5.0, "10.0", "<"));
        assertTrue(droneService.compareWithOperator(10.0, "10.0", "="));
        assertTrue(droneService.compareWithOperator(10.0, "5.0", "!="));
        assertTrue(droneService.compareWithOperator(10.0, "10.0", "<="));
        assertTrue(droneService.compareWithOperator(10.0, "10.0", ">="));

        assertTrue(droneService.compareWithOperator(true, "true", "="));
        assertTrue(droneService.compareWithOperator(true, "false", "!="));

        assertTrue(droneService.compareWithOperator("abc", "abc", "="));
        assertTrue(droneService.compareWithOperator("abc", "def", "!="));

        assertFalse(droneService.compareWithOperator(10.0, "5.0", "invalid"));
    }
}
