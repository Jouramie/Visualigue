package model;

public class BallDescription extends ElementDescription implements java.io.Serializable {
    public BallDescription(String name, Vector2D size, String image) throws ValidationException {
        super(name, size, image);
    }

    @Override
    public TypeDescription getType() {
        return TypeDescription.Ball;
    }
}
