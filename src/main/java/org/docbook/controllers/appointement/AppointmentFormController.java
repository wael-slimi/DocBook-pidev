package org.docbook.controllers.appointement;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.models.appointement;
import tn.esprit.services.ServiceAppointement;
import tn.esprit.utils.ValidationUtil;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Appointment Form Controller - Handles Add/Edit operations
 * Manages form validation and database operations
 */
public class AppointmentFormController implements Initializable {

    @FXML
    private Label formTitle;
    @FXML
    private Label formSubtitle;
    @FXML
    private DatePicker dpScheduledDate;
    @FXML
    private TextField tfTime;
    @FXML
    private ComboBox<String> cbDepartment;
    @FXML
    private TextField tfDoctor;
    @FXML
    private ComboBox<String> cbStatus;
    @FXML
    private TextArea taMessage;
    @FXML
    private Label lblErrorMessage;
    @FXML
    private Button btnCancel;
    @FXML
    private Button btnSave;

    private ServiceAppointement serviceAppointement;
    private appointement currentAppointment;
    private boolean isEditMode = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        serviceAppointement = new ServiceAppointement();

        // Populate department ComboBox
        cbDepartment.getItems().addAll(
                "Cardiology",
                "Dermatology",
                "Neurology",
                "Orthopedics",
                "Pediatrics",
                "Psychiatry",
                "Radiology",
                "General Medicine");

        // Populate status ComboBox
        cbStatus.getItems().addAll(
                "Pending",
                "Confirmed",
                "Completed",
                "Cancelled",
                "Expired");

        setupDefaults();
    }

    /**
     * Setup default values and initialization
     */
    private void setupDefaults() {
        dpScheduledDate.setValue(LocalDate.now());
        tfTime.setText("14:00");
        cbDepartment.setValue("General Medicine");
        cbStatus.setValue("Pending");
    }

    /**
     * Set appointment data for editing
     */
    public void setAppointment(appointement appointment) {
        if (appointment != null) {
            isEditMode = true;
            currentAppointment = appointment;

            formTitle.setText("Edit Appointment");
            formSubtitle.setText("Update appointment details below");
            btnSave.setText("Update Appointment");

            // Populate form with existing data
            dpScheduledDate.setValue(appointment.getScheduledAt().toLocalDate());
            tfTime.setText(appointment.getScheduledAt().format(DateTimeFormatter.ofPattern("HH:mm")));
            cbDepartment.setValue(appointment.getDepartment());
            tfDoctor.setText(appointment.getDoctor());
            cbStatus.setValue(appointment.getStatus());
            taMessage.setText(appointment.getMessage() != null ? appointment.getMessage() : "");
        }
    }

    @FXML
    public void onSave() {
        if (!validateForm()) {
            return;
        }

        try {
            appointement appointment = buildAppointmentFromForm();

            if (isEditMode) {
                // Update existing appointment
                appointment.setId(currentAppointment.getId());
                serviceAppointement.update(appointment);
                showSuccess("Appointment updated successfully!");
            } else {
                // Create new appointment
                serviceAppointement.create(appointment);
                showSuccess("Appointment created successfully!");
            }

            // Close the form after successful save
            closeForm();

        } catch (Exception e) {
            showError("Error saving appointment: " + e.getMessage());
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void onCancel() {
        closeForm();
    }

    /**
     * Validate form fields with professional error handling
     * - No empty required fields
     * - Valid date (not in past)
     * - Valid time format (HH:mm)
     * - Departments and status selected
     */
    private boolean validateForm() {
        lblErrorMessage.setText("");

        // Clear all error styles first
        ValidationUtil.clearDatePickerError(dpScheduledDate);
        ValidationUtil.clearFieldError(tfTime);
        ValidationUtil.clearFieldError(tfDoctor);
        ValidationUtil.clearTextAreaError(taMessage);

        // Validate date - not null and not in past
        if (dpScheduledDate.getValue() == null) {
            showError("Please select a scheduled date");
            ValidationUtil.setDatePickerError(dpScheduledDate, true);
            return false;
        }

        if (!ValidationUtil.isDateNotInPast(dpScheduledDate.getValue())) {
            showError("Scheduled date cannot be in the past");
            ValidationUtil.setDatePickerError(dpScheduledDate, true);
            return false;
        }

        // Validate time
        if (!ValidationUtil.isNotEmpty(tfTime.getText())) {
            showError("Please enter a time in HH:mm format");
            ValidationUtil.setFieldError(tfTime, true);
            return false;
        }

        if (!ValidationUtil.isValidTimeFormat(tfTime.getText())) {
            showError("Invalid time format. Please use HH:mm (e.g., 14:30)");
            ValidationUtil.setFieldError(tfTime, true);
            return false;
        }

        // Validate department
        if (cbDepartment.getValue() == null || cbDepartment.getValue().isEmpty()) {
            showError("Please select a department");
            return false;
        }

        // Validate doctor name
        if (!ValidationUtil.isNotEmpty(tfDoctor.getText())) {
            showError("Please enter a doctor name");
            ValidationUtil.setFieldError(tfDoctor, true);
            return false;
        }

        // Validate status
        if (cbStatus.getValue() == null || cbStatus.getValue().isEmpty()) {
            showError("Please select an appointment status");
            return false;
        }

        return true;
    }

    /**
     * Build appointment object from form fields
     */
    private appointement buildAppointmentFromForm() {
        appointement appointment = new appointement();

        LocalDate date = dpScheduledDate.getValue();
        LocalTime time = LocalTime.parse(tfTime.getText(), DateTimeFormatter.ofPattern("HH:mm"));
        LocalDateTime scheduledAt = LocalDateTime.of(date, time);

        appointment.setScheduledAt(scheduledAt);
        appointment.setDepartment(cbDepartment.getValue());
        appointment.setDoctor(tfDoctor.getText().trim());
        appointment.setMessage(taMessage.getText().trim());
        appointment.setStatus(cbStatus.getValue());

        return appointment;
    }

    /**
     * Show error message
     */
    private void showError(String message) {
        lblErrorMessage.setText(message);
        lblErrorMessage.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
    }

    /**
     * Show success message
     */
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Close the form window
     */
    private void closeForm() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
}
