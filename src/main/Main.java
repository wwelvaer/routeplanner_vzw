package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(final Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("routeplanner.fxml"));
        Scene scene = new Scene(root, 1000, 600);
        primaryStage.setMinWidth(300);
        primaryStage.setMinHeight(230);
        scene.getStylesheets().add(getClass().getResource("routeplanner.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Route Planner");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
