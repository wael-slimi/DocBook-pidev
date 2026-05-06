package org.docbook.controllers.doctor;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.stage.Stage;
import org.docbook.entities.records.Document;
import org.docbook.entities.records.DossierMedical;
import org.docbook.services.medical.DocumentService;
import org.docbook.services.medical.DossierMedicalService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

public class StatsController {

    @FXML private PieChart genderChart;
    @FXML private BarChart<String, Number> docTypeChart;
    @FXML private AreaChart<String, Number> growthChart;
    @FXML private BarChart<String, Number> monthlyActivityChart;
    @FXML private BarChart<String, Number> ageGroupChart;
    
    @FXML private Label totalPatientsLabel;
    @FXML private Label totalDocsLabel;
    @FXML private Label totalPrescriptionsLabel;

    private final DossierMedicalService dossierService = new DossierMedicalService();
    private final DocumentService documentService = new DocumentService();

    @FXML
    public void initialize() {
        loadSummary();
        loadGenderData();
        loadDocumentTypeData();
        loadGrowthData();
        loadMonthlyActivityData();
        loadAgeGroupData();
        applyCustomStyles();
    }

    private void loadSummary() {
        List<DossierMedical> patients = dossierService.getAll();
        List<Document> docs = documentService.getAll();
        long prescriptions = docs.stream().filter(d -> "ordonnance".equals(d.getTypeDocument())).count();

        totalPatientsLabel.setText(String.valueOf(patients.size()));
        totalDocsLabel.setText(String.valueOf(docs.size()));
        totalPrescriptionsLabel.setText(String.valueOf(prescriptions));
    }

    private void applyCustomStyles() {
        int i = 0;
        String[] colors = {"#3b82f6", "#8b5cf6", "#10b981", "#f43f5e", "#f59e0b"};
        for (PieChart.Data data : genderChart.getData()) {
            data.getNode().setStyle("-fx-pie-color: " + colors[i % colors.length] + ";");
            i++;
        }
    }

    private void loadGenderData() {
        List<DossierMedical> patients = dossierService.getAll();
        long males = patients.stream().filter(p -> "M".equalsIgnoreCase(p.getGenre())).count();
        long females = patients.stream().filter(p -> "F".equalsIgnoreCase(p.getGenre())).count();

        genderChart.getData().add(new PieChart.Data("Hommes (" + males + ")", males));
        genderChart.getData().add(new PieChart.Data("Femmes (" + females + ")", females));
    }

    private void loadDocumentTypeData() {
        List<Document> docs = documentService.getAll();
        Map<String, Long> counts = docs.stream()
                .collect(Collectors.groupingBy(Document::getTypeDocument, Collectors.counting()));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        counts.forEach((type, count) -> series.getData().add(new XYChart.Data<>(type, count)));
        docTypeChart.getData().add(series);
    }

    private void loadGrowthData() {
        List<DossierMedical> patients = dossierService.getAll();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nouveaux Dossiers (Mensuel)");

        for (Month month : Month.values()) {
            String monthName = month.getDisplayName(TextStyle.SHORT, Locale.FRENCH);
            long count = patients.stream()
                    .filter(p -> p.getDateCreation() != null && p.getDateCreation().getMonth() == month)
                    .count();
            series.getData().add(new XYChart.Data<>(monthName, count));
        }
        growthChart.getData().add(series);
    }

    private void loadMonthlyActivityData() {
        List<Document> docs = documentService.getAll();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Documents Générés");

        for (Month month : Month.values()) {
            String monthName = month.getDisplayName(TextStyle.SHORT, Locale.FRENCH);
            long count = docs.stream()
                    .filter(d -> d.getDateCreation() != null && d.getDateCreation().getMonth() == month)
                    .count();
            series.getData().add(new XYChart.Data<>(monthName, count));
        }
        monthlyActivityChart.getData().add(series);
    }

    private void loadAgeGroupData() {
        List<DossierMedical> patients = dossierService.getAll();
        int currentYear = LocalDate.now().getYear();

        long kids = patients.stream().filter(p -> p.getDateNaissance() != null && (currentYear - p.getDateNaissance().getYear()) < 18).count();
        long adults = patients.stream().filter(p -> {
            if (p.getDateNaissance() == null) return false;
            int age = currentYear - p.getDateNaissance().getYear();
            return age >= 18 && age < 60;
        }).count();
        long seniors = patients.stream().filter(p -> p.getDateNaissance() != null && (currentYear - p.getDateNaissance().getYear()) >= 60).count();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Répartition par Âge");
        series.getData().add(new XYChart.Data<>("Enfants (0-17)", kids));
        series.getData().add(new XYChart.Data<>("Adultes (18-59)", adults));
        series.getData().add(new XYChart.Data<>("Seniors (60+)", seniors));
        ageGroupChart.getData().add(series);
    }

    @FXML
    private void goBack(ActionEvent event) {
    }

    @FXML
    private void openDoctorDashboard(ActionEvent event) {
        loadView(event, "/fxml/doctor/DoctorDashboard.fxml", "Espace Médecin");
    }

    @FXML
    private void openDocumentView(ActionEvent event) {
        loadView(event, "/fxml/records/DocumentView.fxml", "Documents");
    }

    @FXML
    private void openMapView(ActionEvent event) {
        loadView(event, "/fxml/records/MapView.fxml", "Carte");
    }

    @FXML
    private void handleProfileNav(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/profile.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Mon Profil");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void logout(ActionEvent event) {
        org.docbook.util.AppState.setCurrentUser(null);
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/auth/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("DocBook - Connexion");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadView(ActionEvent event, String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}