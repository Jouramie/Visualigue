package vue;

import java.util.HashMap;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import model.Element;
import model.Vector2D;

public class UIElement
{
    private Group node;
    ImageView image;
    ImageView orientation;
    private Element element;
    private boolean rotating;
    private Group group;
    private Label elementName;
    static private HashMap<String, Image> images = new HashMap();
    
    public UIElement(Element element, double time)
    {
        this.element = element;
        rotating = false;
        
        image = new ImageView();
        
        orientation = new ImageView();
        orientation.setVisible(false);
        
        node = new Group();
        node.getChildren().add(image);
        node.getChildren().add(orientation);
        
        elementName = new Label();
        
        group = new Group();
        group.getChildren().add(elementName);
        group.getChildren().add(node);
        
        refreshNode(time);
    }
    
    public void refreshNode(double time)
    {
        image.setImage(UIElement.getImage(element.getElementDescription().getImage()));
        image.setFitWidth(element.getElementDescription().getSize().getX());
        image.setFitHeight(element.getElementDescription().getSize().getY());
        
        orientation.setImage(UIElement.getImage("/res/orientation.png"));
        orientation.setFitWidth(4*element.getElementDescription().getSize().getX());
        orientation.setFitHeight(4*element.getElementDescription().getSize().getY());
        orientation.setTranslateX(-1.5*element.getElementDescription().getSize().getX());
        orientation.setTranslateY(-1.5*element.getElementDescription().getSize().getY());
        
        elementName.setTranslateY(element.getElementDescription().getSize().getY());
        update(time);
        
        setElementName(elementName.getText());
    }
    
    public void update(double time)
    {
        move(element.getPosition(time).getX(), element.getPosition(time).getY());
        node.setRotate(Math.toDegrees(element.getOrientation(time).getAngle()));
    }
    
    public Node getGroup()
    {
        return group;
    }
    
    public Node getNode()
    {
        return node;
    }
    
    public Element getElement()
    {
        return element;
    }
    
    public void move(double x, double y)
    {
        group.setTranslateX(x - element.getElementDescription().getSize().getX()/2);
        group.setTranslateY(y - element.getElementDescription().getSize().getY()/2);
    }
    
    public Vector2D getPosition()
    {
        double x = group.getTranslateX() + element.getElementDescription().getSize().getX() / 2;
        double y = group.getTranslateY() + element.getElementDescription().getSize().getY() / 2;
        return new Vector2D(x, y);
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
        image.setEffect(borderGlow);
    }
    
    public void unGlow()
    {
        image.setEffect(null);
    }
    
    public void setElementName(String name)
    {
        elementName.setText(name);
        Text text = new Text(name);
        elementName.setTranslateX(element.getElementDescription().getSize().getX()/2 - text.getLayoutBounds().getWidth()/2);
    }
    
    public void setRotating(boolean value)
    {
        rotating = value;
    }
    
    public void showOrientationArrow()
    {
        orientation.setVisible(true);
    }
    
    public void hideOrientationArrow()
    {
        if(!rotating)
        {
            orientation.setVisible(false);
        }
    }
    
    public Node getElementImage()
    {
        return image;
    }
    
    public Node getElementOrientationArrow()
    {
        return orientation;
    }
    
    public boolean isElementNameVisible()
    {
        return elementName.isVisible();
    }
    
    public void setElementNameVisible(boolean visible)
    {
        elementName.setVisible(visible);
    }
}
