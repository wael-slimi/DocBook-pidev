package org.docbook.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.docbook.entities.users.Doctor;
import org.docbook.entities.users.User;
import org.docbook.services.users.UserService;
import org.docbook.util.AppState;

public class ProfileController {

    @FXML private TextField nameField, emailField, specialtyField, priceField;
    @FXML private TextArea bioArea;
    @FXML private PasswordField passwordField;
    @FXML private VBox doctorExtraFields;
    @FXML private Label statusLabel;

    private final UserService userService = new UserService();
    private User currentUser;

    @FXML
    public void initialize() {
        currentUser = AppState.getCurrentUser();

        // SAFETY CHECK: Ensure FXML linked correctly
        if (nameField == null) {
            System.err.println("CRITICAL: FXML fields not injected. Check your fx:id in profile.fxml");
            return;
        }

        if (currentUser != null) {
            nameField.setText(currentUser.getName() != null ? currentUser.getName() : "");
            emailField.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");

            if (currentUser instanceof Doctor) {
                Doctor doc = (Doctor) currentUser;
                if (specialtyField != null) specialtyField.setText(doc.getSpecialty());
                if (priceField != null) priceField.setText(String.valueOf(doc.getConsultationFee()));
                if (bioArea != null) bioArea.setText(doc.getBio());

                if (doctorExtraFields != null) {
                    doctorExtraFields.setVisible(true);
                    doctorExtraFields.setManaged(true);
                }
            } else if (doctorExtraFields != null) {
                doctorExtraFields.setVisible(false);
                doctorExtraFields.setManaged(false);
            }
        }
    }

    @FXML
    private void handleSave() {
        try {
            currentUser.setName(nameField.getText());
            // Only update password if user typed something
            if (passwordField.getText() != null && !passwordField.getText().isEmpty()) {
                currentUser.setPassword(passwordField.getText());
            }

            if (currentUser instanceof Doctor) {
                Doctor doc = (Doctor) currentUser;
                doc.setSpecialty(specialtyField.getText());
                doc.setConsultationFee(Double.parseDouble(priceField.getText()));
                doc.setBio(bioArea.getText());
            }

            userService.updateProfile(currentUser);

            // REFRESH: Update the AppState with the modified object
            AppState.setCurrentUser(currentUser);

            statusLabel.setText("Profil mis à jour !");
            statusLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
        } catch (NumberFormatException e) {
            statusLabel.setText("Erreur: Le prix doit être un nombre.");
            statusLabel.setStyle("-fx-text-fill: #ef4444;");
        } catch (Exception e) {
            statusLabel.setText("Erreur: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #ef4444;");
        }
    }
}