package org.docbook.controllers.patient;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.docbook.entities.users.Doctor;
import org.docbook.services.users.UserService;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SearchController implements Initializable {

    @FXML private TextField nameSearchField;
    @FXML private TextField specialtySearchField;
    @FXML private TextField priceSearchField;
    @FXML private TableView<Doctor> doctorTable;

    @FXML private TableColumn<Doctor, String> colName;
    @FXML private TableColumn<Doctor, String> colEmail;
    @FXML private TableColumn<Doctor, String> colRole;
    @FXML private TableColumn<Doctor, Double> colPrice;

    private final UserService userService = new UserService();
    private final ObservableList<Doctor> doctorList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. Setup Column Mapping
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("specialty"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("consultationFee"));

        // 2. Load Data
        loadDoctorData();

        // 3. Setup Filtering Logic
        FilteredList<Doctor> filteredData = new FilteredList<>(doctorList, p -> true);

        // Listen to all three fields
        nameSearchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters(filteredData));
        specialtySearchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters(filteredData));
        priceSearchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters(filteredData));

        // 4. Setup Sorting
        SortedList<Doctor> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(doctorTable.comparatorProperty());

        doctorTable.setItems(sortedData);
    }

    private void loadDoctorData() {
        doctorList.clear();
        doctorList.addAll(userService.getAllDoctors());
    }

    private void applyFilters(FilteredList<Doctor> filteredData) {
        filteredData.setPredicate(doctor -> {
            // Name Filter
            String nameFilter = nameSearchField.getText() == null ? "" : nameSearchField.getText().toLowerCase().trim();
            boolean matchesName = doctor.getName().toLowerCase().contains(nameFilter);

            // Specialty Filter (The new requirement)
            String specFilter = specialtySearchField.getText() == null ? "" : specialtySearchField.getText().toLowerCase().trim();
            boolean matchesSpecialty = doctor.getSpecialty() != null && doctor.getSpecialty().toLowerCase().contains(specFilter);

            // Price Filter
            String priceFilter = priceSearchField.getText() == null ? "" : priceSearchField.getText().trim();
            boolean matchesPrice = true;
            if (!priceFilter.isEmpty()) {
                try {
                    double maxPrice = Double.parseDouble(priceFilter);
                    matchesPrice = doctor.getConsultationFee() <= maxPrice;
                } catch (NumberFormatException e) {
                    matchesPrice = true;
                }
            }

            return matchesName && matchesSpecialty && matchesPrice;
        });
    }

    @FXML
    private void goToDashboard(ActionEvent event) throws IOException {
        switchView(event, "/fxml/patient/PatientDashboard.fxml");
    }

    @FXML
    private void goToDocuments(ActionEvent event) throws IOException {
        switchView(event, "/fxml/records/DocumentView.fxml");
    }

    @FXML
    private void goToStats(ActionEvent event) throws IOException {
        switchView(event, "/fxml/doctor/StatsView.fxml");
    }

    @FXML
    private void goToMap(ActionEvent event) throws IOException {
        switchView(event, "/fxml/records/MapView.fxml");
    }

    @FXML
    private void goToProfile(ActionEvent event) throws IOException {
        switchView(event, "/fxml/profile.fxml");
    }

    private void switchView(ActionEvent event, String fxmlPath) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    @FXML
    private void logout(ActionEvent event) throws IOException {
        org.docbook.util.AppState.setCurrentUser(null);
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/auth/login.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    @FXML
    private void showHome(ActionEvent event) throws IOException {
        switchView(event, "/fxml/patient/PatientDashboard.fxml");
    }
}