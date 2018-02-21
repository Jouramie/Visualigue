package model;

public abstract class Element implements java.io.Serializable {
    protected ElementDescription description;
    private transient boolean changed;

    public Element(ElementDescription desc) {
        this.description = desc;
    }

    public abstract Vector2D getPosition(double time);

    public abstract Vector2D getOrientation(double time);

    public abstract void setPosition(double time, Vector2D position, double dt);

    public abstract void setOrientation(double time, Vector2D orientation, double dt);

    public double getTrajectoryDuration() {
        return 0.0;
    }

    public void clearTrajectoryFrom(double time) {

    }

    public ElementDescription getElementDescription() {
        return description;
    }

    protected void setElementDescription(ElementDescription description) {
        this.description = description;
    }

    public boolean isChanged() {
        return changed;
    }
}
