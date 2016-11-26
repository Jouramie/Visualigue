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
public class Player extends MobileElement implements java.io.Serializable
{
    private String name;
    private int team;
    
    public Player(PlayerDescription desc)
    {
        this(desc, 0);
    }
    
    public Player (PlayerDescription desc, int team){
        super(desc);
        name = "PlayerName";
        this.team = team;
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
}
