/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author megal_000
 */
public class StrategyCreationDialog implements Initializable{
    
    @FXML
    private ListView listViewStrategy;
        
    @FXML
    private Button btnAdd;
    
    @FXML
    private Button btnLoad;
    
    @FXML
    private Button btnSave;
        
    @FXML
    private VBox vboxAdd;   
    
    @FXML
    private VBox vboxDisplay; 
    
    
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
        } catch (IOException ex)
        {
            Logger.getLogger(StrategyEditionWindow.class.getName()).log(Level.SEVERE, null, ex);
        }

        stage.show();
    }
    
     
    @FXML
    private void onActionSave(ActionEvent e)
    {
        stage.close();
    }
    
    
    @FXML
    private void onActionAdd(ActionEvent e)
    {
        vboxDisplay.setVisible(false);
        vboxAdd.setVisible(true);
    }
    
    @FXML
    private void onActionLoad(ActionEvent e)
    {
        stage.close();
    }
    
    @FXML
    private void onMouseClickedListView()
    {
        vboxDisplay.setVisible(true);
        vboxAdd.setVisible(false);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
             
        listViewStrategy.getItems().add("Stratégie d'attaque");     
        listViewStrategy.getItems().add("Stratégie de défense");     
        listViewStrategy.getItems().add("Stratégie de drill");     
        listViewStrategy.getItems().add("Stratégie de gadien");
        
        listViewStrategy.getSelectionModel().selectedItemProperty().addListener((event) -> {
            onMouseClickedListView();
        });
    }
}

