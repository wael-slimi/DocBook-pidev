package org.docbook.controllers.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.docbook.services.users.UserService;

import java.io.IOException;

public class ResetPasswordController {

    @FXML
    private TextField tokenField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label messageLabel;

    private final UserService userService = new UserService();

    @FXML
    private void handleResetPassword(ActionEvent event) {
        String token = tokenField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (token.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            messageLabel.setText("Please fill in all fields.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            messageLabel.setText("Passwords do not match.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // Password strength validation (optional, but recommended)
        if (newPassword.length() < 6) {
            messageLabel.setText("Password must be at least 6 characters long.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        boolean success = userService.resetPassword(token, newPassword);

        if (success) {
            messageLabel.setText("Password has been reset successfully! Redirecting to login...");
            messageLabel.setStyle("-fx-text-fill: green;");
            // Optionally, navigate back to login after a short delay
            try {
                // For immediate navigation
                handleBackToLogin(event);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            messageLabel.setText("Invalid or expired token, or password reset failed.");
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void handleBackToLogin(ActionEvent event) throws IOException {
        switchScene(event, "/fxml/auth/login.fxml");
    }

    private void switchScene(ActionEvent event, String fxmlPath) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.show();
    }
}
