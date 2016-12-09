package vue;

import controller.GodController;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
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

public class StrategyCreationDialog implements Initializable
{
    @FXML
    private ListView listViewSports;
    
    @FXML
    private ListView listViewStrategies;
    
    @FXML
    private Button btnCreateStrategy;
           
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
    
    
    GodController controller;
    BorderPane root;
    Stage stage;

    public StrategyCreationDialog(GodController controller, Stage primaryStage)
    {
        this.controller = controller;
        this.stage = primaryStage;

        try
        {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/vue/StrategyCreationDialog.fxml"));
            fxmlLoader.setController(this);
            root = (BorderPane) fxmlLoader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Chargement d'une stratégie");
        }
        catch (IOException ex)
        {
            Logger.getLogger(StrategyEditionWindow.class.getName()).log(Level.SEVERE, null, ex);
        }

        stage.show();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        for(Sport s : controller.getSports())
        {
            listViewSports.getItems().add(s.getName());
        }
        
        listViewSports.getSelectionModel().selectedItemProperty().addListener((event) -> {
            onSportSelectionChange();
        });
        
        listViewStrategies.getSelectionModel().selectedItemProperty().addListener((event) -> {
            onStrategySelectionChange();
        });
    }
    
    @FXML
    private void onActionCreateStrategy(ActionEvent e)
    {
        vboxAdd.setVisible(true);
        vboxPreview.setVisible(false);
    }
    
    @FXML
    private void onActionSave(ActionEvent e)
    {
        try
        {
            String sport = (String)listViewSports.getSelectionModel().getSelectedItem();
            String strat = textFieldStrategyName.getText();
            controller.createStrategy(strat, sport);
            
            stage.close();
        }
        catch(ValidationException ex)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Entrée invalide");
            alert.setContentText(ex.getMessage());

            alert.showAndWait();
        }
    }
    
    @FXML
    private void onActionLoadStrategy(ActionEvent e)
    {
        String strat = (String)listViewStrategies.getSelectionModel().getSelectedItem();
        controller.loadStrategy(strat);
        stage.close();
    }
        
    @FXML
    private void onSportSelectionChange()
    {
        updateStrategyList();
        btnCreateStrategy.setDisable(false);
    }
    
    @FXML
    private void onStrategySelectionChange()
    {
        btnLoadStrategy.setDisable(false);
        
        String strat = (String)listViewStrategies.getSelectionModel().getSelectedItem();
        Strategy strategy = controller.getStrategy(strat);
        Image img = new Image(strategy.getSport().getCourtImage());
        imageViewPreview.setImage(img);
    }
    
    private void updateStrategyList()
    {
        listViewStrategies.getItems().clear();
        
        String currentSport = (String)listViewSports.getSelectionModel().getSelectedItem();
        
        if(currentSport != null)
        {
            for(Strategy s : controller.getStrategies())
            {
                if(s.getSport().getName().equals(currentSport))
                {
                    listViewStrategies.getItems().add(s.getName());
                }
            }
        }
    }
}
