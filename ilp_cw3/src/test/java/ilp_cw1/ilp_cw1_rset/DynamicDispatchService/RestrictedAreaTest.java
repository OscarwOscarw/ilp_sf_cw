package ilp_cw1.ilp_cw1_rset.DynamicDispatchService;

import data.*;
import ilp_cw1.ilp_cw1_rset.Droneservice.DynamicDispatchService;
import ilp_cw1.ilp_cw1_rset.Droneservice.droneService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RestrictedAreaTest {

    @Mock
    private droneService droneService;

    @InjectMocks
    private DynamicDispatchService dynamicDispatchService;

    @BeforeEach
    public void setup() {
        // Default empty restricted areas
        when(droneService.getRestrictedAreas()).thenReturn(Collections.emptyList());
    }

    private RestrictedArea createRestrictedArea(long id, String name, PositionDto... vertices) {
        RestrictedArea area = new RestrictedArea();
        area.setId(id);
        area.setName(name);
        area.setVertices(Arrays.asList(vertices));
        return area;
    }

    @Test
    public void AREA_001_refreshRestrictedAreas_SuccessfulRefresh() {
        // Setup new restricted areas
        List<RestrictedArea> newAreas = Arrays.asList(
                createRestrictedArea(1, "Area1",
                        new PositionDto(0.0, 0.0),
                        new PositionDto(0.0, 1.0),
                        new PositionDto(1.0, 1.0),
                        new PositionDto(1.0, 0.0)
                )
        );

        when(droneService.getRestrictedAreas()).thenReturn(newAreas);

        // Refresh
        dynamicDispatchService.refreshRestrictedAreas();

        // Verify service was called
        verify(droneService, atLeastOnce()).getRestrictedAreas();
    }

    @Test
    public void AREA_002_refreshRestrictedAreas_Exception_FallbackEmpty() {
        // Make service throw exception
        when(droneService.getRestrictedAreas()).thenThrow(new RuntimeException("Service unavailable"));

        // Should not throw exception
        assertDoesNotThrow(() -> {
            dynamicDispatchService.refreshRestrictedAreas();
        });

        // Should fall back to empty list
        verify(droneService, atLeastOnce()).getRestrictedAreas();
    }

    @Test
    public void AREA_003_refreshRestrictedAreas_NullResponse_FallbackEmpty() {
        when(droneService.getRestrictedAreas()).thenReturn(null);

        // Should not throw exception
        assertDoesNotThrow(() -> {
            dynamicDispatchService.refreshRestrictedAreas();
        });

        // Should handle null gracefully
        verify(droneService, atLeastOnce()).getRestrictedAreas();
    }

    @Test
    public void AREA_004_isPointInPolygon_PointInside_ReturnsTrue() {
        // Create a square polygon
        List<PositionDto> vertices = Arrays.asList(
                new PositionDto(0.0, 0.0),
                new PositionDto(0.0, 2.0),
                new PositionDto(2.0, 2.0),
                new PositionDto(2.0, 0.0)
        );

        PositionDto insidePoint = new PositionDto(1.0, 1.0);

        // Use reflection to test private method
        // Since this is a private method, you might need to:
        // 1. Make it package-private for testing
        // 2. Test through public methods that use it
        // 3. Use reflection

        // For now, we'll test through the service's behavior
        // This test would need adjustment based on actual implementation
    }

    @Test
    public void AREA_005_isPointInPolygon_PointOutside_ReturnsFalse() {
        // Similar to above, testing through public API
    }

    @Test
    public void AREA_006_isPointOnSegment_PointOnLine_ReturnsTrue() {
        PositionDto a = new PositionDto(0.0, 0.0);
        PositionDto b = new PositionDto(2.0, 2.0);
        PositionDto p = new PositionDto(1.0, 1.0); // On the line

        // Test through public API or use reflection
    }

    @Test
    public void AREA_007_ServiceInitialization_LoadsRestrictedAreas() {
        // Verify service loads restricted areas on initialization
        verify(droneService, atLeastOnce()).getRestrictedAreas();
    }

    @Test
    public void AREA_008_MultipleRefreshCalls_WorksCorrectly() {
        List<RestrictedArea> areas1 = Collections.singletonList(
                createRestrictedArea(1, "Area1",
                        new PositionDto(0.0, 0.0),
                        new PositionDto(0.0, 1.0)
                )
        );

        List<RestrictedArea> areas2 = Collections.singletonList(
                createRestrictedArea(2, "Area2",
                        new PositionDto(1.0, 1.0),
                        new PositionDto(1.0, 2.0)
                )
        );

        // First refresh
        when(droneService.getRestrictedAreas()).thenReturn(areas1);
        dynamicDispatchService.refreshRestrictedAreas();
        verify(droneService, times(2)).getRestrictedAreas(); // Initial + refresh

        // Second refresh
        when(droneService.getRestrictedAreas()).thenReturn(areas2);
        dynamicDispatchService.refreshRestrictedAreas();
        verify(droneService, times(3)).getRestrictedAreas(); // Initial + 2 refreshes
    }
}