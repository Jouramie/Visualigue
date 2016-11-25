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
public class Player extends MobileElement
{
    
    private int team;
    
    public Player(PlayerDescription desc)
    {
        this(desc, 0);
    }
    
    public Player (PlayerDescription desc, int team){
        super(desc);
        this.team = team;
    }
    
    public void setPlayerDescription(PlayerDescription description)
    {
        setElementDescription(description);
    }
    
    public void setTeam(int team)
    {
        this.team = team;
    }
    
    public int getTeam(){
        return this.team;
    }
}
