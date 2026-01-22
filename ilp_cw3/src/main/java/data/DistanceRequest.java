package data;

public class DistanceRequest {
    private PositionDto position1;
    private PositionDto position2;

    public DistanceRequest() {}

    public DistanceRequest(PositionDto start, PositionDto goal) {
        this.position1 = start;
        this.position2 = goal;
    }

    public PositionDto getPosition1() {
        return position1;
    }

    public void setPosition(PositionDto position1) {
        this.position1 = position1;
    }

    public PositionDto getPosition2() {
        return position2;
    }

    public void setPosition1(PositionDto position1) {
        this.position1 = position1;
    }

    public void setPosition2(PositionDto position2) {
        this.position2 = position2;
    }


}
