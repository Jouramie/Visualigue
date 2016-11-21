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
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.Element;
import model.Player;
import model.PlayerDescription;
import model.Vector2D;

public class StrategyEditionWindow implements Initializable, Updatable
{

    private static final char PAUSE_ICON = '⏸';
    private static final char PLAY_ICON = '⏵';

    private enum Toolbox
    {
        ADD_PLAYER, ADD_BALL, ADD_OBSTACLE, MOVE, RECORD, ZOOM
    }

    GodController controller;
    private Stage stage;
    private BorderPane root;
    private List<UIElement> uiElements;
    private UIElement selectedUIElement;
    private Toolbox selectedTool;
    private boolean draggingElement;
    private boolean userChange;

    @FXML
    private Pane scenePane;
    @FXML
    private CheckBox elementNameCheckBox;
    @FXML
    private Button deleteButton;
    @FXML
    private Button moveButton;
    @FXML
    private Button playerButton;
    @FXML
    private Button ballButton;
    @FXML
    private Button obstacleButton;
    @FXML
    private Label xCoordinate;
    @FXML
    private Label yCoordinate;
    @FXML
    private Slider timeLine;
    @FXML
    private ChoiceBox role;
    @FXML
    private ChoiceBox team;
    @FXML
    private TextField positionX;
    @FXML
    private TextField positionY;
    @FXML
    private TextField orientation;
    @FXML
    private TextField speed;
    @FXML
    private Button playPauseButton;
    @FXML
    private Label nameLabel;

    public StrategyEditionWindow(GodController controller, Stage primaryStage)
    {
        this.selectedTool = Toolbox.MOVE;
        this.controller = controller;
        this.uiElements = new ArrayList();
        this.draggingElement = false;

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

        SportEditionDialog sportEdition = new SportEditionDialog(controller, dialog);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        scenePane.setOnMousePressed(this::onMouseClicked);
        scenePane.setOnMouseMoved(this::onMouseMoved);
        scenePane.setOnMouseExited(this::onMouseExited);

        role.setOnAction(this::onActionRole);
        team.setOnAction(this::onActionTeam);
        positionX.setOnAction(this::onActionPositionX);
        positionY.setOnAction(this::onActionPositionY);
        orientation.setOnAction(this::onActionOrientation);

        Rectangle clipRect = new Rectangle(scenePane.getWidth(), scenePane.getHeight());
        clipRect.heightProperty().bind(scenePane.heightProperty());
        clipRect.widthProperty().bind(scenePane.widthProperty());
        scenePane.setClip(clipRect);

        userChange = true;
        timeLine.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) ->
        {
            onSliderValueChange();
        });

        ImageView ice = new ImageView("/res/hockey.png");
        ice.setX(500);
        ice.setY(200);
        ice.setFitWidth(1000);
        ice.setFitHeight(400);
        ice.setTranslateX(-1000 / 2);
        ice.setTranslateY(-400 / 2);
        scenePane.getChildren().add(ice);
        
        for(PlayerDescription description : controller.getAllPlayerDescriptions())
        {
            role.getItems().add(description.getName());
        }
        team.getItems().add("Team Example");

        update();
    }

    @Override
    public void update()
    {
        double t = controller.getCurrentTime();
        userChange = false;
        timeLine.setValue(t * GodController.FPS);
        timeLine.setMax((controller.getDuration() * GodController.FPS) + 10);

        List<Element> elements = controller.getAllElements();
        List<UIElement> elemToDelete = new ArrayList(uiElements);

        for (Element elem : elements)
        {
            boolean found = false;
            for (UIElement uiElem : uiElements)
            {
                if (uiElem.getElement() == elem)
                {
                    uiElem.update(t);
                    elemToDelete.remove(uiElem);
                    found = true;
                    break;
                }
            }

            if (!found)
            {
                UIElement newUIElement = new UIElement(elem, controller.getCurrentTime());
                uiElements.add(newUIElement);
                scenePane.getChildren().add(newUIElement.getGroup());
                newUIElement.getNode().setOnMousePressed(this::onMouseClickedElement);
                newUIElement.getNode().setOnKeyPressed(this::onKeyPressedElement);
                newUIElement.getElementImage().setOnMouseDragged(this::onMouseDraggedElement);
                newUIElement.getElementImage().setOnMouseReleased(this::onMouseReleasedElement);
                newUIElement.getNode().setOnMouseEntered(this::onMouseEnteredElement);
                newUIElement.getElementOrientationArrow().setOnMouseExited(this::onMouseExitedElement);
                newUIElement.getElementOrientationArrow().setOnMouseDragged(this::onMouseRotatingElement);
                newUIElement.getElementOrientationArrow().setOnMouseReleased(this::onMouseReleasedRotatingElement);

                if (elem instanceof Player)
                {
                    Player player = (Player) elem;
                    newUIElement.setElementName("PlayerName");
                }
            }
        }

        for (UIElement uiElem : elemToDelete)
        {
            uiElements.remove(uiElem);
            scenePane.getChildren().remove(uiElem.getGroup());
        }

        if (selectedUIElement != null)
        {
            Vector2D position = selectedUIElement.getElement().getPosition(controller.getCurrentTime());
            updateRightPane(position.getX(), position.getY(), Math.toDegrees(selectedUIElement.getElement().getOrientation(controller.getCurrentTime()).getAngle()));
        }
        else
        {
            updateRightPane(0, 0, 0);
        }
    }

    private void updateRightPane(double x, double y, double ori)
    {
        //set role here
        //set team here
        positionX.setText("" + x);
        positionY.setText("" + y);
        orientation.setText("" + ori);

        if (selectedUIElement != null)
        {
            boolean elementIsPlayer = selectedUIElement.getElement() instanceof Player;

            if (elementIsPlayer)
            {
                Player player = (Player) selectedUIElement.getElement();
                nameLabel.setText("PlayerName");
                elementNameCheckBox.setSelected(selectedUIElement.isElementNameVisible());
                role.getSelectionModel().select(player.getElementDescription().getName());
            }
            else
            {
                nameLabel.setText(selectedUIElement.getElement().getElementDescription().getName());
                elementNameCheckBox.setSelected(false);
                role.getSelectionModel().clearSelection();
            }

            role.setDisable(!elementIsPlayer);
            team.setDisable(!elementIsPlayer);
            positionX.setDisable(false);
            positionY.setDisable(false);
            orientation.setDisable(false);
            elementNameCheckBox.setDisable(!elementIsPlayer);
            deleteButton.setDisable(false);
        }
        else
        {
            nameLabel.setText("Nom joueur / obstacle");
            role.getSelectionModel().clearSelection();
            role.setDisable(true);
            team.setDisable(true);
            positionX.setDisable(true);
            positionY.setDisable(true);
            orientation.setDisable(true);
            elementNameCheckBox.setSelected(false);
            elementNameCheckBox.setDisable(true);
            deleteButton.setDisable(true);
        }
    }

    @Override
    public void updateOnRecord()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        if (selectedTool == Toolbox.MOVE)
        {
            Node node = (Node) e.getSource();

            for (UIElement uiElement : uiElements)
            {
                if (uiElement.getNode().equals(node))
                {
                    selectedUIElement = uiElement;
                    controller.selectElement(uiElement.getElement());
                    uiElement.getNode().requestFocus();
                    uiElement.glow();
                }
                else
                {
                    uiElement.unGlow();
                }
            }

            Vector2D position = selectedUIElement.getElement().getPosition(controller.getCurrentTime());
            updateRightPane(position.getX(), position.getY(), Math.toDegrees(selectedUIElement.getElement().getOrientation(controller.getCurrentTime()).getAngle()));
        }
    }

    private void onKeyPressedElement(KeyEvent e)
    {
        if (e.getCode() == KeyCode.DELETE)
        {
            controller.deleteCurrentElement();
            selectedUIElement = null;
            update();
        }
    }

    private void onMouseDraggedElement(MouseEvent e)
    {
        if (selectedTool == Toolbox.MOVE)
        {
            if (selectedUIElement != null)
            {
                draggingElement = true;

                Point2D point = scenePane.sceneToLocal(e.getSceneX(), e.getSceneY());
                Vector2D dimensions = controller.getCourtDimensions();

                double x = selectedUIElement.getPosition().getX();
                double y = selectedUIElement.getPosition().getY();
                Vector2D elementDimensions = selectedUIElement.getElement().getElementDescription().getSize();

                if (point.getX() - elementDimensions.getX() / 2 >= 0 && point.getX() + elementDimensions.getX() / 2 <= dimensions.getX())
                {
                    x = point.getX();
                }
                if (point.getY() - elementDimensions.getY() / 2 >= 0 && point.getY() + elementDimensions.getY() / 2 <= dimensions.getY())
                {
                    y = point.getY();
                }

                selectedUIElement.move(x, y);
                updateRightPane(point.getX(), point.getY(), Math.toDegrees(selectedUIElement.getElement().getOrientation(controller.getCurrentTime()).getAngle()));
            }
        }
    }

    private void onMouseReleasedElement(MouseEvent e)
    {
        if (selectedTool == Toolbox.MOVE && draggingElement)
        {
            if (selectedUIElement != null)
            {
                draggingElement = false;
                controller.setCurrentElemPosition(selectedUIElement.getPosition());
                update();
            }
        }
    }

    private void onMouseEnteredElement(MouseEvent e)
    {
        if (selectedTool == Toolbox.MOVE)
        {
            Node node = (Node) e.getSource();

            for (UIElement uiElem : uiElements)
            {
                if (uiElem.getNode().equals(node))
                {
                    uiElem.showOrientationArrow();
                }
            }
        }
    }

    private void onMouseExitedElement(MouseEvent e)
    {
        if (selectedTool == Toolbox.MOVE)
        {
            Node node = (Node) e.getSource();

            for (UIElement uiElem : uiElements)
            {
                if (uiElem.getElementOrientationArrow().equals(node))
                {
                    uiElem.hideOrientationArrow();
                }
            }
        }
    }

    private void onMouseRotatingElement(MouseEvent e)
    {
        if (selectedTool == Toolbox.MOVE)
        {
            selectedUIElement.setRotating(true);
            Point2D point = scenePane.sceneToLocal(e.getSceneX(), e.getSceneY());
            Vector2D mousePosition = new Vector2D(point.getX(), point.getY());
            Vector2D elementPosition = new Vector2D(selectedUIElement.getElement().getPosition(controller.getCurrentTime()).getX(), selectedUIElement.getElement().getPosition(controller.getCurrentTime()).getY());
            Vector2D result = mousePosition.substract(elementPosition);
            selectedUIElement.getNode().setRotate(Math.toDegrees(result.getAngle()));

            Vector2D position = selectedUIElement.getElement().getPosition(controller.getCurrentTime());
            updateRightPane(position.getX(), position.getY(), Math.toDegrees(result.getAngle()));
        }
    }

    private void onMouseReleasedRotatingElement(MouseEvent e)
    {
        if (selectedTool == Toolbox.MOVE)
        {
            selectedUIElement.setRotating(false);
            selectedUIElement.hideOrientationArrow();
            Point2D point = scenePane.sceneToLocal(e.getSceneX(), e.getSceneY());
            Vector2D mousePosition = new Vector2D(point.getX(), point.getY());
            Vector2D elementPosition = new Vector2D(selectedUIElement.getElement().getPosition(controller.getCurrentTime()).getX(), selectedUIElement.getElement().getPosition(controller.getCurrentTime()).getY());
            Vector2D result = mousePosition.substract(elementPosition);
            controller.setCurrentElemOrientation(result.normaliser());
            update();
        }
    }

    private void onActionRole(Event e)
    {
        if (selectedUIElement != null)
        {
            controller.setSelectedPlayerRole((String)((ChoiceBox)e.getSource()).getValue());
            selectedUIElement.refreshNode(controller.getCurrentTime());
        }

    }

    private void onActionTeam(Event e)
    {
        if (selectedUIElement != null)
        {
            System.out.println("team ChoiceBox");
        }
    }

    private void onActionPositionX(ActionEvent e)
    {
        if (selectedUIElement != null)
        {
            try
            {
                double newX = Double.parseDouble(positionX.getText());

                double x;
                double y = selectedUIElement.getElement().getPosition(controller.getCurrentTime()).getY();

                Vector2D elementDimensions = selectedUIElement.getElement().getElementDescription().getSize();
                Vector2D dimensions = controller.getCourtDimensions();

                if (newX - elementDimensions.getX() / 2 >= 0)
                {
                    if (newX + elementDimensions.getX() / 2 <= dimensions.getX())
                    {
                        x = newX;
                    }
                    else
                    {
                        x = dimensions.getX() - elementDimensions.getX() / 2;
                    }
                }
                else
                {
                    x = elementDimensions.getX() / 2;
                }

                controller.setCurrentElemPosition(new Vector2D(x, y));
                update();
            } catch (Exception exception)
            {
            }
        }
    }

    private void onActionPositionY(ActionEvent e)
    {
        if (selectedUIElement != null)
        {
            try
            {
                double newY = Double.parseDouble(positionY.getText());

                double x = selectedUIElement.getElement().getPosition(controller.getCurrentTime()).getX();
                double y;

                Vector2D elementDimensions = selectedUIElement.getElement().getElementDescription().getSize();
                Vector2D dimensions = controller.getCourtDimensions();

                if (newY - elementDimensions.getY() / 2 >= 0)
                {
                    if (newY + elementDimensions.getY() / 2 <= dimensions.getY())
                    {
                        y = newY;
                    }
                    else
                    {
                        y = dimensions.getY() - elementDimensions.getY() / 2;
                    }
                }
                else
                {
                    y = elementDimensions.getY() / 2;
                }

                controller.setCurrentElemPosition(new Vector2D(x, y));
                update();
            } catch (Exception exception)
            {
            }
        }
    }

    private void onActionOrientation(ActionEvent e)
    {
        if (selectedUIElement != null)
        {
            try
            {
                double angle = Double.parseDouble(orientation.getText());
                Vector2D ori = new Vector2D(1, 0);
                ori.setAngle(Math.toRadians(angle));
                controller.setCurrentElemOrientation(ori);
                update();
            } catch (Exception exception)
            {
            }
        }
    }

    @FXML
    private void onActionElementNameVisible(ActionEvent e)
    {
        if (selectedUIElement != null)
        {
            selectedUIElement.setElementNameVisible(elementNameCheckBox.isSelected());
        }
    }

    @FXML
    private void onActionDelete(ActionEvent e)
    {
        if (selectedUIElement != null)
        {
            controller.deleteCurrentElement();
            selectedUIElement = null;
            update();
        }
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
        this.obstacleButton.setStyle("-fx-background-color: inherit;");
        selectedTool = Toolbox.MOVE;
    }

    @FXML
    private void onActionPlayerDescription()
    {
        this.controller.selectElementDescription("Player");
        this.moveButton.setStyle("-fx-background-color: inherit;");
        this.playerButton.setStyle("-fx-background-color: lightblue;");
        this.ballButton.setStyle("-fx-background-color: inherit;");
        this.obstacleButton.setStyle("-fx-background-color: inherit;");
        selectedTool = Toolbox.ADD_PLAYER;
    }

    @FXML
    private void onActionBallDescription()
    {
        this.controller.selectElementDescription("Ball");
        this.moveButton.setStyle("-fx-background-color: inherit;");
        this.ballButton.setStyle("-fx-background-color: lightblue;");
        this.playerButton.setStyle("-fx-background-color: inherit;");
        this.obstacleButton.setStyle("-fx-background-color: inherit;");
        selectedTool = Toolbox.ADD_BALL;
    }

    @FXML
    private void onActionObstacleDescription()
    {
        this.controller.selectElementDescription("Obstacle");
        this.moveButton.setStyle("-fx-background-color: inherit;");
        this.obstacleButton.setStyle("-fx-background-color: lightblue;");
        this.playerButton.setStyle("-fx-background-color: inherit;");
        this.ballButton.setStyle("-fx-background-color: inherit;");
        selectedTool = Toolbox.ADD_OBSTACLE;
    }

    @FXML
    private void onActionPlay(ActionEvent e)
    {
        System.out.println("vue.StrategyEditionWindow.onActionPlay()");
        controller.playStrategy();
        playPauseButton.setOnAction(this::onActionPause);
        playPauseButton.setText("" + PAUSE_ICON);
    }

    private void onActionPause(ActionEvent e)
    {
        System.out.println("vue.StrategyEditionWindow.onActionPause()");
        controller.pauseStrategy();
        playPauseButton.setOnAction(this::onActionPlay);
        playPauseButton.setText("" + PLAY_ICON);
    }

    @FXML
    private void onActionRecord()
    {
        System.out.println("vue.StrategyEditionWindow.onActionRecord()");
    }

    @FXML
    private void onActionRestart()
    {
        controller.setCurrentTime(0);
        update();
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
        controller.setCurrentTime(controller.getDuration());
        update();
    }

    @FXML
    private void onActionNextFrame()
    {
        controller.setCurrentTime(controller.getCurrentTime() + (1f / GodController.FPS));
        update();
    }

    @FXML
    private void onActionPrevFrame()
    {
        controller.setCurrentTime(controller.getCurrentTime() - (1f / GodController.FPS));
        update();
    }

    @Override
    public void lastUpdate()
    {
        update();
        playPauseButton.setOnAction(this::onActionPlay);
        Platform.runLater(() ->
        {
            playPauseButton.setText("" + PLAY_ICON);
        });
    }

    public void onSliderValueChange()
    {
        if (userChange)
        {
            controller.setCurrentTime(timeLine.getValue() / controller.FPS);
            update();
        }
        else
        {
            userChange = true;
        }
    }
}
