package data;

import java.util.List;

public class RestrictedArea {
    private String name;
    private Long id;
    private Limits limits;
    private List<PositionDto> vertices;

    // getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Limits getLimits() { return limits; }
    public void setLimits(Limits limits) { this.limits = limits; }

    public List<PositionDto> getVertices() { return vertices; }
    public void setVertices(List<PositionDto> vertices) { this.vertices = vertices; }

    public static class Limits {
        private int lower;
        private int upper;

        // getters and setters
        public int getLower() { return lower; }
        public void setLower(int lower) { this.lower = lower; }

        public int getUpper() { return upper; }
        public void setUpper(int upper) { this.upper = upper; }
    }
}