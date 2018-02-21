package model;

public class PlayerDescription extends ElementDescription implements java.io.Serializable {
    public PlayerDescription(String name, Vector2D size, String image) throws ValidationException {
        super(name, size, image);
    }

    @Override
    public TypeDescription getType() {
        return TypeDescription.Player;
    }
}
