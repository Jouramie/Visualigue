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
public class Sport
{
    private String name;
    private int maxPlayer;
    private String courtImage;
    private Vector2D courtSize;
    
    public Sport(String name, String courtImage, double courtHeight, double courtWidth, int maxPLayer)
    {
        this.name = name;
        this.courtImage = courtImage;
        this.courtSize = new Vector2D(courtWidth, courtHeight);
        this.maxPlayer = maxPlayer;
    }
    
    public String getName()
    {
        return this.name;
    }
    
    public String getCourtImage()
    {
        return this.courtImage;
    }
    
    public Vector2D getCourtSize()
    {
        return this.courtSize;
    }
    
    public int getMaxPlayer()
    {
        return this.maxPlayer;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public void setCourtImage(String courtImage)
    {
        this.courtImage = courtImage;
    }
    
    public void setCourtSize(Vector2D courtSize)
    {
        this.courtSize = courtSize;
    }
    
    public void setMaxPlayer(int maxPlayer)
    {
        this.maxPlayer = maxPlayer;
    }
}
