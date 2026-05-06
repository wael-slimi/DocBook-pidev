package org.docbook.controllers.doctor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.docbook.controllers.ai.SnapshotController;
import org.docbook.entities.records.DossierMedical;
import org.docbook.entities.records.Document;
import org.docbook.services.medical.DossierMedicalService;
import org.docbook.services.medical.DocumentService;
import org.docbook.util.AppState;
import org.docbook.util.GeminiService;
import org.docbook.util.QrCodeService;

import java.io.FileWriter;
import java.util.List;
import java.util.stream.Collectors;

public class PatientListController {

    @FXML private ListView<DossierMedical> patientList;
    @FXML private TextField searchField;
    @FXML private Text selectedPatientName;
    @FXML private Text dossierNumText;
    @FXML private Text dossierEmailText;
    @FXML private Text dossierTelText;
    @FXML private Text dossierAdresseText;
    @FXML private Text statusText;

    private DossierMedical selectedPatient;
    private final DossierMedicalService dossierService = new DossierMedicalService();
    private final DocumentService documentService = new DocumentService();
    private final QrCodeService qrCodeService = new QrCodeService();

    @FXML
    public void initialize() {
        // Set cell factory to display patient names
        patientList.setCellFactory(param -> new javafx.scene.control.ListCell<DossierMedical>() {
            @Override
            protected void updateItem(DossierMedical item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(4);
                    vbox.setStyle("-fx-padding: 12;");
                    
                    javafx.scene.text.Text nameText = new javafx.scene.text.Text(item.getPatientPrenom() + " " + item.getPatientNom());
                    nameText.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-fill: #1e293b;");
                    
                    javafx.scene.text.Text dossierText = new javafx.scene.text.Text("Dossier: " + (item.getNumeroDossier() != null ? item.getNumeroDossier() : ""));
                    dossierText.setStyle("-fx-font-size: 11px; -fx-fill: #64748b;");
                    
                    vbox.getChildren().addAll(nameText, dossierText);
                    setGraphic(vbox);
                }
            }
        });
        
        loadPatients();
        
        patientList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedPatient = newVal;
                updatePatientDetails();
            }
        });
    }

    private void loadPatients() {
        List<DossierMedical> patients = dossierService.getAll();
        ObservableList<DossierMedical> obsList = FXCollections.observableArrayList(patients);
        patientList.setItems(obsList);
    }

    private void updatePatientDetails() {
        if (selectedPatient != null) {
            selectedPatientName.setText(selectedPatient.getPatientPrenom() + " " + selectedPatient.getPatientNom());
            dossierNumText.setText(selectedPatient.getNumeroDossier() != null ? selectedPatient.getNumeroDossier() : "-");
            dossierEmailText.setText(selectedPatient.getEmail() != null ? selectedPatient.getEmail() : "-");
            dossierTelText.setText(selectedPatient.getTelephone() != null ? selectedPatient.getTelephone() : "-");
            dossierAdresseText.setText(selectedPatient.getAdresse() != null ? selectedPatient.getAdresse() : "-");
        }
    }

    @FXML
    private void searchPatients(javafx.scene.input.KeyEvent event) {
        String query = searchField.getText();
        if (query == null || query.isEmpty()) {
            loadPatients();
            return;
        }
        
        final String searchQuery = query.toLowerCase();
        List<DossierMedical> allPatients = dossierService.getAll();
        List<DossierMedical> filtered = allPatients.stream()
            .filter(p -> (p.getPatientNom() != null && p.getPatientNom().toLowerCase().contains(searchQuery)) ||
                        (p.getPatientPrenom() != null && p.getPatientPrenom().toLowerCase().contains(searchQuery)) ||
                        (p.getNumeroDossier() != null && p.getNumeroDossier().toLowerCase().contains(searchQuery)))
            .collect(Collectors.toList());
        
        ObservableList<DossierMedical> obsList = FXCollections.observableArrayList(filtered);
        patientList.setItems(obsList);
    }

    @FXML
    private void generateSnapshot(ActionEvent event) {
        if (selectedPatient == null) {
            showWarning("Aucun patient sélectionné", "Veuillez sélectionner un patient dans la liste.");
            return;
        }
        
        String data = "Patient: " + selectedPatient.getPatientPrenom() + " " + selectedPatient.getPatientNom() + 
                     "\nDossier: " + selectedPatient.getNumeroDossier() +
                     "\nRemarques: " + (selectedPatient.getRemarques() != null ? selectedPatient.getRemarques() : "Aucune");
        
        statusText.setText("Génération du snapshot en cours...");
        
        new Thread(() -> {
            String snapshot = GeminiService.analyzeText(GeminiService.getSnapshotPrompt(), data);
            
            javafx.application.Platform.runLater(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ai/SnapshotView.fxml"));
                    Parent root = loader.load();
                    
                    SnapshotController controller = loader.getController();
                    controller.setData(selectedPatient.getPatientPrenom() + " " + selectedPatient.getPatientNom(), snapshot);
                    
                    Stage stage = new Stage();
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setTitle("Snapshot Médical");
                    stage.setScene(new Scene(root));
                    stage.showAndWait();
                    
                    statusText.setText("Snapshot généré.");
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Erreur", "Erreur lors de l'ouverture du snapshot: " + e.getMessage());
                }
            });
        }).start();
    }

    @FXML
    private void runDiagnosticAssistant(ActionEvent event) {
        if (selectedPatient == null) {
            showWarning("Aucun patient sélectionné", "Veuillez sélectionner un patient dans la liste.");
            return;
        }
        
        statusText.setText("Analyse en cours...");
        
        new Thread(() -> {
            String advice = GeminiService.analyzeText(GeminiService.getDiagnosticAssistantPrompt(), 
                selectedPatient.getRemarques() != null ? selectedPatient.getRemarques() : "Aucun historique");
            
            javafx.application.Platform.runLater(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ai/SnapshotView.fxml"));
                    Parent root = loader.load();
                    
                    SnapshotController controller = loader.getController();
                    controller.setData("Assistant Diagnostic - " + selectedPatient.getPatientPrenom(), advice);
                    
                    Stage stage = new Stage();
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setTitle("Assistant Diagnostic");
                    stage.setScene(new Scene(root));
                    stage.showAndWait();
                    
                    statusText.setText("Analyse terminée.");
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Erreur", "Erreur: " + e.getMessage());
                }
            });
        }).start();
    }

    @FXML
    private void generateQRCode(ActionEvent event) {
        if (selectedPatient == null) {
            showWarning("Aucun patient sélectionné", "Veuillez sélectionner un patient dans la liste.");
            return;
        }
        
        try {
            javafx.scene.image.Image qrImage = qrCodeService.generatePatientQrImage(selectedPatient, 200);
            List<Document> docs = documentService.getByDossierId(selectedPatient.getId());
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ai/PatientQrView.fxml"));
            Parent root = loader.load();
            
            org.docbook.controllers.ai.PatientQrController controller = loader.getController();
            controller.setData(selectedPatient, docs, qrImage);
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("QR Code Patient");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            statusText.setText("QR Code généré.");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Erreur lors de la génération du QR code: " + e.getMessage());
        }
    }

    @FXML
    private void exportPatientsCSV(ActionEvent event) {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Exporter patients en CSV");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("patients_" + java.time.LocalDate.now() + ".csv");
        
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        java.io.File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            try {
                List<DossierMedical> patients = dossierService.getAll();
                FileWriter writer = new FileWriter(file);
                writer.write("Numéro Dossier,Nom,Prénom,Date Naissance,Genre,Email,Téléphone,Adresse,Date Création\n");
                
                for (DossierMedical p : patients) {
                    writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s\n",
                        escapeCSV(p.getNumeroDossier()),
                        escapeCSV(p.getPatientNom()),
                        escapeCSV(p.getPatientPrenom()),
                        escapeCSV(p.getGenre()),
                        escapeCSV(p.getEmail()),
                        escapeCSV(p.getTelephone()),
                        escapeCSV(p.getAdresse()),
                        p.getDateCreation() != null ? p.getDateCreation().toString() : ""));
                }
                writer.close();
                
                showInfo("Export réussi", "Patients exportés vers: " + file.getName());
            } catch (Exception e) {
                e.printStackTrace();
                showError("Erreur d'export", "Erreur: " + e.getMessage());
            }
        }
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void showWarning(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void goToDashboard(ActionEvent event) {
        loadView(event, "/fxml/doctor/DoctorDashboard.fxml", "Espace Médecin");
    }

    @FXML
    private void openDocumentView(ActionEvent event) {
        loadView(event, "/fxml/records/DocumentView.fxml", "Documents");
    }

    @FXML
    private void openStatsView(ActionEvent event) {
        loadView(event, "/fxml/doctor/StatsView.fxml", "Statistiques");
    }

    @FXML
    private void handleProfileNav(ActionEvent event) {
        loadView(event, "/fxml/profile.fxml", "Mon Profil");
    }

    @FXML
    private void logout(ActionEvent event) {
        AppState.setCurrentUser(null);
        loadView(event, "/fxml/auth/login.fxml", "DocBook - Connexion");
    }

    private void loadView(ActionEvent event, String fxmlPath, String title) {
        try {
            java.net.URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.err.println("FXML NOT FOUND: " + fxmlPath);
                return;
            }
            Parent root = FXMLLoader.load(resource);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}