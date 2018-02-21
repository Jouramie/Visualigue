package vue;

import controller.GodController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.ElementDescription;
import model.ValidationException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import static vue.SportEditionDialog.checkPositiveDouble;

public class ElementDescriptionEditionDialog implements Initializable {
    BorderPane root;
    Stage stage;
    String sportName;
    ElementDescription.TypeDescription type;
    ElementDescription desc;
    @FXML
    private Label lblTitre;
    @FXML
    private TextField name;
    @FXML
    private TextField height;
    @FXML
    private TextField width;
    @FXML
    private TextField image;
    @FXML
    private ImageView imageView;

    public ElementDescriptionEditionDialog(Stage primaryStage, String sportName, ElementDescription.TypeDescription type, ElementDescription desc) {
        this.stage = primaryStage;
        this.sportName = sportName;
        this.type = type;
        this.desc = desc;

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/vue/ElementDescriptionEditionDialog.fxml"));
            fxmlLoader.setController(this);
            this.root = (BorderPane) fxmlLoader.load();
            this.stage = primaryStage;
            Scene scene = new Scene(this.root);
            this.stage.setScene(scene);
            this.stage.setTitle("Configuration des " + type + "s");
            this.lblTitre.setText("Configuration des " + type + "s");
        } catch (IOException ex) {
            Logger.getLogger(StrategyEditionWindow.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.stage.show();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (this.desc != null) {
            this.name.setText(this.desc.getName());
            this.width.setText("" + this.desc.getSize().getX());
            this.height.setText("" + this.desc.getSize().getY());
            this.image.setText(this.desc.getImage());
            loadImage(this.desc.getImage());
        }
    }

    @FXML
    private void onActionCancel(ActionEvent e) {
        this.stage.close();
    }

    @FXML
    private void onActionSave(ActionEvent e) {
        if (validateInputs()) {
            String oldName = "";
            if (this.desc != null) {
                oldName = this.desc.getName();
            }

            double h = Double.parseDouble(this.height.getText());
            double w = Double.parseDouble(this.width.getText());

            try {
                switch (this.type) {
                    case Ball:
                        GodController.getInstance().saveBallDescription(this.sportName, oldName, this.name.getText(), this.image.getText(), h, w);
                        break;
                    case Player:
                        GodController.getInstance().savePlayerDescription(this.sportName, oldName, this.name.getText(), this.image.getText(), h, w);
                        break;
                    case Obstacle:
                        GodController.getInstance().saveObstacleDescription(this.sportName, oldName, this.name.getText(), this.image.getText(), h, w);
                        break;
                }

                this.stage.close();
            } catch (ValidationException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Entrée invalide");
                alert.setContentText(ex.getMessage());

                alert.showAndWait();
            }
        }
    }

    @FXML
    private void onActionBrowse(ActionEvent e) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Image de terrain");
        File file = fileChooser.showOpenDialog(this.stage);

        if (file != null) {
            this.image.setText("file:" + file.getPath());
            loadImage(this.image.getText());
        }
    }

    private boolean loadImage(String path) {
        Image img = null;

        try {
            img = new Image(path);
        } catch (Exception e) {
            this.imageView.setImage(null);
            return false;
        }

        if (img.isError()) {
            this.imageView.setImage(null);
            return false;
        }

        this.imageView.setImage(img);
        return true;
    }

    private boolean validateInputs() {
        String errorMsg = "";

        if (!checkPositiveDouble(this.height.getText())) {
            errorMsg += "Hauteur invalide.\n";
        }

        if (!checkPositiveDouble(this.width.getText())) {
            errorMsg += "Longueur invalide.\n";
        }

        if (!loadImage(this.image.getText())) {
            errorMsg += "Image invalide.\n";
        }

        if (!errorMsg.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Entrée invalide");
            alert.setContentText(errorMsg);

            alert.showAndWait();
            return false;
        }

        return true;
    }
}
