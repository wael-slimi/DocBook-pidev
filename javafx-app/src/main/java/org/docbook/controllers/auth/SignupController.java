package org.docbook.controllers.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.docbook.entities.users.User;
import org.docbook.services.users.UserService;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;

public class SignupController extends LoginController implements Initializable {

    // UI Containers
    @FXML private VBox step1Container;
    @FXML private VBox step2Container;
    @FXML private Label stepLabel;
    @FXML private Label progressPercentLabel;
    @FXML private ProgressBar progressBar;

    // Form Fields
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> roleComboBox;

    private final UserService userService = new UserService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize the ComboBox with roles
        if (roleComboBox != null) {
            roleComboBox.getItems().addAll("Médecin", "Patient");
        }
    }

    @FXML
    private void showStep2() {
        // Basic validation for Step 1
        if (nameField.getText().isEmpty() || emailField.getText().isEmpty() || phoneField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez remplir tous les champs de l'étape 1.");
            return;
        }

        // Navigation Logic
        step1Container.setVisible(false);
        step2Container.setVisible(true);

        // Update Progress UI
        stepLabel.setText("Step 2 of 2");
        progressPercentLabel.setText("100% Complete");
        progressBar.setProgress(1.0);
    }

    @FXML
    private void showStep1() {
        step2Container.setVisible(false);
        step1Container.setVisible(true);

        stepLabel.setText("Step 1 of 2");
        progressPercentLabel.setText("50% Complete");
        progressBar.setProgress(0.5);
    }

    @FXML
    private void handleRegistration(ActionEvent event) {
        String name = nameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String pass = passwordField.getText();
        String confirmPass = confirmPasswordField.getText();
        String selectedRole = roleComboBox.getValue();

        // 1. Validation for Step 2 fields
        if (pass.isEmpty() || confirmPass.isEmpty() || selectedRole == null) {
            showAlert(Alert.AlertType.ERROR, "Champs vides", "Veuillez remplir les informations de sécurité.");
            return;
        }

        // 2. Double Checkout (Password Match)
        if (!pass.equals(confirmPass)) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Les mots de passe ne correspondent pas.");
            return;
        }

        // 3. Unique Email Check
        if (userService.emailExists(email)) {
            showAlert(Alert.AlertType.ERROR, "Compte existant",
                    "Cet email est déjà utilisé.");
            return;
        }

        // 4. Password Strength Check
        if (!isPasswordValid(pass)) {
            showAlert(Alert.AlertType.WARNING, "Mot de passe faible",
                    "8 caractères minimum avec au moins un chiffre.");
            return;
        }

        // 5. Create User
        // Mapping UI Role to Database Role
        String dbRole = selectedRole.equals("Médecin") ? "doctor" : "patient";

        User newUser = new User(name, email, pass, dbRole, dbRole);

        userService.create(newUser); // This now sends the verification email

        System.out.println("Registration complete for: " + name + ". Redirecting to email verification.");
        try {
            // Switch to email verification scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auth/email_verification.fxml"));
            Scene scene = new Scene(loader.load());

            EmailVerificationController controller = loader.getController();
            controller.setUserEmail(email); // Pass the email to the verification controller

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load email verification screen.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private boolean isPasswordValid(String password) {
        return password != null && password.matches("^(?=.*[0-9]).{8,}$");
    }

    @FXML
    private void goToLogin(ActionEvent event) throws IOException {
        switchScene(event, "/fxml/auth/login.fxml");
    }
}
