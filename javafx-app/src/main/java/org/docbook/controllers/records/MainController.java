package org.docbook.controllers.records;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    /**
     * Ouvre le tableau de bord medecin depuis le bouton principal.
     */
    @FXML
    private void openDoctorDashboard(ActionEvent event) {
        loadView(event, "/fxml/doctor/DoctorDashboard.fxml", "Espace Médecin");
    }

    /**
     * Ouvre le tableau de bord patient depuis le bouton principal.
     */
    @FXML
    private void openPatientDashboard(ActionEvent event) {
        loadView(event, "/fxml/patient/PatientDashboard.fxml", "Espace Patient");
    }

    /**
     * Ouvre l'espace medecin via un clic sur la carte.
     */
    @FXML
    private void openDoctorDashboardCard(MouseEvent event) {
        // Handle MouseClick from Card
        loadViewCard(event, "/fxml/doctor/DoctorDashboard.fxml", "Espace Médecin");
    }

    /**
     * Ouvre l'espace patient via un clic sur la carte.
     */
    @FXML
    private void openPatientDashboardCard(MouseEvent event) {
        // Handle MouseClick from Card
        loadViewCard(event, "/fxml/patient/PatientDashboard.fxml", "Espace Patient");
    }

    /**
     * Charge une vue FXML a partir d'un evenement souris.
     */
    private void loadViewCard(MouseEvent event, String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("DocBook - " + title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Charge une vue FXML a partir d'un evenement bouton.
     */
    private void loadView(ActionEvent event, String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage;
            if (event != null) {
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            } else {
                // Fallback for card clicks without event
                // This is a bit tricky, usually we'd pass the stage or find it from another node
                // For simplicity, let's assume we can get it from the cards if they are FXML-linked
                // But let's just make it simpler for now and assume it's always from button
                return; 
            }
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("DocBook - " + title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


