package controller;

import java.util.ArrayList;
import java.util.List;
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
    
    private StaticElementDescription test;
    
    public GodController()
    {
        this.sports = new ArrayList<Sport>();
        this.strategy = null;
        this.time = 0.0;
        this.currentElementDescription = null;
        this.selectedElement = null;
        
        test = new StaticElementDescription("test", new Vector2D(40, 40), "./res/test.png");
        this.strategy = new Strategy("Test", null);
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
        /*if(currentElementDescription != null)
        {
            if(currentElementDescription instanceof StaticElementDescription)
            {
                this.strategy.createStaticElement((StaticElementDescription)currentElementDescription);
            }
            else if(currentElementDescription instanceof BallDescription)
            {
                this.strategy.createStaticElement((BallDescription)currentElementDescription);
            }
            else if(currentElementDescription instanceof PlayerDescription)
            {
                this.strategy.createPlayer((PlayerDescription)currentElementDescription);
            }
        }*/
        
        Element elem = this.strategy.createStaticElement(test);
        elem.setPosition(this.time, pos, 0.0);
    }
    
    public void selectElement(Element elem)
    {
        this.selectedElement = elem;
    }
    
    public void selectElementDescription(String name)
    {
        // TODO:
        this.currentElementDescription = test;
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
