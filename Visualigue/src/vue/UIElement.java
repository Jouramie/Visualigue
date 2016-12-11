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

public class UIElement extends UIGeneralElement
{

    private static final String PATH_IMAGE_ROTATION = "/res/orientation.png";

    private Group nameGroup;

    private ImageView orientationArrow;
    private boolean isRotating;

    private Label elementName;
    private Scale elementNameScale;

    private UIGhostElement ghost;

    public UIElement(Element element, double elementNameScaleFactor)
    {
        super(element);
        isRotating = false;

        orientationArrow = new ImageView();
        orientationArrow.setVisible(false);

        rotationGroup.getChildren().add(orientationArrow);

        elementName = new Label();

        nameGroup = new Group();
        nameGroup.getChildren().add(elementName);
        nameGroup.getChildren().add(rotationGroup);

        elementNameScale = new Scale(elementNameScaleFactor, elementNameScaleFactor, 0, 0);
        elementName.getTransforms().add(elementNameScale);
        if (element instanceof Player)
        {
            ghost = new UIGhostElement(element);
        }
        node = nameGroup;
    }

    @Override
    public void refreshNode()
    {
        super.refreshNode();
        orientationArrow.setImage(ImageLoader.getImage(PATH_IMAGE_ROTATION));
        orientationArrow.setFitWidth(4 * element.getElementDescription().getSize().getX());
        orientationArrow.setFitHeight(4 * element.getElementDescription().getSize().getY());
        orientationArrow.setTranslateX(-1.5 * element.getElementDescription().getSize().getX());
        orientationArrow.setTranslateY(-1.5 * element.getElementDescription().getSize().getY());

        elementName.setTranslateY(element.getElementDescription().getSize().getY());

        setElementName(elementName.getText());
        if (ghost != null)
        {
            ghost.refreshNode();
        }
    }

    @Override
    public void update(double time)
    {
        super.update(time);
        if (element instanceof Player)
        {
            setElementName(element.getElementDescription().getName() + "\n" + ((Player) element).getName());
        }
        if (ghost != null)
        {
            ghost.update(time);
        }
    }

    @Override
    public void addGlowEffect()
    {
        super.addGlowEffect();
        if (ghost != null)
        {
            ghost.addGlowEffect();
        }
    }

    @Override
    public void removeGlowEffect()
    {
        super.removeGlowEffect();
        if (ghost != null)
        {
            ghost.removeGlowEffect();
        }
    }

    public Node getGroupName()
    {
        return nameGroup;
    }

    public void setElementName(String name)
    {
        elementName.setText(name);
        double maxWidth = 0;

        for (String line : name.split("\n"))
        {
            Text text = new Text(name);
            if (text.getLayoutBounds().getWidth() * elementNameScale.getX() > maxWidth)
            {
                maxWidth = text.getLayoutBounds().getWidth() * elementNameScale.getX();
            }
        }
        elementName.setTranslateX(element.getElementDescription().getSize().getX() / 2 - maxWidth / 2);
        elementName.setTextAlignment(TextAlignment.CENTER);
    }

    public void setRotating(boolean value)
    {
        isRotating = value;
    }

    public void showOrientationArrow()
    {
        orientationArrow.setVisible(true);
    }

    public void hideOrientationArrow()
    {
        if (!isRotating)
        {
            orientationArrow.setVisible(false);
        }
    }

    public Node getOrientationArrow()
    {
        return orientationArrow;
    }

    public boolean isElementNameVisible()
    {
        return elementName.isVisible();
    }

    public void setElementNameVisible(boolean visible)
    {
        elementName.setVisible(visible);
    }

    public void setElementNameZoomFactor(double factor)
    {
        elementNameScale.setX(factor);
        elementNameScale.setY(factor);
        setElementName(elementName.getText());
    }

    public Node getGhostNode() {
        return ghost == null ? null : ghost.getNode();
    }
}
