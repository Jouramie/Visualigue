package vue;

import controller.GodController;
import java.io.File;
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
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.ElementDescription;
import model.ValidationException;
import static vue.SportEditionDialog.checkPositiveDouble;

public class ElementDescriptionEditionDialog implements Initializable
{
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

    BorderPane root;
    Stage stage;
    String sportName;
    ElementDescription.TypeDescription type;
    ElementDescription desc;
    
    public ElementDescriptionEditionDialog(Stage primaryStage, String sportName, ElementDescription.TypeDescription type, ElementDescription desc)
    {
        stage = primaryStage;
        this.sportName = sportName;
        this.type = type;
        this.desc = desc;

        try
        {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/vue/ElementDescriptionEditionDialog.fxml"));
            fxmlLoader.setController(this);
            root = (BorderPane) fxmlLoader.load();
            stage = primaryStage;
            Scene scene = new Scene(root);
            stage.setScene(scene);            
            stage.setTitle("Configuration des " + type + "s");
            lblTitre.setText("Configuration des " + type + "s");
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
        if(desc != null)
        {
            name.setText(desc.getName());
            width.setText("" + desc.getSize().getX());
            height.setText("" + desc.getSize().getY());
            image.setText(desc.getImage());
            loadImage(desc.getImage());
        }
    }
    
    @FXML
    private void onActionCancel(ActionEvent e)
    {
        stage.close();
    }
    
    @FXML
    private void onActionSave(ActionEvent e)
    {
        if(validateInputs())
        {
            String oldName = "";
            if(desc != null)
            {
                oldName = desc.getName();
            }
            
            double h = Double.parseDouble(height.getText());
            double w = Double.parseDouble(width.getText());
            
            try
            {
                switch(type)
                {
                    case Ball:
                        GodController.getInstance().saveBallDescription(sportName, oldName, name.getText(), image.getText(), h, w);
                        break;
                    case Player:
                        GodController.getInstance().savePlayerDescription(sportName, oldName, name.getText(), image.getText(), h, w);
                        break;
                    case Obstacle:
                        GodController.getInstance().saveObstacleDescription(sportName, oldName, name.getText(), image.getText(), h, w);
                        break;
                }

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
    }
    
    @FXML
    private void onActionBrowse(ActionEvent e)
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Image de terrain");
        File file = fileChooser.showOpenDialog(stage);
        
        if(file != null)
        {
            image.setText("file:" + file.getPath());
            loadImage(image.getText());
        }
    }
    
    private boolean loadImage(String path)
    {
        Image img = null;
        
        try
        {
            img = new Image(path);
        }
        catch(Exception e)
        {
            imageView.setImage(null);
            return false;
        }
        
        if(img.isError())
        {
            imageView.setImage(null);
            return false;
        }
                 
        imageView.setImage(img);
        return true;
    }
    
    private boolean validateInputs()
    {
        String errorMsg = "";
              
        if(!checkPositiveDouble(height.getText()))
        {
            errorMsg += "Hauteur invalide.\n";
        }
        
        if(!checkPositiveDouble(width.getText()))
        {
            errorMsg += "Longueur invalide.\n";
        }
        
        if(!loadImage(image.getText()))
        {
            errorMsg += "Image invalide.\n";
        }
        
        if(!errorMsg.isEmpty())
        {
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
