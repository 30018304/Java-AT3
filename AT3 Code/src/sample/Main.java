package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    Stage stage = new Stage();
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("Main.fxml"));

        Scene scene = new Scene(root, 800, 400);

        primaryStage.setTitle("Food Tracker");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void showWindow() throws Exception {
        start(stage);

    }
}
