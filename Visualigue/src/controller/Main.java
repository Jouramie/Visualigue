package controller;

import javafx.application.Application;
import javafx.stage.Stage;
import vue.StrategyEditionWindow;

public class Main extends Application {
    private Stage stage;

    public Main() {

    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        GodController.load("visualigue.ser");

        stage = primaryStage;
        StrategyEditionWindow mainWindow = new StrategyEditionWindow(primaryStage);
        GodController.getInstance().setWindow(mainWindow);
    }
}
