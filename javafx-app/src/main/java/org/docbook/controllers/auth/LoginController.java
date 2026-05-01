package org.docbook.controllers.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.docbook.services.users.GoogleAuthService;
import org.docbook.services.users.UserService;
import org.docbook.entities.users.User;
import org.docbook.util.AppState;
import org.docbook.util.GoogleAuthHelper;
import org.docbook.util.ThemeManager;

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
            // Check if email is verified
            if (!user.isIsVerified()) {
                showAlert("Email Not Verified", "Please verify your email address before logging in.");
                try {
                    goToEmailVerification(event, email);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }

            if ("doctor".equalsIgnoreCase(user.getDtype()) && "pending".equalsIgnoreCase(user.getStatus())) {
                System.err.println("Login Denied: Doctor account is pending admin approval.");
                showAlert("Account Pending", "Your account is currently under review by an administrator. Please try again once approved.");
                return; // Stop here, don't switch scenes
            }
            // 1. Save the session
            AppState.setCurrentUser(user);
            ThemeManager.loadFromUser(user);

            // 2. Determine the path based on Role and Dtype
            String dashboardPath;

            // Priority 1: Check if Admin
            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                dashboardPath = "/fxml/admin/admin_dashboard.fxml";
            }
            // Priority 2: Check if Doctor
            else if ("doctor".equalsIgnoreCase(user.getDtype())) {
                dashboardPath = "/fxml/doctor/DoctorDashboard.fxml";
            }
            // Default: Patient
            else {
                dashboardPath = "/fxml/patient/PatientDashboard.fxml";
            }

            // 3. Switch Scene with Path Validation
            try {
                // DEBUG: Check if the resource actually exists before switching
                if (getClass().getResource(dashboardPath) == null) {
                    throw new IOException("FXML file not found at: " + dashboardPath);
                }
                switchScene(event, dashboardPath);
            } catch (IOException e) {
                showAlert("Navigation Error", "Critical: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // Check if user exists but just unverified (login returns null in that case too now)
            User existingUser = userService.getUserByEmail(email);
            if (existingUser != null && !existingUser.isIsVerified()) {
                 showAlert("Email Not Verified", "Please verify your email address before logging in.");
                 try {
                     goToEmailVerification(event, email);
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
            } else {
                showAlert("Login Failed", "Invalid email or password.");
            }
        }
    }

    @FXML
    private void handleGoogleLogin(ActionEvent event) {
        new Thread(() -> {
            try {
                GoogleAuthService.GoogleUserInfo googleUser = GoogleAuthHelper.authenticate();

                User user = userService.loginOrCreateWithGoogle(googleUser);

                if (user != null) {
                    AppState.setCurrentUser(user);
                    ThemeManager.loadFromUser(user);

                    String dashboardPath;
                    if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                        dashboardPath = "/fxml/admin/admin_dashboard.fxml";
                    } else if ("doctor".equalsIgnoreCase(user.getDtype())) {
                        dashboardPath = "/fxml/doctor/DoctorDashboard.fxml";
                    } else {
                        dashboardPath = "/fxml/patient/PatientDashboard.fxml";
                    }

                    javafx.application.Platform.runLater(() -> {
                        try {
                            switchScene(event, dashboardPath);
                        } catch (IOException e) {
                            showAlert("Navigation Error", "Could not load dashboard: " + e.getMessage());
                        }
                    });
                }
            } catch (Exception e) {
                javafx.application.Platform.runLater(() ->
                    showAlert("Google Login Failed", "Error: " + e.getMessage())
                );
            }
        }).start();
    }

    private void goToEmailVerification(ActionEvent event, String email) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auth/email_verification.fxml"));
        Scene scene = new Scene(loader.load());

        EmailVerificationController controller = loader.getController();
        controller.setUserEmail(email);

        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void goToSignup(ActionEvent event) throws IOException {
        switchScene(event, "/fxml/auth/signup.fxml");
        }

    @FXML
    private void goToForgotPassword(ActionEvent event) throws IOException {
        switchScene(event, "/fxml/auth/forgot_password.fxml");
    }

    protected void switchScene(ActionEvent event, String fxmlPath) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        ThemeManager.applyTheme(scene);
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
