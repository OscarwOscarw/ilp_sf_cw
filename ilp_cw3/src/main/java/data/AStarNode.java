package data;

public class AStarNode implements Comparable<AStarNode> {
    public PositionDto position;
    double fScore;
    public AStarNode cameFrom;

    public AStarNode(PositionDto position, double fScore, AStarNode cameFrom) {
        this.position = position;
        this.fScore = fScore;
        this.cameFrom = cameFrom;
    }

    @Override
    public int compareTo(AStarNode other) {
        return Double.compare(this.fScore, other.fScore);
    }
}