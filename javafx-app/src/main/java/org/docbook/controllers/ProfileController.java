package org.docbook.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.docbook.entities.users.Doctor;
import org.docbook.entities.users.User;
import org.docbook.services.users.UserService;
import org.docbook.util.AppState;
import org.docbook.util.AvatarService;
import org.docbook.util.ThemeManager;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ProfileController {

    @FXML private TextField nameField, emailField, specialtyField, priceField, avatarPromptField;
    @FXML private TextArea bioArea;
    @FXML private PasswordField passwordField;
    @FXML private VBox doctorExtraFields;
    @FXML private Label statusLabel;
    @FXML private ImageView avatarImage;
    @FXML private ComboBox<String> avatarStyleBox;
    @FXML private Button btnToggleTheme;

    private final UserService userService = new UserService();
    private User currentUser;

    @FXML
    public void initialize() {
        currentUser = AppState.getCurrentUser();

        if (nameField == null) {
            System.err.println("CRITICAL: FXML fields not injected. Check your fx:id in profile.fxml");
            return;
        }

        if (currentUser != null) {
            nameField.setText(currentUser.getName() != null ? currentUser.getName() : "");
            emailField.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");

            loadAvatar();

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

    private void loadAvatar() {
        Platform.runLater(() -> {
            try {
                String avatarPath = AvatarService.getAvatarPath(currentUser.getId());
                if (avatarPath == null) {
                    System.err.println("No avatar found, no default either");
                    return;
                }
                System.out.println("Loading avatar from: " + avatarPath);
                File avatarFile = new File(avatarPath);
                if (!avatarFile.exists()) {
                    System.err.println("Avatar file does not exist: " + avatarPath);
                    return;
                }
                System.out.println("File exists: " + avatarFile.exists() + ", size: " + avatarFile.length());
                Image image = new Image(avatarFile.toURI().toString(), false);
                if (image.isError()) {
                    System.err.println("Image load error for: " + avatarPath);
                    image.getException().printStackTrace();
                } else {
                    System.out.println("Image loaded: " + image.getWidth() + "x" + image.getHeight());
                }
                avatarImage.setImage(image);
            } catch (Exception e) {
                System.err.println("Failed to load avatar: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void handleUploadAvatar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(avatarImage.getScene().getWindow());

        if (selectedFile != null) {
            try {
                System.out.println("Selected file: " + selectedFile.getAbsolutePath());
                System.out.println("File readable: " + selectedFile.canRead());
                System.out.println("File size: " + selectedFile.length());
                AvatarService.saveAvatarFromFile(currentUser.getId(), selectedFile);
                Platform.runLater(() -> loadAvatar());
                statusLabel.setText("Photo uploaded successfully!");
                statusLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
            } catch (Exception e) {
                statusLabel.setText("Error uploading photo: " + e.getMessage());
                statusLabel.setStyle("-fx-text-fill: #ef4444;");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleGenerateAvatar() {
        if (avatarPromptField.isVisible()) {
            String prompt = avatarPromptField.getText();
            if (prompt == null || prompt.trim().isEmpty()) {
                statusLabel.setText("Please enter a description for your avatar.");
                statusLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                return;
            }

            statusLabel.setText("Generating avatar... (this may take a few seconds)");
            statusLabel.setStyle("-fx-text-fill: #3b82f6; -fx-font-weight: bold;");

            new Thread(() -> {
                try {
                    AvatarService.generateAndSaveAvatar(currentUser.getId(), prompt.trim());
                    Platform.runLater(() -> {
                        loadAvatar();
                        avatarPromptField.setVisible(false);
                        avatarPromptField.setManaged(false);
                        statusLabel.setText("AI avatar generated successfully!");
                        statusLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        statusLabel.setText("Error: " + e.getMessage());
                        statusLabel.setStyle("-fx-text-fill: #ef4444;");
                    });
                    e.printStackTrace();
                }
            }).start();
        } else {
            avatarPromptField.setVisible(true);
            avatarPromptField.setManaged(true);
            avatarPromptField.requestFocus();
            statusLabel.setText("Enter a description, then click 'Generate AI Avatar' again.");
            statusLabel.setStyle("-fx-text-fill: #3b82f6; -fx-font-weight: bold;");
        }
    }

    @FXML
    private void handleToggleTheme() {
        ThemeManager.toggleTheme();
        String newTheme = ThemeManager.getCurrentTheme();
        statusLabel.setText("Switched to " + newTheme + " mode.");
        statusLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");

        try {
            currentUser.setThemePreference(newTheme);
            userService.saveThemePreference(currentUser.getId(), newTheme);
        } catch (Exception e) {
            System.err.println("Failed to save theme preference: " + e.getMessage());
        }
    }

    @FXML
    private void handleSave() {
        try {
            currentUser.setName(nameField.getText());
            if (passwordField.getText() != null && !passwordField.getText().isEmpty()) {
                currentUser.setPassword(passwordField.getText());
            }
            currentUser.setAvatarUrl(AvatarService.getAvatarPath(currentUser.getId()));

            if (currentUser instanceof Doctor) {
                Doctor doc = (Doctor) currentUser;
                doc.setSpecialty(specialtyField.getText());
                doc.setConsultationFee(Double.parseDouble(priceField.getText()));
                doc.setBio(bioArea.getText());
            }

            userService.updateProfile(currentUser);

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
