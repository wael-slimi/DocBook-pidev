package org.docbook.controllers.doctor;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.docbook.entities.users.User;
import org.docbook.util.AppState;

public class DoctorDashboardController {

    @FXML
    private Label welcomeLabel; // Make sure your FXML has a Label with fx:id="welcomeLabel"

    @FXML
    public void initialize() {
        // Get the user we saved during login
        User currentUser = AppState.getCurrentUser();

        if (currentUser != null) {
            welcomeLabel.setText("Welcome, Dr. " + currentUser.getName());
        } else {
            welcomeLabel.setText("Welcome, Doctor");
        }
    }

    @FXML
    private void handleLogout() {
        // Clear session and go back to login
        AppState.setCurrentUser(null);
        // Add your switchScene logic here to return to /fxml/auth/login.fxml
    }
}