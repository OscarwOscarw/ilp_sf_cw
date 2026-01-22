package data;

public class PositionDto {
    private Double lng;
    private Double lat;

    public PositionDto(Double lng, Double lat) {
        this.lng = lng;
        this.lat = lat;
    }

    public PositionDto() {

    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }
}
