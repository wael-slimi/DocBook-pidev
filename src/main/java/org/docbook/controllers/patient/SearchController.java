package org.docbook.controllers.patient;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.docbook.entities.users.Doctor;
import org.docbook.services.users.UserService;

import java.net.URL;
import java.util.ResourceBundle;

public class SearchController implements Initializable {

    @FXML private TextField nameSearchField;
    @FXML private TextField priceSearchField;
    @FXML private TableView<Doctor> doctorTable;

    @FXML private TableColumn<Doctor, String> colName;
    @FXML private TableColumn<Doctor, String> colEmail;
    @FXML private TableColumn<Doctor, String> colRole;
    @FXML private TableColumn<Doctor, Double> colPrice;

    // This handles the database connection for you!
    private final UserService userService = new UserService();
    private final ObservableList<Doctor> doctorList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. Setup Column Mapping
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("specialty"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("consultationFee"));

        // 2. Load Data from Service
        loadDoctorData();

        // 3. Setup Filtering Logic
        FilteredList<Doctor> filteredData = new FilteredList<>(doctorList, p -> true);

        nameSearchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters(filteredData));
        priceSearchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters(filteredData));

        // 4. Setup Sorting
        SortedList<Doctor> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(doctorTable.comparatorProperty());

        doctorTable.setItems(sortedData);
    }

    private void loadDoctorData() {
        doctorList.clear();
        // The SQL logic is inside this method in UserService.java
        doctorList.addAll(userService.getAllDoctors());
    }

    private void applyFilters(FilteredList<Doctor> filteredData) {
        filteredData.setPredicate(doctor -> {
            // Name Filter
            String nameFilter = nameSearchField.getText() == null ? "" : nameSearchField.getText().toLowerCase().trim();
            boolean matchesName = doctor.getName().toLowerCase().contains(nameFilter);

            // Price Filter
            String priceFilter = priceSearchField.getText() == null ? "" : priceSearchField.getText().trim();
            boolean matchesPrice = true;
            if (!priceFilter.isEmpty()) {
                try {
                    double maxPrice = Double.parseDouble(priceFilter);
                    matchesPrice = doctor.getConsultationFee() <= maxPrice;
                } catch (NumberFormatException e) {
                    matchesPrice = true; // Skip filtering if input is invalid
                }
            }

            return matchesName && matchesPrice;
        });
    }
}