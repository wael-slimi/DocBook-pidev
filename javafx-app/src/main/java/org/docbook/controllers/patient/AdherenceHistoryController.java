package org.docbook.controllers.patient;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AdherenceHistoryController {

    /**
     * Retourne vers le tableau de bord medecin.
     */
    public void goBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/doctor/DoctorDashboard.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Espace Medecin");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
