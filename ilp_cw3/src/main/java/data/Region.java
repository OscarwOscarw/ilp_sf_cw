package data;

import java.util.List;

public class Region {
    private String name;
    private List<PositionDto> vertices;

    public Region() {}
    public Region(String temp, List<PositionDto> polygon) {
        this.name = temp;
        this.vertices = polygon;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public List<PositionDto> getVertices() {
        return vertices;
    }
    public void setVertices(List<PositionDto> vertices) {
        this.vertices = vertices;
    }
}
