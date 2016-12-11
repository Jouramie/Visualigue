/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vue;

import java.util.HashMap;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import model.Element;
import model.Player;
import model.Vector2D;

/**
 *
 * @author Utilisateur
 */
public abstract class UIGeneralElement
{

    protected Group rotationGroup;
    protected Node node;
    protected Element element;
    protected Color color;
    protected ImageView image;
    protected boolean isSelected;

    static protected HashMap<Integer, Color> teamColor = new HashMap();

    public UIGeneralElement(Element element)
    {
        this.element = element;

        image = new ImageView();

        rotationGroup = new Group();
        rotationGroup.getChildren().add(image);

        isSelected = false;

        node = rotationGroup;
    }

    public void refreshNode()
    {
        image.setImage(ImageLoader.getImage(element.getElementDescription().getImage()));
        image.setFitWidth(element.getElementDescription().getSize().getX());
        image.setFitHeight(element.getElementDescription().getSize().getY());

        if (element instanceof Player)
        {
            Player player = (Player) element;
            InnerShadow innerShadow = new InnerShadow((double) player.getElementDescription().getSize().getX() / 1.8, getColor(player.getTeam()));
            image.setEffect(innerShadow);
        }

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
        node.setTranslateX(x - element.getElementDescription().getSize().getX() / 2);
        node.setTranslateY(y - element.getElementDescription().getSize().getY() / 2);
    }

    public Vector2D getPosition()
    {
        double x = node.getTranslateX() + element.getElementDescription().getSize().getX() / 2;
        double y = node.getTranslateY() + element.getElementDescription().getSize().getY() / 2;
        return new Vector2D(x, y);
    }

    public void update(double time)
    {
        move(element.getPosition(time).getX(), element.getPosition(time).getY());
        rotationGroup.setRotate(Math.toDegrees(element.getOrientation(time).getAngle()));
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

    public void addGlowEffect()
    {
        if (!isSelected)
        {
            Effect eff = image.getEffect();
            DropShadow glow = new DropShadow();
            glow.setOffsetY(0f);
            glow.setOffsetX(0f);
            glow.setColor(Color.YELLOW);
            glow.setWidth(70);
            glow.setHeight(70);
            glow.setInput(eff);
            image.setEffect(glow);
            isSelected = true;
        }
    }

    public void removeGlowEffect()
    {
        if (isSelected)
        {
            if (image.getEffect() instanceof DropShadow)
            {
                DropShadow glow = (DropShadow) image.getEffect();
                Effect input = glow.getInput();
                image.setEffect(input);
            }
            isSelected = false;
        }
    }

    public Node getElementImage()
    {
        return image;
    }

    public Node getGroupRotation()
    {
        return rotationGroup;
    }
}
