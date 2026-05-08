package org.docbook.controllers.records;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.docbook.entities.records.Appointment;
import org.docbook.entities.records.Teleconsultation;
import org.docbook.entities.users.User;
import org.docbook.services.AppointmentService;
import org.docbook.services.TeleconsultationService;
import org.docbook.util.AppState;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.Locale;

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
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    
    private String currentUserRole;
    private int currentUserId;

    @Override
    public void initialize(java.net.URL url, ResourceBundle resourceBundle) {
        teleconsultationService = new TeleconsultationService();
        appointmentService = new AppointmentService();
        
        User currentUser = AppState.getCurrentUser();
        currentUserRole = (currentUser != null) ? currentUser.getRole() : "UNKNOWN";
        currentUserId = (currentUser != null) ? currentUser.getId() : 0;
        
        setupModeFilter();
        loadTeleconsultations();
    }

    private void setupModeFilter() {
        modeFilter.getItems().addAll("All", "video", "chat", "audio");
        modeFilter.setValue("All");
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
            Text emptyText = new Text("\uD83D\uDC4B");
            emptyText.setStyle("-fx-font-size: 48px;");
            
            Text emptyTitle = new Text("No teleconsultations found");
            emptyTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: #333;");
            
            VBox emptyBox = new VBox(10);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setStyle("-fx-padding: 50;");
            emptyBox.getChildren().addAll(emptyText, emptyTitle);
            
            cardContainer.getChildren().add(emptyBox);
            statusLabel.setText("Total: 0");
            return;
        }

        List<Teleconsultation> filtered = filterTeleconsultations();
        
        for (Teleconsultation tc : filtered) {
            cardContainer.getChildren().add(createTeleconsultationCard(tc));
        }

        statusLabel.setText("Total: " + allTeleconsultations.size());
    }
    
    private List<Teleconsultation> filterTeleconsultations() {
        List<Teleconsultation> filtered = allTeleconsultations;
        
        String searchText = searchField.getText() != null ? searchField.getText().toLowerCase().trim() : "";
        if (!searchText.isEmpty()) {
            filtered = filtered.stream()
                .filter(tc -> tc.getMeetingUrl() != null && tc.getMeetingUrl().toLowerCase().contains(searchText))
                .collect(Collectors.toList());
        }
        
        String selectedMode = modeFilter.getValue();
        if (selectedMode != null && !selectedMode.equals("All")) {
            filtered = filtered.stream()
                .filter(tc -> tc.getMode() != null && tc.getMode().equalsIgnoreCase(selectedMode))
                .collect(Collectors.toList());
        }
        
        return filtered;
    }

    private AnchorPane createTeleconsultationCard(Teleconsultation tc) {
        AnchorPane card = new AnchorPane();
        card.setStyle(
            "-fx-border-color: #ecf0f1; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 12; " +
            "-fx-padding: 16; " +
            "-fx-background-color: #ffffff; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 6, 0, 0, 1);");
        card.setPrefHeight(160);

        HBox mainContent = new HBox(20);
        mainContent.setAlignment(Pos.CENTER_LEFT);

        VBox leftSection = new VBox(5);
        
        Appointment apt = null;
        try {
            if (tc.getAppointmentId() > 0) {
                apt = appointmentService.readById(tc.getAppointmentId());
            }
        } catch (Exception e) {
            System.err.println("Error loading appointment: " + e.getMessage());
        }
        
        String doctorName = (apt != null) ? apt.getDoctor() : "N/A";
        Label doctorLabel = new Label("\uD83D\uDC68\u200D\u2695\uFE0F Dr. " + doctorName);
        doctorLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        String deptText = (apt != null) ? apt.getDepartment() : "N/A";
        Label deptLabel = new Label("\uD83C\uDFE5 " + deptText);
        deptLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        String dateText = (apt != null && apt.getScheduledAt() != null) 
            ? apt.getScheduledAt().format(dateFormatter) : "N/A";
        Label dateLabel = new Label("\uD83D\uDCC5 " + dateText);
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        leftSection.getChildren().addAll(doctorLabel, deptLabel, dateLabel);

        VBox middleSection = new VBox(5);
        middleSection.setPrefWidth(300);

        Label modeLabel = new Label("Mode: " + tc.getMode());
        modeLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #666;");

        Label durationLabel = new Label("Duration: " + tc.getDuration() + " minutes");
        durationLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

        Label urlLabel = new Label("Meeting URL:");
        urlLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #999;");

        Hyperlink urlLink = new Hyperlink(tc.getMeetingUrl());
        urlLink.setStyle("-fx-font-size: 11px; -fx-padding: 0; -fx-text-fill: #0058be;");
        urlLink.setOnAction(e -> {
            try {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(tc.getMeetingUrl()));
            } catch (Exception ex) {
                showError("Error opening URL: " + ex.getMessage());
            }
        });

        middleSection.getChildren().addAll(modeLabel, durationLabel, urlLabel, urlLink);

        VBox rightSection = new VBox(8);
        rightSection.setAlignment(Pos.TOP_CENTER);
        rightSection.setPrefWidth(150);

        Label modeBadge = new Label(tc.getMode().toUpperCase());
        modeBadge.setStyle(getModeStyle(tc.getMode()));
        modeBadge.setPrefWidth(130);
        modeBadge.setAlignment(Pos.CENTER);

        HBox buttonBox = new HBox(5);
        buttonBox.setAlignment(Pos.CENTER);

        Button editBtn = createActionButton("\u270F\uFE0F Edit", "edit", () -> openTeleconsultationForm(tc));
        Button pdfBtn = createActionButton("\uD83D\uDCC4 PDF", "pdf", () -> exportPDF(tc));
        Button deleteBtn = createActionButton("\uD83D\uDDD1\uFE0F Delete", "delete", () -> deleteTeleconsultation(tc));

        buttonBox.getChildren().addAll(editBtn, pdfBtn, deleteBtn);

        rightSection.getChildren().addAll(modeBadge, buttonBox);

        mainContent.getChildren().addAll(leftSection, middleSection, rightSection);
        card.getChildren().add(mainContent);
        
        AnchorPane.setLeftAnchor(mainContent, 0.0);
        AnchorPane.setRightAnchor(mainContent, 0.0);
        AnchorPane.setTopAnchor(mainContent, 0.0);
        AnchorPane.setBottomAnchor(mainContent, 0.0);

        return card;
    }

    private String getModeStyle(String mode) {
        return switch (mode) {
            case "video" -> "-fx-padding: 8; -fx-font-size: 11; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-border-radius: 4;";
            case "chat" -> "-fx-padding: 8; -fx-font-size: 11; -fx-background-color: #2196F3; -fx-text-fill: white; -fx-border-radius: 4;";
            case "audio" -> "-fx-padding: 8; -fx-font-size: 11; -fx-background-color: #FF9800; -fx-text-fill: white; -fx-border-radius: 4;";
            default -> "-fx-padding: 8; -fx-font-size: 11; -fx-background-color: #757575; -fx-text-fill: white; -fx-border-radius: 4;";
        };
    }

    private Button createActionButton(String text, String type, Runnable action) {
        Button btn = new Button(text);
        btn.setPrefWidth(80);
        btn.setOnAction(e -> action.run());

        String baseStyle = "-fx-font-size: 11px; -fx-padding: 8 12; -fx-text-fill: white; -fx-border-radius: 4; -fx-cursor: hand;";
        switch (type) {
            case "edit":
                btn.setStyle(baseStyle + " -fx-background-color: #2196F3;");
                break;
            case "delete":
                btn.setStyle(baseStyle + " -fx-background-color: #f44336;");
                break;
            case "pdf":
                btn.setStyle(baseStyle + " -fx-background-color: #FF5722;");
                break;
            default:
                btn.setStyle(baseStyle + " -fx-background-color: #757575;");
        }
        return btn;
    }

    @FXML
    public void onSearch() {
        displayTeleconsultations();
    }
    
    @FXML
    public void onNewTeleconsultation() {
        openTeleconsultationForm(null);
    }

    private void openTeleconsultationForm(Teleconsultation existingTeleconsultation) {
        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(existingTeleconsultation == null ? "New Teleconsultation" : "Edit Teleconsultation");
            dialog.setHeaderText(existingTeleconsultation == null ? "Create a new teleconsultation session" : "Edit teleconsultation details");

            VBox content = new VBox(15);
            content.setStyle("-fx-padding: 20;");

            VBox aptSection = new VBox(5);
            Label aptLabel = new Label("Associated Appointment *");
            aptLabel.setStyle("-fx-font-weight: bold;");
            ComboBox<Appointment> cbAppointment = new ComboBox<>();
            cbAppointment.setPrefWidth(300);

            new Thread(() -> {
                try {
                    // Get appointments for this doctor only (filter by doctor_id)
                    List<Appointment> allAppointments;
                    if (currentUserRole != null && currentUserRole.toUpperCase().contains("DOCTOR")) {
                        allAppointments = appointmentService.getByDoctorId(currentUserId).stream()
                            .filter(a -> a.getStatus() != null && a.getStatus().toLowerCase(Locale.ROOT).equals(Appointment.STATUS_PENDING.toLowerCase(Locale.ROOT)))
                            .collect(Collectors.toList());
                    } else {
                        allAppointments = appointmentService.readAll().stream()
                            .filter(a -> a.getStatus() != null && a.getStatus().toLowerCase(Locale.ROOT).equals(Appointment.STATUS_PENDING.toLowerCase(Locale.ROOT)))
                            .collect(Collectors.toList());
                    }
                    
                    final Appointment targetAppt;
                    if (existingTeleconsultation != null && existingTeleconsultation.getAppointmentId() > 0) {
                        targetAppt = appointmentService.readById(existingTeleconsultation.getAppointmentId());
                    } else {
                        targetAppt = null;
                    }
                    final Appointment finalTargetAppt = targetAppt;
                    Platform.runLater(() -> {
                        cbAppointment.getItems().addAll(allAppointments);
                        
                        // Show patient name + date for doctors
                        String displayRole = currentUserRole != null ? currentUserRole.toUpperCase() : "";
                        final boolean isDoctor = displayRole.contains("DOCTOR");
                        
                        cbAppointment.setCellFactory(param -> new javafx.scene.control.ListCell<Appointment>() {
                            @Override
                            protected void updateItem(Appointment apt, boolean empty) {
                                super.updateItem(apt, empty);
                                if (empty || apt == null) {
                                    setText(null);
                                } else {
                                    // For doctors: show patient ID + date
                                    // For patients: show doctor name
                                    if (isDoctor) {
                                        setText("Patient #" + apt.getPatientId() + " - " + 
                                            (apt.getScheduledAt() != null ? apt.getScheduledAt().toLocalDate().toString() : "N/A"));
                                    } else {
                                        setText(apt.getDoctor() + " - " + apt.getDepartment());
                                    }
                                }
                            }
                        });
                        cbAppointment.setButtonCell(new javafx.scene.control.ListCell<Appointment>() {
                            @Override
                            protected void updateItem(Appointment apt, boolean empty) {
                                super.updateItem(apt, empty);
                                if (empty || apt == null) {
                                    setText(null);
                                } else {
                                    if (isDoctor) {
                                        setText("Patient #" + apt.getPatientId() + " - " + 
                                            (apt.getScheduledAt() != null ? apt.getScheduledAt().toLocalDate().toString() : "N/A"));
                                    } else {
                                        setText(apt.getDoctor() + " - " + apt.getDepartment());
                                    }
                                }
                            }
                        });

                        if (finalTargetAppt != null) {
                            cbAppointment.setValue(finalTargetAppt);
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> showError("Error loading appointments: " + e.getMessage()));
                }
            }).start();

            aptSection.getChildren().addAll(aptLabel, cbAppointment);

            VBox modeSection = new VBox(5);
            Label modeLabel = new Label("Consultation Mode *");
            modeLabel.setStyle("-fx-font-weight: bold;");
            ComboBox<String> cbMode = new ComboBox<>();
            cbMode.getItems().addAll("video", "audio", "chat");
            cbMode.setPrefWidth(300);
            if (existingTeleconsultation != null) {
                cbMode.setValue(existingTeleconsultation.getMode());
            } else {
                cbMode.setValue("video");
            }
            modeSection.getChildren().addAll(modeLabel, cbMode);

            VBox durationSection = new VBox(5);
            Label durationLabel = new Label("Duration (minutes) *");
            durationLabel.setStyle("-fx-font-weight: bold;");
            TextField tfDuration = new TextField();
            tfDuration.setPromptText("30");
            tfDuration.setPrefWidth(300);
            if (existingTeleconsultation != null) {
                tfDuration.setText(String.valueOf(existingTeleconsultation.getDuration()));
            }
            durationSection.getChildren().addAll(durationLabel, tfDuration);

            VBox urlSection = new VBox(5);
            Label urlLabelField = new Label("Meeting URL *");
            urlLabelField.setStyle("-fx-font-weight: bold;");
            TextField tfUrl = new TextField();
            tfUrl.setPromptText("https://meet.google.com/abc-defg-hij");
            tfUrl.setPrefWidth(300);
            if (existingTeleconsultation != null) {
                tfUrl.setText(existingTeleconsultation.getMeetingUrl());
            }
            urlSection.getChildren().addAll(urlLabelField, tfUrl);

            content.getChildren().addAll(aptSection, modeSection, durationSection, urlSection);
            
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            dialog.getDialogPane().setContent(scrollPane);

            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            if (dialog.showAndWait().get() == ButtonType.OK) {
                if (cbAppointment.getValue() == null) {
                    showError("Please select an appointment");
                    return;
                }
                if (cbMode.getValue() == null) {
                    showError("Please select a consultation mode");
                    return;
                }
                if (tfDuration.getText().isEmpty()) {
                    showError("Please enter duration");
                    return;
                }
                if (tfUrl.getText().isEmpty()) {
                    showError("Please enter meeting URL");
                    return;
                }

                try {
                    int duration = Integer.parseInt(tfDuration.getText());
                    if (duration <= 0) {
                        showError("Duration must be greater than 0");
                        return;
                    }

                    new Thread(() -> {
                        try {
                            if (existingTeleconsultation == null) {
                                Teleconsultation newTc = new Teleconsultation();
                                newTc.setAppointmentId(cbAppointment.getValue().getId());
                                newTc.setMode(cbMode.getValue());
                                newTc.setDuration(duration);
                                newTc.setMeetingUrl(tfUrl.getText());
                                teleconsultationService.create(newTc);
                            } else {
                                existingTeleconsultation.setAppointmentId(cbAppointment.getValue().getId());
                                existingTeleconsultation.setMode(cbMode.getValue());
                                existingTeleconsultation.setDuration(duration);
                                existingTeleconsultation.setMeetingUrl(tfUrl.getText());
                                teleconsultationService.update(existingTeleconsultation);
                            }
                            Platform.runLater(() -> {
                                showInfo(existingTeleconsultation == null ? "Teleconsultation created successfully" : "Teleconsultation updated successfully");
                                loadTeleconsultations();
                            });
                        } catch (Exception e) {
                            Platform.runLater(() -> showError("Error saving teleconsultation: " + e.getMessage()));
                        }
                    }).start();
                } catch (NumberFormatException e) {
                    showError("Duration must be a valid number");
                }
            }
        } catch (Exception e) {
            showError("Error opening form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteTeleconsultation(Teleconsultation tc) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Teleconsultation");
        alert.setContentText("Are you sure you want to delete this teleconsultation?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        teleconsultationService.delete(tc.getId());
                        Platform.runLater(() -> {
                            showInfo("Teleconsultation deleted successfully");
                            loadTeleconsultations();
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> showError("Error deleting: " + e.getMessage()));
                    }
                }).start();
            }
        });
    }

    private void exportPDF(Teleconsultation tc) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialFileName("teleconsultation_" + tc.getId() + ".pdf");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

            File file = fileChooser.showSaveDialog(cardContainer.getScene().getWindow());
            if (file != null) {
                showInfo("PDF exported to " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            showError("Error exporting PDF: " + e.getMessage());
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
                title = "DocBook - Medecin";
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