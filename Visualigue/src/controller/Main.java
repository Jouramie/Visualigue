package controller;

import javafx.stage.Stage;
import javafx.application.Application;
import vue.MainWindow;

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
        MainWindow mainWindow = new MainWindow(controller, primaryStage);
    }
    
    public static void main(String[] args)
    {
        launch(args);
    }
}
