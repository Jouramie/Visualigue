package vue;

import controller.GodController;
import controller.Updatable;
import java.io.File;
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

    GodController controller;
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
    private TextField speed;
    @FXML
    private Button playPauseButton;
    @FXML
    private TextField nameTextField;

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
            stage.setOnCloseRequest((event) -> {
                this.controller.save(null);
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
        
        mainPane.addEventFilter(ScrollEvent.ANY, (e) -> {
            onScroll(e);
        });
        
        scenePane.setOnMousePressed(this::onMouseClicked);
        scenePane.setOnMouseMoved(this::onMouseMoved);
        scenePane.setOnMouseExited(this::onMouseExited);

        nameTextField.setOnAction(this::onActionName);
        role.setOnAction(this::onActionRole);
        team.setOnAction(this::onActionTeam);
        positionX.setOnAction(this::onActionPositionX);
        positionY.setOnAction(this::onActionPositionY);
        orientation.setOnAction(this::onActionOrientation);

        // Clipping
        Rectangle clipRect = new Rectangle(mainPane.getWidth(), mainPane.getHeight());
        clipRect.heightProperty().bind(mainPane.heightProperty());
        clipRect.widthProperty().bind(mainPane.widthProperty());
        mainPane.setClip(clipRect);

        userChange = true;
        timeLine.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) ->
        {
            onSliderValueChange();
        });

        terrain = new ImageView();
        scenePane.getChildren().add(terrain);
        scenePane.boundsInParentProperty().addListener((event) -> {
            zoomingGroup.setMinWidth(scenePane.getBoundsInParent().getWidth());
            zoomingGroup.setMinHeight(scenePane.getBoundsInParent().getHeight());
        });
        updateSport();
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
                UIElement newUIElement = new UIElement(elem, controller.getCurrentTime(), 1/sceneScale.getX());
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
        updateVisibleLabelsCheckBox();
        nbMaxPlayerCheckBox.setSelected(controller.getRespectMaxNbOfPlayers());
        
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
                role.getSelectionModel().select(player.getElementDescription().getName());
                team.getSelectionModel().select(TEAM_LABEL + player.getTeam());
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
        for(UIElement elem : uiElements)
        {
            if(elem.isElementNameVisible())
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
        for (ElementDescription desc : controller.getAllPlayerDescriptions())
        {
            Menu m = new Menu(desc.getName());
            for (int i = 0; i < controller.getMaxTeam(); i++)
            {
                MenuItem mi = new MenuItem(TEAM_LABEL + (i + 1));
                mi.setOnAction(this::onActionPlayerDescription);
                m.getItems().add(mi);
            }
            playerButton.getItems().add(m);
            role.getItems().add(desc.getName());
        }
        if(role.getItems().contains(oldSelection))
        {
            role.getSelectionModel().select(oldSelection);
        }

        ballButton.getItems().clear();
        for (ElementDescription desc : controller.getAllBallDescriptions())
        {
            MenuItem mi = new MenuItem(desc.getName());
            mi.setOnAction(this::onActionBallDescription);
            ballButton.getItems().add(mi);
        }

        obstacleButton.getItems().clear();
        for (ElementDescription desc : controller.getAllObstacleDescriptions())
        {
            MenuItem mi = new MenuItem(desc.getName());
            mi.setOnAction(this::onActionObstacleDescription);
            obstacleButton.getItems().add(mi);
        }

        oldSelection = team.getSelectionModel().getSelectedItem();
        team.getItems().clear();
        for (int i = 0; i < controller.getMaxTeam(); i++)
        {
            team.getItems().add(TEAM_LABEL + (i + 1));
        }
        if(team.getItems().contains(oldSelection))
        {
            team.getSelectionModel().select(oldSelection);
        }
    }

    private void updateSport()
    {
        Image img = new Image(controller.getCourtImage());
        terrain.setImage(img);

        double x = controller.getCourtDimensions().getX();
        double y = controller.getCourtDimensions().getY();
        
        terrain.setFitWidth(x);
        terrain.setFitHeight(y);
        
        double factor = (double)mainPane.getWidth()/(double)terrain.getBoundsInParent().getMaxX();
        sceneScale.setX(factor);
        sceneScale.setY(factor);
        for(UIElement elem : uiElements)
        {
            if(elem.getElement() instanceof Player)
            {
                elem.setElementNameZoomFactor(1/factor);
            }
        }
        
        updateElementDescriptions();
        
        for(UIElement elem : uiElements)
        {
            elem.refreshNode(controller.getCurrentTime());
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
        controller.save(null);
        stage.close();
    }

    private void onMouseMoved(MouseEvent e)
    {
        Point2D point = scenePane.sceneToLocal(e.getSceneX(), e.getSceneY());
        if(point.getX() <= controller.getCourtDimensions().getX() &&
           point.getY() <= controller.getCourtDimensions().getY())
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
                controller.addElement(new Vector2D(point.getX(), point.getY()));
            } catch (Exception exception)
            {
                // TODO
            }
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
            selectedUIElement = null;
            controller.deleteCurrentElement();
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
                
                if(controller.isValidCoord(selectedUIElement.getElement().getElementDescription(), new Vector2D(point.getX(), point.getY())))
                {
                    x = point.getX();
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
        }
    }

    private void onActionName(Event e)
    {
        if(selectedUIElement != null && selectedUIElement.getElement() instanceof Player)
        {
            controller.setSelectedPlayerName(nameTextField.getText());
        }
    }
    
    private void onActionRole(Event e)
    {
        if (selectedUIElement != null)
        {
            String choix = (String) ((ChoiceBox) e.getSource()).getValue();
            if (choix != null)
            {
                controller.setSelectedPlayerRole((String) ((ChoiceBox) e.getSource()).getValue());
                selectedUIElement.refreshNode(controller.getCurrentTime());
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

                controller.setSelectedPlayerTeam(team);
                selectedUIElement.refreshNode(controller.getCurrentTime());
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
        
        for(int teamId : controller.getTeams())
        {
            if(controller.getNbOfPlayersInTeam(teamId) > controller.getMaxNbOfPlayers())
            {
                nbOfPlayersRespected = false;
            }
        }
        
        if(nbOfPlayersRespected)
        {
            controller.setRespectMaxNbOfPlayers(nbMaxPlayerCheckBox.isSelected());
        }
        else
        {
            nbMaxPlayerCheckBox.setSelected(false);
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur");
            alert.setContentText("Le nombre de joueurs maximum est de " + controller.getMaxNbOfPlayers() + " par équipe.");

            alert.showAndWait();
        }
    }
    
    @FXML
    private void onActionVisibleLabels(ActionEvent e)
    {
        for(UIElement elem : uiElements)
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
            controller.deleteCurrentElement();
        }
    }
    
    @FXML
    private void onOpen(ActionEvent e)
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Charger une stratégie");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("SER files", "*.ser"));
        File file = fileChooser.showOpenDialog(stage);
        
        if(file != null)
        {
            GodController newController = GodController.load(file.getPath());
            if(newController != null)
            {
                this.controller = newController;
                this.controller.setWindow(this);
                updateSport();
                update();
            }
        }
    }
    
    @FXML
    private void onSave(ActionEvent e)
    {
        controller.save(null);
    }
    
    @FXML
    private void onSaveAs(ActionEvent e)
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sauvegarder stratégie");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("SER files", "*.ser"));
        File file = fileChooser.showSaveDialog(stage);
        
        if(file != null)
        {
            controller.save(file.getPath());
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
        dialog.setOnHidden((event) -> {
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

        SportEditionDialog sportEdition = new SportEditionDialog(controller, dialog);
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
        this.controller.selectElementDescription(TypeDescription.Player, player);
        this.controller.selectTeam(Integer.parseInt(mi.getText().substring(TEAM_LABEL.length())));
        this.moveButton.setStyle("-fx-background-color: inherit;");
        this.playerButton.setStyle("-fx-background-color: lightblue;");
        this.ballButton.setStyle("-fx-background-color: inherit;");
        this.obstacleButton.setStyle("-fx-background-color: inherit;");
        selectedTool = Toolbox.ADD_PLAYER;
    }

    private void onActionBallDescription(ActionEvent e)
    {
        String ball = ((MenuItem) e.getSource()).getText();
        this.controller.selectElementDescription(TypeDescription.Ball, ball);
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
        for(UIElement elem : uiElements)
        {
            if(elem.getElement() instanceof Player)
            {
                elem.setElementNameZoomFactor(1/factor);
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
        for(UIElement elem : uiElements)
        {
            if(elem.getElement() instanceof Player)
            {
                elem.setElementNameZoomFactor(1/factor);
            }
        }
    }
    
    @FXML
    private void onActionZoomOut(ActionEvent e)
    {
        double factor = sceneScale.getX() - ZOOM_SPEED;
        sceneScale.setX(factor);
        sceneScale.setY(factor);
        for(UIElement elem : uiElements)
        {
            if(elem.getElement() instanceof Player)
            {
                elem.setElementNameZoomFactor(1/factor);
            }
        }
    }

    private void onActionObstacleDescription(ActionEvent e)
    {
        String obstacle = ((MenuItem) e.getSource()).getText();
        this.controller.selectElementDescription(TypeDescription.Obstacle, obstacle);
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
    }

    @FXML
    private void onActionNextFrame()
    {
        controller.setCurrentTime(controller.getCurrentTime() + (1f / GodController.FPS));
    }

    @FXML
    private void onActionPrevFrame()
    {
        controller.setCurrentTime(controller.getCurrentTime() - (1f / GodController.FPS));
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
        }
        else
        {
            userChange = true;
        }
    }
}
