package data;

public class ServicePoint {
    private String name;
    private int id;
    private PositionDto location;

    // 构造方法
    public ServicePoint() {}

    public ServicePoint(String name, int id, PositionDto location) {
        this.name = name;
        this.id = id;
        this.location = location;
    }

    // getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public PositionDto getLocation() {
        return location;
    }

    public void setLocation(PositionDto location) {
        this.location = location;
    }
}