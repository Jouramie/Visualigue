package controller;

import java.util.ArrayList;
import java.util.List;
import model.Element;
import model.ElementDescription;
import model.MobileElement;
import model.StaticElement;
import model.Vector2D;

public class GodController
{
    private List<Element> elements;
    
    public GodController()
    {
        elements = new ArrayList();
    }
    
    public void addMobileElement(double x, double y)
    {
        ElementDescription description = new ElementDescription("test", new Vector2D(20, 20), "/res/test.png");
        MobileElement elem = new MobileElement();
        elem.setPosition(0.0, new Vector2D(x, y), 0);
        elem.setPosition(5.0, new Vector2D(x+40, y+40), 0);
        elements.add(elem);
    }
    
    public void addStaticElement(double x, double y)
    {
        ElementDescription description = new ElementDescription("test2", new Vector2D(20, 20), "/res/test.png");
        StaticElement elem = new StaticElement(new Vector2D(x, y), new Vector2D(0, 1), description);
        elements.add(elem);
    }
    
    public List<Element> getAllElements()
    {
        return elements;
    }
}
