import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        Label label = new Label("Welcome to JECRC FOODS");
        Scene scene = new Scene(label, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("JECRC FOODS App");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
