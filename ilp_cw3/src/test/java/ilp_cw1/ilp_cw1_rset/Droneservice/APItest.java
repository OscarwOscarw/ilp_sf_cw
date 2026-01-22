package ilp_cw1.ilp_cw1_rset.Droneservice;

import data.Drone;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class APItest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private droneService droneService;

    @Test
    public void getAllDrones_NormalCase_ReturnsDronesList() {
        // Arrange
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


        Drone[] mockDrones = {
                new Drone("DroneA", "1",capability),
                new Drone("DroneB", "2",capability)
        };

        when(restTemplate.getForObject(anyString(), eq(Drone[].class)))
                .thenReturn(mockDrones);

        List<Drone> result = droneService.getAllDrones();

        assertEquals(2, result.size());
        assertEquals("1", result.get(0).getId());

        verify(restTemplate).getForObject(anyString(), eq(Drone[].class));
    }

    @Test
    public void getAllDrones_EmptyResponse_ReturnsEmptyList() {
        when(restTemplate.getForObject(anyString(), eq(Drone[].class)))
                .thenReturn(new Drone[0]);

        List<Drone> result = droneService.getAllDrones();

        assertTrue(result.isEmpty());
    }

    @Test
    public void getAllDrones_ApiException_ReturnsEmptyList() {
        when(restTemplate.getForObject(anyString(), eq(Drone[].class)))
                .thenThrow(new RuntimeException("Connection failed"));

        List<Drone> result = droneService.getAllDrones();

        assertTrue(result.isEmpty());
    }
}
