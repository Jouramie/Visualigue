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
    
    public Player(PlayerDescription desc)
    {
        super(desc);
        name = "PlayerName";
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
}
