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

public class EmailVerificationController {

    @FXML
    private Label emailDisplayLabel;
    @FXML
    private TextField verificationCodeField;
    @FXML
    private Label messageLabel;

    private String userEmail;
    private final UserService userService = new UserService();

    public void setUserEmail(String email) {
        this.userEmail = email;
        emailDisplayLabel.setText("A verification code has been sent to: " + email);
    }

    @FXML
    private void handleVerify(ActionEvent event) {
        String code = verificationCodeField.getText();
        if (code.isEmpty()) {
            messageLabel.setText("Please enter the verification code.");
            return;
        }

        boolean success = userService.verifyUserEmail(userEmail, code);

        if (success) {
            messageLabel.setText("Email verified successfully! You can now login.");
            messageLabel.setStyle("-fx-text-fill: green;");
            // Optionally redirect to login after a delay
            try {
                handleBackToLogin(event);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            messageLabel.setText("Invalid verification code. Please try again.");
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void handleResendCode(ActionEvent event) {
        boolean success = userService.resendVerificationCode(userEmail);
        if (success) {
            messageLabel.setText("A new verification code has been sent.");
            messageLabel.setStyle("-fx-text-fill: green;");
        } else {
            messageLabel.setText("Failed to resend code. Please try again later.");
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
