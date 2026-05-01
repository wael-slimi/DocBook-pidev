package org.docbook.controllers.admin;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.docbook.entities.UserActivityLog;
import org.docbook.services.UserActivityLogService;

import java.util.List;

public class ActivityLogController {

    @FXML private TableView<UserActivityLog> logTable;
    @FXML private TableColumn<UserActivityLog, Integer> colId;
    @FXML private TableColumn<UserActivityLog, String> colUser, colAction, colDetails, colDate;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> actionFilter;
    @FXML private ComboBox<String> typeFilter;

    private final UserActivityLogService logService = new UserActivityLogService();

    @FXML
    public void initialize() {
        if (logTable == null) return;

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUser.setCellValueFactory(new PropertyValueFactory<>("userName"));
        colAction.setCellValueFactory(new PropertyValueFactory<>("actionType"));
        colDetails.setCellValueFactory(new PropertyValueFactory<>("details"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        colAction.setCellFactory(column -> new TableCell<UserActivityLog, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    setText(item);
                    if ("LOGIN".equals(item)) setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
                    else if ("LOGOUT".equals(item)) setStyle("-fx-text-fill: #64748b; -fx-font-weight: bold;");
                    else if ("ACCOUNT_CREATED".equals(item)) setStyle("-fx-text-fill: #3b82f6; -fx-font-weight: bold;");
                    else if ("ACCOUNT_DELETED".equals(item)) setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    else setStyle("-fx-text-fill: #e5e7eb;");
                }
            }
        });

        actionFilter.getItems().addAll("All", "LOGIN", "LOGOUT", "PROFILE_UPDATE", "PASSWORD_CHANGE", "ACCOUNT_CREATED", "ACCOUNT_DELETED");
        actionFilter.setValue("All");

        typeFilter.getItems().addAll("All", "doctor", "patient", "admin");
        typeFilter.setValue("All");

        actionFilter.setOnAction(e -> refreshData());
        typeFilter.setOnAction(e -> refreshData());
        searchField.textProperty().addListener((obs, old, newVal) -> refreshData());

        refreshData();
    }

    private void refreshData() {
        String search = searchField.getText();
        String action = actionFilter.getValue();
        String dtype = typeFilter.getValue();

        List<UserActivityLog> logs = logService.searchLogs(search, action, dtype);
        logTable.setItems(FXCollections.observableArrayList(logs));
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        actionFilter.setValue("All");
        typeFilter.setValue("All");
        refreshData();
    }

    @FXML
    private void handleClearLogs() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "This will permanently delete ALL activity logs. Continue?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                logService.clearAllLogs();
                refreshData();
            }
        });
    }
}
