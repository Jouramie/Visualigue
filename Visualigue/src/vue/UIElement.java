package vue;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Scale;
import model.Element;
import model.Player;

public class UIElement extends UIGeneralElement {

    private static final String PATH_IMAGE_ROTATION = "/res/orientation.png";

    private final ImageView orientationArrow;
    private final Label elementName;
    private final Scale elementNameScale;
    private boolean isRotating;
    private UIGhostElement ghost;

    public UIElement(Element element, double elementNameScaleFactor) {
        super(element);
        this.isRotating = false;

        this.orientationArrow = new ImageView();
        this.orientationArrow.setVisible(false);

        this.rotationGroup.getChildren().add(this.orientationArrow);

        this.elementName = new Label();

        this.globalGroup = new Group();
        this.globalGroup.getChildren().add(this.elementName);
        this.globalGroup.getChildren().add(this.rotationGroup);

        this.elementNameScale = new Scale(elementNameScaleFactor, elementNameScaleFactor, 0, 0);
        this.elementName.getTransforms().add(this.elementNameScale);
        if (element instanceof Player) {
            this.ghost = new UIGhostElement(element);
        }
    }

    @Override
    public void refreshNode() {
        super.refreshNode();

        this.orientationArrow.setImage(ImageLoader.getImage(PATH_IMAGE_ROTATION));
        this.orientationArrow.setFitWidth(4 * this.element.getElementDescription().getSize().getX());
        this.orientationArrow.setFitHeight(4 * this.element.getElementDescription().getSize().getY());
        this.orientationArrow.setTranslateX(-1.5 * this.element.getElementDescription().getSize().getX());
        this.orientationArrow.setTranslateY(-1.5 * this.element.getElementDescription().getSize().getY());

        this.elementName.setTranslateY(this.element.getElementDescription().getSize().getY());

        setElementName(this.elementName.getText());
        if (this.ghost != null) {
            this.ghost.refreshNode();
        }
    }

    @Override
    public void update(double time) {
        super.update(time);
        if (this.element instanceof Player) {
            setElementName(this.element.getElementDescription().getName() + "\n" + ((Player) this.element).getName());
        }
        if (this.ghost != null) {
            this.ghost.update(time);
        }
    }

    @Override
    public void addGlowEffect() {
        super.addGlowEffect();
        if (this.ghost != null) {
            this.ghost.addGlowEffect();
        }
    }

    @Override
    public void removeGlowEffect() {
        super.removeGlowEffect();
        if (this.ghost != null) {
            this.ghost.removeGlowEffect();
        }
    }

    public Node getGroupName() {
        return this.globalGroup;
    }

    public void setElementName(String name) {
        this.elementName.setText(name);
        double maxWidth = 0;

        for (String line : name.split("\n")) {
            Text text = new Text(name);
            if (text.getLayoutBounds().getWidth() * this.elementNameScale.getX() > maxWidth) {
                maxWidth = text.getLayoutBounds().getWidth() * this.elementNameScale.getX();
            }
        }
        this.elementName.setTranslateX(this.element.getElementDescription().getSize().getX() / 2 - maxWidth / 2);
        this.elementName.setTextAlignment(TextAlignment.CENTER);
    }

    public void setRotating(boolean value) {
        this.isRotating = value;
    }

    public void showOrientationArrow() {
        this.orientationArrow.setVisible(true);
    }

    public void hideOrientationArrow() {
        if (!this.isRotating) {
            this.orientationArrow.setVisible(false);
        }
    }

    public Node getOrientationArrow() {
        return this.orientationArrow;
    }

    public boolean isElementNameVisible() {
        return this.elementName.isVisible();
    }

    public void setElementNameVisible(boolean visible) {
        this.elementName.setVisible(visible);
    }

    public void setElementNameZoomFactor(double factor) {
        this.elementNameScale.setX(factor);
        this.elementNameScale.setY(factor);
        setElementName(this.elementName.getText());
    }

    public Node getGhostNode() {
        return this.ghost == null ? null : this.ghost.getNode();
    }
}
