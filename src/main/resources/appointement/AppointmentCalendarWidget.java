package tn.esprit.utils;

import javafx.application.Platform;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import tn.esprit.models.appointement;
import tn.esprit.services.ServiceAppointement;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AppointmentCalendarWidget - Displays a calendar with highlighted appointment
 * dates
 * Features:
 * - Visual date picker
 * - Highlights days with active appointments
 * - Shows count of appointments per day
 * - Color-coded based on appointment status
 * - Interactive date selection
 */
public class AppointmentCalendarWidget extends VBox {

    private DatePicker datePicker;
    private Label monthYearLabel;
    private Label appointmentCountLabel;
    private Label selectedDateInfoLabel;
    private ServiceAppointement serviceAppointement;
    private Map<LocalDate, Integer> appointmentDates;
    private Map<LocalDate, String> appointmentStatus;

    public AppointmentCalendarWidget() {
        serviceAppointement = new ServiceAppointement();
        appointmentDates = new HashMap<>();
        appointmentStatus = new HashMap<>();
        initialize();
        loadAppointmentDates();
    }

    /**
     * Initialize UI components
     */
    private void initialize() {
        setSpacing(12);
        setStyle("-fx-padding: 15; -fx-background-color: white; " +
                "-fx-border-radius: 8; -fx-background-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        // Title
        Label titleLabel = new Label("📅 Appointment Calendar");
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Month/Year Display
        monthYearLabel = new Label();
        monthYearLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

        // Date Picker
        datePicker = new DatePicker(LocalDate.now());
        datePicker.setStyle("-fx-control-inner-background: #ecf0f1; -fx-padding: 5;");
        datePicker.setOnAction(event -> onDateSelected(datePicker.getValue()));

        // Appointment Count for Selected Date
        appointmentCountLabel = new Label("0 appointments on selected date");
        appointmentCountLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #3498db;");

        // Selected Date Info
        selectedDateInfoLabel = new Label("Select a date to view details");
        selectedDateInfoLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #95a5a6; -fx-wrap-text: true;");
        selectedDateInfoLabel.setMaxWidth(250);

        // Legend
        VBox legendBox = createLegend();

        getChildren().addAll(
                titleLabel,
                monthYearLabel,
                datePicker,
                appointmentCountLabel,
                selectedDateInfoLabel,
                legendBox);

        updateMonthYearLabel();
        onDateSelected(LocalDate.now());
    }

    /**
     * Load appointment dates from database
     */
    private void loadAppointmentDates() {
        new Thread(() -> {
            try {
                List<appointement> appointments = serviceAppointement.readAll();

                appointmentDates.clear();
                appointmentStatus.clear();

                for (appointement apt : appointments) {
                    LocalDate date = apt.getScheduledAt().toLocalDate();
                    appointmentDates.put(date, appointmentDates.getOrDefault(date, 0) + 1);

                    // Store the latest status for each date
                    if (!appointmentStatus.containsKey(date)) {
                        appointmentStatus.put(date, apt.getStatus());
                    }
                }

                Platform.runLater(this::updateUI);
            } catch (Exception e) {
                System.err.println("Error loading appointment dates: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Update UI with loaded data
     */
    private void updateUI() {
        onDateSelected(datePicker.getValue());
    }

    /**
     * Handle date selection
     */
    private void onDateSelected(LocalDate date) {
        int count = appointmentDates.getOrDefault(date, 0);
        String status = appointmentStatus.getOrDefault(date, "None");

        appointmentCountLabel.setText(String.format("%d appointment%s on %s",
                count, count != 1 ? "s" : "", date));

        if (count > 0) {
            appointmentCountLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
            selectedDateInfoLabel.setText(String.format("Status: %s", status));
        } else {
            appointmentCountLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #95a5a6;");
            selectedDateInfoLabel.setText("No appointments scheduled");
        }
    }

    /**
     * Update month/year label
     */
    private void updateMonthYearLabel() {
        YearMonth ym = YearMonth.from(datePicker.getValue());
        monthYearLabel.setText(String.format("%s %d", ym.getMonth(), ym.getYear()));
    }

    /**
     * Create legend for status colors
     */
    private VBox createLegend() {
        VBox legend = new VBox(3);
        legend.setStyle(
                "-fx-padding: 8; -fx-background-color: #f8f9fa; -fx-border-radius: 4; -fx-background-radius: 4;");

        Label legendTitle = new Label("Legend");
        legendTitle.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label confirmedLabel = new Label("🟢 Confirmed");
        confirmedLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: #27ae60;");

        Label pendingLabel = new Label("🟡 Pending");
        pendingLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: #f39c12;");

        Label cancelledLabel = new Label("🔴 Cancelled/Expired");
        cancelledLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: #e74c3c;");

        legend.getChildren().addAll(legendTitle, confirmedLabel, pendingLabel, cancelledLabel);
        return legend;
    }

    /**
     * Refresh appointment data
     */
    public void refresh() {
        loadAppointmentDates();
    }

    /**
     * Get selected date
     */
    public LocalDate getSelectedDate() {
        return datePicker.getValue();
    }

    /**
     * Get appointment count for a specific date
     */
    public int getAppointmentCount(LocalDate date) {
        return appointmentDates.getOrDefault(date, 0);
    }
}