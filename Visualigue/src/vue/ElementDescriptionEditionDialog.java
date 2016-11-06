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
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 *
 * @author megal_000
 */
public class ElementDescriptionEditionDialog implements Initializable
{
    @FXML
    private Label lblTitre;
    
    GodController controller;
    BorderPane root;
    Stage stage;
    
    public ElementDescriptionEditionDialog(GodController controller, Stage primaryStage, String type)
    {
        this.controller = controller;
        stage = primaryStage;

        try
        {

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/vue/ElementDescriptionEditionDialog.fxml"));
            fxmlLoader.setController(this);
            root = (BorderPane) fxmlLoader.load();
            stage = primaryStage;
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Configuration des " + type);
            lblTitre.setText("Configuration des " + type);
        } catch (IOException ex)
        {
            Logger.getLogger(StategyEditionWindow.class.getName()).log(Level.SEVERE, null, ex);
        }

        stage.show();

    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) 
    {
    
    }
    
    @FXML
    private void onActionCancel(ActionEvent e)
    {
        stage.close();
    }
    
    @FXML
    private void onActionSave(ActionEvent e)
    {
        
    }
    
    @FXML
    private void onActionBrowse(ActionEvent e)
    {
        
    }
}
