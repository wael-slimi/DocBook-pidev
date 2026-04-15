package org.docbook.controllers.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.docbook.services.users.UserService;
import org.docbook.entities.users.User;
import org.docbook.util.AppState;

import java.io.IOException;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    private final UserService userService = new UserService();

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String pass = passwordField.getText();

        User user = userService.login(email, pass);
        if (user != null) {
            // 1. Save the session
            AppState.setCurrentUser(user);

            // 2. Determine the path based on user type (dtype)
            String dashboardPath = "";
            if ("doctor".equalsIgnoreCase(user.getDtype())) {
                dashboardPath = "/fxml/doctor/DoctorDashboard.fxml";
            } else {
                dashboardPath = "/fxml/patient/PatientDashboard.fxml";
            }

            // 3. Switch Scene
            try {
                switchScene(event, dashboardPath);
            } catch (IOException e) {
                showAlert("Navigation Error", "Could not find the dashboard file.");
                e.printStackTrace();
            }
        } else {
            showAlert("Login Failed", "Invalid email or password.");
        }
    }

    @FXML
    private void goToSignup(ActionEvent event) throws IOException {
        switchScene(event, "/fxml/auth/signup.fxml");
    }

    protected void switchScene(ActionEvent event, String fxmlPath) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.show();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}