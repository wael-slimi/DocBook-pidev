package org.docbook.controllers.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.docbook.entities.users.User;
import org.docbook.services.users.UserService;
import java.io.IOException;

public class SignupController extends LoginController {
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    private final UserService userService = new UserService();

    @FXML
    private void handleRegistration(ActionEvent event) {
        String name = nameField.getText();
        String email = emailField.getText();
        String pass = passwordField.getText();

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            return;
        }

        // Create new user (defaulting to 'patient' for Step 1)
        User newUser = new User(name, email, pass, "patient", "patient");
        userService.create(newUser);

        System.out.println("Account created successfully!");
        try {
            switchScene(event, "/fxml/auth/login.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToLogin(ActionEvent event) throws IOException {
        switchScene(event, "/fxml/auth/login.fxml");
    }
}