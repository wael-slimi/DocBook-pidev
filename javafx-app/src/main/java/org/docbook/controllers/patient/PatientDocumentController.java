package org.docbook.controllers.patient;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.docbook.entities.records.Document;
import org.docbook.entities.records.DossierMedical;
import org.docbook.entities.users.User;
import org.docbook.services.medical.DossierMedicalService;
import org.docbook.services.medical.DocumentService;
import org.docbook.util.AppState;

import java.util.List;
import java.util.stream.Collectors;

public class PatientDocumentController {

    @FXML private TableView<Document> documentTable;
    @FXML private TableColumn<Document, String> titreCol;
    @FXML private TableColumn<Document, String> typeCol;
    @FXML private TableColumn<Document, String> dateCol;
    @FXML private TableColumn<Document, String> fileCol;

    private final DocumentService documentService = new DocumentService();
    private final DossierMedicalService dossierService = new DossierMedicalService();

    @FXML
    public void initialize() {
        titreCol.setCellValueFactory(new PropertyValueFactory<>("titre"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("typeDocument"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateDocument"));
        fileCol.setCellValueFactory(new PropertyValueFactory<>("fichierPath"));
        
        loadPatientDocuments();
    }

    private void loadPatientDocuments() {
        User currentUser = AppState.getCurrentUser();
        if (currentUser == null) return;
        
        List<DossierMedical> patientDossiers = dossierService.getByPatientId(currentUser.getId());
        List<Document> allDocuments = documentService.getAll();
        
        List<Document> patientDocs = allDocuments.stream()
            .filter(doc -> patientDossiers.stream()
                .anyMatch(d -> d.getId() == doc.getDossierMedicalId()))
            .collect(Collectors.toList());
        
        ObservableList<Document> obsList = FXCollections.observableArrayList(patientDocs);
        documentTable.setItems(obsList);
    }

    @FXML
    private void goToDashboard(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/patient/PatientDashboard.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Dashboard Patient");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToSearch(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/patient/search_doctors.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Recherche Médecins");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToMap(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/records/MapView.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Carte");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleProfileNav(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/profile.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Mon Profil");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void logout(ActionEvent event) {
        AppState.setCurrentUser(null);
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/auth/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("DocBook - Connexion");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}