package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    public static Stage currentStage;

    @Override
    public void start(Stage primaryStage) throws Exception{
        currentStage = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        root.getStylesheets().add(getClass().getResource("style.css").toString());
        primaryStage.setTitle("Everything4Rent");
        primaryStage.setScene(new Scene(root, 700, 450));
        primaryStage.setResizable(false);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
