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
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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
        stage = primaryStage;

        try
        {

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/vue/SportEditionDialog.fxml"));
            fxmlLoader.setController(this);
            root = (BorderPane) fxmlLoader.load();
            stage = primaryStage;
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Configuration des sports");
        } catch (IOException ex)
        {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }

        stage.show();

    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        // TODO
    }

    @FXML
    private void onActionCancel(ActionEvent e)
    {
        stage.close();
    }

    @FXML
    private void onActionAddBall(ActionEvent e)
    {
        Stage dialog = new Stage(StageStyle.TRANSPARENT);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(stage);
        
        ElementEditionDialog elementEdition = new ElementEditionDialog(controller, dialog, "balles");
    }
    
    @FXML
    private void onActionAddPlayer(ActionEvent e)
    {
        Stage dialog = new Stage(StageStyle.TRANSPARENT);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(stage);
        
        ElementEditionDialog elementEdition = new ElementEditionDialog(controller, dialog, "catégories de joueur");
    }
    
    @FXML
    private void onActionAddObstacle(ActionEvent e)
    {
        Stage dialog = new Stage(StageStyle.TRANSPARENT);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(stage);
        
        ElementEditionDialog elementEdition = new ElementEditionDialog(controller, dialog, "obstacles");
    }
    
    @FXML
    private void onActionModifyElement(ActionEvent e)
    {
        
    }
    
    @FXML
    private void onActionDeleteElement(ActionEvent e)
    {
        
    }
}
