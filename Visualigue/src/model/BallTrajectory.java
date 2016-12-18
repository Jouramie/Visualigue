/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Sim
 */
public class BallTrajectory extends Trajectory
{
    private TreeMap<Double, Player> owners;
    private Vector2D ballSize;
    
    public BallTrajectory(Vector2D ballSize)
    {
        owners = new TreeMap();
        this.ballSize = ballSize;
    }
    
    public void giveToOwner(double time, Player player)
    {
        if (owners.isEmpty()) //This could be removed... I think
        {
            owners.put(0d, player);
            return;
        }
        
        if(owners.floorEntry(time) != null && owners.floorEntry(time).getValue() != player)
        {
            owners.put(time, player);
        }
        
        if(positions.get(time) != null)
        {
            positions.remove(time);
        }
    }
    
    public void takeFromOwner(double time)
    {
        if(owners.floorEntry(time) != null && owners.floorEntry(time).getValue() != null)
        {
            positions.put(time, getBallPositionFromPlayer(time));
        }
        owners.put(time, null);
    }
    
    public void takeFromLastOwner(double time)
    {
        if(owners.floorEntry(time) != null && owners.floorEntry(time).getValue() != null)
        {
            positions.put(time, getBallPositionFromPlayer(time));
        }
        owners.put(time, null);
    }
    
    @Override
    public Vector2D getPosition(double time)
    {
        Vector2D pos;
        
        if(owners.floorEntry(time) != null && owners.floorEntry(time).getValue() != null)
        {
            if(owners.ceilingEntry(time) != null && owners.ceilingEntry(time).getValue() != null)
            {
                Vector2D lastPos = getBallPositionFromPlayer(owners.floorKey(time));
                Vector2D nextPos = getBallPositionFromPlayer(owners.ceilingKey(time));
                double delta = (time - owners.floorKey(time)) / (owners.ceilingKey(time) - owners.floorKey(time));
                pos = interpolate(lastPos, nextPos, delta);
            }
            else
            {
                pos = getBallPositionFromPlayer(time);
            }
        }
        else
        {
            Vector2D lastPos = new Vector2D();
            Vector2D nextPos = new Vector2D();
            double delta = 0;
            
            double lastOwnerTime = -Double.MAX_VALUE; //The last time the ball belonged to a player
            if(time >= owners.firstKey())
            {
                lastOwnerTime = owners.floorKey(time);
            }
            while(owners.get(lastOwnerTime) == null)
            {
                if(lastOwnerTime <= owners.firstKey())
                {
                    lastOwnerTime = -Double.MAX_VALUE;
                    break;
                }
                    
                lastOwnerTime = owners.lowerKey(lastOwnerTime);
            }
            if(lastOwnerTime != -Double.MAX_VALUE)
            {
                lastOwnerTime = owners.higherKey(lastOwnerTime);
            }
            
            double nextOwnerTime = Double.MAX_VALUE; //The next time the ball will belong to a player
            if(time <= owners.lastKey())
            {
                nextOwnerTime = owners.ceilingKey(time);
            }
            while(owners.get(nextOwnerTime) == null)
            {
                if(nextOwnerTime >= owners.lastKey())
                {
                    nextOwnerTime = Double.MAX_VALUE;
                    break;
                }
                
                nextOwnerTime = owners.higherKey(nextOwnerTime);
            }
            
            double lastPositionTime = -Double.MAX_VALUE; //The last time the position was recorded
            if(time >= positions.firstKey())
            {
                lastPositionTime = positions.floorKey(time);
            }
            
            double nextPositionTime = Double.MAX_VALUE; //The next time the position will be recorded
            if(time <= positions.lastKey())
            {
                nextPositionTime = positions.ceilingKey(time);
            }
            
            if(nextPositionTime <= nextOwnerTime) //Interpolation to ball position
            {
                if(lastPositionTime >= lastOwnerTime) //Interpolation from ball position
                {
                    lastPos = positions.get(lastPositionTime);
                    nextPos = positions.get(nextPositionTime);
                    delta = (time - lastPositionTime) / (nextPositionTime - lastPositionTime);
                }
                else //Interpolation from player
                {
                    lastPos = getBallPositionFromPlayer(lastOwnerTime);
                    nextPos = positions.get(nextPositionTime);
                    delta = (time - lastOwnerTime) / (nextPositionTime - lastOwnerTime);
                }
            }
            else //Interpolation to player
            {
                if(lastPositionTime >= lastOwnerTime) //Interpolation from ball position
                {
                    lastPos = positions.get(lastPositionTime);
                    nextPos = getBallPositionFromPlayer(nextOwnerTime);
                    delta = (time - lastPositionTime) / (nextOwnerTime - lastPositionTime);
                }
                else //Interpolation from player
                {
                    lastPos = getBallPositionFromPlayer(lastOwnerTime);
                    nextPos = getBallPositionFromPlayer(nextOwnerTime);
                    delta = (time - lastOwnerTime) / (nextOwnerTime - lastOwnerTime);
                }
            }

            pos = interpolate(lastPos, nextPos, delta);
        }
        
        return pos;
    }
    
    @Override
    public double getDuration()
    {
        double duration = 0;
        if (!positions.isEmpty() && !orientations.isEmpty() && !owners.isEmpty())
        {
            duration = Math.max(positions.lastKey(), orientations.lastKey());
            duration = Math.max(duration, owners.lastKey());
        }
        return duration;
    }
    
    private Vector2D getBallPositionFromPlayer(double time)
    {
        Player player = owners.floorEntry(time).getValue();
        Vector2D playerPos = player.getPosition(time);
        Vector2D playerOri = player.getOrientation(time);
        Vector2D size = player.getElementDescription().getSize();

        double x = playerPos.getX() + Math.cos(playerOri.getAngle())*((size.getX() + ballSize.getX())/2);
        double y = playerPos.getY() + Math.sin(playerOri.getAngle())*((size.getY() + ballSize.getY())/2);
        
        return new Vector2D(x, y);
    }
    
    private Vector2D interpolate(Vector2D lastPos, Vector2D nextPos, double delta)
    {
        Vector2D result;
        if(lastPos != null && nextPos != null)
        {
            Vector2D pos = lastPos.clone();

            Vector2D diff = nextPos.substract(lastPos);
            if (diff.equals(new Vector2D()))
            {
                return pos;
            }
            diff = diff.multiply(delta);
            result = pos.add(diff);
        }
        else
        {
            if(lastPos != null)
            {
                result = lastPos;
            }
            else if(nextPos != null)
            {
                result = nextPos;
            }
            else
            {
                result = new Vector2D();
            }
        }
        
        return result;
    }
    
    public void deletePlayer(Player player)
    {
        for(Iterator<Map.Entry<Double, Player>> it = owners.entrySet().iterator(); it.hasNext();)
        {
            Map.Entry<Double, Player> entry = it.next();
            if(entry.getValue() == player)
            {
                it.remove();
            }
        }
    }
}
