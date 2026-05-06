package org.docbook.controllers.patient;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AdherenceHistoryController {

    @FXML
    private void goToDashboard(ActionEvent event) throws IOException {
        switchView(event, "/fxml/patient/PatientDashboard.fxml");
    }

    @FXML
    private void goToDocuments(ActionEvent event) throws IOException {
        switchView(event, "/fxml/records/DocumentView.fxml");
    }

    @FXML
    private void goToSearch(ActionEvent event) throws IOException {
        switchView(event, "/fxml/patient/search_doctors.fxml");
    }

    @FXML
    private void goToStats(ActionEvent event) throws IOException {
        switchView(event, "/fxml/doctor/StatsView.fxml");
    }

    @FXML
    private void goToMap(ActionEvent event) throws IOException {
        switchView(event, "/fxml/records/MapView.fxml");
    }

    @FXML
    private void goToProfile(ActionEvent event) throws IOException {
        switchView(event, "/fxml/patient/PatientProfile.fxml");
    }

    @FXML
    private void logout(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/LoginView.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("DocBook - Connexion");
    }

    private void switchView(ActionEvent event, String fxmlPath) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }
}