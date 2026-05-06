package org.docbook.controllers.patient;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.docbook.entities.records.Document;
import org.docbook.entities.records.DossierMedical;
import org.docbook.entities.users.User;
import org.docbook.controllers.ai.SnapshotController;
import org.docbook.services.medical.DossierMedicalService;
import org.docbook.services.medical.DocumentService;
import org.docbook.util.AppState;
import org.docbook.util.GeminiService;

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

    /**
     * Explique le document sélectionné en langage simple pour le patient.
     */
    @FXML
    private void explainWithAI(ActionEvent event) {
        Document selected = documentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucun document");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner un document à expliquer.");
            alert.showAndWait();
            return;
        }
        
        String content = selected.getTitre() + "\n\n" + 
                        "Type: " + selected.getTypeDocument() + "\n" +
                        "Contenu: " + (selected.getContenu() != null ? selected.getContenu() : "Aucun contenu");
        
        Alert loadingAlert = new Alert(Alert.AlertType.INFORMATION);
        loadingAlert.setTitle("Explication IA");
        loadingAlert.setHeaderText(null);
        loadingAlert.setContentText("Génération de l'explication en langage simple...");
        loadingAlert.show();
        
        new Thread(() -> {
            String explanation = GeminiService.analyzeText(
                "Tu es un assistant médical. Explique ce document médical en langage simple et compréhensible " +
                "pour un patient sans formation médicale. Sois rassurant et clair. Réponds en français.",
                content);
            
            javafx.application.Platform.runLater(() -> {
                loadingAlert.close();
                
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ai/SnapshotView.fxml"));
                    Parent root = loader.load();
                    
                    SnapshotController controller = loader.getController();
                    controller.setData("Explication pour Patient", explanation);
                    
                    Stage stage = new Stage();
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setTitle("Explication du Document");
                    stage.setScene(new Scene(root));
                    stage.showAndWait();
                } catch (Exception e) {
                    e.printStackTrace();
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Erreur");
                    errorAlert.setHeaderText(null);
                    errorAlert.setContentText("Erreur: " + e.getMessage());
                    errorAlert.showAndWait();
                }
            });
        }).start();
    }
}