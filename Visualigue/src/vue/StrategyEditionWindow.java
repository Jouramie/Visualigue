package vue;

import controller.GodController;
import controller.Updatable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
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
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.Element;
import model.ElementDescription;
import model.ElementDescription.TypeDescription;
import model.Player;
import model.Vector2D;

public class StrategyEditionWindow implements Initializable, Updatable
{

    private static final double ZOOM_SPEED = 0.3;
    private static final char PAUSE_ICON = '⏸';
    private static final char PLAY_ICON = '⏵';
    private static final String TEAM_LABEL = "Équipe ";

    private enum Toolbox
    {
        ADD_PLAYER, ADD_BALL, ADD_OBSTACLE, MOVE, RECORD, ZOOM
    }

    private Stage stage;
    private BorderPane root;
    private List<UIElement> uiElements;
    private UIElement selectedUIElement;
    private Toolbox selectedTool;
    private boolean draggingElement;
    private boolean userChange;
    private ImageView terrain;

    @FXML
    private ScrollPane mainPane;
    private Pane zoomingGroup;
    private Pane scenePane;
    private Scale sceneScale;
    @FXML
    private MenuItem undoMenu;
    @FXML
    private MenuItem redoMenu;
    @FXML
    private CheckBox elementNameCheckBox;
    @FXML
    private CheckBox nbMaxPlayerCheckBox;
    @FXML
    private CheckBox visibleLabelsCheckBox;
    @FXML
    private Button deleteButton;
    @FXML
    private Button moveButton;
    @FXML
    private MenuButton playerButton;
    @FXML
    private MenuButton ballButton;
    @FXML
    private MenuButton obstacleButton;
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
    private HBox timeButtonHBox;
    private MaskField speed;
    @FXML
    private Button playPauseButton;
    @FXML
    private TextField nameTextField;

    public StrategyEditionWindow(Stage primaryStage)
    {
        this.selectedTool = Toolbox.MOVE;
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
            stage.setOnCloseRequest((event) ->
            {
                GodController.save(null);
            });
            stage.show();
        } catch (IOException ex)
        {
            Logger.getLogger(StrategyEditionWindow.class.getName()).log(Level.SEVERE, null, ex);
        }

        onActionNewStrategy(null);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        zoomingGroup = new Pane();
        scenePane = new Pane();
        zoomingGroup.getChildren().add(scenePane);
        mainPane.setContent(zoomingGroup);

        sceneScale = new Scale(1.0, 1.0, 0, 0);
        scenePane.getTransforms().add(sceneScale);

        mainPane.addEventFilter(ScrollEvent.ANY, (e) ->
        {
            onScroll(e);
        });

        scenePane.setOnMousePressed(this::onMouseClicked);
        scenePane.setOnMouseMoved(this::onMouseMoved);
        scenePane.setOnMouseExited(this::onMouseExited);

        addRightPaneListener();

        // Clipping
        Rectangle clipRect = new Rectangle(mainPane.getWidth(), mainPane.getHeight());
        clipRect.heightProperty().bind(mainPane.heightProperty());
        clipRect.widthProperty().bind(mainPane.widthProperty());
        mainPane.setClip(clipRect);

        userChange = true;
        timeLine.valueProperty().addListener((observable, oldValue, newValue) ->
        {
            onSliderValueChange();
        });

        timeLine.focusedProperty().addListener((observable, oldValue, newValue) ->
        {
            onSliderExiting();
        });

        speed = new MaskField();
        speed.setMask("xD");
        speed.setAlignment(Pos.CENTER);
        speed.setPrefHeight(25.0);
        speed.setPrefWidth(29.0);
        speed.setText("x2");
        speed.focusedProperty().addListener((observaleValue, oldValue, newValue) ->
        {
            if (!newValue)
            {
                if (speed.getText().equals("x_"))
                {
                    speed.setText("x2");
                }
            }
        });
        timeButtonHBox.getChildren().add(speed);

        terrain = new ImageView();
        scenePane.getChildren().add(terrain);
        scenePane.boundsInParentProperty().addListener((event) ->
        {
            zoomingGroup.setMinWidth(scenePane.getBoundsInParent().getWidth());
            zoomingGroup.setMinHeight(scenePane.getBoundsInParent().getHeight());
        });

        updateSport();
        update();
    }

    @Override
    public void update()
    {
        updateUndoRedo();

        double t = GodController.getInstance().getCurrentTime();
        userChange = false;
        timeLine.setValue(t * GodController.FPS);
        timeLine.setMax((GodController.getInstance().getDuration() * GodController.FPS) + 10);

        List<Element> elements = GodController.getInstance().getAllElements();
        List<UIElement> elemToDelete = new ArrayList(uiElements);

        for (Element elem : elements)
        {
            boolean found = false;
            for (UIElement uiElem : uiElements)
            {
                if (uiElem.getElement() == elem)
                {
                    uiElem.refreshNode();
                    uiElem.update(t);
                    elemToDelete.remove(uiElem);
                    found = true;
                    break;
                }
            }

            if (!found)
            {
                UIElement newUIElement = new UIElement(elem, 1 / sceneScale.getX());
                newUIElement.refreshNode();
                newUIElement.update(t);
                uiElements.add(newUIElement);
                if (newUIElement.getGhostNode() != null)
                {
                    scenePane.getChildren().add(newUIElement.getGhostNode());
                }
                scenePane.getChildren().add(newUIElement.getNode());
                newUIElement.getGroupRotation().setOnMousePressed(this::onMousePressedElement);
                newUIElement.getGroupRotation().setOnKeyPressed(this::onKeyPressedElement);
                newUIElement.getElementImage().setOnMouseDragged(this::onMouseDraggedElement);
                newUIElement.getElementImage().setOnMouseReleased(this::onMouseReleasedElement);
                newUIElement.getGroupRotation().setOnMouseEntered(this::onMouseEnteredElement);
                newUIElement.getOrientationArrow().setOnMouseExited(this::onMouseExitedElement);
                newUIElement.getOrientationArrow().setOnMouseDragged(this::onMouseRotatingElement);
                newUIElement.getOrientationArrow().setOnMouseReleased(this::onMouseReleasedRotatingElement);
            }
        }

        for (UIElement uiElem : elemToDelete)
        {
            uiElements.remove(uiElem);
            scenePane.getChildren().remove(uiElem.getNode());
            if (uiElem.getGhostNode() != null)
            {
                scenePane.getChildren().remove(uiElem.getGhostNode());
            }
        }

        if (selectedUIElement != null)
        {
            Vector2D position = selectedUIElement.getElement().getPosition(GodController.getInstance().getCurrentTime());
            updateRightPane(position.getX(), position.getY(), Math.toDegrees(selectedUIElement.getElement().getOrientation(GodController.getInstance().getCurrentTime()).getAngle()));
        }
        else
        {
            updateRightPane(0, 0, 0);
        }
    }

    private void removeRightPaneListener()
    {
        nameTextField.setOnAction(null);
        role.setOnAction(null);
        team.setOnAction(null);
        positionX.setOnAction(null);
        positionY.setOnAction(null);
        orientation.setOnAction(null);
    }

    private void addRightPaneListener()
    {
        nameTextField.setOnAction(this::onActionName);
        role.setOnAction(this::onActionRole);
        team.setOnAction(this::onActionTeam);
        positionX.setOnAction(this::onActionPositionX);
        positionY.setOnAction(this::onActionPositionY);
        orientation.setOnAction(this::onActionOrientation);
    }

    private void updateRightPane(double x, double y, double ori)
    {
        nbMaxPlayerCheckBox.setSelected(GodController.getInstance().getRespectMaxNbOfPlayers());
        updateVisibleLabelsCheckBox();
        nbMaxPlayerCheckBox.setSelected(GodController.getInstance().getRespectMaxNbOfPlayers());

        positionX.setText("" + x);
        positionY.setText("" + y);
        orientation.setText("" + ori);

        if (selectedUIElement != null)
        {
            boolean elementIsPlayer = selectedUIElement.getElement() instanceof Player;

            if (elementIsPlayer)
            {
                Player player = (Player) selectedUIElement.getElement();
                nameTextField.setDisable(false);
                nameTextField.setText(player.getName());
                elementNameCheckBox.setSelected(selectedUIElement.isElementNameVisible());

                removeRightPaneListener();
                role.getSelectionModel().select(player.getElementDescription().getName());
                team.getSelectionModel().select(TEAM_LABEL + player.getTeam());
                addRightPaneListener();
            }
            else
            {
                nameTextField.setDisable(true);
                nameTextField.setText(selectedUIElement.getElement().getElementDescription().getName());
                elementNameCheckBox.setSelected(false);
                role.getSelectionModel().clearSelection();
                team.getSelectionModel().clearSelection();
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
            nameTextField.setText("Nom joueur / obstacle");
            nameTextField.setDisable(true);
            role.getSelectionModel().clearSelection();
            role.setDisable(true);
            team.getSelectionModel().clearSelection();
            team.setDisable(true);
            positionX.setDisable(true);
            positionY.setDisable(true);
            orientation.setDisable(true);
            elementNameCheckBox.setSelected(false);
            elementNameCheckBox.setDisable(true);
            deleteButton.setDisable(true);
        }
    }

    private void updateVisibleLabelsCheckBox()
    {
        boolean visible = false;
        for (UIElement elem : uiElements)
        {
            if (elem.isElementNameVisible())
            {
                visible = true;
            }
        }
        visibleLabelsCheckBox.setSelected(visible);
    }

    private void updateElementDescriptions()
    {

        playerButton.getItems().clear();
        Object oldSelection = role.getSelectionModel().getSelectedItem();
        role.getItems().clear();
        for (ElementDescription desc : GodController.getInstance().getAllPlayerDescriptions())
        {
            Menu m = new Menu(desc.getName());
            for (int i = 0; i < GodController.getInstance().getMaxTeam(); i++)
            {
                MenuItem mi = new MenuItem(TEAM_LABEL + (i + 1));
                mi.setOnAction(this::onActionPlayerDescription);
                m.getItems().add(mi);
            }
            playerButton.getItems().add(m);
            role.getItems().add(desc.getName());
        }
        if (role.getItems().contains(oldSelection))
        {
            role.getSelectionModel().select(oldSelection);
        }

        ballButton.getItems().clear();
        for (ElementDescription desc : GodController.getInstance().getAllBallDescriptions())
        {
            MenuItem mi = new MenuItem(desc.getName());
            mi.setOnAction(this::onActionBallDescription);
            ballButton.getItems().add(mi);
        }

        obstacleButton.getItems().clear();
        for (ElementDescription desc : GodController.getInstance().getAllObstacleDescriptions())
        {
            MenuItem mi = new MenuItem(desc.getName());
            mi.setOnAction(this::onActionObstacleDescription);
            obstacleButton.getItems().add(mi);
        }

        oldSelection = team.getSelectionModel().getSelectedItem();
        team.getItems().clear();
        for (int i = 0; i < GodController.getInstance().getMaxTeam(); i++)
        {
            team.getItems().add(TEAM_LABEL + (i + 1));
        }
        if (team.getItems().contains(oldSelection))
        {
            team.getSelectionModel().select(oldSelection);
        }
    }

    private void updateSport()
    {
        Image img = ImageLoader.getImage(GodController.getInstance().getCourtImage());
        terrain.setImage(img);

        double x = GodController.getInstance().getCourtDimensions().getX();
        double y = GodController.getInstance().getCourtDimensions().getY();

        terrain.setFitWidth(x);
        terrain.setFitHeight(y);

        double factor = (double) mainPane.getWidth() / (double) terrain.getBoundsInParent().getMaxX();
        sceneScale.setX(factor);
        sceneScale.setY(factor);
        for (UIElement elem : uiElements)
        {
            if (elem.getElement() instanceof Player)
            {
                elem.setElementNameZoomFactor(1 / factor);
            }
        }

        updateElementDescriptions();

        for (UIElement elem : uiElements)
        {
            elem.refreshNode();
            elem.update(GodController.getInstance().getCurrentTime());
        }
    }

    @Override
    public void updateOnRecord()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @FXML
    private void onClose(ActionEvent e)
    {
        GodController.save(null);
        stage.close();
    }

    private void onMouseMoved(MouseEvent e)
    {
        Point2D point = scenePane.sceneToLocal(e.getSceneX(), e.getSceneY());
        if (point.getX() <= GodController.getInstance().getCourtDimensions().getX()
                && point.getY() <= GodController.getInstance().getCourtDimensions().getY())
        {
            xCoordinate.setText("" + point.getX());
            yCoordinate.setText("" + point.getY());
        }
        else
        {
            xCoordinate.setText("-");
            yCoordinate.setText("-");
        }
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
                GodController.getInstance().addElement(new Vector2D(point.getX(), point.getY()));
            } catch (Exception exception)
            {
                // TODO
            }
        }
    }

    private void onMousePressedElement(MouseEvent e)
    {
        System.out.println("ASD");
        if (selectedTool == Toolbox.MOVE)
        {
            Node node = (Node) e.getSource();

            for (UIElement uiElement : uiElements)
            {
                if (uiElement.getGroupRotation().equals(node))
                {
                    selectedUIElement = uiElement;
                    GodController.getInstance().selectElement(uiElement.getElement());
                    uiElement.getNode().requestFocus();
                    uiElement.addGlowEffect();
                }
                else
                {
                    uiElement.removeGlowEffect();
                }
            }

            Vector2D position = selectedUIElement.getElement().getPosition(GodController.getInstance().getCurrentTime());
            updateRightPane(position.getX(), position.getY(), Math.toDegrees(selectedUIElement.getElement().getOrientation(GodController.getInstance().getCurrentTime()).getAngle()));
        }
    }

    private void onKeyPressedElement(KeyEvent e)
    {
        if (e.getCode() == KeyCode.DELETE)
        {
            selectedUIElement = null;
            GodController.getInstance().deleteCurrentElement();
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
                Vector2D dimensions = GodController.getInstance().getCourtDimensions();

                double x = selectedUIElement.getPosition().getX();
                double y = selectedUIElement.getPosition().getY();
                Vector2D elementDimensions = selectedUIElement.getElement().getElementDescription().getSize();

                if (GodController.getInstance().isValidCoord(selectedUIElement.getElement().getElementDescription(), new Vector2D(point.getX(), point.getY())))
                {
                    x = point.getX();
                    y = point.getY();
                }

                selectedUIElement.move(x, y);
                updateRightPane(point.getX(), point.getY(), Math.toDegrees(selectedUIElement.getElement().getOrientation(GodController.getInstance().getCurrentTime()).getAngle()));
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
                GodController.getInstance().setCurrentElemPosition(selectedUIElement.getPosition());
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
                if (uiElem.getGroupRotation().equals(node))
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
                if (uiElem.getOrientationArrow().equals(node))
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
            Vector2D elementPosition = new Vector2D(selectedUIElement.getElement().getPosition(GodController.getInstance().getCurrentTime()).getX(), selectedUIElement.getElement().getPosition(GodController.getInstance().getCurrentTime()).getY());
            Vector2D result = mousePosition.substract(elementPosition);
            selectedUIElement.getGroupRotation().setRotate(Math.toDegrees(result.getAngle()));

            Vector2D position = selectedUIElement.getElement().getPosition(GodController.getInstance().getCurrentTime());
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
            Vector2D elementPosition = new Vector2D(selectedUIElement.getElement().getPosition(GodController.getInstance().getCurrentTime()).getX(), selectedUIElement.getElement().getPosition(GodController.getInstance().getCurrentTime()).getY());
            Vector2D result = mousePosition.substract(elementPosition);
            GodController.getInstance().setCurrentElemOrientation(result.normaliser());
        }
    }

    private void onActionName(Event e)
    {
        if (selectedUIElement != null && selectedUIElement.getElement() instanceof Player)
        {
            GodController.getInstance().setSelectedPlayerName(nameTextField.getText());
        }
    }

    private void onActionRole(Event e)
    {
        if (selectedUIElement != null)
        {
            String choix = (String) ((ChoiceBox) e.getSource()).getValue();
            if (choix != null)
            {
                GodController.getInstance().setSelectedPlayerRole((String) ((ChoiceBox) e.getSource()).getValue());
                //selectedUIElement.refreshNode(GodController.getInstance().getCurrentTime());
            }
        }

    }

    private void onActionTeam(Event e)
    {
        if (selectedUIElement != null)
        {
            String choix = (String) ((ChoiceBox) e.getSource()).getValue();
            if (choix != null)
            {
                int team = Integer.parseInt(choix.substring(TEAM_LABEL.length()));

                GodController.getInstance().setSelectedPlayerTeam(team);
                //selectedUIElement.refreshNode(GodController.getInstance().getCurrentTime());
            }
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
                double y = selectedUIElement.getElement().getPosition(GodController.getInstance().getCurrentTime()).getY();

                Vector2D elementDimensions = selectedUIElement.getElement().getElementDescription().getSize();
                Vector2D dimensions = GodController.getInstance().getCourtDimensions();

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

                GodController.getInstance().setCurrentElemPosition(new Vector2D(x, y));
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

                double x = selectedUIElement.getElement().getPosition(GodController.getInstance().getCurrentTime()).getX();
                double y;

                Vector2D elementDimensions = selectedUIElement.getElement().getElementDescription().getSize();
                Vector2D dimensions = GodController.getInstance().getCourtDimensions();

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

                GodController.getInstance().setCurrentElemPosition(new Vector2D(x, y));
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
                GodController.getInstance().setCurrentElemOrientation(ori);
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
            updateVisibleLabelsCheckBox();
        }
    }

    @FXML
    private void onActionNbMaxPlayer(ActionEvent e)
    {
        boolean nbOfPlayersRespected = true;

        for (int teamId : GodController.getInstance().getTeams())
        {
            if (GodController.getInstance().getNbOfPlayersInTeam(teamId) > GodController.getInstance().getMaxNbOfPlayers())
            {
                nbOfPlayersRespected = false;
            }
        }

        if (nbOfPlayersRespected)
        {
            GodController.getInstance().setRespectMaxNbOfPlayers(nbMaxPlayerCheckBox.isSelected());
        }
        else
        {
            nbMaxPlayerCheckBox.setSelected(false);

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur");
            alert.setContentText("Le nombre de joueurs maximum est de " + GodController.getInstance().getMaxNbOfPlayers() + " par équipe.");

            alert.showAndWait();
        }
    }

    @FXML
    private void onActionVisibleLabels(ActionEvent e)
    {
        for (UIElement elem : uiElements)
        {
            elem.setElementNameVisible(visibleLabelsCheckBox.isSelected());
        }
    }

    @FXML
    private void onActionDelete(ActionEvent e)
    {
        if (selectedUIElement != null)
        {
            selectedUIElement = null;
            GodController.getInstance().deleteCurrentElement();
        }
    }

    @FXML
    private void onOpen(ActionEvent e)
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Charger une stratégie");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("SER files", "*.ser"));
        File file = fileChooser.showOpenDialog(stage);

        if (file != null)
        {
            GodController.load(file.getPath());
            updateSport();
            update();
        }
    }

    @FXML
    private void onSave(ActionEvent e)
    {
        GodController.save(null);
    }

    @FXML
    private void onSaveAs(ActionEvent e)
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sauvegarder stratégie");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("SER files", "*.ser"));
        File file = fileChooser.showSaveDialog(stage);

        if (file != null)
        {
            GodController.save(file.getPath());
        }
    }

    @FXML
    private void onUndo(ActionEvent e)
    {
        GodController.undo();
        updateUndoRedo();
    }

    @FXML
    private void onRedo(ActionEvent e)
    {
        GodController.redo();
        updateUndoRedo();
    }

    private void updateUndoRedo()
    {
        undoMenu.setDisable(!GodController.canUndo());
        redoMenu.setDisable(!GodController.canRedo());
    }

    @FXML
    private void onActionNewStrategy(ActionEvent e)
    {
        Stage dialog = new Stage(StageStyle.TRANSPARENT);
        dialog.initStyle(StageStyle.DECORATED);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(stage);

        StrategyCreationDialog strategyCreation = new StrategyCreationDialog(dialog);
        dialog.setOnHidden((event) ->
        {
            selectedUIElement = null;
            updateSport();
            update();
        });
    }

    @FXML
    private void onActionConfigureSport(ActionEvent e)
    {
        Stage dialog = new Stage(StageStyle.TRANSPARENT);
        dialog.initStyle(StageStyle.DECORATED);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(stage);

        SportEditionDialog sportEdition = new SportEditionDialog(dialog);
        sportEdition.stage.setOnHidden((event) ->
        {
            updateSport();
        });
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

    private void onActionPlayerDescription(ActionEvent e)
    {
        MenuItem mi = (MenuItem) e.getSource();
        String player = mi.getParentMenu().getText();
        GodController.getInstance().selectElementDescription(TypeDescription.Player, player);
        GodController.getInstance().selectTeam(Integer.parseInt(mi.getText().substring(TEAM_LABEL.length())));
        this.moveButton.setStyle("-fx-background-color: inherit;");
        this.playerButton.setStyle("-fx-background-color: lightblue;");
        this.ballButton.setStyle("-fx-background-color: inherit;");
        this.obstacleButton.setStyle("-fx-background-color: inherit;");
        selectedTool = Toolbox.ADD_PLAYER;
    }

    private void onActionBallDescription(ActionEvent e)
    {
        String ball = ((MenuItem) e.getSource()).getText();
        GodController.getInstance().selectElementDescription(TypeDescription.Ball, ball);
        this.moveButton.setStyle("-fx-background-color: inherit;");
        this.ballButton.setStyle("-fx-background-color: lightblue;");
        this.playerButton.setStyle("-fx-background-color: inherit;");
        this.obstacleButton.setStyle("-fx-background-color: inherit;");
        selectedTool = Toolbox.ADD_BALL;
    }

    private void onScroll(ScrollEvent e)
    {
        double factor = sceneScale.getX() + e.getDeltaY() / e.getMultiplierY() * ZOOM_SPEED;
        sceneScale.setX(factor);
        sceneScale.setY(factor);
        for (UIElement elem : uiElements)
        {
            if (elem.getElement() instanceof Player)
            {
                elem.setElementNameZoomFactor(1 / factor);
            }
        }
        e.consume();
    }

    @FXML
    private void onActionZoomIn(ActionEvent e)
    {
        double factor = sceneScale.getX() + ZOOM_SPEED;
        sceneScale.setX(factor);
        sceneScale.setY(factor);
        for (UIElement elem : uiElements)
        {
            if (elem.getElement() instanceof Player)
            {
                elem.setElementNameZoomFactor(1 / factor);
            }
        }
    }

    @FXML
    private void onActionZoomOut(ActionEvent e)
    {
        double factor = sceneScale.getX() - ZOOM_SPEED;
        sceneScale.setX(factor);
        sceneScale.setY(factor);
        for (UIElement elem : uiElements)
        {
            if (elem.getElement() instanceof Player)
            {
                elem.setElementNameZoomFactor(1 / factor);
            }
        }
    }

    private void onActionObstacleDescription(ActionEvent e)
    {
        String obstacle = ((MenuItem) e.getSource()).getText();
        GodController.getInstance().selectElementDescription(TypeDescription.Obstacle, obstacle);
        this.moveButton.setStyle("-fx-background-color: inherit;");
        this.obstacleButton.setStyle("-fx-background-color: lightblue;");
        this.playerButton.setStyle("-fx-background-color: inherit;");
        this.ballButton.setStyle("-fx-background-color: inherit;");
        selectedTool = Toolbox.ADD_OBSTACLE;
    }

    @FXML
    private void onActionPlay(ActionEvent e)
    {
        GodController.getInstance().playStrategy(1);
        playPauseButton.setOnAction(this::onActionPause);
        playPauseButton.setText("" + PAUSE_ICON);
    }

    private void onActionPause(ActionEvent e)
    {
        GodController.getInstance().pauseStrategy();
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
        GodController.getInstance().setCurrentTime(0);
    }

    @FXML
    private void onActionRewind()
    {
        GodController.getInstance().playStrategy(-Integer.parseInt(speed.getText().substring(1)));
        playPauseButton.setOnAction(this::onActionPause);
        playPauseButton.setText("" + PAUSE_ICON);
    }

    @FXML
    private void onActionFastForward()
    {
        GodController.getInstance().playStrategy(Integer.parseInt(speed.getText().substring(1)));
        playPauseButton.setOnAction(this::onActionPause);
        playPauseButton.setText("" + PAUSE_ICON);
    }

    @FXML
    private void onActionGoToEnd()
    {
        GodController.getInstance().setCurrentTime(GodController.getInstance().getDuration());
    }

    @FXML
    private void onActionNextFrame()
    {
        GodController.getInstance().setCurrentTime(GodController.getInstance().getCurrentTime() + (1f / GodController.FPS));
    }

    @FXML
    private void onActionPrevFrame()
    {
        GodController.getInstance().setCurrentTime(GodController.getInstance().getCurrentTime() - (1f / GodController.FPS));
    }

    @FXML
    private void onActionStop(ActionEvent e)
    {
        onActionPause(e);
        GodController.getInstance().setCurrentTime(0);
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

    private void onSliderValueChange()
    {
        if (userChange)
        {
            GodController.getInstance().setCurrentTime(timeLine.getValue() / GodController.getInstance().FPS);
        }
        else
        {
            userChange = true;
        }
    }

    private void onSliderExiting()
    {
        timeLine.setValue((int) timeLine.getValue());
        GodController.getInstance().setCurrentTime(timeLine.getValue() / GodController.getInstance().FPS);
    }
}
