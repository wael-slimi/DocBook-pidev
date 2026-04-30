package org.docbook.entities.appointement;

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
import tn.esprit.models.teleconsultation;
import tn.esprit.services.ServiceAppointement;
import tn.esprit.services.ServiceTeleconsultation;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Teleconsultation Controller - Manages teleconsultation views and CRUD
 * operations
 * Displays teleconsultations with session details, mode, duration, and meeting
 * URL
 * PDF export is exclusively handled here (not in appointments)
 */
public class teleconsultationController implements Initializable {

    @FXML
    private VBox cardContainer;
    @FXML
    private TextField tfSearch;
    @FXML
    private ComboBox<String> modeFilter;
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

    private ServiceTeleconsultation serviceTeleconsultation;
    private ServiceAppointement serviceAppointement;
    private List<teleconsultation> allTeleconsultations;
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        serviceTeleconsultation = new ServiceTeleconsultation();
        serviceAppointement = new ServiceAppointement();
        setupModeFilter();
        loadTeleconsultations();
    }

    private void setupModeFilter() {
        modeFilter.getItems().addAll(
                "All",
                teleconsultation.MODE_VIDEO,
                teleconsultation.MODE_CHAT,
                teleconsultation.MODE_AUDIO);
        modeFilter.setValue("All");
    }

    @FXML
    public void loadTeleconsultations() {
        new Thread(() -> {
            try {
                allTeleconsultations = serviceTeleconsultation.readAll();
                Platform.runLater(this::displayTeleconsultations);
                Platform.runLater(() -> messageLabel.setText(""));
            } catch (Exception e) {
                Platform.runLater(() -> showError("Error loading teleconsultations: " + e.getMessage()));
            }
        }).start();
    }

    private void displayTeleconsultations() {
        cardContainer.getChildren().clear();

        if (allTeleconsultations == null || allTeleconsultations.isEmpty()) {
            Label emptyLabel = new Label("No teleconsultations found");
            emptyLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #999;");
            cardContainer.getChildren().add(emptyLabel);
            statusLabel.setText("Total Teleconsultations: 0");
            return;
        }

        for (teleconsultation tc : allTeleconsultations) {
            cardContainer.getChildren().add(createTeleconsultationCard(tc));
        }

        statusLabel.setText("Total Teleconsultations: " + allTeleconsultations.size());
    }

    private AnchorPane createTeleconsultationCard(teleconsultation tc) {
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
        appointement apt = tc.getAppointment();

        Label doctorLabel = new Label("Dr. " + (apt != null ? apt.getDoctor() : "N/A"));
        doctorLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #333;");

        Label deptLabel = new Label("Department: " + (apt != null ? apt.getDepartment() : "N/A"));
        deptLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #666;");

        Label dateLabel = new Label("Scheduled: " + (apt != null ? apt.getScheduledAt().format(dateFormatter) : "N/A"));
        dateLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #666;");

        leftSection.getChildren().addAll(doctorLabel, deptLabel, dateLabel);

        // Middle section: Consultation details
        VBox middleSection = new VBox(5);
        middleSection.setPrefWidth(300);

        Label modeLabel = new Label("Mode: " + tc.getMode());
        modeLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #666;");

        Label durationLabel = new Label("Duration: " + tc.getDuration() + " minutes");
        durationLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #666;");

        Label urlLabel = new Label("Meeting URL:");
        urlLabel.setStyle("-fx-font-size: 10; -fx-font-weight: bold; -fx-text-fill: #999;");

        Hyperlink urlLink = new Hyperlink(tc.getMeetingUrl());
        urlLink.setStyle("-fx-font-size: 10; -fx-padding: 0;");
        urlLink.setOnAction(e -> {
            try {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(tc.getMeetingUrl()));
            } catch (Exception ex) {
                showError("Error opening URL: " + ex.getMessage());
            }
        });

        middleSection.getChildren().addAll(modeLabel, durationLabel, urlLabel, urlLink);
        VBox.setVgrow(urlLink, Priority.ALWAYS);

        // Right section: Mode badge and buttons
        VBox rightSection = new VBox(8);
        rightSection.setAlignment(Pos.TOP_CENTER);
        rightSection.setPrefWidth(150);

        // Mode badge
        Label modeBadge = new Label(tc.getMode().toUpperCase());
        modeBadge.setStyle(getModeStyle(tc.getMode()));
        modeBadge.setPrefWidth(130);
        modeBadge.setAlignment(Pos.CENTER);

        // Action buttons
        HBox buttonBox = new HBox(5);
        buttonBox.setAlignment(Pos.CENTER);

        Button editBtn = new Button("Edit");
        editBtn.setStyle("-fx-padding: 8 12; -fx-font-size: 10; -fx-background-color: #2196F3; -fx-text-fill: white;");
        editBtn.setOnAction(e -> editTeleconsultation(tc));

        Button pdfBtn = new Button("PDF");
        pdfBtn.setStyle("-fx-padding: 8 12; -fx-font-size: 10; -fx-background-color: #FF5722; -fx-text-fill: white;");
        pdfBtn.setOnAction(e -> exportPDF(tc));

        Button deleteBtn = new Button("Delete");
        deleteBtn
                .setStyle("-fx-padding: 8 12; -fx-font-size: 10; -fx-background-color: #f44336; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> deleteTeleconsultation(tc));

        buttonBox.getChildren().addAll(editBtn, pdfBtn, deleteBtn);
        rightSection.getChildren().addAll(modeBadge, buttonBox);

        mainContent.getChildren().addAll(leftSection, middleSection, rightSection);
        HBox.setHgrow(middleSection, Priority.ALWAYS);

        card.getChildren().add(mainContent);
        AnchorPane.setLeftAnchor(mainContent, 0.0);
        AnchorPane.setRightAnchor(mainContent, 0.0);
        AnchorPane.setTopAnchor(mainContent, 0.0);
        AnchorPane.setBottomAnchor(mainContent, 0.0);

        return card;
    }

    private String getModeStyle(String mode) {
        return switch (mode) {
            case teleconsultation.MODE_VIDEO ->
                "-fx-padding: 8; -fx-font-size: 11; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-border-radius: 4;";
            case teleconsultation.MODE_CHAT ->
                "-fx-padding: 8; -fx-font-size: 11; -fx-background-color: #2196F3; -fx-text-fill: white; -fx-border-radius: 4;";
            case teleconsultation.MODE_AUDIO ->
                "-fx-padding: 8; -fx-font-size: 11; -fx-background-color: #FF9800; -fx-text-fill: white; -fx-border-radius: 4;";
            default ->
                "-fx-padding: 8; -fx-font-size: 11; -fx-background-color: #757575; -fx-text-fill: white; -fx-border-radius: 4;";
        };
    }

    @FXML
    public void onSearch() {
        String query = tfSearch.getText().trim();
        String modeFilterValue = modeFilter.getValue();

        new Thread(() -> {
            try {
                List<teleconsultation> results = serviceTeleconsultation.searchAndFilter(
                        query.isEmpty() ? null : query,
                        (modeFilterValue == null || modeFilterValue.equals("All")) ? null : modeFilterValue);

                Platform.runLater(() -> {
                    allTeleconsultations = results;
                    displayTeleconsultations();
                    messageLabel.setText("Found " + results.size() + " results");
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Search error: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    public void onNewTeleconsultation() {
        openTeleconsultationForm(null);
    }

    private void editTeleconsultation(teleconsultation tc) {
        openTeleconsultationForm(tc);
    }

    private void openTeleconsultationForm(teleconsultation existingTeleconsultation) {
        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(existingTeleconsultation == null ? "New Teleconsultation" : "Edit Teleconsultation");
            dialog.setHeaderText(existingTeleconsultation == null ? "Create a new teleconsultation session"
                    : "Edit teleconsultation details");

            VBox content = new VBox(15);
            content.setStyle("-fx-padding: 20;");

            // Appointment selector
            VBox aptSection = new VBox(5);
            Label aptLabel = new Label("Associated Appointment *");
            aptLabel.setStyle("-fx-font-weight: bold;");
            ComboBox<appointement> cbAppointment = new ComboBox<>();
            cbAppointment.setPrefWidth(300);

            new Thread(() -> {
                try {
                    List<appointement> appointments = serviceAppointement.readAll();
                    Platform.runLater(() -> {
                        cbAppointment.getItems().addAll(appointments);
                        cbAppointment.setCellFactory(param -> new javafx.scene.control.ListCell<appointement>() {
                            @Override
                            protected void updateItem(appointement apt, boolean empty) {
                                super.updateItem(apt, empty);
                                if (empty || apt == null) {
                                    setText(null);
                                } else {
                                    setText(apt.getDoctor() + " - " + apt.getDepartment());
                                }
                            }
                        });
                        cbAppointment.setButtonCell(new javafx.scene.control.ListCell<appointement>() {
                            @Override
                            protected void updateItem(appointement apt, boolean empty) {
                                super.updateItem(apt, empty);
                                if (empty || apt == null) {
                                    setText(null);
                                } else {
                                    setText(apt.getDoctor() + " - " + apt.getDepartment());
                                }
                            }
                        });

                        if (existingTeleconsultation != null && existingTeleconsultation.getAppointment() != null) {
                            cbAppointment.setValue(existingTeleconsultation.getAppointment());
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> showError("Error loading appointments: " + e.getMessage()));
                }
            }).start();

            aptSection.getChildren().addAll(aptLabel, cbAppointment);

            // Mode selector
            VBox modeSection = new VBox(5);
            Label modeLabel = new Label("Consultation Mode *");
            modeLabel.setStyle("-fx-font-weight: bold;");
            ComboBox<String> cbMode = new ComboBox<>();
            cbMode.getItems().addAll(teleconsultation.MODE_VIDEO, teleconsultation.MODE_AUDIO,
                    teleconsultation.MODE_CHAT);
            cbMode.setPrefWidth(300);
            if (existingTeleconsultation != null) {
                cbMode.setValue(existingTeleconsultation.getMode());
            } else {
                cbMode.setValue(teleconsultation.MODE_VIDEO);
            }
            modeSection.getChildren().addAll(modeLabel, cbMode);

            // Duration
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

            // Meeting URL
            VBox urlSection = new VBox(5);
            Label urlLabel = new Label("Meeting URL *");
            urlLabel.setStyle("-fx-font-weight: bold;");
            TextField tfUrl = new TextField();
            tfUrl.setPromptText("https://meet.google.com/abc-defg-hij");
            tfUrl.setPrefWidth(300);
            if (existingTeleconsultation != null) {
                tfUrl.setText(existingTeleconsultation.getMeetingUrl());
            }
            urlSection.getChildren().addAll(urlLabel, tfUrl);

            content.getChildren().addAll(aptSection, modeSection, durationSection, urlSection);

            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            dialog.getDialogPane().setContent(scrollPane);

            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            if (dialog.showAndWait().get() == ButtonType.OK) {
                // Validate inputs
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
                                // Create new
                                teleconsultation newTc = new teleconsultation();
                                newTc.setAppointment(cbAppointment.getValue());
                                newTc.setMode(cbMode.getValue());
                                newTc.setDuration(duration);
                                newTc.setMeetingUrl(tfUrl.getText());
                                serviceTeleconsultation.create(newTc);
                            } else {
                                // Update existing
                                existingTeleconsultation.setAppointment(cbAppointment.getValue());
                                existingTeleconsultation.setMode(cbMode.getValue());
                                existingTeleconsultation.setDuration(duration);
                                existingTeleconsultation.setMeetingUrl(tfUrl.getText());
                                serviceTeleconsultation.update(existingTeleconsultation);
                            }
                            Platform.runLater(() -> {
                                showInfo(existingTeleconsultation == null ? "Teleconsultation created successfully"
                                        : "Teleconsultation updated successfully");
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

    private void deleteTeleconsultation(teleconsultation tc) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Teleconsultation");
        alert.setContentText("Are you sure you want to delete this teleconsultation?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            new Thread(() -> {
                try {
                    serviceTeleconsultation.delete(tc.getId());
                    Platform.runLater(() -> {
                        showInfo("Teleconsultation deleted successfully");
                        loadTeleconsultations();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> showError("Error deleting teleconsultation: " + e.getMessage()));
                }
            }).start();
        }
    }

    private void exportPDF(teleconsultation tc) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialFileName("teleconsultation_" + tc.getId() + ".pdf");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                generatePDF(file, tc);
                showInfo("PDF exported successfully to " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            showError("Error exporting PDF: " + e.getMessage());
        }
    }

    private void generatePDF(File file, teleconsultation tc) throws Exception {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        // Add title
        Paragraph title = new Paragraph("Teleconsultation Summary",
                FontFactory.getFont(FontFactory.HELVETICA, 18, Font.BOLD));
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("\n"));

        appointement apt = tc.getAppointment();

        // Add teleconsultation details
        document.add(new Paragraph("Teleconsultation ID: " + tc.getId(),
                FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD)));
        document.add(new Paragraph("Doctor: Dr. " + (apt != null ? apt.getDoctor() : "N/A"),
                FontFactory.getFont(FontFactory.HELVETICA, 11)));
        document.add(new Paragraph("Department: " + (apt != null ? apt.getDepartment() : "N/A"),
                FontFactory.getFont(FontFactory.HELVETICA, 11)));
        document.add(new Paragraph("Mode: " + tc.getMode().toUpperCase(),
                FontFactory.getFont(FontFactory.HELVETICA, 11, Font.BOLD)));
        document.add(
                new Paragraph("Duration: " + tc.getDuration() + " minutes",
                        FontFactory.getFont(FontFactory.HELVETICA, 11)));
        document.add(
                new Paragraph("Meeting URL: " + tc.getMeetingUrl(), FontFactory.getFont(FontFactory.HELVETICA, 11)));
        if (apt != null) {
            document.add(new Paragraph("Scheduled Date: " + apt.getScheduledAt().format(dateFormatter),
                    FontFactory.getFont(FontFactory.HELVETICA, 11)));
        }
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
