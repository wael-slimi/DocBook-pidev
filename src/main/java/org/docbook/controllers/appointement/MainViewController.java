package org.docbook.controllers.appointement;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Main View Controller - Handles navigation between views
 * Manages the sidebar buttons and content area switching
 */
public class MainViewController implements Initializable {

    @FXML
    private StackPane contentArea;
    @FXML
    private Button btnHome;
    @FXML
    private Button btnAppointments;
    @FXML
    private Button btnTeleconsultations;

    private Parent homeView;
    private Parent appointmentListView;
    private Parent teleconsultationView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Pre-load all views
            loadViews();
            // Show home by default
            if (homeView != null) {
                onNavigateHome();
            } else {
                showErrorState("Failed to load dashboard");
            }
        } catch (Exception e) {
            System.err.println("Error initializing MainViewController: " + e.getMessage());
            e.printStackTrace();
            showErrorState("Error loading dashboard: " + e.getMessage());
        }
    }

    /**
     * Pre-load all views to avoid loading delays
     */
    private void loadViews() {
        try {
            // Load home view
            FXMLLoader homeLoader = new FXMLLoader(getClass().getResource("/home.fxml"));
            homeView = homeLoader.load();
            DashboardController dashboardController = homeLoader.getController();
            dashboardController.setMainViewController(this);
            System.out.println("✓ Home view loaded successfully");
        } catch (Exception e) {
            System.err.println("✗ Error loading home.fxml: " + e.getMessage());
            e.printStackTrace();
            homeView = null;
        }

        try {
            // Load appointments view
            FXMLLoader appointmentLoader = new FXMLLoader(getClass().getResource("/AppointmentList.fxml"));
            appointmentListView = appointmentLoader.load();
            System.out.println("✓ Appointment list view loaded successfully");
        } catch (Exception e) {
            System.err.println("✗ Error loading AppointmentList.fxml: " + e.getMessage());
            e.printStackTrace();
            appointmentListView = null;
        }

        try {
            // Load teleconsultation view
            FXMLLoader teleLoader = new FXMLLoader(getClass().getResource("/teleconsultation.fxml"));
            teleconsultationView = teleLoader.load();
            System.out.println("✓ Teleconsultation view loaded successfully");
        } catch (Exception e) {
            System.err.println("✗ Error loading teleconsultation.fxml: " + e.getMessage());
            e.printStackTrace();
            teleconsultationView = null;
        }
    }

    @FXML
    public void onNavigateHome() {
        if (homeView == null) {
            showErrorState("Home view not loaded");
            return;
        }
        updateActiveButton(btnHome);
        contentArea.getChildren().setAll(homeView);
    }

    @FXML
    public void onNavigateAppointments() {
        if (appointmentListView == null) {
            showErrorState("Appointment list view not loaded");
            return;
        }
        updateActiveButton(btnAppointments);
        contentArea.getChildren().setAll(appointmentListView);
    }

    @FXML
    public void onNavigateTeleconsultations() {
        if (teleconsultationView == null) {
            showErrorState("Teleconsultation view not loaded");
            return;
        }
        updateActiveButton(btnTeleconsultations);
        contentArea.getChildren().setAll(teleconsultationView);
    }

    /**
     * Show error state in the content area
     */
    private void showErrorState(String errorMessage) {
        VBox errorBox = new VBox(10);
        errorBox.setStyle("-fx-alignment: center; -fx-padding: 50; -fx-text-fill: #e74c3c;");
        errorBox.getChildren().addAll(
                new javafx.scene.control.Label("⚠️ Error"),
                new javafx.scene.control.Label(errorMessage));
        contentArea.getChildren().setAll(errorBox);
    }

    /**
     * Quick action: New Appointment
     */
    @FXML
    public void onNewAppointment() {
        System.out.println("New Appointment action triggered");
        onNavigateAppointments();
    }

    /**
     * Quick action: New Consultation
     */
    @FXML
    public void onNewConsultation() {
        System.out.println("New Consultation action triggered");
        onNavigateTeleconsultations();
    }

    /**
     * Quick action: View All Appointments
     */
    @FXML
    public void onViewAllAppointments() {
        System.out.println("View All Appointments action triggered");
        onNavigateAppointments();
    }

    /**
     * Quick action: View All Consultations
     */
    @FXML
    public void onViewAllConsultations() {
        System.out.println("View All Consultations action triggered");
        onNavigateTeleconsultations();
    }

    /**
     * Update the visual state of buttons - only one is active at a time
     */
    private void updateActiveButton(Button activeButton) {
        if (btnHome != null)
            btnHome.getStyleClass().remove("active");
        if (btnAppointments != null)
            btnAppointments.getStyleClass().remove("active");
        if (btnTeleconsultations != null)
            btnTeleconsultations.getStyleClass().remove("active");

        if (activeButton != null) {
            activeButton.getStyleClass().add("active");
        }
    }
}
