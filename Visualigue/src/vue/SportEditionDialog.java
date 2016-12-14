/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vue;

import controller.GodController;
import java.io.File;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Sport;
import javafx.stage.StageStyle;
import model.BallDescription;
import model.ElementDescription;
import model.ObstacleDescription;
import model.PlayerDescription;
import model.ValidationException;

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

    BorderPane root;
    Stage stage;

    public SportEditionDialog(Stage primaryStage)
    {
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
            Logger.getLogger(StrategyEditionWindow.class.getName()).log(Level.SEVERE, null, ex);
        }

        stage.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        updateSportsList();
        updateCurrentSport();
        
        sports.getSelectionModel().selectedItemProperty().addListener((event) -> {
            this.updateCurrentSport();
        });
        
        elementDescriptions.getSelectionModel().selectedItemProperty().addListener((event) -> {
            this.updateCurrentDescription();
        });
    }

    @FXML
    private void onActionAddSport(ActionEvent e)
    {
        sports.getSelectionModel().clearSelection();
        updateCurrentSport();
    }
    
    @FXML
    private void onActionSave(ActionEvent e)
    {
        if(!validateInputs())
        {
            return;
        }
        
        double height = Double.parseDouble(courtHeight.getText());
        double width = Double.parseDouble(courtWidth.getText());
        int numPlayer = Integer.parseInt(playerNumber.getText());
        int teams = Integer.parseInt(numTeams.getText());

        String sport = (String)sports.getSelectionModel().getSelectedItem();
        
        try
        {
            GodController.getInstance().saveSport(sport, sportName.getText(), courtImage.getText(), height, width, numPlayer, teams);
            updateSportsList();
            sports.getSelectionModel().select(sportName.getText());
        }
        catch(ValidationException ex)
        {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Entrée invalide");
            alert.setContentText(ex.getMessage());

            alert.showAndWait();
        }        
    }
    
    @FXML
    private void onActionDelete(ActionEvent e)
    {
        String sportN = (String)sports.getSelectionModel().getSelectedItem();
        if(sportN != null)
        {
            GodController.getInstance().deleteSport(sportN);
            updateSportsList();
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
            courtImage.setText("file:" + file.getPath());
            loadImage(courtImage.getText());
        }
    }
    
    private Sport getCurrentSport()
    {
        String currentName = (String)sports.getSelectionModel().getSelectedItem();
        return GodController.getInstance().getSport(currentName);
    }
    
    @FXML
    private void onActionCancel(ActionEvent e)
    {
        stage.close();
    }

    @FXML
    private void onActionAddBall(ActionEvent e)
    {
        editDescription(ElementDescription.TypeDescription.Ball, null);
    }
    
    @FXML
    private void onActionAddPlayer(ActionEvent e)
    {
        editDescription(ElementDescription.TypeDescription.Player, null);
    }
    
    @FXML
    private void onActionAddObstacle(ActionEvent e)
    {
        editDescription(ElementDescription.TypeDescription.Obstacle, null);
    }
    
    @FXML
    private void onActionModifyElement(ActionEvent e)
    {
        if(!elementDescriptions.getSelectionModel().isEmpty())
        {
            ElementDescription.TypeDescription type = ElementDescription.TypeDescription.Ball;
            ElementDescription desc = null;
            
            String sportN = (String)sports.getSelectionModel().getSelectedItem();
            TreeItem<String> treeItem = (TreeItem<String>)elementDescriptions.getSelectionModel().getSelectedItem();
            switch(treeItem.getParent().getValue())
            {
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
    private void onActionDeleteElement(ActionEvent e)
    {
        if(!elementDescriptions.getSelectionModel().isEmpty())
        {
            String sportN = (String)sports.getSelectionModel().getSelectedItem();
            TreeItem<String> treeItem = (TreeItem<String>)elementDescriptions.getSelectionModel().getSelectedItem();
            
            switch(treeItem.getParent().getValue())
            {
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

    private void updateSportsList()
    {
        List<Sport> allSports = GodController.getInstance().getSports();

        String oldSelection = (String)sports.getSelectionModel().getSelectedItem();

        sports.getItems().clear();
        for(Sport sport : allSports)
        {
            sports.getItems().add(sport.getName());
        }

        if(oldSelection != null)
        {
            sports.getSelectionModel().select(oldSelection);
        }
    }
    
    private void updateCurrentSport()
    {
        Sport currentSport = getCurrentSport();
        if(currentSport != null)
        {
            sportName.setText(currentSport.getName());
            courtImage.setText(currentSport.getCourtImage());
            courtHeight.setText("" + currentSport.getCourtSize().getY());
            courtWidth.setText("" + currentSport.getCourtSize().getX());
            playerNumber.setText("" + currentSport.getMaxPlayer());
            numTeams.setText("" + currentSport.getMaxTeam());
            elementsSection.setVisible(true);
            deleteSportBtn.setDisable(false);
        }
        else
        {
            sportName.setText("Nouveau sport");
            courtImage.setText("");
            courtHeight.setText("0.0");
            courtWidth.setText("0.0");
            playerNumber.setText("0");
            numTeams.setText("0");
            elementsSection.setVisible(false);
            deleteSportBtn.setDisable(true);
        }
        
        updateDescriptions();
        loadImage(courtImage.getText());
    }
    
    private void updateDescriptions()
    {
        Sport currentSport = getCurrentSport();
        
        if(currentSport != null)
        {
            TreeItem<String> treeRoot = new TreeItem("root");
            treeRoot.setExpanded(true);

            TreeItem<String> balls = new TreeItem("Balles");
            balls.setExpanded(true);
            for(BallDescription desc : currentSport.getAllBallDescriptions())
            {
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
            for(PlayerDescription desc : currentSport.getAllPlayerDescriptions())
            {
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
            for(ObstacleDescription desc : currentSport.getAllObstacleDescriptions())
            {
                TreeItem<String> item = new TreeItem(desc.getName());
                ImageView image = new ImageView(ImageLoader.getImage(desc.getImage()));
                image.setFitWidth(16);
                image.setFitHeight(16);
                image.setPreserveRatio(true);
                item.setGraphic(image);
                obstacles.getChildren().add(item);
            }
            treeRoot.getChildren().add(obstacles);
            
            elementDescriptions.setRoot(treeRoot);
        }
    }
    
    private void updateCurrentDescription()
    {
        if(!elementDescriptions.getSelectionModel().isEmpty())
        {
            TreeItem<String> item = (TreeItem<String>)elementDescriptions.getSelectionModel().getSelectedItem();
            if(!item.getParent().getValue().equals("root"))
            {
                modifyElementBtn.setDisable(false);
                deleteElementBtn.setDisable(false);
                return;
            }
        }

        modifyElementBtn.setDisable(true);
        deleteElementBtn.setDisable(true);
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
            court.setImage(null);
            return false;
        }
        
        if(img.isError())
        {
            court.setImage(null);
            return false;
        }
                 
        court.setImage(img);
        return true;
    }
    
    private void editDescription(ElementDescription.TypeDescription type, ElementDescription desc)
    {
        Stage dialog = new Stage(StageStyle.TRANSPARENT);
        dialog.initStyle(StageStyle.DECORATED);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(stage);
        
        String sportN = (String)sports.getSelectionModel().getSelectedItem();
        ElementDescriptionEditionDialog elementEdition = new ElementDescriptionEditionDialog(dialog, sportN, type, desc);
        dialog.setOnHidden((event) -> {
            updateDescriptions();
        });
    }
    
    private boolean validateInputs()
    {
        String errorMsg = "";
        if(!checkPositiveDouble(courtHeight.getText()))
        {
            errorMsg += "Hauteur invalide.\n";
        }
        
        if(!checkPositiveDouble(courtWidth.getText()))
        {
            errorMsg += "Longueur invalide.\n";
        }
        
        if(!checkPositiveInteger(playerNumber.getText()))
        {
            errorMsg += "Nombre de joueurs invalide.\n";
        }
        
        if(!checkPositiveInteger(numTeams.getText()))
        {
            errorMsg += "Nombre de joueurs par équipe invalide.\n";
        }
        
        if(!errorMsg.isEmpty())
        {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Entrée invalide");
            alert.setContentText(errorMsg);

            alert.showAndWait();
            return false;
        }
        
        return true;
    }
    
    public static boolean checkPositiveDouble(String str)
    {
        try
        {
            double value = Double.parseDouble(str);
            if(value >= 0)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        catch(NumberFormatException e)
        {
            return false;
        }
    }
    
    public static boolean checkPositiveInteger(String str)
    {
        try
        {
            Integer.parseUnsignedInt(str);
            return true;
        }
        catch(NumberFormatException e)
        {
            return false;
        }
    }
}
