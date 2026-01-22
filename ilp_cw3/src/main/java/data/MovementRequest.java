package data;

public class MovementRequest {
    private PositionDto start;
    private double angle;

    public MovementRequest() {}

    public MovementRequest(PositionDto position, double angle) {
        this.start = position;
        this.angle = angle;
    }

    public PositionDto getStart() {
        return start;
    }
    public void setStart(PositionDto start) {
        this.start = start;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

}
