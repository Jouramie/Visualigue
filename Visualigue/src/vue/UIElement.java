package vue;

import java.util.HashMap;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import model.Element;
import model.Player;
import model.Vector2D;

public class UIElement
{
    private Group node;
    private ImageView image;
    private ImageView orientation;
    private Color color;
    private Element element;
    private boolean rotating;
    private Group group;
    private Label elementName;
    static private HashMap<String, Image> images = new HashMap();
    static private HashMap<Integer, Color> teamColor = new HashMap();

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

        if (element instanceof Player)
        {
            Player player = (Player)element;
            InnerShadow innerShadow = new InnerShadow((double)player.getElementDescription().getSize().getX()/1.8, getColor(player.getTeam()));
            image.setEffect(innerShadow);
        }

        orientation.setImage(UIElement.getImage("/res/orientation.png"));
        orientation.setFitWidth(4 * element.getElementDescription().getSize().getX());
        orientation.setFitHeight(4 * element.getElementDescription().getSize().getY());
        orientation.setTranslateX(-1.5 * element.getElementDescription().getSize().getX());
        orientation.setTranslateY(-1.5 * element.getElementDescription().getSize().getY());

        elementName.setTranslateY(element.getElementDescription().getSize().getY());
        update(time);

        setElementName(elementName.getText());
    }

    public void update(double time)
    {
        move(element.getPosition(time).getX(), element.getPosition(time).getY());
        node.setRotate(Math.toDegrees(element.getOrientation(time).getAngle()));
        if(element instanceof Player)
        {
            setElementName(element.getElementDescription().getName() + "\n" + ((Player)element).getName());
        }
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
        group.setTranslateX(x - element.getElementDescription().getSize().getX() / 2);
        group.setTranslateY(y - element.getElementDescription().getSize().getY() / 2);
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
        if (result == null)
        {
            result = new Image(image);
            images.put(image, result);
        }

        return result;
    }

    public void glow()
    {
        Effect e = image.getEffect();
        if (e instanceof DropShadow)
        {
            return;
        }

        DropShadow borderGlow = new DropShadow();
        borderGlow.setOffsetY(0f);
        borderGlow.setOffsetX(0f);
        borderGlow.setColor(Color.YELLOW);
        borderGlow.setWidth(70);
        borderGlow.setHeight(70);
        borderGlow.setInput(e);
        image.setEffect(borderGlow);

    }

    public void unGlow()
    {
        if (image.getEffect() instanceof DropShadow)
        {
            DropShadow glow = (DropShadow) image.getEffect();
            if (glow.getInput() != null)
            {
                Effect input = glow.getInput();
                image.setEffect(input);
            }
            else
            {
                image.setEffect(null);
            }
        }
    }

    public void setElementName(String name)
    {
        elementName.setText(name);
        double maxWidth = 0;
        
        for(String line : name.split("\n"))
        {
            Text text = new Text(name);
            if(text.getLayoutBounds().getWidth() > maxWidth)
            {
                maxWidth = text.getLayoutBounds().getWidth();
            }
        }
        elementName.setTranslateX(element.getElementDescription().getSize().getX()/2 - maxWidth/2);
        elementName.setTextAlignment(TextAlignment.CENTER);
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
        if (!rotating)
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

    private static void initColors()
    {
        teamColor.put(1, Color.BLUE);
        teamColor.put(2, Color.GREEN);
        teamColor.put(3, Color.WHITE);
        teamColor.put(4, Color.BLACK);
        teamColor.put(5, Color.RED);
        teamColor.put(6, Color.YELLOW);
        teamColor.put(7, Color.ORANGE);
        teamColor.put(8, Color.AQUA);
    }

    private static Color getColor(Integer team)
    {
        if (teamColor.isEmpty())
        {
            initColors();
        }
        Color result = teamColor.get(team);
        if (result == null)
        {
            result = Color.rgb((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
            teamColor.put(team, result);
        }
        return result;
    }

}
