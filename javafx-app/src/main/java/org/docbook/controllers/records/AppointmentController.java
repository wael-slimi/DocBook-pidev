package org.docbook.controllers.records;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.docbook.entities.records.Appointment;
import org.docbook.entities.users.User;
import org.docbook.services.AppointmentService;
import org.docbook.services.users.UserService;
import org.docbook.entities.users.Doctor;
import org.docbook.util.AppState;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AppointmentController implements javafx.fxml.Initializable {

    @FXML private VBox cardContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Label statusLabel;
    @FXML private Label messageLabel;
    @FXML private Button btnSearch;
    @FXML private Button btnRefresh;
    @FXML private Button btnNew;

    private AppointmentService appointmentService;
    private List<Appointment> allAppointments;
    private static final String dateFormatter = "yyyy-MM-dd HH:mm";
    
    private String currentUserRole;
    private int currentUserId;

    @Override
    public void initialize(java.net.URL url, ResourceBundle resourceBundle) {
        appointmentService = new AppointmentService();
        
        User currentUser = AppState.getCurrentUser();
        currentUserRole = (currentUser != null) ? currentUser.getRole() : "UNKNOWN";
        currentUserId = (currentUser != null) ? currentUser.getId() : 0;
        
        setupStatusFilter();
        loadAppointments();
    }

    private void setupStatusFilter() {
        statusFilter.getItems().addAll(
            "All", "PENDING", "CONFIRMED", "COMPLETED", "CANCELLED");
        statusFilter.setValue("All");
        
        if (currentUserRole.equals("patient") || currentUserRole.equals("PATIENT")) {
            btnNew.setVisible(true);
            btnNew.setText("+ Demander RDV");
        }
    }

    @FXML
    public void loadAppointments() {
        new Thread(() -> {
            try {
if (currentUserRole != null && currentUserRole.toUpperCase().contains("PATIENT")) {
                    allAppointments = appointmentService.getByPatientId(currentUserId);
                } else if (currentUserRole != null && currentUserRole.toUpperCase().contains("DOCTOR")) {
                    allAppointments = appointmentService.getByDoctorId(currentUserId);
                } else {
                    allAppointments = appointmentService.readAll();
                }
                
                Platform.runLater(this::displayAppointments);
                Platform.runLater(() -> messageLabel.setText(""));
            } catch (Exception e) {
                Platform.runLater(() -> showError("Error loading: " + e.getMessage()));
            }
        }).start();
    }

    private void displayAppointments() {
        cardContainer.getChildren().clear();

        if (allAppointments == null || allAppointments.isEmpty()) {
            Label emptyLabel = new Label("No appointments found");
            emptyLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #999;");
            cardContainer.getChildren().add(emptyLabel);
            statusLabel.setText("Total: 0");
            return;
        }

        for (Appointment apt : allAppointments) {
            cardContainer.getChildren().add(createAppointmentCard(apt));
        }
        statusLabel.setText("Total: " + allAppointments.size());
    }

    private AnchorPane createAppointmentCard(Appointment apt) {
        AnchorPane card = new AnchorPane();
        card.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 8; -fx-padding: 15; -fx-background-color: #ffffff;");
        card.setPrefHeight(140);

        HBox mainContent = new HBox(20);
        mainContent.setAlignment(Pos.CENTER_LEFT);

        VBox leftSection = new VBox(5);
        boolean isPatient = currentUserRole != null && currentUserRole.toUpperCase().contains("PATIENT");
        String labelPrefix = isPatient ? "Dr. " : "Patient #";
        String labelValue = isPatient ? apt.getDoctor() : String.valueOf(apt.getPatientId());
        Label doctorLabel = new Label(labelPrefix + labelValue);
        doctorLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #333;");

        String dateStr = (apt.getScheduledAt() != null) ? apt.getScheduledAt().toString() : "N/A";
        Label dateLabel = new Label("Date: " + dateStr);
        dateLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #666;");

        leftSection.getChildren().addAll(doctorLabel, dateLabel);

        VBox middleSection = new VBox(5);
        Label reasonTitle = new Label("Reason:");
        reasonTitle.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #999;");
        TextArea messageArea = new TextArea(apt.getMessage() != null ? apt.getMessage() : "");
        messageArea.setWrapText(true);
        messageArea.setPrefRowCount(3);
        messageArea.setStyle("-fx-font-size: 11;");
        messageArea.setEditable(false);
        middleSection.getChildren().addAll(reasonTitle, messageArea);

        VBox rightSection = new VBox(8);
        rightSection.setAlignment(Pos.TOP_CENTER);
        rightSection.setPrefWidth(150);

        Label statusBadge = new Label(apt.getStatus());
        statusBadge.setStyle(getStatusStyle(apt.getStatus()));
        statusBadge.setPrefWidth(130);
        statusBadge.setAlignment(Pos.CENTER);

        HBox buttonBox = new HBox(5);
        buttonBox.setAlignment(Pos.CENTER);

        boolean isDoctor = currentUserRole != null && currentUserRole.toUpperCase().contains("DOCTOR");
        if (isDoctor) {
            Button deleteBtn = new Button("Delete");
            deleteBtn.setStyle("-fx-padding: 8 12; -fx-font-size: 10; -fx-background-color: #f44336; -fx-text-fill: white;");
            deleteBtn.setOnAction(e -> deleteAppointment(apt));
            buttonBox.getChildren().add(deleteBtn);
        }

        rightSection.getChildren().addAll(statusBadge, buttonBox);
        mainContent.getChildren().addAll(leftSection, middleSection, rightSection);
        card.getChildren().add(mainContent);
        AnchorPane.setLeftAnchor(mainContent, 0.0);
        AnchorPane.setRightAnchor(mainContent, 0.0);
        return card;
    }

    private String getStatusStyle(String status) {
        return switch (status) {
            case "PENDING" -> "-fx-padding: 8; -fx-font-size: 11; -fx-background-color: #FFC107; -fx-text-fill: white;";
            case "CONFIRMED" -> "-fx-padding: 8; -fx-font-size: 11; -fx-background-color: #4CAF50; -fx-text-fill: white;";
            case "COMPLETED" -> "-fx-padding: 8; -fx-font-size: 11; -fx-background-color: #8BC34A; -fx-text-fill: white;";
            case "CANCELLED" -> "-fx-padding: 8; -fx-font-size: 11; -fx-background-color: #f44336; -fx-text-fill: white;";
            default -> "-fx-padding: 8; -fx-font-size: 11; -fx-background-color: #757575; -fx-text-fill: white;";
        };
    }

    @FXML
    public void onNewAppointment() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Demander un Rendez-vous");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 10, 10, 10));

        // Load doctors from database
        List<String> doctorNames = new ArrayList<>();
        List<Integer> doctorIds = new ArrayList<>();
        try {
            UserService us = new UserService();
            List<Doctor> doctors = us.getAllDoctors();
            for (Doctor d : doctors) {
                doctorNames.add("Dr. " + d.getName());
                doctorIds.add(d.getId());
            }
        } catch (Exception e) {
            doctorNames.add("Dr. Smith");
            doctorIds.add(1);
        }
        if (doctorNames.isEmpty()) {
            doctorNames.add("Dr. Smith");
            doctorIds.add(1);
        }

        ComboBox<String> deptCombo = new ComboBox<>();
        deptCombo.getItems().addAll("General Medicine", "Cardiology", "Dermatology", "Neurology", "Orthopedics");
        deptCombo.setValue("General Medicine");

        // Use index-based selection
        final List<Integer> docIds = new ArrayList<>(doctorIds);
        
        ComboBox<String> doctorCombo = new ComboBox<>();
        doctorCombo.getItems().addAll(doctorNames);
        if (!doctorNames.isEmpty()) doctorCombo.setValue(doctorNames.get(0));

        DatePicker datePicker = new DatePicker();
        datePicker.setValue(LocalDate.now().plusDays(1));

        ComboBox<String> timeCombo = new ComboBox<>();
        timeCombo.getItems().addAll("09:00", "10:00", "11:00", "14:00", "15:00");
        timeCombo.setValue("09:00");

        TextArea reasonArea = new TextArea();
        reasonArea.setPromptText("Motif de consultation...");
        reasonArea.setPrefRowCount(3);

        grid.add(new Label("Département:"), 0, 0);
        grid.add(deptCombo, 1, 0);
        grid.add(new Label("Médecin:"), 0, 1);
        grid.add(doctorCombo, 1, 1);
        grid.add(new Label("Date:"), 0, 2);
        grid.add(datePicker, 1, 2);
        grid.add(new Label("Heure:"), 0, 3);
        grid.add(timeCombo, 1, 3);
        grid.add(new Label("Motif:"), 0, 4);
        grid.add(reasonArea, 1, 4);

        dialog.getDialogPane().setContent(grid);
        
        ButtonType submitBtn = new ButtonType("Soumettre", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitBtn, ButtonType.CANCEL);

        dialog.showAndWait();

        ButtonType result = dialog.getDialogPane().getButtonTypes().get(0);
        if (result.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
            try {
                // Get selected doctor ID
                int selectedIndex = doctorCombo.getSelectionModel().getSelectedIndex();
                int selectedDoctorId = (selectedIndex >= 0 && selectedIndex < docIds.size()) 
                    ? docIds.get(selectedIndex) : 0;

                Appointment newAppt = new Appointment();
                newAppt.setDepartment(deptCombo.getValue());
                newAppt.setDoctor(doctorCombo.getValue());
                newAppt.setDoctorId(selectedDoctorId);
                
                // Fixed date parsing
                String dateStr = datePicker.getValue().toString();
                String timeStr = timeCombo.getValue();
                LocalDateTime dateTime = LocalDateTime.parse(dateStr + "T" + timeStr + ":00");
                
                newAppt.setScheduledAt(dateTime);
                newAppt.setMessage(reasonArea.getText());
                newAppt.setStatus("PENDING");
                newAppt.setPatientId(currentUserId);
                
                appointmentService.create(newAppt);
                showInfo("Demande soumise avec succès!");
                loadAppointments();
            } catch (Exception e) {
                showError("Erreur: " + e.getMessage());
            }
        }
    }

    private void deleteAppointment(Appointment apt) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setContentText("Delete this appointment?");
        if (alert.showAndWait().get() == ButtonType.OK) {
            new Thread(() -> {
                try {
                    appointmentService.delete(apt.getId());
                    Platform.runLater(() -> {
                        showInfo("Deleted successfully");
                        loadAppointments();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> showError("Error: " + e.getMessage()));
                }
            }).start();
        }
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
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

    @FXML
    public void onSearch() {
        String query = searchField.getText().trim();
        String statusFilterValue = statusFilter.getValue();

        new Thread(() -> {
            try {
                List<Appointment> results;

                if (query.isEmpty() && (statusFilterValue == null || statusFilterValue.equals("All"))) {
                    results = allAppointments;
                } else if (!query.isEmpty()) {
                    results = appointmentService.search(query);
                } else {
                    results = appointmentService.getByStatus(statusFilterValue);
                }

                if (statusFilterValue != null && !statusFilterValue.equals("All")) {
                    results = results.stream()
                        .filter(apt -> apt.getStatus().equals(statusFilterValue))
                        .toList();
                }

                List<Appointment> finalResults = results;
                Platform.runLater(() -> {
                    allAppointments = finalResults;
                    displayAppointments();
                    messageLabel.setText("Found " + finalResults.size() + " results");
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Search error: " + e.getMessage()));
            }
        }).start();
    }
}