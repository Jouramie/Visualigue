package vue;

import java.util.HashMap;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import model.Element;

public class UIElement
{
    private ImageView node;
    private Element element;
    static private HashMap<String, Image> images = new HashMap();
    
    public UIElement(Element element)
    {
        this.element = element;
        
        this.node = new ImageView();
        this.node.setImage(UIElement.getImage(this.element.getElementDescription().getImage()));
        this.node.setX(this.element.getPosition(0).getX());
        this.node.setY(this.element.getPosition(0).getY());
        this.node.setFitWidth(this.element.getElementDescription().getSize().getX());
        this.node.setFitHeight(this.element.getElementDescription().getSize().getY());
        this.node.setTranslateX(-this.element.getElementDescription().getSize().getX()/2);
        this.node.setTranslateY(-this.element.getElementDescription().getSize().getY()/2);
    }
    
    public void update(double time)
    {
        node.relocate(element.getPosition(time).getX(), element.getPosition(time).getY());
        this.node.setImage(UIElement.getImage(this.element.getElementDescription().getImage()));
    }
    
    public Node getNode()
    {
        return this.node;
    }
    
    public Element getElement()
    {
        return this.element;
    }
    
    static private Image getImage(String image)
    {
        Image result = images.get(image);
        if(result == null)
        {
            result = new Image(image);
            images.put(image, result);
        }
        
        return result;
    }
    
    public void glow()
    {
        DropShadow borderGlow= new DropShadow();
        borderGlow.setOffsetY(0f);
        borderGlow.setOffsetX(0f);
        borderGlow.setColor(Color.YELLOW);
        borderGlow.setWidth(70);
        borderGlow.setHeight(70);
        node.setEffect(borderGlow);
    }
    
    public void unGlow()
    {
        node.setEffect(null);
    }
}
