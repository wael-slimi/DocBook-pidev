package org.docbook.controllers.records;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.docbook.entities.records.Appointment;
import org.docbook.services.AppointmentService;
import org.docbook.util.AppState;
import org.docbook.entities.users.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ResourceBundle;

public class AppointmentFormController implements javafx.fxml.Initializable {

    @FXML private ComboBox<String> departmentCombo;
    @FXML private ComboBox<String> doctorCombo;
    @FXML private DatePicker datePicker;
    @FXML private TextArea messageArea;

    private AppointmentService appointmentService;
    private Appointment appointmentToEdit;
    private String currentUserRole;
    private int currentUserId;

    @Override
    public void initialize(java.net.URL url, ResourceBundle resourceBundle) {
        appointmentService = new AppointmentService();
        
        User currentUser = AppState.getCurrentUser();
        currentUserRole = (currentUser != null) ? currentUser.getRole() : "UNKNOWN";
        currentUserId = (currentUser != null) ? currentUser.getId() : 0;
        
        setupDepartments();
        setupDoctors();
        setupDatePicker();
    }

    private void setupDepartments() {
        departmentCombo.getItems().addAll(
            "General Medicine",
            "Cardiology",
            "Dermatology",
            "Neurology",
            "Orthopedics",
            "Pediatrics",
            "Psychiatry",
            "Radiology"
        );
    }

    private void setupDoctors() {
        doctorCombo.getItems().addAll(
            "Dr. Smith",
            "Dr. Johnson",
            "Dr. Williams",
            "Dr. Brown",
            "Dr. Davis",
            "Dr. Miller"
        );
    }

    private void setupDatePicker() {
        datePicker.setValue(LocalDate.now().plusDays(1));
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
    }

    public void setAppointment(Appointment appointment) {
        this.appointmentToEdit = appointment;
        if (appointment != null) {
            departmentCombo.setValue(appointment.getDepartment());
            doctorCombo.setValue(appointment.getDoctor());
            if (appointment.getScheduledAt() != null) {
                datePicker.setValue(appointment.getScheduledAt().toLocalDate());
            }
            messageArea.setText(appointment.getMessage());
        }
    }

    @FXML
    public void onSubmit() {
        if (!validateForm()) return;

        try {
            Appointment appointment = appointmentToEdit != null ? appointmentToEdit : new Appointment();
            
            appointment.setDepartment(departmentCombo.getValue());
            appointment.setDoctor(doctorCombo.getValue());
            
            LocalDate date = datePicker.getValue();
            if (date != null) {
                appointment.setScheduledAt(LocalDateTime.of(date, LocalTime.of(9, 0)));
            }
            
            appointment.setMessage(messageArea.getText());
            appointment.setStatus(Appointment.STATUS_PENDING);
            
            if (currentUserRole.equals("PATIENT")) {
                appointment.setPatientId(currentUserId);
            } else if (currentUserRole.equals("DOCTOR")) {
                appointment.setDoctorId(currentUserId);
            }

            if (appointmentToEdit != null) {
                appointmentService.update(appointment);
                showInfo("Appointment updated successfully");
            } else {
                appointmentService.create(appointment);
                showInfo("Appointment created successfully");
            }

            closeWindow();
        } catch (Exception e) {
            showError("Error saving appointment: " + e.getMessage());
        }
    }

    @FXML
    public void onCancel() {
        closeWindow();
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();
        
        if (departmentCombo.getValue() == null || departmentCombo.getValue().isEmpty()) {
            errors.append("Please select a department.\n");
        }
        if (doctorCombo.getValue() == null || doctorCombo.getValue().isEmpty()) {
            errors.append("Please select a doctor.\n");
        }
        if (datePicker.getValue() == null) {
            errors.append("Please select a date.\n");
        }
        
        if (errors.length() > 0) {
            showError(errors.toString());
            return false;
        }
        return true;
    }

    private void closeWindow() {
        Stage stage = (Stage) departmentCombo.getScene().getWindow();
        stage.close();
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