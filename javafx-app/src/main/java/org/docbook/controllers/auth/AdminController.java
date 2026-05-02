package org.docbook.controllers.auth;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.docbook.entities.UserActivityLog;
import org.docbook.entities.users.User;
import org.docbook.services.UserActivityLogService;
import org.docbook.services.users.UserService;
import org.docbook.util.ThemeManager;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AdminController {

    @FXML private BorderPane mainPane;
    @FXML private TextField searchField;
    @FXML private Label pendingCountLabel;

    @FXML private VBox dashboardView, usersView, approvalsView, logsView;

    @FXML private Button btnDashboard, btnUserManagement, btnDoctorApprovals, btnSystemLogs, btnToggleTheme, btnExportCSV;

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> colName, colRole, colEmail, colStatus;
    @FXML private TableColumn<User, Void> colAction;

    @FXML private TableView<User> approvalTable;
    @FXML private TableColumn<User, String> colApproveName, colApproveSpecialty, colApproveEmail, colApproveStatus;
    @FXML private TableColumn<User, Void> colApproveAction;

    @FXML private TableView<UserActivityLog> logTable;
    @FXML private TableColumn<UserActivityLog, Integer> colLogId;
    @FXML private TableColumn<UserActivityLog, String> colLogUser, colLogAction, colLogDetails, colLogDate;
    @FXML private TextField logSearchField;
    @FXML private ComboBox<String> logActionFilter;

    @FXML private Label statTotalUsers, statTotalTrend;
    @FXML private Label statDoctors, statDoctorsTrend;
    @FXML private Label statPatients, statPatientsTrend;
    @FXML private ProgressBar distDoctorsBar, distPatientsBar, distAdminsBar;
    @FXML private Label distDoctorsLabel, distPatientsLabel, distAdminsLabel;
    @FXML private Label distDoctorsPct, distPatientsPct, distAdminsPct;
    @FXML private Label distRecent;
    @FXML private ComboBox<String> filterTypeCombo, filterStatusCombo;

    private final UserService userService = new UserService();
    private final UserActivityLogService logService = new UserActivityLogService();
    private FilteredList<User> filteredUserData;
    private FilteredList<UserActivityLog> filteredLogData;

    @FXML
    public void initialize() {
        setupUserTable();
        setupApprovalTable();
        setupLogTable();
        setupFilters();
        handleSwitchToDashboard();
        ThemeManager.applyTheme(mainPane != null ? mainPane.getScene() : null);
    }

    private void setupUserTable() {
        if (userTable == null) return;
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (colRole != null) colRole.setCellValueFactory(new PropertyValueFactory<>("dtype"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        if (colRole != null) {
            colRole.setCellFactory(column -> new TableCell<User, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                        setText(null); setStyle("");
                    } else {
                        User u = getTableRow().getItem();
                        setText(u.getDtype() != null ? u.getDtype().toUpperCase() : "");
                        setStyle("-fx-text-fill: #4b5563; -fx-font-weight: bold;");
                    }
                }
            });
        }

        colStatus.setCellFactory(column -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null); setStyle("");
                } else {
                    User u = getTableRow().getItem();
                    String status = u.getStatus();
                    if ("pending".equalsIgnoreCase(status)) {
                        setText("PENDING");
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else if ("approved".equalsIgnoreCase(status)) {
                        setText("ACTIVE");
                        setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                    } else {
                        setText(status != null ? status.toUpperCase() : "");
                        setStyle("-fx-text-fill: #7f8c8d; -fx-font-weight: bold;");
                    }
                }
            }
        });

        colAction.setCellFactory(param -> new TableCell<User, Void>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox container = new HBox(10, editBtn, deleteBtn);
            {
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    User u = getTableRow().getItem();
                    deleteBtn.setOnAction(e -> handleDelete(u));
                    setGraphic(container);
                }
            }
        });
    }

    private void setupApprovalTable() {
        if (approvalTable == null) return;
        colApproveName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colApproveEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        colApproveStatus.setCellFactory(column -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null); setStyle("");
                } else {
                    setText("PENDING");
                    setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                }
            }
        });

        colApproveAction.setCellFactory(param -> new TableCell<User, Void>() {
            private final Button acceptBtn = new Button("Accept");
            private final Button denyBtn = new Button("Deny");
            private final HBox container = new HBox(10, acceptBtn, denyBtn);
            {
                acceptBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-cursor: hand;");
                denyBtn.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-cursor: hand;");
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    User u = getTableRow().getItem();
                    acceptBtn.setOnAction(e -> handleApprove(u));
                    denyBtn.setOnAction(e -> handleDeny(u));
                    setGraphic(container);
                }
            }
        });
    }

    private void setupLogTable() {
        if (logTable == null) return;
        colLogId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colLogUser.setCellValueFactory(new PropertyValueFactory<>("userName"));
        colLogAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colLogDetails.setCellValueFactory(new PropertyValueFactory<>("details"));
        colLogDate.setCellValueFactory(new PropertyValueFactory<>("timestamp"));

        colLogAction.setCellFactory(column -> new TableCell<UserActivityLog, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null); setStyle("");
                } else {
                    setText(item);
                    String action = getTableRow().getItem().getActionType();
                    if ("LOGIN".equals(action)) {
                        setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                    } else if ("ACCOUNT_CREATED".equals(action)) {
                        setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    private void setupFilters() {
        if (filterTypeCombo != null) {
            filterTypeCombo.getItems().addAll("All Types", "Doctor", "Patient", "Admin");
            filterTypeCombo.setValue("All Types");
            filterTypeCombo.setOnAction(e -> applyUserFilters());
        }
        if (filterStatusCombo != null) {
            filterStatusCombo.getItems().addAll("All Statuses", "Approved", "Pending", "Denied");
            filterStatusCombo.setValue("All Statuses");
            filterStatusCombo.setOnAction(e -> applyUserFilters());
        }
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> applyUserFilters());
        }
        if (logActionFilter != null) {
            logActionFilter.getItems().addAll("All Actions", "LOGIN", "ACCOUNT_CREATED", "ACCOUNT_DELETED");
            logActionFilter.setValue("All Actions");
            logActionFilter.setOnAction(e -> applyLogFilters());
        }
        if (logSearchField != null) {
            logSearchField.textProperty().addListener((obs, oldVal, newVal) -> applyLogFilters());
        }
    }

    private void applyUserFilters() {
        if (filteredUserData == null) return;
        filteredUserData.setPredicate(user -> {
            boolean matchesSearch = true;
            boolean matchesType = true;
            boolean matchesStatus = true;

            if (searchField != null && !searchField.getText().isEmpty()) {
                String query = searchField.getText().toLowerCase();
                matchesSearch = (user.getName() != null && user.getName().toLowerCase().contains(query))
                    || (user.getEmail() != null && user.getEmail().toLowerCase().contains(query));
            }

            if (filterTypeCombo != null && !"All Types".equals(filterTypeCombo.getValue())) {
                String type = filterTypeCombo.getValue().toLowerCase();
                matchesType = type.equals(user.getDtype());
            }

            if (filterStatusCombo != null && !"All Statuses".equals(filterStatusCombo.getValue())) {
                String status = filterStatusCombo.getValue().toLowerCase();
                matchesStatus = status.equals(user.getStatus());
            }

            return matchesSearch && matchesType && matchesStatus;
        });
    }

    private void applyLogFilters() {
        if (filteredLogData == null) return;
        filteredLogData.setPredicate(log -> {
            boolean matchesSearch = true;
            boolean matchesAction = true;

            if (logSearchField != null && !logSearchField.getText().isEmpty()) {
                String query = logSearchField.getText().toLowerCase();
                matchesSearch = (log.getUserName() != null && log.getUserName().toLowerCase().contains(query))
                    || (log.getDetails() != null && log.getDetails().toLowerCase().contains(query));
            }

            if (logActionFilter != null && !"All Actions".equals(logActionFilter.getValue())) {
                matchesAction = logActionFilter.getValue().equals(log.getActionType());
            }

            return matchesSearch && matchesAction;
        });
    }

    @FXML
    private void handleClearFilters() {
        if (searchField != null) searchField.clear();
        if (filterTypeCombo != null) filterTypeCombo.setValue("All Types");
        if (filterStatusCombo != null) filterStatusCombo.setValue("All Statuses");
        if (filteredUserData != null) filteredUserData.setPredicate(p -> true);
    }

    private void switchView(VBox newView, Button activeButton) {
        dashboardView.setVisible(false); dashboardView.setManaged(false);
        usersView.setVisible(false); usersView.setManaged(false);
        approvalsView.setVisible(false); approvalsView.setManaged(false);
        logsView.setVisible(false); logsView.setManaged(false);

        newView.setVisible(true);
        newView.setManaged(true);

        btnDashboard.setStyle(null);
        btnUserManagement.setStyle(null);
        btnDoctorApprovals.setStyle(null);
        btnSystemLogs.setStyle(null);

        activeButton.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #006a61; -fx-border-color: #006a61; -fx-border-width: 0 4 0 0; -fx-font-weight: bold;");
    }

    @FXML
    private void handleSwitchToDashboard() {
        switchView(dashboardView, btnDashboard);
        refreshStats();
    }

    @FXML
    private void handleSwitchToUsers() {
        switchView(usersView, btnUserManagement);
        loadUserTable();
    }

    @FXML
    private void handleSwitchToApprovals() {
        switchView(approvalsView, btnDoctorApprovals);
        loadApprovalTable();
    }

    @FXML
    private void handleSwitchToLogs() {
        switchView(logsView, btnSystemLogs);
        loadLogTable();
    }

    private void refreshStats() {
        try {
            int totalUsers = userService.getTotalUsers();
            int doctorCount = userService.getUserCountByType("doctor");
            int patientCount = userService.getUserCountByType("patient");
            int adminCount = userService.getUserCountByType("admin");
            int pendingCount = userService.getPendingDoctorCount();
            int recentWeek = userService.getRecentRegistrations(7);
            int activeUsers = userService.getActiveUsers();

            if (statTotalUsers != null) statTotalUsers.setText(String.valueOf(totalUsers));
            if (statTotalTrend != null) statTotalTrend.setText(activeUsers + " active accounts");
            if (statDoctors != null) statDoctors.setText(String.valueOf(doctorCount));
            if (statDoctorsTrend != null) statDoctorsTrend.setText("Verified & practicing");
            if (statPatients != null) statPatients.setText(String.valueOf(patientCount));
            if (statPatientsTrend != null) statPatientsTrend.setText("Verified & active");
            if (pendingCountLabel != null) pendingCountLabel.setText(String.valueOf(pendingCount));

            int totalForDist = doctorCount + patientCount + adminCount;
            if (totalForDist > 0) {
                int doctorPct = Math.round((float) doctorCount / totalForDist * 100);
                int patientPct = Math.round((float) patientCount / totalForDist * 100);
                int adminPct = 100 - doctorPct - patientPct;

                if (distDoctorsBar != null) distDoctorsBar.setProgress(doctorPct / 100.0);
                if (distDoctorsLabel != null) distDoctorsLabel.setText("Doctors: " + doctorCount);
                if (distDoctorsPct != null) distDoctorsPct.setText(doctorPct + "%");

                if (distPatientsBar != null) distPatientsBar.setProgress(patientPct / 100.0);
                if (distPatientsLabel != null) distPatientsLabel.setText("Patients: " + patientCount);
                if (distPatientsPct != null) distPatientsPct.setText(patientPct + "%");

                if (distAdminsBar != null) distAdminsBar.setProgress(adminPct / 100.0);
                if (distAdminsLabel != null) distAdminsLabel.setText("Admins: " + adminCount);
                if (distAdminsPct != null) distAdminsPct.setText(adminPct + "%");
            }

            if (distRecent != null) distRecent.setText(recentWeek + " new registrations this week");

        } catch (SQLException e) {
            System.err.println("Stats update error: " + e.getMessage());
        }
    }

    private void loadUserTable() {
        try {
            List<User> data = userService.getAllUsersForAdmin();
            filteredUserData = new FilteredList<>(FXCollections.observableArrayList(data), p -> true);
            applyUserFilters();
            userTable.setItems(filteredUserData);
            refreshStats();
        } catch (Exception e) {
            System.err.println("Database Error: " + e.getMessage());
        }
    }

    private void loadApprovalTable() {
        try {
            List<User> data = userService.getPendingDoctors();
            approvalTable.setItems(FXCollections.observableArrayList(data));
            refreshStats();
        } catch (Exception e) {
            System.err.println("Database Error: " + e.getMessage());
        }
    }

    private void loadLogTable() {
        try {
            List<UserActivityLog> data = logService.getAllLogs();
            filteredLogData = new FilteredList<>(FXCollections.observableArrayList(data), p -> true);
            applyLogFilters();
            logTable.setItems(filteredLogData);
        } catch (Exception e) {
            System.err.println("Database Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefreshLogs() {
        loadLogTable();
    }

    private void handleApprove(User u) {
        try {
            userService.updateStatus(u.getId(), "approved");
            loadApprovalTable();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to approve: " + e.getMessage());
        }
    }

    private void handleDeny(User u) {
        try {
            userService.updateStatus(u.getId(), "denied");
            loadApprovalTable();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to deny: " + e.getMessage());
        }
    }

    private void handleDelete(User u) {
        userService.delete(u.getId());
        loadUserTable();
    }

    @FXML
    private void handleExportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Users to CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("users_export_" + LocalDate.now() + ".csv");
        File file = fileChooser.showSaveDialog(btnExportCSV.getScene().getWindow());

        if (file != null) {
            try {
                userService.exportUsersToCSV(file);
                showAlert(Alert.AlertType.INFORMATION, "Export Complete", "Users exported to: " + file.getName());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Export Failed", "Error: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleToggleTheme() {
        ThemeManager.toggleTheme();
        String newTheme = ThemeManager.getCurrentTheme();
        try {
            User currentUser = org.docbook.util.AppState.getCurrentUser();
            if (currentUser != null) {
                currentUser.setThemePreference(newTheme);
                userService.saveThemePreference(currentUser.getId(), newTheme);
            }
        } catch (Exception e) {
            System.err.println("Failed to save theme preference: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleLogout() {
        try {
            Parent loginRoot = javafx.fxml.FXMLLoader.load(getClass().getResource("/fxml/auth/login.fxml"));
            javafx.stage.Stage stage = (javafx.stage.Stage) mainPane.getScene().getWindow();
            javafx.scene.Scene loginScene = new javafx.scene.Scene(loginRoot);
            ThemeManager.applyTheme(loginScene);
            stage.setScene(loginScene);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
