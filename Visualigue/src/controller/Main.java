package controller;

import javafx.stage.Stage;
import javafx.application.Application;
import vue.StategyEditionWindow;

public class Main extends Application
{
    private GodController controller;
    private Stage stage;
    
    public Main()
    {
        
    }
    
    @Override
    public void start(Stage primaryStage)
    {
        stage = primaryStage;
        controller = new GodController();
        StategyEditionWindow mainWindow = new StategyEditionWindow(controller, primaryStage);
    }
    
    public static void main(String[] args)
    {
        launch(args);
    }
}
