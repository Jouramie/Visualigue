package model;

public class ObstacleElement extends Element implements java.io.Serializable {
    private Vector2D position;
    private Vector2D orientation;

    public ObstacleElement(ElementDescription description) {
        super(description);
        this.position = new Vector2D(0, 0);
        this.orientation = new Vector2D(0, 0);
    }

    public ObstacleElement(Vector2D position, Vector2D orientation, ElementDescription description) {
        super(description);
        this.position = position;
        this.orientation = orientation;
    }

    @Override
    public Vector2D getPosition(double time) {
        return this.position;
    }

    @Override
    public Vector2D getOrientation(double time) {
        return this.orientation;
    }

    @Override
    public void setPosition(double time, Vector2D position, double dt) {
        this.position = position;
    }

    @Override
    public void setOrientation(double time, Vector2D orientation, double dt) {
        this.orientation = orientation;
    }
}
