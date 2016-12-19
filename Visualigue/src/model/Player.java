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
 * @author emile
 */
public class Player extends MobileElement implements java.io.Serializable
{
    private String name;
    private int team;
    private TreeMap<Double, Ball> balls;
    
    public Player(PlayerDescription desc)
    {
        this(desc, 0);
    }
    
    public Player (PlayerDescription desc, int team){
        super(desc);
        name = "PlayerName";
        this.team = team;
        this.balls = new TreeMap();
    }
    
    public void setPlayerDescription(PlayerDescription description)
    {
        setElementDescription(description);
    }
    
    public String getName()
    {
        return name;
    }
    
    public void setName(String newName)
    {
        this.name = newName;
    }
    
    public void setTeam(int team)
    {
        this.team = team;
    }
    
    public int getTeam()
    {
        return this.team;
    }
    
    public void giveBall(double time, Ball ball)
    {
        if(this.balls.floorEntry(time) != null && this.balls.floorEntry(time).getValue() != null)
        {
            this.balls.floorEntry(time).getValue().takeFromLastOwner(time);
        }
        this.balls.put(time, ball);
        
        if(ball != null)
        {
            ball.giveToOwner(time, this);

            double lastTime = time; //The last entry where this ball belongs to the player
            while(this.balls.higherEntry(lastTime) != null && (this.balls.higherEntry(lastTime).getValue() == null || this.balls.higherEntry(lastTime).getValue() == ball))
            {
                lastTime = this.balls.higherKey(lastTime);
            }

            if(lastTime == this.balls.lastKey() && (this.balls.get(lastTime) == null || this.balls.get(lastTime) == ball))
            {
                return;
            }

            ball.takeFromOwner(this.balls.higherKey(lastTime));
        }
    }
    
    public Ball getBall(double time)
    {
        Ball ball = null;
        if(this.balls.floorEntry(time) != null)
        {
            ball = this.balls.floorEntry(time).getValue();
        }
        return ball;
    }
    
    public void deleteBall(Ball ball)
    {
        for(Iterator<Map.Entry<Double, Ball>> it = balls.entrySet().iterator(); it.hasNext();)
        {
            Map.Entry<Double, Ball> entry = it.next();
            if(entry.getValue() == ball)
            {
                it.remove();
            }
        }
    }
}
