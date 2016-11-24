package vue;

import javafx.scene.layout.Pane;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import model.Vector2D;

public class Camera
{
    public static final double ZOOM_SPEED = 0.005;

    private Pane pane;
    private Scale scale;
    private Translate trans;
    private double width;
    private double height;
    private double targetX;
    private double targetY;
    private double x;
    private double y;
    private double factor;
    private double targetFactor;

    public Camera()
    {
        pane = null;
        scale = new Scale(1, 1, 0, 0);
        trans = new Translate(0, 0);
        width = 0.0;
        height = 0.0;
        x = 0.0;
        y = 0.0;
        factor = 1.0;
    }

    public void setSize(double pWidth, double pHeight)
    {
        if(pWidth > 0)
        {
            width = pWidth;
        }

        if(pHeight > 0)
        {
            height = pHeight;
        }
        
        Vector2D translation = getTranslation();
        trans.setX(translation.getX());
        trans.setY(translation.getY());
    }
    
    public void setPane(Pane pane)
    {
        this.pane = pane;
        pane.getTransforms().add(scale);
        pane.getTransforms().add(trans);
    }

    public void move(double pX, double pY)
    {
        x = pX;
        y = pY;
        
        Vector2D translation = getTranslation();
        trans.setX(translation.getX());
        trans.setY(translation.getY());
    }

    public Vector2D getVector()
    {
        return new Vector2D(x, y);
    }

    public void zoom(double delta)
    {
        factor = getFactor() + delta * ZOOM_SPEED;
        scale.setX(factor);
        scale.setY(factor);
    }

    public Vector2D getTranslation()
    {
        Vector2D result = new Vector2D();

        double posX = (1.0 / factor) * width / 2 - x;
        double posY = (1.0 / factor) * height / 2 - y;

        result.setX(posX);
        result.setY(posY);

        return result;
    }

    public double getFactor()
    {
        return factor;
    }
}
