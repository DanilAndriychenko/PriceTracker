package org.econia;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static final String FXML = "main.fxml";
    private static final String APP_ICON = "appIcon1.jpg";
    private static final String TITLE = "Моніторинг цін";
    private static final Controller controller = new Controller();

    public static Controller getController() {
        return controller;
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(FXML));
        fxmlLoader.setController(controller);
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
//        stage.setMaximized(true);
        stage.setTitle(TITLE);
        stage.getIcons().add(new Image(getClass().getResourceAsStream(APP_ICON)));
        stage.setOnCloseRequest(event -> System.exit(0));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}