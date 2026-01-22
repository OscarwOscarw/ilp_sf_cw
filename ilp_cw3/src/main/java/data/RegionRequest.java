package data;

public class RegionRequest {
    private PositionDto position;
    private Region region;

    public RegionRequest() {}
    public RegionRequest(PositionDto point, Region temp) {
        this.position = point;
        this.region = temp;
    }

    public PositionDto getPosition() {
        return position;
    }
    public void setPosition(PositionDto position) {
        this.position = position;
    }
    public Region getRegion() {
        return region;
    }
    public void setRegion(Region region) {
        this.region = region;
    }
}
