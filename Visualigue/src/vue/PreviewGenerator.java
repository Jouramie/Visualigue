package vue;

import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.transform.Scale;
import model.Element;
import model.MobileElement;
import model.Strategy;

public class PreviewGenerator {
    public PreviewGenerator() {

    }

    public Image generatePreview(Strategy strategy) {
        // Main pane
        Pane scenePane = new Pane();
        Scene scene = new Scene(scenePane, 1000, 800);
        Scale sceneScale = new Scale(1.0, 1.0, 0, 0);
        scenePane.getTransforms().add(sceneScale);

        // Create terrain
        Image imgTerrain = ImageLoader.getImage(strategy.getSport().getCourtImage());
        ImageView terrain = new ImageView(imgTerrain);

        double x = strategy.getSport().getCourtSize().getX();
        double y = strategy.getSport().getCourtSize().getY();

        terrain.setFitWidth(x);
        terrain.setFitHeight(y);

        scenePane.getChildren().add(terrain);

        double factor = 1000.0 / (double) terrain.getBoundsInParent().getMaxX();
        sceneScale.setX(factor);
        sceneScale.setY(factor);

        // Create elements
        for (Element elem : strategy.getAllElements()) {
            UIElement newUIElement = new UIElement(elem, 1.0 / factor); // TODO: Check scaling
            newUIElement.setElementNameVisible(true);
            newUIElement.refreshNode();
            newUIElement.update(0.0);
            scenePane.getChildren().add(newUIElement.getNode());
        }

        // Write the trajectory
        for (Element elem : strategy.getAllElements()) {
            if (elem instanceof MobileElement) {
                MobileElement mobile = (MobileElement) elem;

                Path path = new Path();
                path.setStroke(Color.BLUE);
                path.setStrokeWidth(2.0 / factor);
                path.getElements().add(new MoveTo(mobile.getPosition(0.0).getX(), mobile.getPosition(0.0).getY()));

                double oldTime = 0.0;
                double currentTime = mobile.getNextKeyFrame(oldTime);
                while (oldTime != currentTime) {
                    path.getElements().add(new LineTo(mobile.getPosition(currentTime).getX(), mobile.getPosition(currentTime).getY()));

                    oldTime = currentTime;
                    currentTime = mobile.getNextKeyFrame(currentTime);
                }

                scenePane.getChildren().add(path);
            }
        }

        WritableImage image = new WritableImage((int) 1000, (int) ((1000.0 / x) * y));
        scenePane.snapshot(new SnapshotParameters(), image);

        return image;
    }
}
