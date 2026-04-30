package org.docbook.entities.appointement;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.models.appointement;
import tn.esprit.services.NotificationService;
import tn.esprit.services.RatingService;
import tn.esprit.services.ServiceAppointement;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Appointment List Controller - Displays all appointments as cards
 * Handles search, filtering, and CRUD operations
 */
public class AppointmentListController implements Initializable {

    @FXML
    private TextField tfSearch;
    @FXML
    private ComboBox<String> cbStatusFilter;
    @FXML
    private VBox appointmentsContainer;
    @FXML
    private VBox emptyStateContainer;
    @FXML
    private Label lblTotalCount;
    @FXML
    private Label lblPendingCount;
    @FXML
    private Label lblConfirmedCount;

    private ServiceAppointement serviceAppointement;
    private List<appointement> allAppointments;
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        serviceAppointement = new ServiceAppointement();

        cbStatusFilter.getItems().addAll("All", "Pending", "Confirmed", "Completed", "Cancelled", "Expired");
        cbStatusFilter.setValue("All");

        tfSearch.textProperty().addListener((obs, oldVal, newVal) -> displayAppointments());
        cbStatusFilter.valueProperty().addListener((obs, oldVal, newVal) -> onStatusFilterChanged());

        loadAppointments();
    }

    @FXML
    public void loadAppointments() {
        new Thread(() -> {
            try {
                allAppointments = serviceAppointement.readAll();
                Platform.runLater(this::displayAppointments);
            } catch (Exception e) {
                System.err.println("Error loading appointments: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    public void onSearch() {
        displayAppointments();
    }

    @FXML
    public void onStatusFilterChanged() {
        displayAppointments();
    }

    @FXML
    public void onNewAppointment() {
        openAppointmentForm(null);
    }

    private void displayAppointments() {
        appointmentsContainer.getChildren().clear();

        if (allAppointments == null || allAppointments.isEmpty()) {
            showEmptyState();
            updateStatistics(allAppointments);
            return;
        }

        List<appointement> filteredAppointments = filterAppointments();

        if (filteredAppointments.isEmpty()) {
            showEmptyState();
        } else {
            emptyStateContainer.setVisible(false);
            emptyStateContainer.setManaged(false);
            for (appointement apt : filteredAppointments) {
                appointmentsContainer.getChildren().add(createAppointmentCard(apt));
            }
        }

        updateStatistics(allAppointments);
    }

    private List<appointement> filterAppointments() {
        List<appointement> filtered = allAppointments;

        String searchText = tfSearch.getText().toLowerCase().trim();
        if (!searchText.isEmpty()) {
            filtered = filtered.stream()
                    .filter(a -> a.getDoctor().toLowerCase().contains(searchText) ||
                            a.getDepartment().toLowerCase().contains(searchText) ||
                            (a.getMessage() != null && a.getMessage().toLowerCase().contains(searchText)) ||
                            a.getStatus().toLowerCase().contains(searchText))
                    .collect(Collectors.toList());
        }

        String selectedStatus = cbStatusFilter.getValue();
        if (selectedStatus != null && !selectedStatus.equals("All")) {
            filtered = filtered.stream()
                    .filter(a -> a.getStatus().equals(selectedStatus))
                    .collect(Collectors.toList());
        }

        return filtered;
    }

    private VBox createAppointmentCard(appointement apt) {
        VBox card = new VBox(10);
        card.getStyleClass().add("appointment-card");
        card.setStyle(
                "-fx-padding: 16; " +
                        "-fx-background-color: #ffffff; " +
                        "-fx-border-color: #ecf0f1; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 6, 0, 0, 1);");

        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label doctorLabel = new Label("👨‍⚕️ " + apt.getDoctor());
        doctorLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label statusBadge = new Label(apt.getStatus());
        statusBadge.getStyleClass().addAll("badge", "badge-" + apt.getStatus().toLowerCase());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerBox.getChildren().addAll(doctorLabel, spacer, statusBadge);

        VBox detailsBox = new VBox(5);
        detailsBox.setStyle("-fx-padding: 10 0;");

        Label departmentLabel = new Label("🏥 " + apt.getDepartment());
        departmentLabel.getStyleClass().add("appointment-department");

        Label dateLabel = new Label("📅 " + apt.getScheduledAt().format(dateFormatter));
        dateLabel.getStyleClass().add("appointment-detail");

        detailsBox.getChildren().addAll(departmentLabel, dateLabel);

        if (apt.getMessage() != null && !apt.getMessage().isEmpty()) {
            Label messageLabel = new Label("💬 " + apt.getMessage());
            messageLabel.setWrapText(true);
            messageLabel.getStyleClass().add("appointment-detail");
            detailsBox.getChildren().add(messageLabel);
        }

        HBox actionsBox = new HBox(8);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);
        actionsBox.setStyle("-fx-padding: 10 0 0 0; -fx-border-color: #ecf0f1; -fx-border-width: 1 0 0 0;");

        Button editBtn = createActionButton("✏️ Edit", "edit", () -> openAppointmentForm(apt));
        Button deleteBtn = createActionButton("🗑️ Delete", "delete", () -> deleteAppointment(apt));
        Button pdfBtn = createActionButton("📄 PDF", "pdf", () -> generatePDF(apt));

        // ── Star Rating Row ──────────────────────────────────────────────
        HBox ratingBox = new HBox(4);
        ratingBox.setAlignment(Pos.CENTER_LEFT);
        ratingBox.setStyle("-fx-padding: 8 0 0 0;");

        Label ratingLabel = new Label("Rate: ");
        ratingLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
        ratingBox.getChildren().add(ratingLabel);

        // 5 star buttons (plain ToggleButton styled as stars)
        ToggleButton[] stars = new ToggleButton[5];
        for (int i = 0; i < 5; i++) {
            final int starValue = i + 1;
            stars[i] = new ToggleButton("\u2606"); // ☆
            stars[i].setStyle("-fx-font-size: 16px; -fx-background-color: transparent; -fx-text-fill: #f39c12; -fx-cursor: hand; -fx-padding: 2;");
            stars[i].setOnAction(e -> {
                // Fill stars up to the clicked one
                for (int j = 0; j < 5; j++) {
                    if (j < starValue) {
                        stars[j].setText("\u2605"); // ★
                        stars[j].setStyle("-fx-font-size: 16px; -fx-background-color: transparent; -fx-text-fill: #f39c12; -fx-cursor: hand; -fx-padding: 2;");
                    } else {
                        stars[j].setText("\u2606"); // ☆
                    }
                }
                // Save rating asynchronously
                new Thread(() -> {
                    boolean saved = RatingService.saveAppointmentRating(apt.getId(), 1, starValue, "");
                    Platform.runLater(() -> {
                        if (saved) {
                            NotificationService.showSuccessToast("\u2B50 Rating Saved",
                                    RatingService.getStarDisplay(starValue) + " Thank you for rating Dr. " + apt.getDoctor());
                        } else {
                            // DB table may not exist yet — still show the visual feedback
                            NotificationService.showInfoToast("\u2B50 Rating Recorded",
                                    RatingService.getStarDisplay(starValue) + " (offline — DB table not yet created)");
                        }
                    });
                }).start();
            });
            ratingBox.getChildren().add(stars[i]);
        }

        actionsBox.getChildren().addAll(editBtn, deleteBtn, pdfBtn);
        card.getChildren().addAll(headerBox, detailsBox, ratingBox, actionsBox);

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

    private void openAppointmentForm(appointement appointmentToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FormView.fxml"));
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
            System.err.println("Error opening appointment form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteAppointment(appointement apt) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete Appointment?");
        confirmDialog.setContentText("Are you sure you want to delete this appointment?\n\nDoctor: " + apt.getDoctor());

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        serviceAppointement.delete(apt.getId());
                        Platform.runLater(() -> {
                            showNotification("Appointment deleted successfully", Alert.AlertType.INFORMATION);
                            loadAppointments();
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> showNotification("Error deleting appointment: " + e.getMessage(),
                                Alert.AlertType.ERROR));
                        System.err.println("Error: " + e.getMessage());
                        e.printStackTrace();
                    }
                }).start();
            }
        });
    }

    /**
     * Generate a PDF report for the given appointment using iText 5.
     * Opens a Save dialog so the user can choose the output location.
     */
    private void generatePDF(appointement apt) {
        // Let the user pick a save location
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Appointment PDF");
        fileChooser.setInitialFileName("appointment_" + apt.getId() + ".pdf");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File file = fileChooser.showSaveDialog(appointmentsContainer.getScene().getWindow());
        if (file == null) {
            return; // user cancelled
        }

        new Thread(() -> {
            try {
                buildPdf(apt, file);
                Platform.runLater(() -> showNotification("PDF saved to:\n" + file.getAbsolutePath(),
                        Alert.AlertType.INFORMATION));
            } catch (Exception e) {
                System.err.println("PDF generation failed: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(
                        () -> showNotification("Failed to generate PDF:\n" + e.getMessage(), Alert.AlertType.ERROR));
            }
        }).start();
    }

    /**
     * Build and write the PDF file using iText 5.
     */
    private void buildPdf(appointement apt, File outputFile) throws Exception {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(outputFile));
        document.open();

        // ── Fonts ──────────────────────────────────────────────────
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.DARK_GRAY);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.WHITE);
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.DARK_GRAY);
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, BaseColor.GRAY);

        // ── Title ──────────────────────────────────────────────────
        Paragraph title = new Paragraph("Medilab — Appointment Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // ── Two-column table ───────────────────────────────────────
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(20);
        table.setWidths(new float[] { 35f, 65f });

        BaseColor headerBg = new BaseColor(52, 152, 219); // #3498db

        addTableRow(table, "Field", "Value", headerFont, headerBg, true);
        addTableRow(table, "Appointment ID", String.valueOf(apt.getId()), headerFont, headerBg, false);
        addTableRow(table, "Doctor", apt.getDoctor(), cellFont, null, false);
        addTableRow(table, "Department", apt.getDepartment(), cellFont, null, false);
        addTableRow(table, "Scheduled At", apt.getScheduledAt().format(dateFormatter), cellFont, null, false);
        addTableRow(table, "Status", apt.getStatus(), cellFont, null, false);
        addTableRow(table, "Notes",
                (apt.getMessage() != null && !apt.getMessage().isBlank()) ? apt.getMessage() : "—",
                cellFont, null, false);

        document.add(table);

        // ── Status note ────────────────────────────────────────────
        String statusNote = switch (apt.getStatus()) {
            case "Confirmed" -> "✓ This appointment is confirmed. Please arrive 10 minutes early.";
            case "Pending" -> "⏳ This appointment is awaiting confirmation.";
            case "Cancelled" -> "✗ This appointment has been cancelled.";
            case "Completed" -> "✔ This appointment has been completed.";
            default -> "";
        };

        if (!statusNote.isBlank()) {
            Paragraph note = new Paragraph(statusNote,
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, BaseColor.DARK_GRAY));
            note.setSpacingBefore(5);
            document.add(note);
        }

        // ── Footer ─────────────────────────────────────────────────
        Paragraph footer = new Paragraph(
                "\nGenerated by Medilab Healthcare System  •  " +
                        java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();
        System.out.println("✅ PDF written to: " + outputFile.getAbsolutePath());
    }

    /**
     * Helper: add a two-cell row to the PDF table.
     * If {@code isHeader} is true the row gets a coloured background.
     */
    private void addTableRow(PdfPTable table, String label, String value,
            Font font, BaseColor bg, boolean isHeader) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        PdfPCell valueCell = new PdfPCell(new Phrase(value,
                isHeader ? font : FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.DARK_GRAY)));

        labelCell.setPadding(8);
        valueCell.setPadding(8);
        labelCell.setBorder(Rectangle.BOX);
        valueCell.setBorder(Rectangle.BOX);

        if (bg != null) {
            labelCell.setBackgroundColor(bg);
            valueCell.setBackgroundColor(bg);
        } else {
            // Alternating rows (light grey for label column)
            labelCell.setBackgroundColor(new BaseColor(236, 240, 241)); // #ecf0f1
        }

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void showEmptyState() {
        appointmentsContainer.getChildren().clear();
        emptyStateContainer.setVisible(true);
        emptyStateContainer.setManaged(true);
    }

    private void updateStatistics(List<appointement> appointments) {
        if (appointments == null) {
            lblTotalCount.setText("Total: 0");
            lblPendingCount.setText("Pending: 0");
            lblConfirmedCount.setText("Confirmed: 0");
            return;
        }

        long total = appointments.size();
        long pending = appointments.stream().filter(a -> a.getStatus().equals(appointement.STATUS_PENDING)).count();
        long confirmed = appointments.stream().filter(a -> a.getStatus().equals(appointement.STATUS_CONFIRMED)).count();

        lblTotalCount.setText("Total: " + total);
        lblPendingCount.setText("Pending: " + pending);
        lblConfirmedCount.setText("Confirmed: " + confirmed);
    }

    private void showNotification(String message, Alert.AlertType alertType) {
        if (alertType == Alert.AlertType.ERROR) {
            NotificationService.showErrorToast("Error", message);
        } else {
            NotificationService.showSuccessToast("\u2705 Done", message);
        }
    }
}