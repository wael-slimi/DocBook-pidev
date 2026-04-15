package org.docbook.mains;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class mainFx extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load your Login screen first
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auth/login.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setTitle("DOCBOOK - Telemedicine Portal");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
