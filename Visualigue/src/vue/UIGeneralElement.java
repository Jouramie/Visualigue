/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vue;

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

import java.util.HashMap;

public abstract class UIGeneralElement {

    static protected HashMap<Integer, Color> teamColor = new HashMap();
    protected Group rotationGroup;
    protected Group globalGroup;
    protected Element element;
    protected Color color;
    protected ImageView image;
    protected boolean isSelected;

    public UIGeneralElement(Element element) {
        this.element = element;

        this.image = new ImageView();

        this.rotationGroup = new Group();
        this.rotationGroup.getChildren().add(this.image);

        this.isSelected = false;

        this.globalGroup = this.rotationGroup;
    }

    private static void initColors() {
        teamColor.put(1, Color.BLUE);
        teamColor.put(2, Color.GREEN);
        teamColor.put(3, Color.WHITE);
        teamColor.put(4, Color.BLACK);
        teamColor.put(5, Color.RED);
        teamColor.put(6, Color.YELLOW);
        teamColor.put(7, Color.ORANGE);
        teamColor.put(8, Color.AQUA);
    }

    private static Color getColor(Integer team) {
        if (teamColor.isEmpty()) {
            initColors();
        }
        Color result = teamColor.get(team);
        if (result == null) {
            result = Color.rgb((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
            teamColor.put(team, result);
        }
        return result;
    }

    public void refreshNode() {
        this.image.setImage(ImageLoader.getImage(this.element.getElementDescription().getImage()));
        this.image.setFitWidth(this.element.getElementDescription().getSize().getX());
        this.image.setFitHeight(this.element.getElementDescription().getSize().getY());

        if (this.element instanceof Player) {
            Player player = (Player) this.element;
            InnerShadow innerShadow = new InnerShadow(player.getElementDescription().getSize().getX() / 1.8, getColor(player.getTeam()));
            this.image.setEffect(innerShadow);
            if (this.isSelected) {
                this.isSelected = false;
                addGlowEffect();
            }
        }
    }

    public Node getNode() {
        return this.globalGroup;
    }

    public Element getElement() {
        return this.element;
    }

    public void move(double x, double y) {
        this.globalGroup.setTranslateX(x - this.element.getElementDescription().getSize().getX() / 2);
        this.globalGroup.setTranslateY(y - this.element.getElementDescription().getSize().getY() / 2);
    }

    public Vector2D getPosition() {
        double x = this.globalGroup.getTranslateX() + this.element.getElementDescription().getSize().getX() / 2;
        double y = this.globalGroup.getTranslateY() + this.element.getElementDescription().getSize().getY() / 2;
        return new Vector2D(x, y);
    }

    public void update(double time) {
        move(this.element.getPosition(time).getX(), this.element.getPosition(time).getY());
        this.rotationGroup.setRotate(Math.toDegrees(this.element.getOrientation(time).getAngle()));
    }

    public void addGlowEffect() {
        if (!this.isSelected) {
            Effect eff = this.image.getEffect();
            DropShadow glow = new DropShadow();
            glow.setOffsetY(0f);
            glow.setOffsetX(0f);
            glow.setColor(Color.YELLOW);
            glow.setWidth(70);
            glow.setHeight(70);
            glow.setInput(eff);
            this.image.setEffect(glow);
            this.isSelected = true;
        }
    }

    public void removeGlowEffect() {
        if (this.isSelected) {
            if (this.image.getEffect() instanceof DropShadow) {
                DropShadow glow = (DropShadow) this.image.getEffect();
                Effect input = glow.getInput();
                this.image.setEffect(input);
            }
            this.isSelected = false;
        }
    }

    public Node getElementImage() {
        return this.image;
    }

    public Node getGroupRotation() {
        return this.rotationGroup;
    }
}
