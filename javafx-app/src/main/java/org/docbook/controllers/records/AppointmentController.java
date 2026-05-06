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
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.docbook.entities.records.Appointment;
import org.docbook.entities.users.User;
import org.docbook.services.AppointmentService;
import org.docbook.services.RatingService;
import org.docbook.services.users.UserService;
import org.docbook.entities.users.Doctor;
import org.docbook.util.AppState;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AppointmentController implements javafx.fxml.Initializable {

    @FXML private VBox cardContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Label statusLabel;
    @FXML private Label messageLabel;
    @FXML private Label lblTotalCount;
    @FXML private Label lblPendingCount;
    @FXML private Label lblConfirmedCount;
    @FXML private Button btnSearch;
    @FXML private Button btnRefresh;
    @FXML private Button btnNew;

    private AppointmentService appointmentService;
    private List<Appointment> allAppointments;
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    
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
        
        if (currentUserRole != null && currentUserRole.toUpperCase().contains("PATIENT")) {
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
            cardContainer.getChildren().add(emptyStateView());
            updateStatistics(null);
            return;
        }

        List<Appointment> filtered = filterAppointments();
        
        if (filtered.isEmpty()) {
            cardContainer.getChildren().add(emptyStateView());
        } else {
            for (Appointment apt : filtered) {
                cardContainer.getChildren().add(createAppointmentCard(apt));
            }
        }
        
        updateStatistics(allAppointments);
    }
    
    private List<Appointment> filterAppointments() {
        List<Appointment> filtered = allAppointments;
        
        String searchText = searchField.getText() != null ? searchField.getText().toLowerCase().trim() : "";
        if (!searchText.isEmpty()) {
            filtered = filtered.stream()
                .filter(a -> (a.getDoctor() != null && a.getDoctor().toLowerCase().contains(searchText)) ||
                           (a.getDepartment() != null && a.getDepartment().toLowerCase().contains(searchText)) ||
                           (a.getMessage() != null && a.getMessage().toLowerCase().contains(searchText)) ||
                           (a.getStatus() != null && a.getStatus().toLowerCase().contains(searchText)))
                .collect(Collectors.toList());
        }
        
        String selectedStatus = statusFilter.getValue();
        if (selectedStatus != null && !selectedStatus.equals("All")) {
            filtered = filtered.stream()
                .filter(a -> a.getStatus().equals(selectedStatus))
                .collect(Collectors.toList());
        }
        
        return filtered;
    }
    
    private VBox emptyStateView() {
        VBox container = new VBox(10);
        container.setAlignment(Pos.CENTER);
        container.setStyle("-fx-padding: 50;");
        
        Text icon = new Text("\uD83D\uDC4B");
        icon.setStyle("-fx-font-size: 48px;");
        
        Text title = new Text("No Appointments Found");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: #333;");
        
        Text subtitle = new Text("Start by requesting a new appointment");
        subtitle.setStyle("-fx-font-size: 14px; -fx-fill: #666;");
        
        container.getChildren().addAll(icon, title, subtitle);
        return container;
    }
    
    private void updateStatistics(List<Appointment> appointments) {
        if (appointments == null) {
            lblTotalCount.setText("Total: 0");
            lblPendingCount.setText("Pending: 0");
            lblConfirmedCount.setText("Confirmed: 0");
            statusLabel.setText("Total: 0");
            return;
        }
        
        long total = appointments.size();
        long pending = appointments.stream().filter(a -> "PENDING".equals(a.getStatus())).count();
        long confirmed = appointments.stream().filter(a -> "CONFIRMED".equals(a.getStatus())).count();
        
        lblTotalCount.setText("Total: " + total);
        lblPendingCount.setText("Pending: " + pending);
        lblConfirmedCount.setText("Confirmed: " + confirmed);
        statusLabel.setText("Total: " + total);
    }

    private AnchorPane createAppointmentCard(Appointment apt) {
        AnchorPane card = new AnchorPane();
        card.setStyle(
            "-fx-border-color: #ecf0f1; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 12; " +
            "-fx-padding: 16; " +
            "-fx-background-color: #ffffff; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 6, 0, 0, 1);");
        card.setPrefHeight(180);

        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        boolean isPatient = currentUserRole != null && currentUserRole.toUpperCase().contains("PATIENT");
        String labelValue = isPatient ? apt.getDoctor() : "Patient #" + apt.getPatientId();
        
        Label doctorLabel = new Label("\uD83D\uDC68\u200D\u2695\uFE0F " + labelValue);
        doctorLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label statusBadge = new Label(apt.getStatus());
        statusBadge.setStyle(getStatusStyle(apt.getStatus()));
        statusBadge.setPrefWidth(130);
        statusBadge.setAlignment(Pos.CENTER);
        
        Label deptLabel = new Label("\uD83C\uDFE5 " + apt.getDepartment());
        deptLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        Label dateLabel = new Label("\uD83D\uDCC5 " + (apt.getScheduledAt() != null ? apt.getScheduledAt().format(dateFormatter) : "N/A"));
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        headerBox.getChildren().addAll(doctorLabel, statusBadge);

        VBox detailsBox = new VBox(5);
        detailsBox.setStyle("-fx-padding: 10 0;");
        
        detailsBox.getChildren().addAll(deptLabel, dateLabel);

        if (apt.getMessage() != null && !apt.getMessage().isEmpty()) {
            Label messageLabelCard = new Label("\uD83D\uDCAC " + apt.getMessage());
            messageLabelCard.setWrapText(true);
            messageLabelCard.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
            detailsBox.getChildren().add(messageLabelCard);
        }

        // Star Rating Row (for confirmed appointments)
        HBox ratingBox = new HBox(4);
        ratingBox.setAlignment(Pos.CENTER_LEFT);
        ratingBox.setStyle("-fx-padding: 8 0 0 0;");
        
        if (isPatient && "CONFIRMED".equals(apt.getStatus())) {
            Label ratingLabel = new Label("Rate: ");
            ratingLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
            ratingBox.getChildren().add(ratingLabel);
            
            ToggleButton[] stars = new ToggleButton[5];
            int existingRating = RatingService.getAppointmentRating(apt.getId());
            
            for (int i = 0; i < 5; i++) {
                final int starValue = i + 1;
                stars[i] = new ToggleButton("\u2606");
                stars[i].setStyle("-fx-font-size: 16px; -fx-background-color: transparent; -fx-text-fill: #f39c12; -fx-cursor: hand; -fx-padding: 2;");
                
                if (starValue <= existingRating) {
                    stars[i].setText("\u2605");
                }
                
                stars[i].setOnAction(e -> {
                    for (int j = 0; j < 5; j++) {
                        if (j < starValue) {
                            stars[j].setText("\u2605");
                            stars[j].setStyle("-fx-font-size: 16px; -fx-background-color: transparent; -fx-text-fill: #f39c12; -fx-cursor: hand; -fx-padding: 2;");
                        } else {
                            stars[j].setText("\u2606");
                        }
                    }
                    new Thread(() -> {
                        boolean saved = RatingService.saveAppointmentRating(apt.getId(), currentUserId, starValue, "");
                        Platform.runLater(() -> {
                            if (saved) {
                                showInfo("\u2B50 Rating Saved: " + RatingService.getStarDisplay(starValue));
                            } else {
                                showInfo("\u2B50 Rating Recorded (offline)");
                            }
                        });
                    }).start();
                });
                ratingBox.getChildren().add(stars[i]);
            }
        }

        HBox buttonBox = new HBox(8);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setStyle("-fx-padding: 10 0 0 0; -fx-border-color: #ecf0f1; -fx-border-width: 1 0 0 0;");

        Button editBtn = createActionButton("\u270F\uFE0F Edit", "edit", () -> openAppointmentForm(apt));
        Button deleteBtn = createActionButton("\uD83D\uDDD1\uFE0F Delete", "delete", () -> deleteAppointment(apt));
        Button pdfBtn = createActionButton("\uD83D\uDCC4 PDF", "pdf", () -> showInfo("PDF: Feature unavailable (iText5 not configured)"));

        buttonBox.getChildren().addAll(editBtn, deleteBtn, pdfBtn);

        card.getChildren().addAll(headerBox, detailsBox, ratingBox, buttonBox);
        
        AnchorPane.setLeftAnchor(headerBox, 0.0);
        AnchorPane.setTopAnchor(headerBox, 0.0);
        AnchorPane.setLeftAnchor(detailsBox, 0.0);
        AnchorPane.setTopAnchor(detailsBox, 40.0);
        AnchorPane.setLeftAnchor(ratingBox, 0.0);
        AnchorPane.setTopAnchor(ratingBox, 100.0);
        AnchorPane.setRightAnchor(buttonBox, 0.0);
        AnchorPane.setTopAnchor(buttonBox, 130.0);
        
        return card;
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
                btn.setStyle(baseStyle + " -fx-background-color: #FF9800;");
                break;
            default:
                btn.setStyle(baseStyle + " -fx-background-color: #757575;");
        }
        return btn;
    }

    private void openAppointmentForm(Appointment appointmentToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/records/AppointmentForm.fxml"));
            Parent formRoot = loader.load();

            AppointmentFormController formController = loader.getController();
            if (appointmentToEdit != null) {
                formController.setAppointment(appointmentToEdit);
            }

            Stage formStage = new Stage();
            formStage.setTitle(appointmentToEdit == null ? "New Appointment" : "Edit Appointment");
            formStage.initModality(Modality.APPLICATION_MODAL);
            formStage.setScene(new Scene(formRoot, 500, 600));
            formStage.showAndWait();

            loadAppointments();
        } catch (Exception e) {
            showError("Error opening form: " + e.getMessage());
        }
    }

    private String getStatusStyle(String status) {
        return switch (status) {
            case "PENDING" -> "-fx-padding: 8; -fx-font-size: 11; -fx-background-color: #FFC107; -fx-text-fill: white; -fx-border-radius: 4;";
            case "CONFIRMED" -> "-fx-padding: 8; -fx-font-size: 11; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-border-radius: 4;";
            case "COMPLETED" -> "-fx-padding: 8; -fx-font-size: 11; -fx-background-color: #8BC34A; -fx-text-fill: white; -fx-border-radius: 4;";
            case "CANCELLED" -> "-fx-padding: 8; -fx-font-size: 11; -fx-background-color: #f44336; -fx-text-fill: white; -fx-border-radius: 4;";
            default -> "-fx-padding: 8; -fx-font-size: 11; -fx-background-color: #757575; -fx-text-fill: white; -fx-border-radius: 4;";
        };
    }

    @FXML
    public void onNewAppointment() {
        openAppointmentForm(null);
    }

    private void deleteAppointment(Appointment apt) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Appointment?");
        alert.setContentText("Are you sure you want to delete this appointment?\n\nDoctor: " + apt.getDoctor());

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        appointmentService.delete(apt.getId());
                        Platform.runLater(() -> {
                            showInfo("Appointment deleted successfully");
                            loadAppointments();
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> showError("Error deleting: " + e.getMessage()));
                    }
                }).start();
            }
        });
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

    @FXML
    public void onSearch() {
        displayAppointments();
    }
    
    @FXML
    public void onStatusFilterChanged() {
        displayAppointments();
    }
}