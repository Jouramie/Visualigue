package model;

import java.util.Map.Entry;
import java.util.TreeMap;

public class Trajectory
{
    private TreeMap<Double, Vector2D> positions;
    private TreeMap<Double, Vector2D> orientations;
    
    public Trajectory()
    {
        this.positions = new TreeMap();
        this.orientations = new TreeMap();
    }
    
    public void setPosition(double time, double interpolation, Vector2D position)
    {
        double beginTime = Math.max(0, time - interpolation);
        double endTime = Math.min(0, time - interpolation);
        
        Vector2D beginPos = getPosition(beginTime);
        Vector2D endPos = getPosition(endTime);
        
        flushPositions(beginTime, endTime);
        
        positions.put(beginTime, beginPos);
        positions.put(endTime, endPos);
        positions.put(time, position);
    }
    
    public void setOrientation(double time, double interpolation, Vector2D orientation)
    {
        double beginTime = Math.max(0, time - interpolation);
        double endTime = Math.min(0, time - interpolation);
        
        Vector2D beginOrientation = getOrientation(beginTime);
        Vector2D endOrientation = getOrientation(endTime);
        
        flushOrientations(beginTime, endTime);
        
        orientations.put(beginTime, beginOrientation);
        orientations.put(endTime, endOrientation);
        orientations.put(time, orientation);
    }
    
    public Vector2D getPosition(double time)
    {
        if(positions.isEmpty())
        {
            return new Vector2D();
        }
        
        Vector2D pos = positions.get(time);
        if(pos != null)
        {
            return pos;
        }
        
        Entry<Double, Vector2D> floorEntry = positions.floorEntry(time);
        Entry<Double, Vector2D> ceilingEntry = positions.ceilingEntry(time);
        if(floorEntry == null)
        {
            floorEntry = positions.firstEntry();
        }
        if(ceilingEntry == null)
        {
            ceilingEntry = positions.lastEntry();
        }
        
        Vector2D result = floorEntry.getValue().clone();
        double delta = (time - floorEntry.getKey()) / (ceilingEntry.getKey() - floorEntry.getKey());
        Vector2D diff = ceilingEntry.getValue().substract(floorEntry.getValue());
        diff.multiply(delta);
        result.add(diff);
        return result;
    }
    
    public Vector2D getOrientation(double time)
    {
        // TODO: changer pour une meilleure interpolation.
        if(orientations.isEmpty())
        {
            return new Vector2D();
        }
        
        Vector2D pos = orientations.get(time);
        if(pos != null)
        {
            return pos;
        }
        
        Entry<Double, Vector2D> floorEntry = orientations.floorEntry(time);
        Entry<Double, Vector2D> ceilingEntry = orientations.ceilingEntry(time);
        if(floorEntry == null)
        {
            floorEntry = orientations.firstEntry();
        }
        if(ceilingEntry == null)
        {
            ceilingEntry = orientations.lastEntry();
        }
        
        Vector2D result = floorEntry.getValue().clone();
        double delta = (time - floorEntry.getKey()) / (ceilingEntry.getKey() - floorEntry.getKey());
        Vector2D diff = ceilingEntry.getValue().substract(floorEntry.getValue());
        diff.multiply(delta);
        result.add(diff);
        return result;
    }
    
    public double getDuration()
    {
        return Math.max(positions.lastKey(), orientations.lastKey());
    }
    
    public void flushPositions(double begin, double end)
    {
        TreeMap<Double, Vector2D> temp = new TreeMap(positions.subMap(begin, end));
        positions = temp;
    }
    
    public void flushOrientations(double begin, double end)
    {
        TreeMap<Double, Vector2D> temp = new TreeMap(orientations.subMap(begin, end));
        orientations = temp;
    }
}