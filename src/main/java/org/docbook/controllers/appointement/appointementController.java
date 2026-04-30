package org.docbook.controllers.appointement;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import tn.esprit.models.appointement;
import tn.esprit.services.ServiceAppointement;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class appointementController implements Initializable {

    @FXML
    private VBox cardContainer;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> statusFilter;
    @FXML
    private Label statusLabel;
    @FXML
    private Label messageLabel;
    @FXML
    private Button btnSearch;
    @FXML
    private Button btnRefresh;
    @FXML
    private Button btnNew;

    private ServiceAppointement serviceAppointement;
    private List<appointement> allAppointments;
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        serviceAppointement = new ServiceAppointement();
        setupStatusFilter();
        loadAppointments();
    }

    private void setupStatusFilter() {
        statusFilter.getItems().addAll(
                "All",
                appointement.STATUS_PENDING,
                appointement.STATUS_CONFIRMED,
                appointement.STATUS_COMPLETED,
                appointement.STATUS_CANCELLED,
                appointement.STATUS_EXPIRED);
        statusFilter.setValue("All");
    }

    @FXML
    public void loadAppointments() {
        new Thread(() -> {
            try {
                allAppointments = serviceAppointement.readAll();
                Platform.runLater(this::displayAppointments);
                Platform.runLater(() -> messageLabel.setText(""));
            } catch (Exception e) {
                Platform.runLater(() -> showError("Error loading appointments: " + e.getMessage()));
            }
        }).start();
    }

    private void displayAppointments() {
        cardContainer.getChildren().clear();

        if (allAppointments == null || allAppointments.isEmpty()) {
            Label emptyLabel = new Label("No appointments found");
            emptyLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #999;");
            cardContainer.getChildren().add(emptyLabel);
            statusLabel.setText("Total Appointments: 0");
            return;
        }

        for (appointement apt : allAppointments) {
            cardContainer.getChildren().add(createAppointmentCard(apt));
        }

        statusLabel.setText("Total Appointments: " + allAppointments.size());
    }

    private AnchorPane createAppointmentCard(appointement apt) {
        AnchorPane card = new AnchorPane();
        card.setStyle(
                "-fx-border-color: #e0e0e0; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 8; " +
                        "-fx-padding: 15; " +
                        "-fx-background-color: #ffffff; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);");
        AnchorPane.setLeftAnchor(card, 0.0);
        AnchorPane.setRightAnchor(card, 0.0);
        card.setPrefHeight(140);

        // Main content HBox
        HBox mainContent = new HBox(20);
        mainContent.setStyle("-fx-alignment: center-left;");

        // Left section: Basic info
        VBox leftSection = new VBox(5);
        Label doctorLabel = new Label("Dr. " + apt.getDoctor());
        doctorLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #333;");

        Label deptLabel = new Label("Department: " + apt.getDepartment());
        deptLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #666;");

        Label dateLabel = new Label("Scheduled: " + apt.getScheduledAt().format(dateFormatter));
        dateLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #666;");

        leftSection.getChildren().addAll(doctorLabel, deptLabel, dateLabel);

        // Middle section: Message
        VBox middleSection = new VBox(5);
        middleSection.setPrefWidth(300);
        Label messageTitle = new Label("Message:");
        messageTitle.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #999;");
        TextArea messageArea = new TextArea(apt.getMessage() != null ? apt.getMessage() : "");
        messageArea.setWrapText(true);
        messageArea.setPrefRowCount(3);
        messageArea.setStyle("-fx-font-size: 11; -fx-control-inner-background: #f9f9f9;");
        messageArea.setEditable(false);
        middleSection.getChildren().addAll(messageTitle, messageArea);
        VBox.setVgrow(messageArea, Priority.ALWAYS);

        // Right section: Status badge and buttons
        VBox rightSection = new VBox(8);
        rightSection.setAlignment(Pos.TOP_CENTER);
        rightSection.setPrefWidth(150);

        // Status badge
        Label statusBadge = new Label(apt.getStatus());
        statusBadge.setStyle(getStatusStyle(apt.getStatus()));
        statusBadge.setPrefWidth(130);
        statusBadge.setAlignment(Pos.CENTER);

        // Action buttons
        HBox buttonBox = new HBox(5);
        buttonBox.setAlignment(Pos.CENTER);

        Button editBtn = new Button("Edit");
        editBtn.setStyle("-fx-padding: 8 12; -fx-font-size: 10; -fx-background-color: #2196F3; -fx-text-fill: white;");
        editBtn.setOnAction(e -> editAppointment(apt));

        Button pdfBtn = new Button("PDF");
        pdfBtn.setStyle("-fx-padding: 8 12; -fx-font-size: 10; -fx-background-color: #FF5722; -fx-text-fill: white;");
        pdfBtn.setOnAction(e -> exportPDF(apt));

        Button deleteBtn = new Button("Delete");
        deleteBtn
                .setStyle("-fx-padding: 8 12; -fx-font-size: 10; -fx-background-color: #f44336; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> deleteAppointment(apt));

        buttonBox.getChildren().addAll(editBtn, pdfBtn, deleteBtn);
        rightSection.getChildren().addAll(statusBadge, buttonBox);

        mainContent.getChildren().addAll(leftSection, middleSection, rightSection);
        HBox.setHgrow(middleSection, Priority.ALWAYS);

        card.getChildren().add(mainContent);
        AnchorPane.setLeftAnchor(mainContent, 0.0);
        AnchorPane.setRightAnchor(mainContent, 0.0);
        AnchorPane.setTopAnchor(mainContent, 0.0);
        AnchorPane.setBottomAnchor(mainContent, 0.0);

        return card;
    }

    private String getStatusStyle(String status) {
        return switch (status) {
            case appointement.STATUS_PENDING ->
                "-fx-padding: 8; -fx-font-size: 11; -fx-background-color: #FFC107; -fx-text-fill: white; -fx-border-radius: 4;";
            case appointement.STATUS_CONFIRMED ->
                "-fx-padding: 8; -fx-font-size: 11; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-border-radius: 4;";
            case appointement.STATUS_COMPLETED ->
                "-fx-padding: 8; -fx-font-size: 11; -fx-background-color: #8BC34A; -fx-text-fill: white; -fx-border-radius: 4;";
            case appointement.STATUS_CANCELLED ->
                "-fx-padding: 8; -fx-font-size: 11; -fx-background-color: #f44336; -fx-text-fill: white; -fx-border-radius: 4;";
            case appointement.STATUS_EXPIRED ->
                "-fx-padding: 8; -fx-font-size: 11; -fx-background-color: #9E9E9E; -fx-text-fill: white; -fx-border-radius: 4;";
            default ->
                "-fx-padding: 8; -fx-font-size: 11; -fx-background-color: #757575; -fx-text-fill: white; -fx-border-radius: 4;";
        };
    }

    @FXML
    public void onSearch() {
        String query = searchField.getText().trim();
        String statusFilterValue = statusFilter.getValue();

        new Thread(() -> {
            try {
                List<appointement> results;

                if (query.isEmpty() && (statusFilterValue == null || statusFilterValue.equals("All"))) {
                    results = allAppointments;
                } else if (!query.isEmpty()) {
                    results = serviceAppointement.search(query);
                } else {
                    results = serviceAppointement.getByStatus(statusFilterValue);
                    return;
                }

                // Apply status filter if needed
                if (statusFilterValue != null && !statusFilterValue.equals("All")) {
                    results = results.stream()
                            .filter(apt -> apt.getStatus().equals(statusFilterValue))
                            .toList();
                }

                List<appointement> finalResults = results;
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

    @FXML
    public void onNewAppointment() {
        showInfo("New appointment dialog would open here");
        // In a full implementation, this would open a new dialog for creating
        // appointments
    }

    private void editAppointment(appointement apt) {
        showInfo("Edit dialog for appointment " + apt.getId() + " would open here");
        // In a full implementation, this would open an edit dialog
    }

    private void deleteAppointment(appointement apt) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Appointment");
        alert.setContentText("Are you sure you want to delete this appointment?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            new Thread(() -> {
                try {
                    serviceAppointement.delete(apt.getId());
                    Platform.runLater(() -> {
                        showInfo("Appointment deleted successfully");
                        loadAppointments();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> showError("Error deleting appointment: " + e.getMessage()));
                }
            }).start();
        }
    }

    private void exportPDF(appointement apt) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialFileName("appointment_" + apt.getId() + ".pdf");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                generatePDF(file, apt);
                showInfo("PDF exported successfully to " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            showError("Error exporting PDF: " + e.getMessage());
        }
    }

    private void generatePDF(File file, appointement apt) throws Exception {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        // Add title
        Paragraph title = new Paragraph("Appointment Summary",
                FontFactory.getFont(FontFactory.HELVETICA, 18, Font.BOLD));
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("\n"));

        // Add appointment details
        document.add(new Paragraph("Appointment ID: " + apt.getId(),
                FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD)));
        document.add(new Paragraph("Doctor: Dr. " + apt.getDoctor(), FontFactory.getFont(FontFactory.HELVETICA, 11)));
        document.add(
                new Paragraph("Department: " + apt.getDepartment(), FontFactory.getFont(FontFactory.HELVETICA, 11)));
        document.add(new Paragraph("Scheduled Date: " + apt.getScheduledAt().format(dateFormatter),
                FontFactory.getFont(FontFactory.HELVETICA, 11)));
        document.add(
                new Paragraph("Status: " + apt.getStatus(), FontFactory.getFont(FontFactory.HELVETICA, 11, Font.BOLD)));
        document.add(new Paragraph("Message: " + (apt.getMessage() != null ? apt.getMessage() : "N/A"),
                FontFactory.getFont(FontFactory.HELVETICA, 11)));
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Generated on: " + java.time.LocalDateTime.now().format(dateFormatter),
                FontFactory.getFont(FontFactory.HELVETICA, 10, Font.ITALIC)));

        document.close();
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
}
