package model;

public abstract class MobileElement extends Element implements java.io.Serializable {
    protected Trajectory trajectory;

    public MobileElement() {
        super(null);
        this.trajectory = new Trajectory();
    }

    public MobileElement(ElementDescription description) {
        super(description);
        this.trajectory = new Trajectory();
    }

    public MobileElement(Vector2D vector2D, Vector2D vector2D0, ElementDescription description) {
        super(description);
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Vector2D getPosition(double time) {
        return this.trajectory.getPosition(time);
    }

    @Override
    public Vector2D getOrientation(double time) {
        return this.trajectory.getOrientation(time);
    }

    @Override
    public void setPosition(double time, Vector2D position, double dt) {
        this.trajectory.setPosition(time, dt, position);
    }

    @Override
    public void setOrientation(double time, Vector2D orientation, double dt) {
        this.trajectory.setOrientation(time, dt, orientation);
    }

    @Override
    public double getTrajectoryDuration() {
        return this.trajectory.getDuration();
    }

    @Override
    public void clearTrajectoryFrom(double time) {
        this.trajectory.flushPositions(time, this.trajectory.getDuration());
        this.trajectory.flushOrientations(time, this.trajectory.getDuration());
    }

    public double getPreviousKeyFrame(double currentTime) {
        return this.trajectory.getPreviousKeyFrame(currentTime);
    }

    public double getNextKeyFrame(double currentTime) {
        return this.trajectory.getNextKeyFrame(currentTime);
    }
}
