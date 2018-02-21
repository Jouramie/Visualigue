package vue;

import controller.GodController;
import controller.Updatable;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
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
import model.*;
import model.ElementDescription.TypeDescription;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StrategyEditionWindow implements Initializable, Updatable {

    private static final double ZOOM_SPEED = 0.3;
    private static final char PAUSE_ICON = '⏸';
    private static final char PLAY_ICON = '⏵';
    private static final String TEAM_LABEL = "Équipe ";
    private final List<UIElement> uiElements;
    private Stage stage;
    private Scene scene;
    private BorderPane root;
    private UIElement selectedUIElement;
    private Toolbox selectedTool;
    private boolean draggingElement;
    private ImageView terrain;
    private boolean recording;
    private boolean updating;
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
    @FXML
    private TextField frameStep;

    public StrategyEditionWindow(Stage primaryStage) {
        this.selectedTool = Toolbox.MOVE;
        this.uiElements = new ArrayList<>();
        this.draggingElement = false;
        this.recording = false;
        this.updating = false;

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/vue/StrategyEditionWindow.fxml"));
            fxmlLoader.setController(this);
            this.root = fxmlLoader.load();
            this.stage = primaryStage;
            this.scene = new Scene(this.root, 1000, 800);
            this.stage.setScene(this.scene);
            this.stage.setTitle("VisuaLigue");
            this.stage.setOnCloseRequest((event) ->
            {
                GodController.save(null);
            });
            this.stage.show();
        } catch (IOException ex) {
            Logger.getLogger(StrategyEditionWindow.class.getName()).log(Level.SEVERE, null, ex);
        }

        openNewStrategy(true);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.zoomingGroup = new Pane();
        this.scenePane = new Pane();
        this.zoomingGroup.getChildren().add(this.scenePane);
        this.mainPane.setContent(this.zoomingGroup);

        this.sceneScale = new Scale(1.0, 1.0, 0, 0);
        this.scenePane.getTransforms().add(this.sceneScale);

        this.mainPane.addEventFilter(ScrollEvent.ANY, this::onScroll);
        this.mainPane.setOnKeyPressed(this::onKeyPressed);

        this.scenePane.setOnMousePressed(this::onMousePressedScene);
        this.scenePane.setOnMouseMoved(this::onMouseMovedScene);
        this.scenePane.setOnMouseExited(this::onMouseExitedScene);

        addRightPaneListener();

        // Clipping
        Rectangle clipRect = new Rectangle(this.mainPane.getWidth(), this.mainPane.getHeight());
        clipRect.heightProperty().bind(this.mainPane.heightProperty());
        clipRect.widthProperty().bind(this.mainPane.widthProperty());
        this.mainPane.setClip(clipRect);

        this.timeLine.valueProperty().addListener((observable, oldValue, newValue) ->
        {
            onValueChangeSlider();
        });

        this.speed = new MaskField();
        this.speed.setMask("xD");
        this.speed.setAlignment(Pos.CENTER);
        this.speed.setPrefHeight(25.0);
        this.speed.setPrefWidth(29.0);
        this.speed.setText("x2");
        this.speed.focusedProperty().addListener((observaleValue, oldValue, newValue) ->
        {
            if (!newValue) {
                if (this.speed.getText().equals("x_")) {
                    this.speed.setText("x2");
                }
            }
        });
        this.timeButtonHBox.getChildren().add(this.speed);

        this.terrain = new ImageView();
        this.scenePane.getChildren().add(this.terrain);
        this.scenePane.boundsInParentProperty().addListener((event) ->
        {
            this.zoomingGroup.setMinWidth(this.scenePane.getBoundsInParent().getWidth());
            this.zoomingGroup.setMinHeight(this.scenePane.getBoundsInParent().getHeight());
        });

        this.frameStep.textProperty().addListener((observable, oldValue, newValue) ->
        {
            try {
                int value = Integer.parseInt(newValue);
            } catch (NumberFormatException ex) {
                this.frameStep.setText(oldValue);
            }
        });
    }

    @Override
    public void update() {
        this.updating = true;
        updateUndoRedo();

        double t = GodController.getInstance().getCurrentTime();
        this.timeLine.setValue(t * GodController.FPS_EDIT);
        this.timeLine.setMax((GodController.getInstance().getDuration() * GodController.FPS_EDIT) + 10);
        ObservableList<Node> scenePaneChildren = FXCollections.observableArrayList(this.scenePane.getChildren());
        this.selectedUIElement = null;

        List<Element> elements = GodController.getInstance().getAllElements();
        List<UIElement> elemToDelete = new ArrayList<>(this.uiElements);

        for (Element elem : elements) {
            boolean isSelectedElement = elem == GodController.getInstance().getSelectedElement();
            boolean found = false;
            for (UIElement uiElem : this.uiElements) {
                if (uiElem.getElement() == elem) {
                    if (isSelectedElement) {
                        this.selectedUIElement = uiElem;
                        uiElem.addGlowEffect();
                    } else {
                        uiElem.removeGlowEffect();
                    }
                    uiElem.update(t);
                    elemToDelete.remove(uiElem);
                    found = true;
                    break;
                }
            }

            if (!found) {
                UIElement uiElem = new UIElement(elem, 1 / this.sceneScale.getX());
                uiElem.refreshNode();
                uiElem.update(t);
                this.uiElements.add(uiElem);
                if (uiElem.getGhostNode() != null) {
                    scenePaneChildren.add(uiElem.getGhostNode());
                }
                scenePaneChildren.add(uiElem.getNode());
                uiElem.getGroupRotation().setOnMousePressed(this::onMousePressedElement);
                uiElem.getGroupRotation().setOnKeyPressed(this::onKeyPressed);
                uiElem.getElementImage().setOnMouseDragged(this::onMouseDraggedElement);
                uiElem.getElementImage().setOnMouseReleased(this::onMouseReleasedElement);
                uiElem.getGroupRotation().setOnMouseEntered(this::onMouseEnteredElement);
                uiElem.getOrientationArrow().setOnMouseExited(this::onMouseExitedElement);
                uiElem.getOrientationArrow().setOnMouseDragged(this::onMouseDraggedRotatingElement);
                uiElem.getOrientationArrow().setOnMouseReleased(this::onMouseReleasedRotatingElement);

                if (isSelectedElement) {
                    this.selectedUIElement = uiElem;
                    uiElem.addGlowEffect();
                } else {
                    uiElem.removeGlowEffect();
                }
            }
        }

        for (UIElement uiElem : elemToDelete) {
            this.uiElements.remove(uiElem);
            scenePaneChildren.remove(uiElem.getNode());
            if (uiElem.getGhostNode() != null) {
                scenePaneChildren.remove(uiElem.getGhostNode());
            }
            if (this.selectedUIElement == uiElem) {
                this.selectedUIElement = null;
            }
        }

        scenePaneChildren.sort((o1, o2) ->
        {
            int value;
            if (o1 instanceof ImageView) {
                value = Integer.MIN_VALUE;
            } else if (o2 instanceof ImageView) {
                value = Integer.MAX_VALUE;
            } else {
                value = Double.compare(o1.getOpacity(), o2.getOpacity());
            }
            return value;
        });

        this.scenePane.getChildren().setAll(scenePaneChildren);

        updateRightPane();
        this.updating = false;
    }

    private void removeRightPaneListener() {
        this.nameTextField.setOnAction(null);
        this.nameTextField.focusedProperty().removeListener((obsevable, oldValue, newValue) ->
        {
            if (!newValue) {
                onActionName(null);
            }
        });
        this.role.setOnAction(null);
        this.team.setOnAction(null);
        this.positionX.setOnAction(null);
        this.positionX.focusedProperty().removeListener((obsevable, oldValue, newValue) ->
        {
            if (!newValue) {
                onActionPositionX(null);
            }
        });
        this.positionY.setOnAction(null);
        this.positionY.focusedProperty().removeListener((obsevable, oldValue, newValue) ->
        {
            if (!newValue) {
                onActionPositionY(null);
            }
        });
        this.orientation.setOnAction(null);
        this.orientation.focusedProperty().removeListener((obsevable, oldValue, newValue) ->
        {
            if (!newValue) {
                onActionOrientation(null);
            }
        });
    }

    private void addRightPaneListener() {
        this.nameTextField.setOnAction(this::onActionName);
        this.nameTextField.focusedProperty().addListener((obsevable, oldValue, newValue) ->
        {
            if (!newValue) {
                onActionName(null);
            }
        });
        this.role.setOnAction(this::onActionRole);
        this.team.setOnAction(this::onActionTeam);
        this.positionX.setOnAction(this::onActionPositionX);
        this.positionX.focusedProperty().addListener((obsevable, oldValue, newValue) ->
        {
            if (!newValue) {
                onActionPositionX(null);
            }
        });
        this.positionY.setOnAction(this::onActionPositionY);
        this.positionY.focusedProperty().addListener((obsevable, oldValue, newValue) ->
        {
            if (!newValue) {
                onActionPositionY(null);
            }
        });
        this.orientation.setOnAction(this::onActionOrientation);
        this.orientation.focusedProperty().addListener((obsevable, oldValue, newValue) ->
        {
            if (!newValue) {
                onActionOrientation(null);
            }
        });
    }

    private void updateRightPane() {
        if (this.selectedUIElement != null) {
            double time = GodController.getInstance().getCurrentTime();
            updateRightPane(this.selectedUIElement.getElement().getPosition(time).getX(), this.selectedUIElement.getElement().getPosition(time).getY(), Math.toDegrees(this.selectedUIElement.getElement().getOrientation(time).getAngle()));
        } else {
            updateRightPane(0, 0, 0);
        }
    }

    private void updateRightPane(double x, double y, double ori) {
        this.nbMaxPlayerCheckBox.setSelected(GodController.getInstance().getRespectMaxNbOfPlayers());

        this.positionX.setText(String.format("%1$.2f", x));
        this.positionY.setText(String.format("%1$.2f", y));
        this.orientation.setText(String.format("%1$.2f", ori));

        if (this.selectedUIElement != null) {
            boolean elementIsPlayer = this.selectedUIElement.getElement() instanceof Player;

            if (elementIsPlayer) {
                Player player = (Player) this.selectedUIElement.getElement();
                this.nameTextField.setDisable(false);
                this.nameTextField.setText(player.getName());

                removeRightPaneListener();
                this.role.getSelectionModel().select(player.getElementDescription().getName());
                this.team.getSelectionModel().select(TEAM_LABEL + player.getTeam());
                addRightPaneListener();
            } else {
                this.nameTextField.setDisable(true);
                this.nameTextField.setText(this.selectedUIElement.getElement().getElementDescription().getName());
                this.role.getSelectionModel().clearSelection();
                this.team.getSelectionModel().clearSelection();
            }

            this.role.setDisable(!elementIsPlayer);
            this.team.setDisable(!elementIsPlayer);
            this.positionX.setDisable(false);
            this.positionY.setDisable(false);
            this.orientation.setDisable(false);
            this.deleteButton.setDisable(false);
        } else {
            this.nameTextField.setText("Nom joueur / obstacle");
            this.nameTextField.setDisable(true);
            this.role.getSelectionModel().clearSelection();
            this.role.setDisable(true);
            this.team.getSelectionModel().clearSelection();
            this.team.setDisable(true);
            this.positionX.setDisable(true);
            this.positionY.setDisable(true);
            this.orientation.setDisable(true);
            this.deleteButton.setDisable(true);
        }
    }

    private void updateElementDescriptions() {

        this.playerButton.getItems().clear();
        Object oldSelection = this.role.getSelectionModel().getSelectedItem();
        this.role.getItems().clear();
        for (ElementDescription desc : GodController.getInstance().getAllPlayerDescriptions()) {
            Menu m = new Menu(desc.getName());
            for (int i = 0; i < GodController.getInstance().getMaxTeam(); i++) {
                MenuItem mi = new MenuItem(TEAM_LABEL + (i + 1));
                mi.setOnAction(this::onActionPlayerDescription);
                m.getItems().add(mi);
            }
            this.playerButton.getItems().add(m);
            this.role.getItems().add(desc.getName());
        }
        if (this.role.getItems().contains(oldSelection)) {
            this.role.getSelectionModel().select(oldSelection);
        }

        this.ballButton.getItems().clear();
        for (ElementDescription desc : GodController.getInstance().getAllBallDescriptions()) {
            MenuItem mi = new MenuItem(desc.getName());
            mi.setOnAction(this::onActionBallDescription);
            this.ballButton.getItems().add(mi);
        }

        this.obstacleButton.getItems().clear();
        for (ElementDescription desc : GodController.getInstance().getAllObstacleDescriptions()) {
            MenuItem mi = new MenuItem(desc.getName());
            mi.setOnAction(this::onActionObstacleDescription);
            this.obstacleButton.getItems().add(mi);
        }

        oldSelection = this.team.getSelectionModel().getSelectedItem();
        this.team.getItems().clear();
        for (int i = 0; i < GodController.getInstance().getMaxTeam(); i++) {
            this.team.getItems().add(TEAM_LABEL + (i + 1));
        }
        if (this.team.getItems().contains(oldSelection)) {
            this.team.getSelectionModel().select(oldSelection);
        }
    }

    private void updateSport() {
        Image img = ImageLoader.getImage(GodController.getInstance().getCourtImage());
        this.terrain.setImage(img);

        double x = GodController.getInstance().getCourtDimensions().getX();
        double y = GodController.getInstance().getCourtDimensions().getY();

        this.terrain.setFitWidth(x);
        this.terrain.setFitHeight(y);

        double factor = this.mainPane.getWidth() / this.terrain.getBoundsInParent().getMaxX();
        if (factor == 0) {
            factor = 1;
        }
        this.sceneScale.setX(factor);
        this.sceneScale.setY(factor);
        for (UIElement elem : this.uiElements) {
            if (elem.getElement() instanceof Player) {
                elem.setElementNameZoomFactor(1 / factor);
            }
        }

        updateElementDescriptions();

        for (UIElement elem : this.uiElements) {
            elem.refreshNode();
            elem.update(GodController.getInstance().getCurrentTime());
        }
    }

    @Override
    public Vector2D updateOnRecord(MobileElement mobile) {
        double t = GodController.getInstance().getCurrentTime();
        this.timeLine.setValue(t * GodController.FPS_EDIT);
        this.timeLine.setMax((GodController.getInstance().getDuration() * GodController.FPS_EDIT) + 10);

        for (UIElement uiElem : this.uiElements) {
            if (uiElem.getElement() != mobile) {
                uiElem.update(t);
            }
        }

        return this.selectedUIElement.getPosition();
    }

    @FXML
    private void onClose(ActionEvent e) {
        GodController.save(null);
        this.stage.close();
    }

    private void onMouseMovedScene(MouseEvent e) {
        Point2D point = this.scenePane.sceneToLocal(e.getSceneX(), e.getSceneY());
        if (point.getX() <= GodController.getInstance().getCourtDimensions().getX()
                && point.getY() <= GodController.getInstance().getCourtDimensions().getY()) {
            this.xCoordinate.setText(String.format("%1$.2f", point.getX()));
            this.yCoordinate.setText(String.format("%1$.2f", point.getY()));
        } else {
            this.xCoordinate.setText("-");
            this.yCoordinate.setText("-");
        }
    }

    private void onMouseExitedScene(MouseEvent e) {
        this.xCoordinate.setText("-");
        this.yCoordinate.setText("-");
    }

    private void onMousePressedScene(MouseEvent e) {
        if (this.selectedTool == Toolbox.ADD_BALL || this.selectedTool == Toolbox.ADD_OBSTACLE || this.selectedTool == Toolbox.ADD_PLAYER) {
            Point2D point = this.scenePane.sceneToLocal(e.getSceneX(), e.getSceneY());
            try {
                Element elem = GodController.getInstance().addElement(new Vector2D(point.getX(), point.getY()));
                GodController.getInstance().selectElement(elem);
            } catch (Exception exception) {
                // TODO
            }
        }
    }

    private void onMousePressedElement(MouseEvent e) {
        if (this.selectedTool == Toolbox.MOVE || this.selectedTool == Toolbox.RECORD) {
            this.mainPane.requestFocus();
            Node node = (Node) e.getSource();

            for (UIElement uiElement : this.uiElements) {
                if (uiElement.getGroupRotation().equals(node)) {
                    if (uiElement != this.selectedUIElement) {
                        GodController.getInstance().selectElement(uiElement.getElement());
                    }
                }
            }

            if (this.selectedTool == Toolbox.RECORD && this.selectedUIElement.getElement() instanceof MobileElement) {
                this.recording = true;
                GodController.getInstance().beginRecording((MobileElement) this.selectedUIElement.getElement());
            }
        }
    }

    private void onKeyPressed(KeyEvent e) {
        if (e.getCode() == KeyCode.DELETE) {
            this.selectedUIElement = null;
            GodController.getInstance().deleteCurrentElement();
        }
    }

    private void onMouseDraggedElement(MouseEvent e) {
        if (this.selectedTool == Toolbox.MOVE || this.selectedTool == Toolbox.RECORD) {
            if (this.selectedUIElement != null) {
                this.draggingElement = true;

                Point2D point = this.scenePane.sceneToLocal(e.getSceneX(), e.getSceneY());

                double x = this.selectedUIElement.getPosition().getX();
                double y = this.selectedUIElement.getPosition().getY();

                Vector2D elementDimensions = this.selectedUIElement.getElement().getElementDescription().getSize();

                if (GodController.getInstance().isValidCoord(this.selectedUIElement.getElement().getElementDescription(), new Vector2D(point.getX(), point.getY())) && GodController.getInstance().isInterpolationValid(new Vector2D(point.getX(), point.getY()))) {
                    x = point.getX();
                    y = point.getY();
                }

                this.selectedUIElement.move(x, y);
                updateRightPane(point.getX(), point.getY(), Math.toDegrees(this.selectedUIElement.getElement().getOrientation(GodController.getInstance().getCurrentTime()).getAngle()));
            }
        }
    }

    private void onMouseReleasedElement(MouseEvent e) {
        if (this.selectedTool == Toolbox.MOVE && this.draggingElement) {
            if (this.selectedUIElement != null) {
                this.draggingElement = false;
                GodController.getInstance().setCurrentElemPosition(this.selectedUIElement.getPosition());
            }
        } else if (this.selectedTool == Toolbox.RECORD) {
            if (this.selectedUIElement != null) {
                this.draggingElement = false;
                GodController.getInstance().stopRecording();
                this.recording = false;
                this.selectedTool = Toolbox.MOVE;
            }
        }
    }

    private void onMouseEnteredElement(MouseEvent e) {
        if (this.selectedTool == Toolbox.MOVE) {
            Node node = (Node) e.getSource();

            for (UIElement uiElem : this.uiElements) {
                if (uiElem.getGroupRotation().equals(node)) {
                    uiElem.showOrientationArrow();
                } else {
                    uiElem.hideOrientationArrow();
                }
            }
        }
    }

    private void onMouseExitedElement(MouseEvent e) {
        if (this.selectedTool == Toolbox.MOVE) {
            Node node = (Node) e.getSource();

            for (UIElement uiElem : this.uiElements) {
                if (uiElem.getOrientationArrow().equals(node)) {
                    uiElem.hideOrientationArrow();
                }
            }
        }
    }

    private void onMouseDraggedRotatingElement(MouseEvent e) {
        if (this.selectedTool == Toolbox.MOVE) {
            this.selectedUIElement.setRotating(true);
            Point2D point = this.scenePane.sceneToLocal(e.getSceneX(), e.getSceneY());
            Vector2D mousePosition = new Vector2D(point.getX(), point.getY());
            Vector2D elementPosition = new Vector2D(this.selectedUIElement.getElement().getPosition(GodController.getInstance().getCurrentTime()).getX(), this.selectedUIElement.getElement().getPosition(GodController.getInstance().getCurrentTime()).getY());
            Vector2D result = mousePosition.substract(elementPosition);
            this.selectedUIElement.getGroupRotation().setRotate(Math.toDegrees(result.getAngle()));

            Vector2D position = this.selectedUIElement.getElement().getPosition(GodController.getInstance().getCurrentTime());
            updateRightPane(position.getX(), position.getY(), Math.toDegrees(result.getAngle()));
        }
    }

    private void onMouseReleasedRotatingElement(MouseEvent e) {
        if (this.selectedTool == Toolbox.MOVE) {
            this.selectedUIElement.setRotating(false);
            this.selectedUIElement.hideOrientationArrow();
            Point2D point = this.scenePane.sceneToLocal(e.getSceneX(), e.getSceneY());
            Vector2D mousePosition = new Vector2D(point.getX(), point.getY());
            Vector2D elementPosition = new Vector2D(this.selectedUIElement.getElement().getPosition(GodController.getInstance().getCurrentTime()).getX(), this.selectedUIElement.getElement().getPosition(GodController.getInstance().getCurrentTime()).getY());
            Vector2D result = mousePosition.substract(elementPosition);
            GodController.getInstance().setCurrentElemOrientation(result.normaliser());
        }
    }

    private void onActionName(ActionEvent e) {
        if (this.selectedUIElement != null && this.selectedUIElement.getElement() instanceof Player) {
            GodController.getInstance().setSelectedPlayerName(this.nameTextField.getText());
        }
    }

    private void onActionRole(Event e) {
        if (this.selectedUIElement != null) {
            String choix = (String) ((ChoiceBox) e.getSource()).getValue();
            if (choix != null) {
                GodController.getInstance().setSelectedPlayerRole((String) ((ChoiceBox) e.getSource()).getValue());
                this.selectedUIElement.refreshNode();
            }
        }

    }

    private void onActionTeam(Event e) {
        if (this.selectedUIElement != null) {
            String choix = (String) ((ChoiceBox) e.getSource()).getValue();
            if (choix != null) {
                int teamNumber = Integer.parseInt(choix.substring(TEAM_LABEL.length()));

                GodController.getInstance().setSelectedPlayerTeam(teamNumber);
                this.selectedUIElement.refreshNode();
            }
        }
    }

    private void onActionPositionX(ActionEvent e) {
        if (this.selectedUIElement != null) {
            try {
                double newX = Double.parseDouble(this.positionX.getText());

                double x;
                double y = this.selectedUIElement.getElement().getPosition(GodController.getInstance().getCurrentTime()).getY();

                Vector2D elementDimensions = this.selectedUIElement.getElement().getElementDescription().getSize();
                Vector2D dimensions = GodController.getInstance().getCourtDimensions();

                if (newX - elementDimensions.getX() / 2 >= 0) {
                    if (newX + elementDimensions.getX() / 2 <= dimensions.getX()) {
                        x = newX;
                    } else {
                        x = dimensions.getX() - elementDimensions.getX() / 2;
                    }
                } else {
                    x = elementDimensions.getX() / 2;
                }

                if (x != this.selectedUIElement.getElement().getPosition(GodController.getInstance().getCurrentTime()).getX()) {
                    GodController.getInstance().setCurrentElemPosition(new Vector2D(x, y));
                }
            } catch (NumberFormatException ex) {
            }
        }
    }

    private void onActionPositionY(ActionEvent e) {
        if (this.selectedUIElement != null) {
            try {
                double newY = Double.parseDouble(this.positionY.getText());

                double x = this.selectedUIElement.getElement().getPosition(GodController.getInstance().getCurrentTime()).getX();
                double y;

                Vector2D elementDimensions = this.selectedUIElement.getElement().getElementDescription().getSize();
                Vector2D dimensions = GodController.getInstance().getCourtDimensions();

                if (newY - elementDimensions.getY() / 2 >= 0) {
                    if (newY + elementDimensions.getY() / 2 <= dimensions.getY()) {
                        y = newY;
                    } else {
                        y = dimensions.getY() - elementDimensions.getY() / 2;
                    }
                } else {
                    y = elementDimensions.getY() / 2;
                }

                if (y != this.selectedUIElement.getElement().getPosition(GodController.getInstance().getCurrentTime()).getY()) {
                    GodController.getInstance().setCurrentElemPosition(new Vector2D(x, y));
                }
            } catch (NumberFormatException ex) {
            }
        }
    }

    private void onActionOrientation(ActionEvent e) {
        if (this.selectedUIElement != null) {
            try {
                double angle = Double.parseDouble(this.orientation.getText());
                Vector2D ori = new Vector2D(1, 0);
                ori.setAngle(Math.toRadians(angle));

                if (angle != this.selectedUIElement.getElement().getOrientation(GodController.getInstance().getCurrentTime()).getAngle()) {
                    GodController.getInstance().setCurrentElemOrientation(ori);
                }
            } catch (NumberFormatException ex) {
            }
        }
    }

    @FXML
    private void onActionNbMaxPlayer(ActionEvent e) {
        boolean nbOfPlayersRespected = true;

        for (int teamId : GodController.getInstance().getTeams()) {
            if (GodController.getInstance().getNbOfPlayersInTeam(teamId) > GodController.getInstance().getMaxNbOfPlayers()) {
                nbOfPlayersRespected = false;
            }
        }

        if (nbOfPlayersRespected) {
            GodController.getInstance().setRespectMaxNbOfPlayers(this.nbMaxPlayerCheckBox.isSelected());
        } else {
            this.nbMaxPlayerCheckBox.setSelected(false);

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur");
            alert.setContentText("Le nombre de joueurs maximum est de " + GodController.getInstance().getMaxNbOfPlayers() + " par équipe.");

            alert.showAndWait();
        }
    }

    @FXML
    private void onActionVisibleLabels(ActionEvent e) {
        for (UIElement elem : this.uiElements) {
            elem.setElementNameVisible(this.visibleLabelsCheckBox.isSelected());
        }
    }

    @FXML
    private void onActionDelete(ActionEvent e) {
        if (this.selectedUIElement != null) {
            this.selectedUIElement = null;
            GodController.getInstance().deleteCurrentElement();
        }
    }

    @FXML
    private void onOpen(ActionEvent e) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Charger une stratégie");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("SER files", "*.ser"));
        File file = fileChooser.showOpenDialog(this.stage);

        if (file != null) {
            GodController.load(file.getPath());
            updateSport();
            update();
        }
    }

    @FXML
    private void onSave(ActionEvent e) {
        GodController.save(null);
    }

    @FXML
    private void onSaveAs(ActionEvent e) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sauvegarder stratégie");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("SER files", "*.ser"));
        File file = fileChooser.showSaveDialog(this.stage);

        if (file != null) {
            GodController.save(file.getPath());
        }
    }

    @FXML
    private void onUndo(ActionEvent e) {
        GodController.undo();
        updateUndoRedo();
    }

    @FXML
    private void onRedo(ActionEvent e) {
        GodController.redo();
        updateUndoRedo();
    }

    private void updateUndoRedo() {
        this.undoMenu.setDisable(!GodController.canUndo());
        this.redoMenu.setDisable(!GodController.canRedo());
    }

    @FXML
    private void onExportImage(ActionEvent e) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sauvegarder stratégie");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Image PNG", "*.png"),
                new ExtensionFilter("Image JPEG", "*.jpg"),
                new ExtensionFilter("Image bitmap", "*.bmp")
        );
        File file = fileChooser.showSaveDialog(this.stage);

        if (file != null) {
            PreviewGenerator gen = new PreviewGenerator();
            Image img = gen.generatePreview(GodController.getInstance().getCurrentStrategy());

            BufferedImage bImage = SwingFXUtils.fromFXImage(img, null);
            BufferedImage imageRGB = new BufferedImage(bImage.getWidth(), bImage.getHeight(), BufferedImage.OPAQUE);
            Graphics2D graphics = imageRGB.createGraphics();
            graphics.drawImage(bImage, 0, 0, null);

            try {
                String extension = "";
                int i = file.getPath().lastIndexOf('.');
                if (i > 0) {
                    extension = file.getPath().substring(i + 1);
                }
                ImageIO.write(imageRGB, extension, file);
            } catch (IOException ex) {
                Logger.getLogger(StrategyEditionWindow.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    @FXML
    private void onActionNewStrategy(ActionEvent e) {
        openNewStrategy(false);
    }

    private void openNewStrategy(boolean disableClose) {
        Stage dialog = new Stage(StageStyle.TRANSPARENT);
        dialog.initStyle(StageStyle.DECORATED);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(this.stage);

        // At the first opening, we disable the close button.
        if (disableClose) {
            dialog.setOnCloseRequest((event) -> event.consume());
        }

        StrategyCreationDialog strategyCreation = new StrategyCreationDialog(dialog);
        dialog.setOnHidden((event) ->
        {
            updateSport();
            update();
        });
    }

    @FXML
    private void onActionConfigureSport(ActionEvent e) {
        Stage dialog = new Stage(StageStyle.TRANSPARENT);
        dialog.initStyle(StageStyle.DECORATED);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(this.stage);

        SportEditionDialog sportEdition = new SportEditionDialog(dialog);
        sportEdition.stage.setOnHidden((event) -> updateSport());
    }

    @FXML
    private void onActionMoveTool() {
        this.moveButton.setStyle("-fx-background-color: lightblue;");
        this.playerButton.setStyle("-fx-background-color: inherit;");
        this.ballButton.setStyle("-fx-background-color: inherit;");
        this.obstacleButton.setStyle("-fx-background-color: inherit;");
        this.selectedTool = Toolbox.MOVE;
    }

    private void onActionPlayerDescription(ActionEvent e) {
        MenuItem mi = (MenuItem) e.getSource();
        String player = mi.getParentMenu().getText();
        GodController.getInstance().selectElementDescription(TypeDescription.Player, player);
        GodController.getInstance().selectTeam(Integer.parseInt(mi.getText().substring(TEAM_LABEL.length())));
        this.moveButton.setStyle("-fx-background-color: inherit;");
        this.playerButton.setStyle("-fx-background-color: lightblue;");
        this.ballButton.setStyle("-fx-background-color: inherit;");
        this.obstacleButton.setStyle("-fx-background-color: inherit;");
        this.selectedTool = Toolbox.ADD_PLAYER;
    }

    private void onActionBallDescription(ActionEvent e) {
        String ball = ((MenuItem) e.getSource()).getText();
        GodController.getInstance().selectElementDescription(TypeDescription.Ball, ball);
        this.moveButton.setStyle("-fx-background-color: inherit;");
        this.ballButton.setStyle("-fx-background-color: lightblue;");
        this.playerButton.setStyle("-fx-background-color: inherit;");
        this.obstacleButton.setStyle("-fx-background-color: inherit;");
        this.selectedTool = Toolbox.ADD_BALL;
    }

    @FXML
    private void onActionRecord() {
        this.moveButton.setStyle("-fx-background-color: lightblue;");
        this.playerButton.setStyle("-fx-background-color: inherit;");
        this.ballButton.setStyle("-fx-background-color: inherit;");
        this.obstacleButton.setStyle("-fx-background-color: inherit;");
        this.selectedTool = Toolbox.RECORD;
    }

    private void onScroll(ScrollEvent e) {
        double factor = this.sceneScale.getX() + e.getDeltaY() / e.getMultiplierY() * ZOOM_SPEED;
        this.sceneScale.setX(factor);
        this.sceneScale.setY(factor);
        for (UIElement elem : this.uiElements) {
            if (elem.getElement() instanceof Player) {
                elem.setElementNameZoomFactor(1 / factor);
            }
        }
        e.consume();
    }

    @FXML
    private void onActionZoomIn(ActionEvent e) {
        double factor = this.sceneScale.getX() + ZOOM_SPEED;
        this.sceneScale.setX(factor);
        this.sceneScale.setY(factor);
        for (UIElement elem : this.uiElements) {
            if (elem.getElement() instanceof Player) {
                elem.setElementNameZoomFactor(1 / factor);
            }
        }
    }

    @FXML
    private void onActionZoomOut(ActionEvent e) {
        double factor = this.sceneScale.getX() - ZOOM_SPEED;
        this.sceneScale.setX(factor);
        this.sceneScale.setY(factor);
        for (UIElement elem : this.uiElements) {
            if (elem.getElement() instanceof Player) {
                elem.setElementNameZoomFactor(1 / factor);
            }
        }
    }

    private void onActionObstacleDescription(ActionEvent e) {
        String obstacle = ((MenuItem) e.getSource()).getText();
        GodController.getInstance().selectElementDescription(TypeDescription.Obstacle, obstacle);
        this.moveButton.setStyle("-fx-background-color: inherit;");
        this.obstacleButton.setStyle("-fx-background-color: lightblue;");
        this.playerButton.setStyle("-fx-background-color: inherit;");
        this.ballButton.setStyle("-fx-background-color: inherit;");
        this.selectedTool = Toolbox.ADD_OBSTACLE;
    }

    @FXML
    private void onActionPlay(ActionEvent e) {
        GodController.getInstance().playStrategy(1);
        this.playPauseButton.setOnAction(this::onActionPause);
        this.playPauseButton.setText("" + PAUSE_ICON);
        this.timeLine.setDisable(true);
    }

    private void onActionPause(ActionEvent e) {
        GodController.getInstance().pauseStrategy();
        this.playPauseButton.setOnAction(this::onActionPlay);
        this.playPauseButton.setText("" + PLAY_ICON);
        this.timeLine.setDisable(false);
    }

    @FXML
    private void onActionRestart() {
        GodController.getInstance().setCurrentTime(0);
    }

    @FXML
    private void onActionRewind() {
        GodController.getInstance().playStrategy(-Integer.parseInt(this.speed.getText().substring(1)));
        this.playPauseButton.setOnAction(this::onActionPause);
        this.playPauseButton.setText("" + PAUSE_ICON);
        this.timeLine.setDisable(true);
    }

    @FXML
    private void onActionFastForward() {
        GodController.getInstance().playStrategy(Integer.parseInt(this.speed.getText().substring(1)));
        this.playPauseButton.setOnAction(this::onActionPause);
        this.playPauseButton.setText("" + PAUSE_ICON);
        this.timeLine.setDisable(true);
    }

    @FXML
    private void onActionGoToEnd() {
        GodController.getInstance().setCurrentTime(GodController.getInstance().getDuration());
    }

    @FXML
    private void onActionNextFrame() {
        GodController.getInstance().setCurrentTime(GodController.getInstance().getCurrentTime() + (1f / GodController.FPS_EDIT));
    }

    @FXML
    private void onActionPrevFrame() {
        GodController.getInstance().setCurrentTime(GodController.getInstance().getCurrentTime() - (1f / GodController.FPS_EDIT));
    }

    @FXML
    private void onActionStepFrame() {
        int step = Integer.parseInt(this.frameStep.getText());
        GodController.getInstance().setCurrentTime(GodController.getInstance().getCurrentTime() + (((double) step) / GodController.FPS_EDIT));
    }

    @FXML
    private void onActionStop(ActionEvent e) {
        onActionPause(e);
        GodController.getInstance().setCurrentTime(0);
        this.timeLine.setDisable(false);
    }

    @Override
    public void lastUpdate() {
        update();
        this.playPauseButton.setOnAction(this::onActionPlay);
        Platform.runLater(() ->
        {
            this.playPauseButton.setText("" + PLAY_ICON);
        });
        this.timeLine.setDisable(false);
    }

    private void onValueChangeSlider() {
        if (!this.recording && !this.updating) {
            GodController.getInstance().setCurrentTime(this.timeLine.getValue() / GodController.FPS_EDIT);
        }
    }

    private enum Toolbox {
        ADD_PLAYER, ADD_BALL, ADD_OBSTACLE, MOVE, RECORD, ZOOM
    }
}
