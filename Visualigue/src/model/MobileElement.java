package model;

public class MobileElement extends Element
{
    private Trajectory trajectory;
    
    public MobileElement()
    {
        this.trajectory = new Trajectory();
    }

    public MobileElement(Vector2D vector2D, Vector2D vector2D0, ElementDescription description) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public Vector2D getPosition(double time)
    {
        return this.trajectory.getPosition(time);
    }
    
    @Override
    public Vector2D getOrientation(double time)
    {
        return this.trajectory.getOrientation(time);
    }
    
    @Override
    public void setPosition(double time, Vector2D position, double dt)
    {
        this.trajectory.setPosition(time, dt, position);
    }
    
    @Override
    public void setOrientation(double time, Vector2D orientation, double dt)
    {
        this.trajectory.setOrientation(time, dt, orientation);
    }
    
    @Override
    public double getTrajectoryDuration()
    {
        return this.trajectory.getDuration();
    }
    
    @Override
    public void clearTrajectoryFrom(double time)
    {
        this.trajectory.flushPositions(time, this.trajectory.getDuration());
        this.trajectory.flushOrientations(time, this.trajectory.getDuration());
    }
}
