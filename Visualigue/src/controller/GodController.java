package controller;

import java.util.ArrayList;
import java.util.List;
import model.BallDescription;
import model.Element;
import model.ElementDescription;
import model.PlayerDescription;
import model.Sport;
import model.StaticElementDescription;
import model.Strategy;
import model.Vector2D;

public class GodController
{
    private List<Sport> sports;
    private Strategy strategy;
    private double time;
    private ElementDescription currentElementDescription;
    private Element selectedElement;
    
    private PlayerDescription playerDescription;
    private BallDescription ballDescription;
    private StaticElementDescription staticDescription;
    
    public GodController()
    {
        this.sports = new ArrayList<Sport>();
        this.strategy = null;
        this.time = 0.0;
        this.currentElementDescription = null;
        this.selectedElement = null;
        
        // Tests values
        playerDescription = new PlayerDescription("player", new Vector2D(40, 40), "./res/player.png");
        ballDescription = new BallDescription("ball", new Vector2D(20, 20), "./res/test.png");
        staticDescription = new StaticElementDescription("static", new Vector2D(20, 20), "./res/cone.png");
        this.strategy = new Strategy("Test", null);
        
        this.sports.add(new Sport("Hockey", "hockey.png", 400, 1000, 5));
    }
    
    public void createStrategy(Sport sport, String name)
    {
        //this.strategy = new Strategy(name, sport);
    }
    
    public void saveStrategy(String path)
    {
        // TODO
    }
    
    public void loadStrategy(String path)
    {
        
    }
    
    public void addElement(Vector2D pos) throws Exception
    {
        if(currentElementDescription != null)
        {
            Element elem = null;
            
            if(currentElementDescription instanceof StaticElementDescription)
            {
                elem = this.strategy.createStaticElement((StaticElementDescription)currentElementDescription);
            }
            else if(currentElementDescription instanceof BallDescription)
            {
                elem = this.strategy.createBall((BallDescription)currentElementDescription);
            }
            else if(currentElementDescription instanceof PlayerDescription)
            {
                elem = this.strategy.createPlayer((PlayerDescription)currentElementDescription);
            }
            
            elem.setPosition(this.time, pos, 0.0);
        }
    }
    
    public void selectElement(Element elem)
    {
        this.selectedElement = elem;
    }
    
    public void selectElementDescription(String name)
    {
        if(name.equals("Player"))
        {
            this.currentElementDescription = playerDescription;
        }
        else if(name.equals("Ball"))
        {
            this.currentElementDescription = ballDescription;
        }
        else if(name.equals("Static"))
        {
            this.currentElementDescription = staticDescription;
        }
    }
    
    public void setCurrentElemPosition(Vector2D pos)
    {
        if(this.selectedElement != null)
        {
            this.selectedElement.setPosition(this.time, pos, 0.0);
        }
    }
    
    public List<Element> getAllElements()
    {
        if(this.strategy != null)
        {
            return this.strategy.getAllElements();
        }
        
        return new ArrayList<Element>();
    }
    
    public void addSport(String name, String courtImage, double courtHeight, double courtWidth, int playerNumber)
    {    
        Sport sport = new Sport(name, courtImage, courtHeight, courtWidth, playerNumber);
        sports.add(sport);
    }
    
    public void saveSport(String oldName, String newName, String courtImage, double courtHeight, double courtWidth, int playerNumber)
    {
        for(Sport sport : sports)
        {
            if(sport.getName().equals(oldName))
            {
                sport.setName(newName);
                sport.setCourtImage(courtImage);
                sport.setCourtSize(new Vector2D(courtHeight, courtWidth));
                sport.setMaxPlayer(playerNumber);
            }
        }
    }
    
    public List<Sport> getSports()
    {
        return this.sports;
    }
    
    public double getCurrentTime()
    {
        return this.time;
    }
}
