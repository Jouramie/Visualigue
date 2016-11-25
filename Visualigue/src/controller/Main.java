package controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
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
        File f = new File("visualigue.ser");
        if(f.exists() && !f.isDirectory())
        {
            try
            {
                FileInputStream fileIn = new FileInputStream("visualigue.ser");
                ObjectInputStream in = new ObjectInputStream(fileIn);
                controller = (GodController)in.readObject();
                in.close();
                fileIn.close();
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }
        
        if(controller == null)
        {
            controller = new GodController();
        }
        
        stage = primaryStage;
        StrategyEditionWindow mainWindow = new StrategyEditionWindow(controller, primaryStage);
        controller.setWindow(mainWindow);
    }
    
    public static void main(String[] args)
    {
        launch(args);
    }
}
