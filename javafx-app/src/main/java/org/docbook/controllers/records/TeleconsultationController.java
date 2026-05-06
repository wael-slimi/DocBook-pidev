package org.docbook.controllers.records;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.docbook.entities.records.Appointment;
import org.docbook.entities.records.Teleconsultation;
import org.docbook.services.AppointmentService;
import org.docbook.services.TeleconsultationService;
import org.docbook.util.AppState;
import org.docbook.entities.users.User;

import java.util.ArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class TeleconsultationController implements javafx.fxml.Initializable {

    @FXML private VBox cardContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> modeFilter;
    @FXML private Label statusLabel;
    @FXML private Label messageLabel;
    @FXML private Button btnSearch;
    @FXML private Button btnRefresh;
    @FXML private Button btnNew;

    private TeleconsultationService teleconsultationService;
    private AppointmentService appointmentService;
    private List<Teleconsultation> allTeleconsultations;
    
    private String currentUserRole;
    private int currentUserId;

    @Override
    public void initialize(java.net.URL url, ResourceBundle resourceBundle) {
        teleconsultationService = new TeleconsultationService();
        appointmentService = new AppointmentService();
        
        User currentUser = AppState.getCurrentUser();
        currentUserRole = (currentUser != null) ? currentUser.getRole() : "UNKNOWN";
        currentUserId = (currentUser != null) ? currentUser.getId() : 0;
        
        System.out.println("TeleconsultationController - Role: " + currentUserRole + ", UserID: " + currentUserId);
        
        setupModeFilter();
        loadTeleconsultations();
    }

    private void setupModeFilter() {
        modeFilter.getItems().addAll("All", "video", "audio", "chat");
        modeFilter.setValue("All");
        
        if (currentUserRole.equals("PATIENT")) {
            btnNew.setVisible(false);
        }
    }

    @FXML
    public void loadTeleconsultations() {
        new Thread(() -> {
            try {
                if (currentUserRole != null && currentUserRole.toUpperCase().contains("PATIENT")) {
                    allTeleconsultations = teleconsultationService.getByPatientId(currentUserId);
                } else if (currentUserRole != null && currentUserRole.toUpperCase().contains("DOCTOR")) {
                    allTeleconsultations = teleconsultationService.getByDoctorId(currentUserId);
                } else {
                    allTeleconsultations = teleconsultationService.readAll();
                }
                
                Platform.runLater(this::displayTeleconsultations);
                Platform.runLater(() -> messageLabel.setText(""));
            } catch (Exception e) {
                Platform.runLater(() -> showError("Error loading: " + e.getMessage()));
            }
        }).start();
    }

    private void displayTeleconsultations() {
        cardContainer.getChildren().clear();

        if (allTeleconsultations == null || allTeleconsultations.isEmpty()) {
            Label emptyLabel = new Label("No teleconsultations found");
            emptyLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #999;");
            cardContainer.getChildren().add(emptyLabel);
            statusLabel.setText("Total: 0");
            return;
        }

        for (Teleconsultation tele : allTeleconsultations) {
            cardContainer.getChildren().add(createTeleconsultationCard(tele));
        }

        statusLabel.setText("Total: " + allTeleconsultations.size());
    }

    private AnchorPane createTeleconsultationCard(Teleconsultation tele) {
        AnchorPane card = new AnchorPane();
        card.setStyle(
            "-fx-border-color: #e0e0e0; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 8; " +
            "-fx-padding: 15; " +
            "-fx-background-color: #ffffff; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);");
        card.setPrefHeight(180);

        HBox mainContent = new HBox(20);
        mainContent.setAlignment(Pos.CENTER_LEFT);

        VBox leftSection = new VBox(5);
        Appointment apt = tele.getAppointment();
        
        Label patientLabel = new Label("Patient ID: " + (apt != null ? apt.getPatientId() : "N/A"));
        patientLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #333;");

        Label dateLabel = new Label("Date: " + (apt != null && apt.getScheduledAt() != null ? apt.getScheduledAt().toString() : "N/A"));
        dateLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #666;");

        Label reasonLabel = new Label("Reason: " + (apt != null ? apt.getMessage() : "N/A"));
        reasonLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #666;");

        leftSection.getChildren().addAll(patientLabel, dateLabel, reasonLabel);

        VBox middleSection = new VBox(5);
        middleSection.setPrefWidth(300);
        
        Label durationLabel = new Label("Duration: " + tele.getDuration() + " min");
        durationLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #333;");
        
        Label urlTitle = new Label("Video Link:");
        urlTitle.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #999;");
        
        Hyperlink urlLink = new Hyperlink(tele.getMeetingUrl());
        urlLink.setStyle("-fx-font-size: 11;");
        urlLink.setOnAction(e -> openMeetingUrl(tele.getMeetingUrl()));
        
        Label accessTitle = new Label("Mode: " + tele.getMode());
        accessTitle.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #333;");
        
        middleSection.getChildren().addAll(durationLabel, urlTitle, urlLink, accessTitle);

        VBox rightSection = new VBox(8);
        rightSection.setAlignment(Pos.TOP_CENTER);
        rightSection.setPrefWidth(150);

        Label statusBadge = new Label("TELECONSULTATION");
        statusBadge.setStyle("-fx-padding: 8; -fx-font-size: 11; -fx-background-color: #2196F3; -fx-text-fill: white; -fx-border-radius: 4;");
        statusBadge.setPrefWidth(130);
        statusBadge.setAlignment(Pos.CENTER);

        HBox buttonBox = new HBox(5);
        buttonBox.setAlignment(Pos.CENTER);

        // Doctors can edit, Patients can only join
        if (!currentUserRole.equals("PATIENT")) {
            Button editBtn = new Button("Edit");
            editBtn.setStyle("-fx-padding: 8 12; -fx-font-size: 10; -fx-background-color: #2196F3; -fx-text-fill: white;");
            editBtn.setOnAction(e -> editTeleconsultation(tele));

            Button deleteBtn = new Button("Delete");
            deleteBtn.setStyle("-fx-padding: 8 12; -fx-font-size: 10; -fx-background-color: #f44336; -fx-text-fill: white;");
            deleteBtn.setOnAction(e -> deleteTeleconsultation(tele));

            buttonBox.getChildren().addAll(editBtn, deleteBtn);
        }

        Button joinBtn = new Button("Join");
        joinBtn.setStyle("-fx-padding: 8 12; -fx-font-size: 10; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        joinBtn.setOnAction(e -> joinTeleconsultation(tele));
        buttonBox.getChildren().add(joinBtn);

        rightSection.getChildren().addAll(statusBadge, buttonBox);
        mainContent.getChildren().addAll(leftSection, middleSection, rightSection);
        card.getChildren().add(mainContent);
        AnchorPane.setLeftAnchor(mainContent, 0.0);
        AnchorPane.setRightAnchor(mainContent, 0.0);

        return card;
    }

    @FXML
    public void onSearch() {
        String query = searchField.getText().trim();
        String modeFilterValue = modeFilter.getValue();

        new Thread(() -> {
            try {
                List<Teleconsultation> results;

                if (query.isEmpty() && (modeFilterValue == null || modeFilterValue.equals("All"))) {
                    results = allTeleconsultations;
                } else if (!query.isEmpty()) {
                    results = teleconsultationService.searchAndFilter(query, modeFilterValue);
                } else if ("video".equals(modeFilterValue) || "audio".equals(modeFilterValue) || "chat".equals(modeFilterValue)) {
                    results = teleconsultationService.getByMode(modeFilterValue);
                } else {
                    results = allTeleconsultations;
                }

                List<Teleconsultation> finalResults = results;
                Platform.runLater(() -> {
                    allTeleconsultations = finalResults;
                    displayTeleconsultations();
                    messageLabel.setText("Found " + finalResults.size() + " results");
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Search error: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    public void onNewTeleconsultation() {
        // Only doctors can create teleconsultations
        if ("patient".equals(currentUserRole) || "PATIENT".equals(currentUserRole)) {
            showError("Only doctors can create teleconsultations");
            return;
        }
        
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Créer une Téléconsultation");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 10, 10, 10));

        // Load pending appointments
        List<Appointment> pendingAppts = new ArrayList<>();
        try {
            var allAppts = appointmentService.readAll();
            for (var apt : allAppts) {
                if ("PENDING".equals(apt.getStatus()) && apt.getDoctorId() == currentUserId) {
                    pendingAppts.add(apt);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading appointments: " + e.getMessage());
        }

        ComboBox<String> apptCombo = new ComboBox<>();
        for (Appointment apt : pendingAppts) {
            apptCombo.getItems().add("ID: " + apt.getId() + " - " + apt.getScheduledAt());
        }

        ComboBox<String> modeCombo = new ComboBox<>();
        modeCombo.getItems().addAll("video", "chat", "audio");
        modeCombo.setValue("video");

        TextField durationField = new TextField("30");
        durationField.setPromptText("Duration in minutes");

        TextField linkField = new TextField();
        linkField.setPromptText("Meeting URL (Zoom, Google Meet, etc.)");

        grid.add(new Label("Appointment:"), 0, 0);
        grid.add(apptCombo, 1, 0);
        grid.add(new Label("Mode:"), 0, 1);
        grid.add(modeCombo, 1, 1);
        grid.add(new Label("Duration (min):"), 0, 2);
        grid.add(durationField, 1, 2);
        grid.add(new Label("Meeting URL:"), 0, 3);
        grid.add(linkField, 1, 3);

        dialog.getDialogPane().setContent(grid);
        
        ButtonType submitBtn = new ButtonType("Créer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitBtn, ButtonType.CANCEL);

        dialog.showAndWait();

        ButtonType result = dialog.getDialogPane().getButtonTypes().get(0);
        if (result.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
            try {
                int selectedIndex = apptCombo.getSelectionModel().getSelectedIndex();
                if (selectedIndex < 0 || pendingAppts.isEmpty()) {
                    showError("Please select an appointment");
                    return;
                }
                
                Appointment selectedAppt = pendingAppts.get(selectedIndex);
                
                Teleconsultation tele = new Teleconsultation();
                tele.setAppointmentId(selectedAppt.getId());
                tele.setMode(modeCombo.getValue());
                tele.setDuration(Integer.parseInt(durationField.getText()));
                tele.setMeetingUrl(linkField.getText());
                
                teleconsultationService.create(tele);
                
                // Update appointment status
                selectedAppt.setStatus("CONFIRMED");
                appointmentService.update(selectedAppt);
                
                showInfo("Téléconsultation créée avec succès!");
                loadTeleconsultations();
            } catch (Exception e) {
                showError("Erreur: " + e.getMessage());
            }
        }
    }

    private void editTeleconsultation(Teleconsultation tele) {
        // Simple edit dialog for mode, duration, video_link
        Dialog<Teleconsultation> dialog = new Dialog<>();
        dialog.setTitle("Edit Teleconsultation");
        dialog.setHeaderText("Edit mode, duration and meeting link");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 10, 10, 10));

        TextField durationField = new TextField(String.valueOf(tele.getDuration()));
        durationField.setPromptText("Duration in minutes");

        ComboBox<String> modeCombo = new ComboBox<>();
        modeCombo.getItems().addAll("video", "chat", "audio");
        modeCombo.setValue(tele.getMode());

        TextField linkField = new TextField(tele.getMeetingUrl());
        linkField.setPromptText("Video meeting URL");

        grid.add(new Label("Duration (min):"), 0, 0);
        grid.add(durationField, 1, 0);
        grid.add(new Label("Mode:"), 0, 1);
        grid.add(modeCombo, 1, 1);
        grid.add(new Label("Video Link:"), 0, 2);
        grid.add(linkField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            try {
                tele.setDuration(Integer.parseInt(durationField.getText()));
                tele.setMode(modeCombo.getValue());
                tele.setMeetingUrl(linkField.getText());
                teleconsultationService.update(tele);
                showInfo("Teleconsultation updated successfully");
                loadTeleconsultations();
            } catch (Exception e) {
                showError("Error updating: " + e.getMessage());
            }
        });
    }

    private void deleteTeleconsultation(Teleconsultation tele) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Teleconsultation");
        alert.setContentText("Are you sure you want to delete this teleconsultation?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            new Thread(() -> {
                try {
                    teleconsultationService.delete(tele.getId());
                    Platform.runLater(() -> {
                        showInfo("Teleconsultation deleted successfully");
                        loadTeleconsultations();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> showError("Error deleting: " + e.getMessage()));
                }
            }).start();
        }
    }

    private void joinTeleconsultation(Teleconsultation tele) {
        if (tele.getMeetingUrl() != null && !tele.getMeetingUrl().isEmpty()) {
            openMeetingUrl(tele.getMeetingUrl());
        } else {
            showError("No meeting URL available");
        }
    }

    private void openMeetingUrl(String url) {
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception e) {
            showError("Could not open meeting URL: " + e.getMessage());
        }
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

@FXML
    private void goBack() {
        try {
            User currentUser = AppState.getCurrentUser();
            String userRole = (currentUser != null) ? currentUser.getRole() : "UNKNOWN";
            
            String fxml;
            String title;
            
            if (userRole != null && userRole.toUpperCase().contains("DOCTOR")) {
                fxml = "/fxml/doctor/DoctorDashboard.fxml";
                title = "DocBook - Médecin";
            } else {
                fxml = "/fxml/patient/PatientDashboard.fxml";
                title = "DocBook - Patient";
            }
            
            java.net.URL resource = getClass().getResource(fxml);
            if (resource != null) {
                Parent root = FXMLLoader.load(resource);
                Scene scene = cardContainer.getScene();
                if (scene != null) {
                    Stage stage = (Stage) scene.getWindow();
                    stage.setScene(new Scene(root));
                    stage.setTitle(title);
                    stage.show();
                }
            }
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }
}