package org.docbook.controllers.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.docbook.services.users.UserService;

import java.io.IOException;

public class ForgotPasswordController {

    @FXML
    private TextField emailField;

    @FXML
    private Label messageLabel;

    private final UserService userService = new UserService();

    @FXML
    private void handleSendResetLink(ActionEvent event) {
        String email = emailField.getText();
        if (email.isEmpty()) {
            messageLabel.setText("Please enter your email address.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        boolean success = userService.sendPasswordResetLink(email);

        if (success) {
            messageLabel.setText("If an account with that email exists, a password reset token has been sent.");
            messageLabel.setStyle("-fx-text-fill: green;");
        } else {
            messageLabel.setText("Failed to send reset link. Please check your email or try again.");
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void handleGoToResetPassword(ActionEvent event) throws IOException {
        switchScene(event, "/fxml/auth/reset_password.fxml");
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
