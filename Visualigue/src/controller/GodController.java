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
        
        test = new StaticElementDescription("test", new Vector2D(40, 40), "test.jpg");
    }
    
    public void createStrategy(Sport sport, String name)
    {
        this.strategy = new Strategy(name, sport);
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
            if(currentElementDescription instanceof StaticElementDescription)
            {
                this.strategy.createStaticElement((StaticElementDescription)currentElementDescription);
            }
            /*else if(currentElementDescription instanceof BallDescription)
            {
                this.strategy.createStaticElement((BallDescription)currentElementDescription);
            }*/
            else if(currentElementDescription instanceof PlayerDescription)
            {
                this.strategy.createPlayer((PlayerDescription)currentElementDescription);
            }
        }
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
    
    public double getCurrentTime()
    {
        return this.time;
    }
}
