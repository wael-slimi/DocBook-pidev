package org.docbook.controllers.records;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.docbook.controllers.ai.SnapshotController;
import org.docbook.entities.records.Document;
import org.docbook.entities.records.DossierMedical;
import org.docbook.services.medical.DocumentService;
import org.docbook.services.medical.DossierMedicalService;
import org.docbook.util.GeminiService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;

public class DocumentController {

    @FXML private TableView<Document> documentTable;

    @FXML private TableColumn<Document, String> titreCol;
    @FXML private TableColumn<Document, String> typeCol;
    @FXML private TableColumn<Document, LocalDate> dateCol;
    @FXML private TableColumn<Document, String> fileCol;

    @FXML private ComboBox<DossierMedical> dossierIdCombo;
    @FXML private TextField titreField;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private DatePicker dateField;
    @FXML private TextArea contenuField;
    @FXML private javafx.scene.text.Text dossierIdError;
    @FXML private javafx.scene.text.Text titreError;
    @FXML private javafx.scene.text.Text typeError;
    @FXML private javafx.scene.text.Text dateError;
    @FXML private javafx.scene.text.Text contenuError;

    @FXML private javafx.scene.text.Text fileNameText;
    @FXML private javafx.scene.text.Text statusText;

    private String selectedFilePath = null;
    private final DocumentService documentService = new DocumentService();
    private final DossierMedicalService dossierMedicalService = new DossierMedicalService();

    /**
     * Initialise les composants, les listes deroulantes et le chargement des documents.
     */
    @FXML
    public void initialize() {
        typeCombo.setItems(FXCollections.observableArrayList("consultation", "ordonnance", "certificat", "imagerie", "autre"));
        loadDossierIds();
        

        titreCol.setCellValueFactory(new PropertyValueFactory<>("titre"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("typeDocument"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateDocument"));
        fileCol.setCellValueFactory(new PropertyValueFactory<>("fichierPath"));

        documentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                DossierMedical selectedDossier = dossierIdCombo.getItems()
                        .stream()
                        .filter(d -> d.getId() == newSel.getDossierMedicalId())
                        .findFirst()
                        .orElse(null);
                dossierIdCombo.setValue(selectedDossier);
                titreField.setText(newSel.getTitre());
                typeCombo.setValue(newSel.getTypeDocument());
                dateField.setValue(newSel.getDateDocument());
                contenuField.setText(newSel.getContenu());
                
                selectedFilePath = newSel.getFichierPath();
                if (selectedFilePath != null && !selectedFilePath.isEmpty()) {
                    File f = new File(selectedFilePath);
                    fileNameText.setText(f.getName());
                } else {
                    fileNameText.setText("Aucun fichier");
                }
            }
        });

        loadDocuments();
    }

    /**
     * Charge les dossiers medicaux disponibles dans la combobox.
     */
    private void loadDossierIds() {
        List<DossierMedical> dossiers = dossierMedicalService.getAll();
        dossierIdCombo.setItems(FXCollections.observableArrayList(dossiers));
        dossierIdCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(DossierMedical dossier) {
                if (dossier == null) return "";
                return dossier.getPatientPrenom() + " " + dossier.getPatientNom() + " (Dossier " + dossier.getNumeroDossier() + ")";
            }

            @Override
            public DossierMedical fromString(String string) {
                return null;
            }
        });
    }

    /**
     * Charge tous les documents dans le tableau principal.
     */
    private void loadDocuments() {
        List<Document> list = documentService.getAll();
        ObservableList<Document> obsList = FXCollections.observableArrayList(list);
        documentTable.setItems(obsList);
    }

    /**
     * Permet de joindre un fichier local et de l'enregistrer dans le dossier uploads.
     */
    @FXML
    private void uploadFile(ActionEvent event) {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Sélectionner un fichier");
        fileChooser.getExtensionFilters().addAll(
            new javafx.stage.FileChooser.ExtensionFilter("Documents & Images", "*.pdf", "*.png", "*.jpg", "*.jpeg", "*.docx")
        );
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            try {
                File uploadDir = new File("uploads");
                if (!uploadDir.exists()) uploadDir.mkdir();
                
                String uniqueName = System.currentTimeMillis() + "_" + selectedFile.getName();
                File destFile = new File(uploadDir, uniqueName);
                
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                
                selectedFilePath = destFile.getPath();
                fileNameText.setText(selectedFile.getName());
            } catch (Exception e) {
                e.printStackTrace();
                statusText.setText("Erreur d'upload");
                statusText.setStyle("-fx-fill: #ef4444;");
            }
        }
    }

    /**
     * Verifie les champs du formulaire document avant enregistrement.
     */
    private boolean validateDocumentForm() {
        boolean isValid = true;
        
        if (dossierIdCombo.getValue() == null) {
            dossierIdError.setText("Veuillez sélectionner un dossier.");
            isValid = false;
        } else {
            dossierIdError.setText("");
        }
        
        if (titreField.getText() == null || titreField.getText().trim().isEmpty()) {
            titreError.setText("Le titre est obligatoire.");
            isValid = false;
        } else {
            titreError.setText("");
        }
        
        if (typeCombo.getValue() == null) {
            typeError.setText("Veuillez sélectionner un type.");
            isValid = false;
        } else {
            typeError.setText("");
        }
        
        if (dateField.getValue() == null) {
            dateError.setText("Date requise.");
            isValid = false;
        } else {
            dateError.setText("");
        }
        
        if (contenuField.getText() == null || contenuField.getText().trim().isEmpty()) {
            contenuError.setText("Contenu obligatoire.");
            isValid = false;
        } else {
            contenuError.setText("");
        }

        return isValid;
    }

    /**
     * Cree un nouveau document a partir des donnees saisies.
     */
    @FXML
    private void addDocument(ActionEvent event) {
        if (!validateDocumentForm()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Formulaire incomplet");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez remplir tous les champs obligatoires pour ajouter un document.");
            alert.showAndWait();
            return;
        }

        try {
            Document doc = new Document();
            doc.setDossierMedicalId(dossierIdCombo.getValue().getId());
            doc.setTitre(titreField.getText());
            doc.setTypeDocument(typeCombo.getValue());
            doc.setDateDocument(dateField.getValue());
            doc.setContenu(contenuField.getText());
            doc.setFichierPath(selectedFilePath);

            documentService.add(doc);
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Succès");
            successAlert.setHeaderText(null);
            successAlert.setContentText("Document ajouté avec succès !");
            successAlert.showAndWait();
            loadDocuments();
            clearForm();
        } catch (Exception e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Erreur");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("Erreur lors de l'ajout du document: " + e.getMessage());
            errorAlert.showAndWait();
        }
    }

    /**
     * Met a jour le document selectionne avec les nouvelles valeurs.
     */
    @FXML
    private void updateDocument(ActionEvent event) {
        Document selected = documentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusText.setText("Sélectionnez un document !");
            statusText.setStyle("-fx-fill: #ef4444;");
            return;
        }
        if (!validateDocumentForm()) return;

        try {
            selected.setDossierMedicalId(dossierIdCombo.getValue().getId());
            selected.setTitre(titreField.getText());
            selected.setTypeDocument(typeCombo.getValue());
            selected.setDateDocument(dateField.getValue());
            selected.setContenu(contenuField.getText());
            selected.setFichierPath(selectedFilePath);

            documentService.update(selected);
            statusText.setText("Mise à jour réussie !");
            statusText.setStyle("-fx-fill: #3b82f6;");
            loadDocuments();
        } catch (Exception e) {
            dossierIdError.setText("Dossier invalide !");
        }
    }

    /**
     * Supprime le document selectionne dans le tableau.
     */
    @FXML
    private void deleteDocument(ActionEvent event) {
        Document selected = documentTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        documentService.delete(selected.getId());
        statusText.setText("Document supprimé.");
        statusText.setStyle("-fx-fill: #ef4444;");
        loadDocuments();
        clearForm();
    }

    /**
     * Reinitialise le formulaire document et les messages associes.
     */
    @FXML
    private void clearForm() {
        dossierIdCombo.setValue(null);
        titreField.clear();
        typeCombo.setValue(null);
        dateField.setValue(null);
        contenuField.clear();
        selectedFilePath = null;
        fileNameText.setText("Aucun fichier");
        documentTable.getSelectionModel().clearSelection();
        
        dossierIdError.setText("");
        titreError.setText("");
        typeError.setText("");
        dateError.setText("");
        contenuError.setText("");
        statusText.setText("");
    }

    /**
     * Retourne vers le tableau de bord medecin.
     */
    @FXML
    private void openDoctorDashboard(ActionEvent event) {
        loadView(event, "/fxml/doctor/DoctorDashboard.fxml", "Espace Médecin");
    }

    /**
     * Ouvre la page d'historique d'adherence.
     */
    @FXML
    private void openAdherenceHistory(ActionEvent event) {
        loadView(event, "/fxml/patient/AdherenceHistoryView.fxml", "Historique d'Adhérence");
    }

    /**
     * Ouvre la vue des statistiques.
     */
    @FXML
    private void openStatsView(ActionEvent event) {
        loadView(event, "/fxml/doctor/StatsView.fxml", "Statistiques");
    }

    /**
     * Ouvre la vue de la carte.
     */
    @FXML
    private void openMapView(ActionEvent event) {
        loadView(event, "/fxml/records/MapView.fxml", "Carte");
    }

    /**
     * Ouvre le profil utilisateur.
     */
    @FXML
    private void handleProfileNav(ActionEvent event) {
        loadView(event, "/fxml/profile.fxml", "Mon Profil");
    }

    /**
     * Deconnecte l'utilisateur et charge la vue d'accueil.
     */
    @FXML
    private void logout(ActionEvent event) {
        org.docbook.util.AppState.setCurrentUser(null);
        loadView(event, "/fxml/auth/login.fxml", "DocBook - Connexion");
    }

    @FXML
    private void generatePatientSnapshot(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/doctor/DoctorDashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Espace Médecin");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void runDiagnosticAssistant(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/doctor/DoctorDashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Espace Médecin");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToDashboard(ActionEvent event) {
        loadView(event, "/fxml/doctor/DoctorDashboard.fxml", "Espace Médecin");
    }

    @FXML
    private void goToSearch(ActionEvent event) {
        loadView(event, "/fxml/patient/search_doctors.fxml", "Recherche Médecins");
    }

    @FXML
    private void goToMap(ActionEvent event) {
        loadView(event, "/fxml/records/MapView.fxml", "Carte");
    }

    /**
     * Charge une vue FXML et met a jour la scene courante.
     */
    private void loadView(ActionEvent event, String fxmlPath, String title) {
        try {
            java.net.URL resource = getClass().getResource(fxmlPath);

            if (resource == null) {
                System.err.println("FXML NOT FOUND: " + fxmlPath);
                System.err.println("Check case-sensitivity and folder structure in src/main/resources");
                return;
            }

            Parent root = FXMLLoader.load(resource);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            System.err.println("FXML LOAD ERROR: The file exists, but there is an error INSIDE the FXML or its Controller.");
            e.printStackTrace();
        }
    }

    /**
     * Exporte les documents en fichier CSV.
     */
    @FXML
    private void exportDocumentsCSV(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter documents en CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("documents_" + java.time.LocalDate.now() + ".csv");
        
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        java.io.File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            try {
                List<Document> docs = documentService.getAll();
                FileWriter writer = new FileWriter(file);
                writer.write("ID,Titre,Type,Date,Dossier ID,Fichier,Contenu,Date Création\n");
                
                for (Document doc : docs) {
                    writer.write(String.format("%d,%s,%s,%s,%d,%s,%s,%s\n",
                        doc.getId(),
                        escapeCSV(doc.getTitre()),
                        escapeCSV(doc.getTypeDocument()),
                        doc.getDateDocument() != null ? doc.getDateDocument().toString() : "",
                        doc.getDossierMedicalId(),
                        escapeCSV(doc.getFichierPath()),
                        escapeCSV(doc.getContenu()),
                        doc.getDateCreation() != null ? doc.getDateCreation().toString() : ""));
                }
                writer.close();
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export réussi");
                alert.setHeaderText(null);
                alert.setContentText("Documents exportés avec succès vers: " + file.getName());
                alert.showAndWait();
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur d'export");
                alert.setHeaderText(null);
                alert.setContentText("Erreur lors de l'export: " + e.getMessage());
                alert.showAndWait();
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

    /**
     * Filtre les documents en fonction de la recherche.
     */
    @FXML
    private void filterDocuments(KeyEvent event) {
        String query = searchField.getText();
        if (query == null || query.isEmpty()) {
            loadDocuments();
            return;
        }
        
        final String searchQuery = query.toLowerCase();
        List<Document> allDocs = documentService.getAll();
        List<Document> filtered = allDocs.stream()
            .filter(d -> (d.getTitre() != null && d.getTitre().toLowerCase().contains(searchQuery)) ||
                        (d.getTypeDocument() != null && d.getTypeDocument().toLowerCase().contains(searchQuery)) ||
                        (d.getContenu() != null && d.getContenu().toLowerCase().contains(searchQuery)))
            .collect(java.util.stream.Collectors.toList());
        
        ObservableList<Document> obsList = FXCollections.observableArrayList(filtered);
        documentTable.setItems(obsList);
    }

    /**
     * Génère automatiquement un titre pour le document via IA.
     */
    @FXML
    private void runAutoTitleAI(ActionEvent event) {
        String content = contenuField.getText();
        if (content == null || content.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucune donnée");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez d'abord saisir du contenu dans le champ 'Contenu'.");
            alert.showAndWait();
            return;
        }
        
        Alert loadingAlert = new Alert(Alert.AlertType.INFORMATION);
        loadingAlert.setTitle("Titre Auto IA");
        loadingAlert.setHeaderText(null);
        loadingAlert.setContentText("Génération du titre en cours...");
        loadingAlert.show();
        
        new Thread(() -> {
            String result = GeminiService.analyzeText(GeminiService.getAutoTitlePrompt(), content);
            
            javafx.application.Platform.runLater(() -> {
                loadingAlert.close();
                
                // Parse result - expected format: "Titre | Type"
                String[] parts = result.split("\\|");
                if (parts.length >= 1) {
                    titreField.setText(parts[0].trim());
                }
                if (parts.length >= 2 && typeCombo.getItems() != null) {
                    String typeStr = parts[1].trim().toLowerCase();
                    for (String type : typeCombo.getItems()) {
                        if (type.toLowerCase().contains(typeStr)) {
                            typeCombo.setValue(type);
                            break;
                        }
                    }
                }
                
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Titre généré");
                successAlert.setHeaderText(null);
                successAlert.setContentText("Titre suggestions: " + result);
                successAlert.showAndWait();
            });
        }).start();
    }

    /**
     * Vérifie la sécurité des prescriptions via IA.
     */
    @FXML
    private void runSafetyGuardAI(ActionEvent event) {
        if (dossierIdCombo.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucun dossier");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner un patient pour vérifier les interactions médicamenteuses.");
            alert.showAndWait();
            return;
        }
        
        String prescription = contenuField.getText();
        if (prescription == null || prescription.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucune prescription");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez saisir une prescription à vérifier.");
            alert.showAndWait();
            return;
        }
        
        // Get patient history
        DossierMedical patient = dossierIdCombo.getValue();
        String patientHistory = "Patient: " + patient.getPatientNom() + " " + patient.getPatientPrenom() + 
                               "\nHistorique: " + (patient.getRemarques() != null ? patient.getRemarques() : "Aucun");
        
        Alert loadingAlert = new Alert(Alert.AlertType.INFORMATION);
        loadingAlert.setTitle("Vérification Sécurité");
        loadingAlert.setHeaderText(null);
        loadingAlert.setContentText("Analyse des interactions en cours...");
        loadingAlert.show();
        
        new Thread(() -> {
            String result = GeminiService.analyzeText(GeminiService.getSafetyGuardPrompt(patientHistory), prescription);
            
            javafx.application.Platform.runLater(() -> {
                loadingAlert.close();
                
                Alert resultAlert = new Alert(Alert.AlertType.INFORMATION);
                resultAlert.setTitle("Résultat de la vérification");
                resultAlert.setHeaderText("Analyse de sécurité:");
                resultAlert.setContentText(result);
                resultAlert.showAndWait();
            });
        }).start();
    }

    /**
     * Génère une version simplifiée pour le patient via IA.
     */
    @FXML
    private void runPatientFriendlyAI(ActionEvent event) {
        String content = contenuField.getText();
        if (content == null || content.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucune donnée");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez d'abord sélectionner un document ou saisir du contenu.");
            alert.showAndWait();
            return;
        }
        
        Alert loadingAlert = new Alert(Alert.AlertType.INFORMATION);
        loadingAlert.setTitle("Version Patient");
        loadingAlert.setHeaderText(null);
        loadingAlert.setContentText("Génération de la version simplifiée...");
        loadingAlert.show();
        
        new Thread(() -> {
            String result = GeminiService.analyzeText(GeminiService.getPatientFriendlyPrompt(), content);
            
            javafx.application.Platform.runLater(() -> {
                loadingAlert.close();
                
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ai/SnapshotView.fxml"));
                    Parent root = loader.load();
                    
                    SnapshotController controller = loader.getController();
                    controller.setData("Version Patient", result);
                    
                    Stage stage = new Stage();
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setTitle("Version Patient");
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


