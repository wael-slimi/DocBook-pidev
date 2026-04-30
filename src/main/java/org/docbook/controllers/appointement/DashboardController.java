package org.docbook.controllers.appointement;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.models.appointement;
import tn.esprit.services.GeminiService;
import tn.esprit.services.NotificationService;
import tn.esprit.services.ServiceAppointement;
import tn.esprit.utils.AppointmentCalendarWidget;
import tn.esprit.utils.WeatherWidget;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Dashboard Controller - Displays statistics and quick actions
 * Shows overview of appointments, consultations, analytics, and weather
 * Features:
 * - Real-time statistics (total, confirmed, today's appointments)
 * - PieChart analytics showing appointment status distribution
 * - Weather widget integration
 * - Appointment calendar with highlights
 */
public class DashboardController implements Initializable {

    @FXML
    private Label lblTotalAppointments;
    @FXML
    private Label lblActiveConsultations;
    @FXML
    private Label lblTodayAppointments;
    @FXML
    private VBox recentAppointmentsContainer;
    @FXML
    private VBox chartsContainer;
    @FXML
    private HBox widgetsContainer;

    private ServiceAppointement serviceAppointement;
    private MainViewController mainViewController;
    private PieChart appointmentStatusChart;
    private WeatherWidget weatherWidget;
    private AppointmentCalendarWidget calendarWidget;
    private GeminiService geminiService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        serviceAppointement = new ServiceAppointement();
        geminiService = new GeminiService();
        setupCharts();
        setupWidgets();
        loadDashboardData();
    }

    /**
     * Setup PieChart for appointment status distribution
     */
    private void setupCharts() {
        appointmentStatusChart = new PieChart();
        appointmentStatusChart.setTitle("Appointment Status Distribution");
        appointmentStatusChart.setStyle("-fx-font-size: 12px;");
        appointmentStatusChart.setLegendVisible(true);
        appointmentStatusChart.setLabelsVisible(true);
        appointmentStatusChart.setPrefHeight(300);

        // Add chart to container if it exists
        if (chartsContainer != null) {
            chartsContainer.getChildren().clear();
            chartsContainer.getChildren().add(appointmentStatusChart);
            VBox.setVgrow(appointmentStatusChart, Priority.ALWAYS);
        }
    }

    /**
     * Setup weather and calendar widgets
     */
    private void setupWidgets() {
        if (widgetsContainer != null) {
            widgetsContainer.getChildren().clear();
            widgetsContainer.setSpacing(15);
            widgetsContainer.setPrefHeight(350);

            // Weather Widget
            weatherWidget = new WeatherWidget();
            HBox.setHgrow(weatherWidget, Priority.ALWAYS);
            weatherWidget.setPrefWidth(250);

            // Calendar Widget
            calendarWidget = new AppointmentCalendarWidget();
            HBox.setHgrow(calendarWidget, Priority.ALWAYS);
            calendarWidget.setPrefWidth(250);

            widgetsContainer.getChildren().addAll(weatherWidget, calendarWidget);
        }
    }

    /**
     * Update appointment status chart with current data
     */
    private void updateAppointmentChart(List<appointement> allAppointments) {
        // Count appointments by status
        long confirmedCount = allAppointments.stream()
                .filter(a -> a.getStatus().equals(appointement.STATUS_CONFIRMED))
                .count();
        long pendingCount = allAppointments.stream()
                .filter(a -> a.getStatus().equals(appointement.STATUS_PENDING))
                .count();
        long completedCount = allAppointments.stream()
                .filter(a -> a.getStatus().equals(appointement.STATUS_COMPLETED))
                .count();
        long cancelledCount = allAppointments.stream()
                .filter(a -> a.getStatus().equals(appointement.STATUS_CANCELLED) ||
                        a.getStatus().equals(appointement.STATUS_EXPIRED))
                .count();

        // Create pie chart data
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Confirmed ✓", confirmedCount > 0 ? confirmedCount : 1),
                new PieChart.Data("Pending ⏳", pendingCount > 0 ? pendingCount : 1),
                new PieChart.Data("Completed ✔", completedCount > 0 ? completedCount : 1),
                new PieChart.Data("Cancelled/Expired ✗", cancelledCount > 0 ? cancelledCount : 1));

        appointmentStatusChart.setData(pieChartData);

        // Color the pie chart segments
        ObservableList<PieChart.Data> data = appointmentStatusChart.getData();
        data.get(0).getNode().setStyle("-fx-pie-color: #27ae60;"); // Green for confirmed
        data.get(1).getNode().setStyle("-fx-pie-color: #f39c12;"); // Orange for pending
        data.get(2).getNode().setStyle("-fx-pie-color: #3498db;"); // Blue for completed
        data.get(3).getNode().setStyle("-fx-pie-color: #e74c3c;"); // Red for cancelled
    }

    /**
     * Load dashboard statistics from database
     */
    private void loadDashboardData() {
        new Thread(() -> {
            try {
                List<appointement> allAppointments = serviceAppointement.readAll();

                // Calculate statistics
                long totalAppointments = allAppointments.size();
                long activeConsultations = allAppointments.stream()
                        .filter(a -> a.getStatus().equals(appointement.STATUS_CONFIRMED))
                        .count();
                long todayAppointments = allAppointments.stream()
                        .filter(a -> a.getScheduledAt().toLocalDate().equals(LocalDate.now()))
                        .count();

                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    lblTotalAppointments.setText(String.valueOf(totalAppointments));
                    lblActiveConsultations.setText(String.valueOf(activeConsultations));
                    lblTodayAppointments.setText(String.valueOf(todayAppointments));

                    // Update charts and widgets
                    updateAppointmentChart(allAppointments);
                    if (calendarWidget != null) {
                        calendarWidget.refresh();
                    }

                    // Display recent appointments
                    displayRecentAppointments(allAppointments);
                });

            } catch (Exception e) {
                System.err.println("Error loading dashboard data: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Display recent appointments in the dashboard
     */
    private void displayRecentAppointments(List<appointement> allAppointments) {
        recentAppointmentsContainer.getChildren().clear();

        // Show only the 3 most recent appointments
        allAppointments.stream()
                .limit(3)
                .forEach(apt -> {
                    recentAppointmentsContainer.getChildren().add(createRecentAppointmentItem(apt));
                });

        if (allAppointments.isEmpty()) {
            Label emptyLabel = new Label("No appointments yet");
            emptyLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 12px;");
            recentAppointmentsContainer.getChildren().add(emptyLabel);
        }
    }

    /**
     * Create a compact appointment item for display
     */
    private VBox createRecentAppointmentItem(appointement apt) {
        VBox item = new VBox(5);
        item.setStyle(
                "-fx-padding: 10; " +
                        "-fx-border-color: #ecf0f1; " +
                        "-fx-border-width: 0 0 1 0; " +
                        "-fx-background-color: transparent;");

        Label doctorLabel = new Label(apt.getDoctor() + " - " + apt.getDepartment());
        doctorLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #2c3e50;");

        Label dateLabel = new Label("📅 "
                + apt.getScheduledAt().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");

        Label statusLabel = new Label(apt.getStatus());
        statusLabel.getStyleClass().add("badge");
        statusLabel.getStyleClass().add("badge-" + apt.getStatus().toLowerCase());

        item.getChildren().addAll(doctorLabel, dateLabel, statusLabel);
        return item;
    }

    @FXML
    public void onNewAppointment() {
        openAppointmentForm(null);
    }

    @FXML
    public void onNewConsultation() {
        if (mainViewController != null) {
            mainViewController.onNavigateTeleconsultations();
        }
    }

    @FXML
    public void onViewAllAppointments() {
        if (mainViewController != null) {
            mainViewController.onNavigateAppointments();
        }
    }

    @FXML
    public void onViewAllConsultations() {
        if (mainViewController != null) {
            mainViewController.onNavigateTeleconsultations();
        }
    }

    public void setMainViewController(MainViewController mainViewController) {
        this.mainViewController = mainViewController;
    }

    /**
     * Open the AI Chat window — wired to btnOpenChat in home.fxml
     */
    @FXML
    public void openChatWindow() {
        Stage chatStage = new Stage();
        chatStage.setTitle("\uD83E\uDD16 Medilab AI Assistant");
        chatStage.setWidth(620);
        chatStage.setHeight(720);
        chatStage.initModality(Modality.NONE); // non-blocking

        VBox chatContainer = new VBox(10);
        chatContainer.setPadding(new Insets(15));
        chatContainer.setStyle("-fx-background-color: #ffffff;");

        TextArea chatHistory = new TextArea();
        chatHistory.setEditable(false);
        chatHistory.setWrapText(true);
        chatHistory.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 11; -fx-control-inner-background: #f5f5f5;");
        chatHistory.setText("\uD83E\uDD16 Medilab AI Assistant\n\nHello! I'm your healthcare assistant. Ask me about:\n"
                + "\u2022 Appointment management\n\u2022 Medical information\n\u2022 Clinic navigation\n\n");

        HBox inputBox = new HBox(8);
        inputBox.setAlignment(Pos.CENTER_LEFT);

        TextField messageInput = new TextField();
        messageInput.setPromptText("Type your question here...");
        messageInput.setStyle("-fx-font-size: 12; -fx-padding: 8;");

        Button sendButton = new Button("Send");
        sendButton.setStyle("-fx-padding: 8 16; -fx-font-size: 11; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand;");

        sendButton.setOnAction(e -> {
            String userMessage = messageInput.getText().trim();
            if (!userMessage.isEmpty()) {
                chatHistory.appendText("\n\uD83D\uDC64 You: " + userMessage + "\n");
                messageInput.clear();
                chatHistory.appendText("\uD83E\uDD16 Medilab: Processing...\n");
                geminiService.sendMessage(userMessage, response -> {
                    String currentText = chatHistory.getText();
                    String processingLine = "\uD83E\uDD16 Medilab: Processing...\n";
                    if (currentText.endsWith(processingLine)) {
                        chatHistory.setText(currentText.substring(0, currentText.length() - processingLine.length()));
                    }
                    chatHistory.appendText("\uD83E\uDD16 Medilab: " + response + "\n\n");
                    chatHistory.setScrollTop(Double.MAX_VALUE);
                });
            }
        });

        messageInput.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) sendButton.fire();
        });

        inputBox.getChildren().addAll(messageInput, sendButton);
        HBox.setHgrow(messageInput, Priority.ALWAYS);

        chatContainer.getChildren().addAll(
                new Label("\uD83D\uDCAC Chat History"),
                chatHistory,
                new Separator(),
                inputBox);
        VBox.setVgrow(chatHistory, Priority.ALWAYS);

        ScrollPane scrollPane = new ScrollPane(chatContainer);
        scrollPane.setFitToWidth(true);
        chatStage.setScene(new Scene(scrollPane));
        chatStage.show();

        // Show a toast confirming chat opened
        NotificationService.showSuccessToast("\uD83E\uDD16 AI Chat", "Medilab AI Assistant is ready!");
    }

    /**
     * Test notification — wired to btnTestNotification in home.fxml
     */
    @FXML
    public void onTestNotification() {
        NotificationService.showSuccessToast("\u2705 Notification Test", "ControlsFX toast notifications are working!");
        NotificationService.sendRatingConfirmation("demo@medilab.com", 5);
    }

    /**
     * Open the appointment form in a modal window
     */
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

            // Refresh dashboard after form closes
            loadDashboardData();

        } catch (Exception e) {
            System.err.println("Error opening appointment form: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
