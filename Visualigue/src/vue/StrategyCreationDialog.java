package vue;

import controller.GodController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Sport;
import model.Strategy;
import model.ValidationException;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StrategyCreationDialog implements Initializable {
    BorderPane root;
    Stage stage;
    @FXML
    private ListView listViewSports;
    @FXML
    private ListView listViewStrategies;
    @FXML
    private Button btnCreateStrategy;
    @FXML
    private Button btnDelete;
    @FXML
    private VBox vboxAdd;
    @FXML
    private TextField textFieldStrategyName;
    @FXML
    private VBox vboxPreview;
    @FXML
    private ImageView imageViewPreview;
    @FXML
    private Button btnLoadStrategy;

    public StrategyCreationDialog(Stage primaryStage) {
        this.stage = primaryStage;

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/vue/StrategyCreationDialog.fxml"));
            fxmlLoader.setController(this);
            this.root = (BorderPane) fxmlLoader.load();
            Scene scene = new Scene(this.root);
            this.stage.setScene(scene);
            this.stage.setTitle("Chargement d'une stratégie");
        } catch (IOException ex) {
            Logger.getLogger(StrategyEditionWindow.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.stage.show();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        for (Sport s : GodController.getInstance().getSports()) {
            this.listViewSports.getItems().add(s.getName());
        }

        this.listViewSports.getSelectionModel().selectedItemProperty().addListener((event) -> {
            onSportSelectionChange();
        });

        this.listViewStrategies.getSelectionModel().selectedItemProperty().addListener((event) -> {
            onStrategySelectionChange();
        });
    }

    @FXML
    private void onActionCreateStrategy(ActionEvent e) {
        this.vboxAdd.setVisible(true);
        this.vboxPreview.setVisible(false);
    }

    @FXML
    private void onActionDelete(ActionEvent e) {
        GodController.getInstance().deleteStrategy((String) this.listViewStrategies.getSelectionModel().getSelectedItem());
        updateStrategyList();
    }

    @FXML
    private void onActionBack(ActionEvent e) {
        this.vboxAdd.setVisible(false);
        this.vboxPreview.setVisible(true);
    }

    @FXML
    private void onActionSave(ActionEvent e) {
        try {
            String sport = (String) this.listViewSports.getSelectionModel().getSelectedItem();
            String strat = this.textFieldStrategyName.getText();
            GodController.getInstance().createStrategy(strat, sport);

            this.stage.close();
        } catch (ValidationException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Entrée invalide");
            alert.setContentText(ex.getMessage());

            alert.showAndWait();
        }
    }

    @FXML
    private void onActionLoadStrategy(ActionEvent e) {
        String strat = (String) this.listViewStrategies.getSelectionModel().getSelectedItem();
        GodController.getInstance().loadStrategy(strat);
        this.stage.close();
    }

    @FXML
    private void onSportSelectionChange() {
        updateStrategyList();
        this.btnCreateStrategy.setDisable(false);
    }

    @FXML
    private void onStrategySelectionChange() {
        this.btnLoadStrategy.setDisable(false);

        String strat = (String) this.listViewStrategies.getSelectionModel().getSelectedItem();
        Strategy strategy = GodController.getInstance().getStrategy(strat);

        if (strategy != null) {
            PreviewGenerator gen = new PreviewGenerator();
            Image img = gen.generatePreview(strategy);
            this.imageViewPreview.setImage(img);

            this.btnDelete.setDisable(false);
        } else {
            this.btnDelete.setDisable(true);
        }
    }

    private void updateStrategyList() {
        this.listViewStrategies.getItems().clear();

        String currentSport = (String) this.listViewSports.getSelectionModel().getSelectedItem();

        if (currentSport != null) {
            for (Strategy s : GodController.getInstance().getStrategies()) {
                if (s.getSport().getName().equals(currentSport)) {
                    this.listViewStrategies.getItems().add(s.getName());
                }
            }
        }
    }
}

