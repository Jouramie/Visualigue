package vue;

import controller.GodController;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.Element;

public class MainWindow implements Initializable
{

    GodController controller;
    private Stage stage;
    private BorderPane root;
    private List<UIElement> uiElements;
    private double currentTime;

    @FXML
    private Pane scenePane;

    public MainWindow(GodController controller, Stage primaryStage)
    {
        this.controller = controller;
        this.uiElements = new ArrayList();
        this.currentTime = 0;

        try
        {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/vue/MainWindow.fxml"));
            fxmlLoader.setController(this);
            root = (BorderPane) fxmlLoader.load();
            stage = primaryStage;
            Scene scene = new Scene(root, 500, 400);
            stage.setScene(scene);
            stage.setTitle("VisuaLigue");
            stage.show();
        } catch (IOException ex)
        {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        scenePane.setOnMousePressed(this::onMouseClicked);

        update();
    }

    public void update()
    {
        List<Element> elements = controller.getAllElements();
        List<UIElement> elemToDelete = new ArrayList(uiElements);

        for (Element elem : elements)
        {
            boolean found = false;
            for (UIElement uiElem : uiElements)
            {
                if (uiElem.getElement() == elem)
                {
                    uiElem.update(this.currentTime / 1000.0);
                    elemToDelete.remove(uiElem);
                    found = true;
                    break;
                }
            }

            if (!found)
            {
                UIElement newUIElement = new UIElement(elem);
                uiElements.add(newUIElement);
                scenePane.getChildren().add(newUIElement.getNode());
            }
        }

        for (UIElement uiElem : elemToDelete)
        {
            uiElements.remove(uiElem);
        }

        // On efface ce qu'il y a dans le pane
        /*scenePane.getChildren().clear();
        
        // On recrée tous les nodes nécessaires
        List<Element> elements = controller.getAllElements();
        for(Element elem : elements)
        {
            ImageView sprite = new ImageView(elem.getElementDescription().getImage());
            sprite.setX(elem.getPosition(0).getX());
            sprite.setY(elem.getPosition(0).getY());
            sprite.setFitWidth(elem.getElementDescription().getSize().getX());
            sprite.setFitHeight(elem.getElementDescription().getSize().getY());
            sprite.setTranslateX(-elem.getElementDescription().getSize().getX()/2);
            sprite.setTranslateY(-elem.getElementDescription().getSize().getY()/2);
            scenePane.getChildren().add(sprite);
        }*/
    }

    private void onMouseClicked(MouseEvent e)
    {
        Point2D point = scenePane.sceneToLocal(e.getSceneX(), e.getSceneY());
        controller.addStaticElement(point.getX(), point.getY());
        update();
    }

    @FXML
    private void onActionConfigureSport(ActionEvent e)
    {
        Stage dialog = new Stage(StageStyle.TRANSPARENT);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(stage);
        
        SportEditionDialog sportEdition = new SportEditionDialog(controller, dialog);
        
    }
}
