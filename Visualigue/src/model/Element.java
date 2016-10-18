package model;

public abstract class Element
{
    private ElementDescription description;
    private boolean changed;
    
    public abstract Vector2D getPosition(double time);
    public abstract Vector2D getOrientation(double time);
    public abstract void setPosition(double time, Vector2D position, double dt);
    public abstract void setOrientation(double time, Vector2D orientation, double dt);
    
    public double getTrajectoryDuration()
    {
        return 0.0;
    }
    
    public void clearTrajectoryFrom(double time)
    {
        
    }
    
    public ElementDescription getElementDescription()
    {
        return description;
    }
    
    public boolean isChanged()
    {
        return changed;
    }
    
    protected void setElementDescription(ElementDescription description)
    {
        
    }
}
