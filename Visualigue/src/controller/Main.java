package controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import javafx.stage.Stage;
import javafx.application.Application;
import vue.StrategyEditionWindow;

public class Main extends Application
{
    private Stage stage;
    
    public Main()
    {
        
    }
    
    @Override
    public void start(Stage primaryStage)
    {
        GodController.load("visualigue.ser");
        
        stage = primaryStage;
        StrategyEditionWindow mainWindow = new StrategyEditionWindow(primaryStage);
        GodController.getInstance().setWindow(mainWindow);
    }
    
    public static void main(String[] args)
    {
        launch(args);
    }
}
