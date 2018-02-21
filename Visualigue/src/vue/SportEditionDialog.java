package vue;

import controller.GodController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SportEditionDialog implements Initializable {
    BorderPane root;
    Stage stage;
    @FXML
    private ListView sports;
    @FXML
    private TextField sportName;
    @FXML
    private TextField courtImage;
    @FXML
    private TextField courtHeight;
    @FXML
    private TextField courtWidth;
    @FXML
    private TextField numTeams;
    @FXML
    private TextField playerNumber;
    @FXML
    private ImageView court;
    @FXML
    private ImageView elementDescriptionView;
    @FXML
    private TreeView elementDescriptions;
    @FXML
    private Button modifyElementBtn;
    @FXML
    private Button deleteElementBtn;
    @FXML
    private VBox elementsSection;
    @FXML
    private Button deleteSportBtn;

    public SportEditionDialog(Stage primaryStage) {
        this.stage = primaryStage;

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/vue/SportEditionDialog.fxml"));
            fxmlLoader.setController(this);
            this.root = (BorderPane) fxmlLoader.load();
            Scene scene = new Scene(this.root);
            this.stage.setScene(scene);
            this.stage.setTitle("Configuration des sports");
        } catch (IOException ex) {
            Logger.getLogger(StrategyEditionWindow.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.stage.show();
    }

    public static boolean checkPositiveDouble(String str) {
        try {
            double value = Double.parseDouble(str);
            if (value >= 0) {
                return true;
            } else {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean checkPositiveInteger(String str) {
        try {
            Integer.parseUnsignedInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        updateSportsList();
        updateCurrentSport();

        this.sports.getSelectionModel().selectedItemProperty().addListener((event) -> {
            this.updateCurrentSport();
        });

        this.elementDescriptions.getSelectionModel().selectedItemProperty().addListener((event) -> {
            this.updateCurrentDescription();
        });
    }

    @FXML
    private void onActionAddSport(ActionEvent e) {
        this.sports.getSelectionModel().clearSelection();
        updateCurrentSport();
    }

    @FXML
    private void onActionSave(ActionEvent e) {
        if (!validateInputs()) {
            return;
        }

        double height = Double.parseDouble(this.courtHeight.getText());
        double width = Double.parseDouble(this.courtWidth.getText());
        int numPlayer = Integer.parseInt(this.playerNumber.getText());
        int teams = Integer.parseInt(this.numTeams.getText());

        String sport = (String) this.sports.getSelectionModel().getSelectedItem();

        try {
            GodController.getInstance().saveSport(sport, this.sportName.getText(), this.courtImage.getText(), height, width, numPlayer, teams);
            updateSportsList();
            this.sports.getSelectionModel().select(this.sportName.getText());
        } catch (ValidationException ex) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Entrée invalide");
            alert.setContentText(ex.getMessage());

            alert.showAndWait();
        }
    }

    @FXML
    private void onActionDelete(ActionEvent e) {
        String sportN = (String) this.sports.getSelectionModel().getSelectedItem();
        if (sportN != null) {
            GodController.getInstance().deleteSport(sportN);
            updateSportsList();
        }
    }

    @FXML
    private void onActionBrowse(ActionEvent e) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Image de terrain");
        File file = fileChooser.showOpenDialog(this.stage);

        if (file != null) {
            this.courtImage.setText("file:" + file.getPath());
            loadImage(this.courtImage.getText());
        }
    }

    private Sport getCurrentSport() {
        String currentName = (String) this.sports.getSelectionModel().getSelectedItem();
        return GodController.getInstance().getSport(currentName);
    }

    @FXML
    private void onActionCancel(ActionEvent e) {
        this.stage.close();
    }

    @FXML
    private void onActionAddBall(ActionEvent e) {
        editDescription(ElementDescription.TypeDescription.Ball, null);
    }

    @FXML
    private void onActionAddPlayer(ActionEvent e) {
        editDescription(ElementDescription.TypeDescription.Player, null);
    }

    @FXML
    private void onActionAddObstacle(ActionEvent e) {
        editDescription(ElementDescription.TypeDescription.Obstacle, null);
    }

    @FXML
    private void onActionModifyElement(ActionEvent e) {
        if (!this.elementDescriptions.getSelectionModel().isEmpty()) {
            ElementDescription.TypeDescription type = ElementDescription.TypeDescription.Ball;
            ElementDescription desc = null;

            String sportN = (String) this.sports.getSelectionModel().getSelectedItem();
            TreeItem<String> treeItem = (TreeItem<String>) this.elementDescriptions.getSelectionModel().getSelectedItem();
            switch (treeItem.getParent().getValue()) {
                case "Balles":
                    type = ElementDescription.TypeDescription.Ball;
                    desc = GodController.getInstance().getBallDescription(sportN, treeItem.getValue());
                    break;
                case "Joueurs":
                    type = ElementDescription.TypeDescription.Player;
                    desc = GodController.getInstance().getPlayerDescription(sportN, treeItem.getValue());
                    break;
                case "Obstacles":
                    type = ElementDescription.TypeDescription.Obstacle;
                    desc = GodController.getInstance().getObstacleDescription(sportN, treeItem.getValue());
                    break;
            }

            editDescription(type, desc);
        }
    }

    @FXML
    private void onActionDeleteElement(ActionEvent e) {
        if (!this.elementDescriptions.getSelectionModel().isEmpty()) {
            String sportN = (String) this.sports.getSelectionModel().getSelectedItem();
            TreeItem<String> treeItem = (TreeItem<String>) this.elementDescriptions.getSelectionModel().getSelectedItem();

            switch (treeItem.getParent().getValue()) {
                case "Balles":
                    GodController.getInstance().deleteBallDescription(sportN, treeItem.getValue());
                    break;
                case "Joueurs":
                    GodController.getInstance().deletePlayerDescription(sportN, treeItem.getValue());
                    break;
                case "Obstacles":
                    GodController.getInstance().deleteObstacleDescription(sportN, treeItem.getValue());
                    break;
            }

            updateDescriptions();
        }
    }

    private void updateSportsList() {
        List<Sport> allSports = GodController.getInstance().getSports();

        String oldSelection = (String) this.sports.getSelectionModel().getSelectedItem();

        this.sports.getItems().clear();
        for (Sport sport : allSports) {
            this.sports.getItems().add(sport.getName());
        }

        if (oldSelection != null) {
            this.sports.getSelectionModel().select(oldSelection);
        }
    }

    private void updateCurrentSport() {
        Sport currentSport = getCurrentSport();
        if (currentSport != null) {
            this.sportName.setText(currentSport.getName());
            this.courtImage.setText(currentSport.getCourtImage());
            this.courtHeight.setText("" + currentSport.getCourtSize().getY());
            this.courtWidth.setText("" + currentSport.getCourtSize().getX());
            this.playerNumber.setText("" + currentSport.getMaxPlayer());
            this.numTeams.setText("" + currentSport.getMaxTeam());
            this.elementsSection.setVisible(true);
            this.deleteSportBtn.setDisable(false);
        } else {
            this.sportName.setText("Nouveau sport");
            this.courtImage.setText("");
            this.courtHeight.setText("0.0");
            this.courtWidth.setText("0.0");
            this.playerNumber.setText("0");
            this.numTeams.setText("0");
            this.elementsSection.setVisible(false);
            this.deleteSportBtn.setDisable(true);
        }

        updateDescriptions();
        loadImage(this.courtImage.getText());
    }

    private void updateDescriptions() {
        Sport currentSport = getCurrentSport();

        if (currentSport != null) {
            TreeItem<String> treeRoot = new TreeItem("root");
            treeRoot.setExpanded(true);

            TreeItem<String> balls = new TreeItem("Balles");
            balls.setExpanded(true);
            for (BallDescription desc : currentSport.getAllBallDescriptions()) {
                TreeItem<String> item = new TreeItem(desc.getName());
                ImageView image = new ImageView(ImageLoader.getImage(desc.getImage()));
                image.setFitWidth(16);
                image.setFitHeight(16);
                image.setPreserveRatio(true);
                item.setGraphic(image);
                balls.getChildren().add(item);
            }
            treeRoot.getChildren().add(balls);

            TreeItem<String> players = new TreeItem("Joueurs");
            players.setExpanded(true);
            for (PlayerDescription desc : currentSport.getAllPlayerDescriptions()) {
                TreeItem<String> item = new TreeItem(desc.getName());
                ImageView image = new ImageView(ImageLoader.getImage(desc.getImage()));
                image.setFitWidth(16);
                image.setFitHeight(16);
                image.setPreserveRatio(true);
                item.setGraphic(image);
                players.getChildren().add(item);
            }
            treeRoot.getChildren().add(players);

            TreeItem<String> obstacles = new TreeItem("Obstacles");
            obstacles.setExpanded(true);
            for (ObstacleDescription desc : currentSport.getAllObstacleDescriptions()) {
                TreeItem<String> item = new TreeItem(desc.getName());
                ImageView image = new ImageView(ImageLoader.getImage(desc.getImage()));
                image.setFitWidth(16);
                image.setFitHeight(16);
                image.setPreserveRatio(true);
                item.setGraphic(image);
                obstacles.getChildren().add(item);
            }
            treeRoot.getChildren().add(obstacles);

            this.elementDescriptions.setRoot(treeRoot);
        }
    }

    private void updateCurrentDescription() {
        if (!this.elementDescriptions.getSelectionModel().isEmpty()) {
            TreeItem<String> item = (TreeItem<String>) this.elementDescriptions.getSelectionModel().getSelectedItem();
            if (!item.getParent().getValue().equals("root")) {
                this.modifyElementBtn.setDisable(false);
                this.deleteElementBtn.setDisable(false);
                return;
            }
        }

        this.modifyElementBtn.setDisable(true);
        this.deleteElementBtn.setDisable(true);
    }

    private boolean loadImage(String path) {
        Image img = null;

        try {
            img = new Image(path);
        } catch (Exception e) {
            this.court.setImage(null);
            return false;
        }

        if (img.isError()) {
            this.court.setImage(null);
            return false;
        }

        this.court.setImage(img);
        return true;
    }

    private void editDescription(ElementDescription.TypeDescription type, ElementDescription desc) {
        Stage dialog = new Stage(StageStyle.TRANSPARENT);
        dialog.initStyle(StageStyle.DECORATED);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(this.stage);

        String sportN = (String) this.sports.getSelectionModel().getSelectedItem();
        ElementDescriptionEditionDialog elementEdition = new ElementDescriptionEditionDialog(dialog, sportN, type, desc);
        dialog.setOnHidden((event) -> {
            updateDescriptions();
        });
    }

    private boolean validateInputs() {
        String errorMsg = "";
        if (!checkPositiveDouble(this.courtHeight.getText())) {
            errorMsg += "Hauteur invalide.\n";
        }

        if (!checkPositiveDouble(this.courtWidth.getText())) {
            errorMsg += "Longueur invalide.\n";
        }

        if (!checkPositiveInteger(this.playerNumber.getText())) {
            errorMsg += "Nombre de joueurs invalide.\n";
        }

        if (!checkPositiveInteger(this.numTeams.getText())) {
            errorMsg += "Nombre de joueurs par équipe invalide.\n";
        }

        if (!errorMsg.isEmpty()) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Entrée invalide");
            alert.setContentText(errorMsg);

            alert.showAndWait();
            return false;
        }

        return true;
    }
}
