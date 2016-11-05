/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vue;

import controller.GodController;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import model.Sport;

/**
 * FXML Controller class
 *
 * @author Utilisateur
 */
public class SportEditionDialog implements Initializable
{
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
    private TextField playerNumber;

    @FXML
    private ImageView court;

    @FXML
    private ImageView elementDescriptionView;

    @FXML
    private TreeView elementDescription;

    GodController controller;
    BorderPane root;
    Stage stage;

    public SportEditionDialog(GodController controller, Stage primaryStage)
    {
        this.controller = controller;
        this.stage = primaryStage;

        try
        {

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/vue/SportEditionDialog.fxml"));
            fxmlLoader.setController(this);
            root = (BorderPane) fxmlLoader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Configuration des sports");
        } catch (IOException ex)
        {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }

        stage.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        updateSportsList();
    }

    @FXML
    private void onActionAddSport(ActionEvent e)
    {
        controller.addSport("Nouveau sport", "", 0, 0, 0);
        updateSportsList();
    }
    
    @FXML
    private void onActionSave(ActionEvent e)
    {
        try
        {
            int height = Integer.parseInt(courtHeight.getText());
            int width = Integer.parseInt(courtWidth.getText());
            int numPlayer = Integer.parseInt(playerNumber.getText());
            
            Sport sport = getCurrentSport();
            if(sport != null)
            {
                controller.saveSport(sport.getName(), sportName.getText(), courtImage.getText(), height, width, numPlayer);
                sport.setName(sportName.getText());
                updateSportsList();
            }
        }
        catch(Exception exception)
        {
            
        }
    }
    
    private Sport getCurrentSport()
    {
        String currentName = (String)sports.getSelectionModel().getSelectedItem();
        List<Sport> allSports = controller.getSports();
        for(Sport s : allSports)
        {
            if(s.getName().equals(currentName))
            {
                return s;
            }
        }
        
        return null;
    }

    private void updateSportsList()
    {
        List<Sport> allSports = controller.getSports();
        
        Sport oldSelection = getCurrentSport();
        
        sports.getItems().clear();
        for(Sport sport : allSports)
        {
            sports.getItems().add(sport.getName());
        }
        
        if(oldSelection != null)
        {
            sports.getSelectionModel().select(oldSelection.getName());
        }
    }
}
