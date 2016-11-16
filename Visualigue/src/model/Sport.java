package model;

import java.util.Map;
import java.util.TreeMap;

public class Sport
{
    private String name;
    private int maxPlayer;
    private int maxTeam;
    private String courtImage;
    private Vector2D courtSize;
    private Map<String, ObstacleDescription> obstacleDescriptions;
    private Map<String, BallDescription> ballDescriptions;
    private Map<String, PlayerDescription> playerDescriptions;
    
    public Sport(String name, String courtImage, double courtHeight, double courtWidth, int maxPlayer, int maxTeam)
    {
        this.name = name;
        this.courtImage = courtImage;
        this.courtSize = new Vector2D(courtWidth, courtHeight);
        this.maxPlayer = maxPlayer;
        this.maxTeam = maxTeam;
        
        this.obstacleDescriptions = new TreeMap();
        this.ballDescriptions = new TreeMap();
        this.playerDescriptions = new TreeMap();
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
