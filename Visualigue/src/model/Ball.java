/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 *
 * @author emile
 */
public class Ball extends MobileElement implements java.io.Serializable
{
    public Ball(BallDescription desc)
    {
        super(desc);
        trajectory = new BallTrajectory(description.getSize());
    }
    
    public void giveToOwner(double time, Player player)
    {
        ((BallTrajectory)trajectory).giveToOwner(time, player);
    }
    
    public void takeFromOwner(double time)
    {
        ((BallTrajectory)trajectory).takeFromOwner(time);
    }
    
    public void takeFromLastOwner(double time)
    {
        ((BallTrajectory)trajectory).takeFromLastOwner(time);
    }
    
    public void deletePlayer(Player player)
    {
        ((BallTrajectory)trajectory).deletePlayer(player);
    }
}
