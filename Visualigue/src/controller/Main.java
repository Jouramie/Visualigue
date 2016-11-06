package controller;

import javafx.stage.Stage;
import javafx.application.Application;
import vue.StrategyEditionWindow;

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
        StrategyEditionWindow mainWindow = new StrategyEditionWindow(controller, primaryStage);
    }
    
    public static void main(String[] args)
    {
        launch(args);
    }
}
