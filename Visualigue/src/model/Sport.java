/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    private HashMap<String, ObstacleDescription> obstacleDescriptions;
    private HashMap<String, BallDescription> ballDescriptions;
    private HashMap<String, PlayerDescription> playerDescriptions;
    
    public Sport(String name, String courtImage, double courtWidth, double courtHeight, int maxPLayer)
    {
        this.name = name;
        this.courtImage = courtImage;
        this.courtSize = new Vector2D(courtWidth, courtHeight);
        this.maxPlayer = maxPlayer;
        this.obstacleDescriptions = new HashMap<>();
        this.ballDescriptions = new HashMap<>();
        this.playerDescriptions = new HashMap<>();
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
    
    public void createObstacleDescription(String name, Vector2D size, String image)
    {
        this.obstacleDescriptions.put(name, new ObstacleDescription(name, size, image));
    }
    
    public void createBallDescription(String name, Vector2D size, String image)
    {
        this.ballDescriptions.put(name, new BallDescription(name, size, image));
    }
    
    public void createPlayerDescription(String name, Vector2D size, String image)
    {
        this.playerDescriptions.put(name, new PlayerDescription(name, size, image));
    }
    
    public ObstacleDescription getObstacleDescription(String name)
    {
        ObstacleDescription obstacleDescription = null;
        
        for(ObstacleDescription description : this.obstacleDescriptions.values())
        {
            if(description.getName().equals(name))
            {
               obstacleDescription = description; 
            }
        }
        
        return obstacleDescription;
    }
    
    public BallDescription getBallDescription(String name)
    {
        BallDescription ballDescription = null;
        
        for(BallDescription description : this.ballDescriptions.values())
        {
            if(description.getName().equals(name))
            {
               ballDescription = description; 
            }
        }
        
        return ballDescription;
    }
    
    public PlayerDescription getPlayerDescription(String name)
    {
        PlayerDescription playerDescription = null;
        
        for(PlayerDescription description : this.playerDescriptions.values())
        {
            if(description.getName().equals(name))
            {
               playerDescription = description; 
            }
        }
        
        return playerDescription;
    }
    
    public List<ObstacleDescription> getAllObstacleDescriptions()
    {
        return new ArrayList<>(obstacleDescriptions.values());
    }
    
    public List<BallDescription> getAllBallDescriptions()
    {
        return new ArrayList<>(ballDescriptions.values());
    }
    
    public List<PlayerDescription> getAllPlayerDescriptions()
    {
        return new ArrayList<>(playerDescriptions.values());
    }
}
