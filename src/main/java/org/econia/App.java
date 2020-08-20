package org.econia;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
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

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(FXML));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
//        stage.setMaximized(true);
        stage.setTitle(TITLE);
        stage.getIcons().add(new Image(getClass().getResourceAsStream(APP_ICON)));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}