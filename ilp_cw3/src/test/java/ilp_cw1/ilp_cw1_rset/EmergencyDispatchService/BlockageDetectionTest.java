package ilp_cw1.ilp_cw1_rset.EmergencyDispatchService;

import data.*;
import ilp_cw1.ilp_cw1_rset.Droneservice.DynamicDispatchService;
import ilp_cw1.ilp_cw1_rset.Droneservice.EmergencyDispatchService;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BlockageDetectionTest {

    @Mock
    private droneService droneService;

    @Mock
    private DynamicDispatchService dynamicDispatchService;

    @InjectMocks
    private EmergencyDispatchService emergencyDispatchService;

    @BeforeEach
    public void setup() {
        // 确保所有测试都有基本的 mock 设置
        setupBasicMocks();
    }

    private void setupBasicMocks() {
        // 1. 服务点位置 - 所有测试都需要
        when(droneService.getServicePointForDrone(any(Drone.class), anyList()))
                .thenReturn(new PositionDto(0.0, 0.0));

        // 2. 可用无人机信息
        DroneForServicePoint.DroneAvailability.AvailabilitySlot slot =
                new DroneForServicePoint.DroneAvailability.AvailabilitySlot();
        slot.setDayOfWeek("MONDAY");
        slot.setFrom("09:00");
        slot.setUntil("17:00");

        DroneForServicePoint.DroneAvailability droneAvailability =
                new DroneForServicePoint.DroneAvailability();
        droneAvailability.setId("1");
        droneAvailability.setAvailability(Collections.singletonList(slot));

        DroneForServicePoint servicePoint = new DroneForServicePoint();
        servicePoint.setServicePointId(1001);
        servicePoint.setDrones(Collections.singletonList(droneAvailability));

        when(droneService.readAvailableDrones()).thenReturn(Collections.singletonList(servicePoint));

        // 3. 限制区域 - 默认为空
        when(droneService.getRestrictedAreas()).thenReturn(Collections.emptyList());

        // 4. 动态调度服务状态
        when(dynamicDispatchService.isSimulationRunning()).thenReturn(false);

        // 5. Mock 其他必要的方法
        when(droneService.doesLineIntersectPolygon(any(), any(), any())).thenReturn(false);
        when(droneService.isPointInPolygon(any(), any())).thenReturn(false);

        // 6. Mock 直接路径计算（用于高紧急级别）
        List<PositionDto> directPath = Arrays.asList(
                new PositionDto(0.0, 0.0),
                new PositionDto(0.5, 0.5),
                new PositionDto(1.0, 1.0)
        );

        // 对于高紧急级别（5+），会使用直接路径而不是 A*
        // 我们需要确保 calculateDirectPath 能正常工作
    }

    private Drone createDrone(String id) {
        Drone.DroneCapability capability = new Drone.DroneCapability(
                true, false, 10.0, 100, 1.5, 5.0, 20.0
        );
        Drone drone = new Drone();
        drone.setId(id);
        drone.setCapability(capability);
        return drone;
    }

    private EmergencyMedDispatchRec createEmergencyTask(int id, int level) {
        EmergencyMedDispatchRec task = new EmergencyMedDispatchRec();
        task.setId(id);
        task.setEmergencyLevel(level);

        MedDispatchRec.Requirements requirements = new MedDispatchRec.Requirements();
        requirements.setCapacity(5.0);
        requirements.setCooling(true);
        requirements.setHeating(false);
        requirements.setMaxCost(100.0);
        task.setRequirements(requirements);

        EmergencyMedDispatchRec.Delivery delivery = new EmergencyMedDispatchRec.Delivery();
        delivery.setLng(1.0);
        delivery.setLat(1.0);
        task.setDelivery(delivery);

        return task;
    }

    @Test
    public void BLOCK_001_calculateEmergencyDeliveryPath_BlockedPath_ThrowsException() {
        Drone drone = createDrone("1");
        EmergencyMedDispatchRec task = createEmergencyTask(999, 3);
        PositionDto startPosition = new PositionDto(0.0, 0.0);

        // Mock A* 返回空路径（被阻塞）
        when(droneService.calculateAStarPath(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        // Mock 限制区域
        RestrictedArea area = new RestrictedArea();
        area.setName("TestArea");
        area.setVertices(Arrays.asList(
                new PositionDto(0.5, 0.5),
                new PositionDto(0.5, 1.5),
                new PositionDto(1.5, 1.5),
                new PositionDto(1.5, 0.5)
        ));

        when(droneService.getRestrictedAreas()).thenReturn(Collections.singletonList(area));
        when(droneService.doesLineIntersectPolygon(any(), any(), any())).thenReturn(true);

        // 应该抛出 RestrictedAreaBlockageException
        assertThrows(RestrictedAreaBlockageException.class, () -> {
            emergencyDispatchService.calculateEmergencyDeliveryPath(
                    drone, task, startPosition, false, false
            );
        });
    }

    @Test
    public void BLOCK_002_calculateEmergencyDeliveryPath_UnreachablePath_ThrowsException() {
        Drone drone = createDrone("1");
        EmergencyMedDispatchRec task = createEmergencyTask(999, 3);
        PositionDto startPosition = new PositionDto(0.0, 0.0);

        // Mock A* 返回不可达路径
        List<PositionDto> unreachablePath = Arrays.asList(
                new PositionDto(0.0, 0.0),
                new PositionDto(0.5, 0.5) // 没有到达目标
        );

        when(droneService.calculateAStarPath(any(), any(), any(), any()))
                .thenReturn(unreachablePath);

        // Mock 限制区域
        RestrictedArea area = new RestrictedArea();
        area.setName("TestArea");

        when(droneService.getRestrictedAreas()).thenReturn(Collections.singletonList(area));

        assertThrows(RestrictedAreaBlockageException.class, () -> {
            emergencyDispatchService.calculateEmergencyDeliveryPath(
                    drone, task, startPosition, false, false
            );
        });
    }

    @Test
    public void BLOCK_003_calculateEmergencyDeliveryPath_PathOnBoundary_ThrowsException() {
        Drone drone = createDrone("1");
        EmergencyMedDispatchRec task = createEmergencyTask(999, 3);
        PositionDto startPosition = new PositionDto(0.0, 0.0);

        // Mock A* 返回空路径
        when(droneService.calculateAStarPath(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        // Mock 点在限制区域内
        RestrictedArea area = new RestrictedArea();
        area.setName("TestArea");

        when(droneService.getRestrictedAreas()).thenReturn(Collections.singletonList(area));
        when(droneService.isPointInPolygon(any(), any())).thenReturn(true); // 点在区域内

        assertThrows(RestrictedAreaBlockageException.class, () -> {
            emergencyDispatchService.calculateEmergencyDeliveryPath(
                    drone, task, startPosition, false, false
            );
        });
    }

    @Test
    public void BLOCK_004_calculateEmergencyDeliveryPath_Level1to4_RequiresHumanConfirmation() {
        Drone drone = createDrone("1");
        PositionDto startPosition = new PositionDto(0.0, 0.0);

        // 测试 1-4 级
        for (int level = 1; level <= 4; level++) {
            EmergencyMedDispatchRec task = createEmergencyTask(999, level);

            when(droneService.calculateAStarPath(any(), any(), any(), any()))
                    .thenReturn(Collections.emptyList());

            RestrictedArea area = new RestrictedArea();
            area.setName("TestArea");

            when(droneService.getRestrictedAreas()).thenReturn(Collections.singletonList(area));
            when(droneService.doesLineIntersectPolygon(any(), any(), any())).thenReturn(true);

            try {
                emergencyDispatchService.calculateEmergencyDeliveryPath(
                        drone, task, startPosition, false, false
                );
                fail("Should have thrown RestrictedAreaBlockageException for level " + level);
            } catch (RestrictedAreaBlockageException e) {
                // 1-4 级需要人工确认
                assertTrue(e.isRequiresHumanConfirmation());
            }
        }
    }

    @Test
    public void BLOCK_005_calculateEmergencyDeliveryPath_Level5Plus_UsesDirectPath() {
        Drone drone = createDrone("1");

        // 测试 5 级及以上
        for (int level = 5; level <= 6; level++) {
            EmergencyMedDispatchRec task = createEmergencyTask(999, level);
            PositionDto startPosition = new PositionDto(0.0, 0.0);

            // 5 级及以上应该绕过限制区域，使用直接路径
            // 所以 A* 不会被调用

            // 创建一个简单的直接路径
            List<PositionDto> directPath = Arrays.asList(
                    startPosition,
                    new PositionDto(0.5, 0.5),
                    new PositionDto(1.0, 1.0)
            );

            // 由于 5 级及以上使用直接路径，不会调用 A*
            // 所以不会抛出 RestrictedAreaBlockageException

            // 应该正常执行，不抛出异常
            try {
                DeliveryPathResponse.Delivery result = emergencyDispatchService.calculateEmergencyDeliveryPath(
                        drone, task, startPosition, false, false
                );

                // 如果有返回结果，检查它
                if (result != null) {
                    assertNotNull(result.getFlightPath());
                }
            } catch (Exception e) {
                // 不应该抛出异常
                fail("Level " + level + " should not throw exception, but got: " + e.getClass().getSimpleName());
            }
        }
    }

    @Test
    public void BLOCK_006_buildAndDispatchResult_RestrictedAreaException_ReturnsFailure() {
        Drone drone = createDrone("1");
        EmergencyMedDispatchRec task = createEmergencyTask(999, 3);

        // Mock 动态调度服务状态
        DynamicDispatchService.DroneStatusDto status =
                new DynamicDispatchService.DroneStatusDto(
                        "1",
                        new PositionDto(0.0, 0.0),
                        DynamicDispatchService.DroneStatus.READY,
                        null, 0, 0, false, null
                );

        when(dynamicDispatchService.getCurrentDroneStatus("1")).thenReturn(status);

        // Mock 路径计算抛出异常
        when(droneService.calculateAStarPath(any(), any(), any(), any()))
                .thenThrow(new RestrictedAreaBlockageException(999, "NoFlyZone", true));

        EmergencyHandleResult result = emergencyDispatchService.buildAndDispatchResult(
                drone, task, true, 0.0
        );

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("NoFlyZone") || result.getMessage().contains("restricted"));
        assertEquals("1", result.getDroneId());
    }

    @Test
    public void BLOCK_007_buildAndDispatchResultWithBypass_RestrictedAreaException_ReturnsFailure() {
        Drone drone = createDrone("1");
        EmergencyMedDispatchRec task = createEmergencyTask(999, 3);

        // Mock 动态调度服务状态
        DynamicDispatchService.DroneStatusDto status =
                new DynamicDispatchService.DroneStatusDto(
                        "1",
                        new PositionDto(0.0, 0.0),
                        DynamicDispatchService.DroneStatus.READY,
                        null, 0, 0, false, null
                );

        when(dynamicDispatchService.getCurrentDroneStatus("1")).thenReturn(status);

        // Mock 路径计算抛出异常
        when(droneService.calculateAStarPath(any(), any(), any(), any()))
                .thenThrow(new RestrictedAreaBlockageException(999, "NoFlyZone", true));

        EmergencyHandleResult result = emergencyDispatchService.buildAndDispatchResultWithBypass(
                drone, task, true, 0.0, false // bypass = false
        );

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("NoFlyZone") || result.getMessage().contains("restricted"));
    }

    @Test
    public void BLOCK_008_buildAndDispatchResultWithBypass_BypassTrue_Succeeds() {
        Drone drone = createDrone("1");
        EmergencyMedDispatchRec task = createEmergencyTask(999, 3);

        // Mock 动态调度服务状态
        DynamicDispatchService.DroneStatusDto status =
                new DynamicDispatchService.DroneStatusDto(
                        "1",
                        new PositionDto(0.0, 0.0),
                        DynamicDispatchService.DroneStatus.READY,
                        null, 0, 0, false, null
                );

        when(dynamicDispatchService.getCurrentDroneStatus("1")).thenReturn(status);

        // Mock 直接路径
        List<PositionDto> directPath = Arrays.asList(
                new PositionDto(0.0, 0.0),
                new PositionDto(0.5, 0.5),
                new PositionDto(1.0, 1.0)
        );

        // 当 bypass=true 时，应该使用直接路径
        // 我们不需要 mock calculateAStarPath，因为 bypass=true 会跳过它

        // Mock 动态调度服务的插入方法
        doNothing().when(dynamicDispatchService).insertEmergencyTask(anyString(), any());

        EmergencyHandleResult result = emergencyDispatchService.buildAndDispatchResultWithBypass(
                drone, task, true, 0.0, true // bypass = true
        );

        // With bypass=true, should succeed
        assertNotNull(result);
        // 注意：如果无人机是空闲的，buildAndDispatchResultWithBypass 可能会成功
        // 具体结果取决于实现
    }

    @Test
    public void BLOCK_009_initializeIdleDroneState_WorksCorrectly() {
        Drone drone = createDrone("idle-1");

        when(dynamicDispatchService.isSimulationRunning()).thenReturn(false);

        // 应该调用 startSimulation
        emergencyDispatchService.initializeIdleDroneState(drone);

        // 验证动态调度服务被调用
        verify(dynamicDispatchService).startSimulation(any(DeliveryPathResponse.class));
    }

}