package org.docbook.mains;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.docbook.util.ThemeManager;

public class mainFx extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auth/login.fxml"));
        Scene scene = new Scene(loader.load());
        ThemeManager.applyTheme(scene);
        primaryStage.setTitle("DOCBOOK - Telemedicine Portal");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
