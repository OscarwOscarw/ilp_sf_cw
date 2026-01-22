package ilp_cw1.ilp_cw1_rset.Droneservice;

import data.PositionDto;
import data.DeliveryPathResponse;
import data.RestrictedArea;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class GeoJSONConverterTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ilpService ilpService; // mock 依赖

    @InjectMocks
    private droneService service; // 测试对象

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void GEO_001_emptyData_returnsEmptyFeatureCollection() {
        String geoJson = service.convertMultipleDronesToGeoJson(Collections.emptyList(), Collections.emptyList());
        assertEquals("{\"type\":\"FeatureCollection\",\"features\":[]}", geoJson);
    }

    @Test
    public void GEO_002_singleDronePath_returnsLineStringFeature() {
        DeliveryPathResponse.DronePath dronePath = new DeliveryPathResponse.DronePath();
        dronePath.setDroneId("drone-1");
        DeliveryPathResponse.Delivery delivery = new DeliveryPathResponse.Delivery();
        delivery.setFlightPath(Arrays.asList(
                new PositionDto(0.0, 0.0),
                new PositionDto(0.001, 0.001)
        ));
        dronePath.setDeliveries(Collections.singletonList(delivery));

        String geoJson = service.convertMultipleDronesToGeoJson(Collections.singletonList(dronePath), null);
        assertTrue(geoJson.contains("\"type\": \"LineString\""));
        assertTrue(geoJson.contains("\"droneId\": \"drone-1\""));
    }

    @Test
    public void GEO_003_multipleDronePaths_returnsMultipleLineStringFeatures() {
        List<DeliveryPathResponse.DronePath> paths = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            DeliveryPathResponse.DronePath dp = new DeliveryPathResponse.DronePath();
            dp.setDroneId("drone-" + i);
            DeliveryPathResponse.Delivery delivery = new DeliveryPathResponse.Delivery();
            delivery.setFlightPath(Arrays.asList(
                    new PositionDto(0.0, (double) i),
                    new PositionDto(0.001, i + 0.001)
            ));
            dp.setDeliveries(Collections.singletonList(delivery));
            paths.add(dp);
        }

        String geoJson = service.convertMultipleDronesToGeoJson(paths, null);
        assertEquals(3, geoJson.split("\"type\": \"LineString\"").length - 1);
    }

    @Test
    public void GEO_004_withRestrictedArea_returnsPolygonFeature() {
        DeliveryPathResponse.DronePath dronePath = new DeliveryPathResponse.DronePath();
        dronePath.setDroneId("drone-1");
        DeliveryPathResponse.Delivery delivery = new DeliveryPathResponse.Delivery();
        delivery.setFlightPath(Arrays.asList(new PositionDto(0.0, 0.0), new PositionDto(0.001, 0.001)));
        dronePath.setDeliveries(Collections.singletonList(delivery));

        RestrictedArea area = new RestrictedArea();
        area.setId((long) 1);
        area.setName("NoFlyZone");
        area.setVertices(Arrays.asList(
                new PositionDto( 0.0,0.0),
                new PositionDto(0.0,1.0),
                new PositionDto(1.0,1.0),
                new PositionDto(1.0,0.0)
        ));

        String geoJson = service.convertMultipleDronesToGeoJson(
                Collections.singletonList(dronePath),
                Collections.singletonList(area)
        );

        assertTrue(geoJson.contains("\"type\": \"LineString\""));
        assertTrue(geoJson.contains("\"type\": \"Polygon\""));
        assertTrue(geoJson.contains("\"areaId\": 1"));
    }

    @Test
    public void GEO_005_invalidPathData_skippedInGeoJson() {
        DeliveryPathResponse.DronePath dronePath = new DeliveryPathResponse.DronePath();
        dronePath.setDroneId("drone-1");
        DeliveryPathResponse.Delivery delivery = new DeliveryPathResponse.Delivery();
        delivery.setFlightPath(Collections.singletonList(new PositionDto(0.0, 0.0))); // less than 2 points
        dronePath.setDeliveries(Collections.singletonList(delivery));

        String geoJson = service.convertMultipleDronesToGeoJson(Collections.singletonList(dronePath), null);
        assertEquals("{\"type\": \"FeatureCollection\",\"features\": []}", geoJson);
    }

    @Test
    public void GEO_006_largeData_performanceTest() {
        DeliveryPathResponse.DronePath dronePath = new DeliveryPathResponse.DronePath();
        dronePath.setDroneId("drone-1");
        List<PositionDto> pathPoints = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            pathPoints.add(new PositionDto(i * 0.001, i * 0.001));
        }
        DeliveryPathResponse.Delivery delivery = new DeliveryPathResponse.Delivery();
        delivery.setFlightPath(pathPoints);
        dronePath.setDeliveries(Collections.singletonList(delivery));

        long start = System.currentTimeMillis();
        String geoJson = service.convertMultipleDronesToGeoJson(Collections.singletonList(dronePath), null);
        long duration = System.currentTimeMillis() - start;

        assertTrue(duration < 100, "GeoJSON generation took too long: " + duration + "ms");
        assertTrue(geoJson.contains("\"type\": \"LineString\""));
    }
}
