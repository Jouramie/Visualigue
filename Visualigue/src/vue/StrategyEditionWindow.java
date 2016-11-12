package vue;

import controller.GodController;
import controller.Updatable;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.Element;
import model.Vector2D;

public class StrategyEditionWindow implements Initializable, Updatable
{

    private enum Toolbox
    {
        ADD_PLAYER, ADD_BALL, ADD_OBSTACLE, MOVE, RECORD, ZOOM
    }
    
    public static final int FPS = 2;

    GodController controller;
    private Stage stage;
    private BorderPane root;
    private List<UIElement> uiElements;
    private UIElement selectedUIElement;
    private Toolbox selectedTool;

    @FXML
    private Pane scenePane;
    @FXML
    private Button moveButton;
    @FXML
    private Button playerButton;
    @FXML
    private Button ballButton;
    @FXML
    private Button staticButton;
    @FXML
    private Label xCoordinate;
    @FXML
    private Label yCoordinate;
    @FXML
    private Slider timeLine;

    public StrategyEditionWindow(GodController controller, Stage primaryStage)
    {
        this.selectedTool = Toolbox.MOVE;
        this.controller = controller;
        this.uiElements = new ArrayList();

        try
        {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/vue/StrategyEditionWindow.fxml"));
            fxmlLoader.setController(this);
            root = (BorderPane) fxmlLoader.load();
            stage = primaryStage;
            Scene scene = new Scene(root, 1000, 800);
            stage.setScene(scene);
            stage.setTitle("VisuaLigue");
            stage.show();
        } catch (IOException ex)
        {
            Logger.getLogger(StrategyEditionWindow.class.getName()).log(Level.SEVERE, null, ex);
        }

        Stage dialog = new Stage(StageStyle.TRANSPARENT);
        dialog.initStyle(StageStyle.DECORATED);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(stage);

        StrategyCreationDialog strategyCreation = new StrategyCreationDialog(controller, dialog);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        scenePane.setOnMousePressed(this::onMouseClicked);
        scenePane.setOnMouseMoved(this::onMouseMoved);
        scenePane.setOnMouseExited(this::onMouseExited);

        timeLine.setMinorTickCount(4);
        
        Rectangle clipRect = new Rectangle(scenePane.getWidth(), scenePane.getHeight());
        clipRect.heightProperty().bind(scenePane.heightProperty());
        clipRect.widthProperty().bind(scenePane.widthProperty());
        scenePane.setClip(clipRect);

        ImageView ice = new ImageView("/res/hockey.png");
        ice.setX(500);
        ice.setY(200);
        ice.setFitWidth(1000);
        ice.setFitHeight(400);
        ice.setTranslateX(-1000 / 2);
        ice.setTranslateY(-400 / 2);
        scenePane.getChildren().add(ice);

        update();
    }

    public void update()
    {
        timeLine.setValue(controller.getCurrentTime() * FPS);
        timeLine.setMax((controller.getDuration() * FPS) + 10);

        List<Element> elements = controller.getAllElements();
        List<UIElement> elemToDelete = new ArrayList(uiElements);

        for (Element elem : elements)
        {
            boolean found = false;
            for (UIElement uiElem : uiElements)
            {
                if (uiElem.getElement() == elem)
                {
                    uiElem.update(this.controller.getCurrentTime());
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
                newUIElement.getNode().setOnMousePressed(this::onMouseClickedElement);
                newUIElement.getElementImage().setOnMouseDragged(this::onMouseDraggedElement);
                newUIElement.getElementImage().setOnMouseReleased(this::onMouseReleasedElement);
                newUIElement.getNode().setOnMouseEntered(this::onMouseEnteredElement);
                newUIElement.getElementOrientationArrow().setOnMouseExited(this::onMouseExitedElement);
                newUIElement.getElementOrientationArrow().setOnMouseDragged(this::onMouseRotatingElement);
                newUIElement.getElementOrientationArrow().setOnMouseReleased(this::onMouseReleasedRotatingElement);
            }
        }

        for (UIElement uiElem : elemToDelete)
        {
            uiElements.remove(uiElem);
        }

    }
    
    private void onMouseMoved(MouseEvent e)
    {
        Point2D point = scenePane.sceneToLocal(e.getSceneX(), e.getSceneY());
        xCoordinate.setText("" + point.getX());
        yCoordinate.setText("" + point.getY());
    }
    
    

    private void onMouseExited(MouseEvent e)
    {
        xCoordinate.setText("-");
        yCoordinate.setText("-");
    }

    private void onMouseClicked(MouseEvent e)
    {
        if (selectedTool == Toolbox.ADD_BALL || selectedTool == Toolbox.ADD_OBSTACLE || selectedTool == Toolbox.ADD_PLAYER)
        {
            Point2D point = scenePane.sceneToLocal(e.getSceneX(), e.getSceneY());
            try
            {
                Element elem = controller.addElement(new Vector2D(point.getX(), point.getY()));
            } catch (Exception exception)
            {
                // TODO
            }
            update();
        }
    }

    private void onMouseClickedElement(MouseEvent e)
    {
        Node node = (Node) e.getSource();

        for (UIElement uiElement : uiElements)
        {
            if (uiElement.getNode().equals(node))
            {
                selectedUIElement = uiElement;
                controller.selectElement(uiElement.getElement());

                uiElement.glow();
            }
            else
            {
                uiElement.unGlow();
            }
        }
    }

    private void onMouseDraggedElement(MouseEvent e)
    {
        if (selectedTool == Toolbox.MOVE)
        {
            if (selectedUIElement != null)
            {
                Point2D point = scenePane.sceneToLocal(e.getSceneX(), e.getSceneY());
                selectedUIElement.move(point.getX(), point.getY());
            }
        }
    }

    private void onMouseReleasedElement(MouseEvent e)
    {
        if (selectedTool == Toolbox.MOVE)
        {
            if (selectedUIElement != null)
            {
                Point2D point = scenePane.sceneToLocal(e.getSceneX(), e.getSceneY());
                controller.setCurrentElemPosition(new Vector2D(point.getX(), point.getY()));
            }
        }
    }
    
    private void onMouseEnteredElement(MouseEvent e)
    {
        Node node = (Node)e.getSource();
        
        for (UIElement uiElem : uiElements)
        {
            if (uiElem.getNode().equals(node))
            {
                uiElem.showOrientationArrow();
            }
        }
    }
    
    private void onMouseExitedElement(MouseEvent e)
    {
        Node node = (Node)e.getSource();
        
        for (UIElement uiElem : uiElements)
        {
            if (uiElem.getElementOrientationArrow().equals(node))
            {
                uiElem.hideOrientationArrow();
            }
        }
    }
    
    private void onMouseRotatingElement(MouseEvent e)
    {
        selectedUIElement.setRotating(true);
        Point2D point = scenePane.sceneToLocal(e.getSceneX(), e.getSceneY());
        Vector2D mousePosition = new Vector2D(point.getX(), point.getY());
        Vector2D elementPosition = new Vector2D(selectedUIElement.getElement().getPosition(controller.getCurrentTime()).getX(), selectedUIElement.getElement().getPosition(controller.getCurrentTime()).getY());
        Vector2D result = mousePosition.substract(elementPosition);
        selectedUIElement.getNode().setRotate(Math.toDegrees(result.getAngle()));
    }
    
    private void onMouseReleasedRotatingElement(MouseEvent e)
    {
        selectedUIElement.setRotating(false);
        selectedUIElement.hideOrientationArrow();
        Point2D point = scenePane.sceneToLocal(e.getSceneX(), e.getSceneY());
        Vector2D mousePosition = new Vector2D(point.getX(), point.getY());
        Vector2D elementPosition = new Vector2D(selectedUIElement.getElement().getPosition(controller.getCurrentTime()).getX(), selectedUIElement.getElement().getPosition(controller.getCurrentTime()).getY());
        Vector2D result = mousePosition.substract(elementPosition);
        controller.setCurrentElemOrientation(result.normaliser());
    }

    @FXML
    private void onActionNewStrategy(ActionEvent e)
    {
        Stage dialog = new Stage(StageStyle.TRANSPARENT);
        dialog.initStyle(StageStyle.DECORATED);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(stage);

        StrategyCreationDialog strategyCreation = new StrategyCreationDialog(controller, dialog);
    }

    @FXML
    private void onActionConfigureSport(ActionEvent e)
    {
        Stage dialog = new Stage(StageStyle.TRANSPARENT);
        dialog.initStyle(StageStyle.DECORATED);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(stage);

        SportEditionDialog sportEdition = new SportEditionDialog(controller, dialog);
    }

    @FXML
    private void onActionMoveTool()
    {
        this.moveButton.setStyle("-fx-background-color: lightblue;");
        this.playerButton.setStyle("-fx-background-color: inherit;");
        this.ballButton.setStyle("-fx-background-color: inherit;");
        this.staticButton.setStyle("-fx-background-color: inherit;");
        selectedTool = Toolbox.MOVE;
    }

    @FXML
    private void onActionPlayerDescription()
    {
        this.controller.selectElementDescription("Player");
        this.moveButton.setStyle("-fx-background-color: inherit;");
        this.playerButton.setStyle("-fx-background-color: lightblue;");
        this.ballButton.setStyle("-fx-background-color: inherit;");
        this.staticButton.setStyle("-fx-background-color: inherit;");
        selectedTool = Toolbox.ADD_PLAYER;
    }

    @FXML
    private void onActionBallDescription()
    {
        this.controller.selectElementDescription("Ball");
        this.moveButton.setStyle("-fx-background-color: inherit;");
        this.ballButton.setStyle("-fx-background-color: lightblue;");
        this.playerButton.setStyle("-fx-background-color: inherit;");
        this.staticButton.setStyle("-fx-background-color: inherit;");
        selectedTool = Toolbox.ADD_BALL;
    }

    @FXML
    private void onActionStaticDescription()
    {
        this.controller.selectElementDescription("Static");
        this.moveButton.setStyle("-fx-background-color: inherit;");
        this.staticButton.setStyle("-fx-background-color: lightblue;");
        this.playerButton.setStyle("-fx-background-color: inherit;");
        this.ballButton.setStyle("-fx-background-color: inherit;");
        selectedTool = Toolbox.ADD_OBSTACLE;
    }

    @FXML
    private void onActionPlay()
    {
        System.out.println("vue.StrategyEditionWindow.onActionPlay()");
    }

    @FXML
    private void onActionRecord()
    {
        System.out.println("vue.StrategyEditionWindow.onActionRecord()");
    }

    @FXML
    private void onActionRestart()
    {
        System.out.println("vue.StrategyEditionWindow.onActionRestart()");
    }

    @FXML
    private void onActionRewind()
    {
        System.out.println("vue.StrategyEditionWindow.onActionRewind()");
    }

    @FXML
    private void onActionFastForward()
    {
        System.out.println("vue.StrategyEditionWindow.onActionFastForward()");
    }

    @FXML
    private void onActionGoToEnd()
    {
        System.out.println("vue.StrategyEditionWindow.onActionGoToEnd()");
    }    

    @FXML
    private void onActionNextFrame()
    {
        System.out.println("vue.StrategyEditionWindow.onActionNextFrame()");
        controller.setCurrentTime(controller.getCurrentTime() + (1f / FPS));
        update();
    }

    @FXML
    private void onActionPrevFrame()
    {
        System.out.println("vue.StrategyEditionWindow.onActionLastFrame()");
        controller.setCurrentTime(controller.getCurrentTime() - (1f / FPS));
        update();
    }
    
    @Override
    public void updateOnRecord()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
