package model;

public class ObstacleDescription extends ElementDescription implements java.io.Serializable {
    public ObstacleDescription(String name, Vector2D size, String image) throws ValidationException {
        super(name, size, image);
    }

    @Override
    public TypeDescription getType() {
        return TypeDescription.Obstacle;
    }
}
