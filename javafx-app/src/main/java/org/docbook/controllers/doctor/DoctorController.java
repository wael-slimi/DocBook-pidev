package org.docbook.controllers.doctor;

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
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.docbook.controllers.ai.AIReportController;
import org.docbook.controllers.ai.SnapshotController;
import org.docbook.entities.records.DossierMedical;
import org.docbook.entities.users.Doctor;
import org.docbook.services.medical.DossierMedicalService;
import org.docbook.util.AppState;
import org.docbook.util.GeminiService;
import org.docbook.util.QrCodeService;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class DoctorController {

    @FXML private StackPane contentArea;

    @FXML private Label doctorNameLabel;
    @FXML private ImageView doctorAvatarImg;

    private DossierMedical selectedDossier;
    private final QrCodeService qrCodeService = new QrCodeService();
    @FXML
    private TableView<DossierMedical> patientTable;
    @FXML private ListView<String> sidebarPatientList;
    @FXML
    private TableColumn<DossierMedical, String> numCol;
    @FXML
    private TableColumn<DossierMedical, String> nameCol;
    @FXML
    private TableColumn<DossierMedical, String> firstNameCol;
    @FXML
    private TableColumn<DossierMedical, String> emailCol;

    @FXML
    private Label totalPatientsLabel;
    @FXML private Label todayConsultationsLabel;
    @FXML private Label activeDossiersLabel;
    @FXML private TextField numDossierField;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private DatePicker dateNaissField;
    @FXML private TextField genreField;
    @FXML private TextField emailPatientField;
    @FXML private TextField telephoneField;
    @FXML private TextField adresseField;
    @FXML private TextField searchField;
    @FXML private TextArea remarquesField;

    @FXML private javafx.scene.text.Text numDossierError;
    @FXML private javafx.scene.text.Text nomError;
    @FXML private javafx.scene.text.Text prenomError;
    @FXML private javafx.scene.text.Text dateNaissanceError;
    @FXML private javafx.scene.text.Text genreError;
    @FXML private javafx.scene.text.Text emailError;
    @FXML private javafx.scene.text.Text telephoneError;
    @FXML private javafx.scene.text.Text adresseError;

    @FXML
    private javafx.scene.text.Text statusText;

    private final DossierMedicalService dossierService = new DossierMedicalService();
    private final org.docbook.services.medical.DocumentService documentService = new org.docbook.services.medical.DocumentService();

    /**
     * Initialise les colonnes, la selection de ligne et le chargement initial des dossiers.
*/
    @FXML
    public void initialize() {
        // Load doctor info
        loadDoctorInfo();
        
        numCol.setCellValueFactory(new PropertyValueFactory<>("numeroDossier"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("patientNom"));
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("patientPrenom"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

patientTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                if (numDossierField != null) numDossierField.setText(newSel.getNumeroDossier());
                if (nomField != null) nomField.setText(newSel.getPatientNom());
                if (prenomField != null) prenomField.setText(newSel.getPatientPrenom());
                if (genreField != null) genreField.setText(newSel.getGenre());
                if (emailPatientField != null) emailPatientField.setText(newSel.getEmail());
                if (telephoneField != null) telephoneField.setText(newSel.getTelephone());
                if (adresseField != null) adresseField.setText(newSel.getAdresse());
                if (remarquesField != null) remarquesField.setText(newSel.getRemarques());
            }
        });

        loadPatients();
    }

    // Inside DoctorController.java
    @FXML
    private void handleProfileNav(ActionEvent event) {
        loadView(event, "/fxml/profile.fxml", "Mon Profil");
    }

    /**
     * Charge la liste des dossiers medicaux dans le tableau principal.
     */
    private void loadPatients() {
        List<DossierMedical> list = dossierService.getAll();
        ObservableList<DossierMedical> obsList = FXCollections.observableArrayList(list);
        patientTable.setItems(obsList);
        totalPatientsLabel.setText(String.valueOf(list.size()));
        
        // Update stats
        if (todayConsultationsLabel != null) {
            todayConsultationsLabel.setText(String.valueOf(list.size()));
        }
        if (activeDossiersLabel != null) {
            activeDossiersLabel.setText(String.valueOf(list.size()));
        }
        
        // Populate sidebar patient list
        if (sidebarPatientList != null) {
            ObservableList<String> patientNames = FXCollections.observableArrayList();
            for (DossierMedical p : list) {
                patientNames.add(p.getPatientPrenom() + " " + p.getPatientNom());
            }
            sidebarPatientList.setItems(patientNames);
            
            // Add click handler
            sidebarPatientList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    String selectedName = newVal;
                    for (DossierMedical p : list) {
                        String fullName = p.getPatientPrenom() + " " + p.getPatientNom();
                        if (fullName.equals(selectedName)) {
                            selectDossier(p);
                            break;
                        }
                    }
                }
            });
        }
    }

    /**
     * Charge les informations du médecin connecté.
     */
    private void loadDoctorInfo() {
        org.docbook.entities.users.User currentUser = org.docbook.util.AppState.getCurrentUser();
        if (currentUser != null && doctorNameLabel != null) {
            String name = currentUser.getName();
            if (name != null && !name.isEmpty()) {
                doctorNameLabel.setText("Dr. " + name);
            } else {
                doctorNameLabel.setText("Dr. Médecin");
            }
        }
    }

/**
     * Verifie les champs du formulaire dossier avant creation ou mise a jour.
     */
    private boolean validateDossierForm() {
        boolean isValid = true;

        if (numDossierField == null || numDossierField.getText() == null || numDossierField.getText().trim().isEmpty()) { 
            if (numDossierError != null) numDossierError.setText("Champ obligatoire."); 
            isValid = false; 
        } else { 
            if (numDossierError != null) numDossierError.setText(""); 
        }

        if (nomField == null || nomField.getText() == null || !nomField.getText().matches("[a-zA-ZÀ-ÿ\\s]+")) { 
            if (nomError != null) nomError.setText("Nom invalide."); 
            isValid = false; 
        } else { 
            if (nomError != null) nomError.setText(""); 
        }

        if (prenomField == null || prenomField.getText() == null || !prenomField.getText().matches("[a-zA-ZÀ-ÿ\\s]+")) { 
            if (prenomError != null) prenomError.setText("Prénom invalide."); 
            isValid = false; 
        } else { 
            if (prenomError != null) prenomError.setText(""); 
        }

        if (dateNaissField == null || dateNaissField.getValue() == null) { 
            if (dateNaissField != null) dateNaissField.setValue(null);
            if (dateNaissField != null) dateNaissField.setPromptText("Veuillez choisir une date.");
            isValid = false; 
        }

        if (genreField == null || genreField.getText() == null || !genreField.getText().matches("[MFmf]")) { 
            if (genreError != null) genreError.setText("M ou F uniquement."); 
            isValid = false; 
        } else { 
            if (genreError != null) genreError.setText(""); 
        }

        if (emailPatientField == null || emailPatientField.getText() == null || !emailPatientField.getText().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) { 
            if (emailError != null) emailError.setText("Email invalide."); 
            isValid = false; 
        } else { 
            if (emailError != null) emailError.setText(""); 
        }

        if (telephoneField == null || telephoneField.getText() == null || !telephoneField.getText().matches("\\d{8,}")) { 
            if (telephoneError != null) telephoneError.setText("Numéro invalide (min 8 chiffres)."); 
            isValid = false; 
        } else { 
            if (telephoneError != null) telephoneError.setText(""); 
        }

        if (adresseField == null || adresseField.getText() == null || adresseField.getText().trim().isEmpty()) { 
            if (adresseError != null) adresseError.setText("Adresse obligatoire."); 
            isValid = false; 
        } else { 
            if (adresseError != null) adresseError.setText(""); 
        }

        return isValid;
    }

/**
     * Ajoute un nouveau dossier medical depuis le formulaire.
     */
    @FXML
    private void addDossierPanel(ActionEvent event) {
        // Debug: check which fields are null
        StringBuilder missing = new StringBuilder();
        if (numDossierField == null) missing.append("numDossierField, ");
        if (nomField == null) missing.append("nomField, ");
        if (prenomField == null) missing.append("prenomField, ");
        if (dateNaissField == null) missing.append("dateNaissField, ");
        if (genreField == null) missing.append("genreField, ");
        if (emailPatientField == null) missing.append("emailPatientField, ");
        if (telephoneField == null) missing.append("telephoneField, ");
        if (adresseField == null) missing.append("adresseField, ");
        if (remarquesField == null) missing.append("remarquesField, ");
        
        if (missing.length() > 0) {
            System.err.println("Missing fields: " + missing.toString());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Champs manquants");
            alert.setContentText("Les champs suivants ne sont pas disponibles: " + missing.toString());
            alert.showAndWait();
            return;
        }
        
        if (!validateDossierForm()) return;

        try {
            DossierMedical dm = new DossierMedical();
            dm.setNumeroDossier(numDossierField.getText());
            dm.setPatientNom(nomField.getText());
            dm.setPatientPrenom(prenomField.getText());
            dm.setDateNaissance(dateNaissField.getValue() != null ? dateNaissField.getValue() : java.time.LocalDate.now());
            dm.setGenre(genreField.getText());
            dm.setEmail(emailPatientField.getText());
            dm.setTelephone(telephoneField.getText());
            dm.setAdresse(adresseField.getText());
            dm.setRemarques(remarquesField != null ? remarquesField.getText() : "");

            dossierService.add(dm);
            loadPatients();
            clearDossierPanel(event);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Patient ajouté avec succès!");
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Erreur lors de l'ajout: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Met a jour le dossier selectionne avec les valeurs du formulaire.
     */
    @FXML
    private void updateDossierPanel(ActionEvent event) {
        DossierMedical selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusText.setText("Sélectionnez un dossier !");
            statusText.setStyle("-fx-fill: #ef4444;");
            return;
        }
        if (!validateDossierForm()) return;

        selected.setNumeroDossier(numDossierField.getText());
        selected.setPatientNom(nomField.getText());
        selected.setPatientPrenom(prenomField.getText());
        selected.setDateNaissance(dateNaissField.getValue());
        selected.setGenre(genreField.getText());
        selected.setEmail(emailPatientField.getText());
        selected.setTelephone(telephoneField.getText());
        selected.setAdresse(adresseField.getText());
        selected.setRemarques(remarquesField.getText());

        dossierService.update(selected);
        loadPatients();
        statusText.setText("Mise à jour réussie");
        statusText.setStyle("-fx-fill: #10b981;");
    }

    /**
     * Vide le formulaire et reinitialise les messages d'erreur.
     */
    @FXML
    private void clearDossierPanel(ActionEvent event) {
        if (numDossierField != null) numDossierField.clear();
        if (nomField != null) nomField.clear();
        if (prenomField != null) prenomField.clear();
        if (dateNaissField != null) dateNaissField.setValue(null);
        if (genreField != null) genreField.clear();
        if (emailPatientField != null) emailPatientField.clear();
        if (telephoneField != null) telephoneField.clear();
        if (adresseField != null) adresseField.clear();
        if (remarquesField != null) remarquesField.clear();
        if (patientTable != null) patientTable.getSelectionModel().clearSelection();

        if (numDossierError != null) numDossierError.setText("");
        nomError.setText("");
        prenomError.setText("");
        dateNaissanceError.setText("");
        genreError.setText("");
        emailError.setText("");
        telephoneError.setText("");
        adresseError.setText("");
        statusText.setText("");
    }

    /**
     * Ouvre la vue de gestion des documents et prescriptions.
     */
    @FXML
    private void openDocumentView(ActionEvent event) {
        loadView(event, "/fxml/records/DocumentView.fxml", "Prescriptions et Documents");
    }

    /**
     * Ouvre la vue d'historique d'adherence.
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
     * Ouvre la vue de la liste des patients avec outils AI.
     */
    @FXML
    private void openPatientListView(ActionEvent event) {
        loadView(event, "/fxml/doctor/PatientListView.fxml", "Mes Patients");
    }

    /**
     * Ouvre la vue de la carte.
     */
    @FXML
    private void openMapView(ActionEvent event) {
        loadView(event, "/fxml/records/MapView.fxml", "Carte");
    }

    @FXML
    private void openAppointmentsView(ActionEvent event) {
        loadView(event, "/fxml/records/AppointmentView.fxml", "Rendez-vous");
    }

    @FXML
    private void openTeleconsultationsView(ActionEvent event) {
        loadView(event, "/fxml/records/TeleconsultationView.fxml", "Téléconsultations");
    }

    /**
     * Deconnecte l'utilisateur et retourne a la page d'accueil.
     */
    @FXML
    private void logout(ActionEvent event) {
        System.out.println("Session cleared. Redirecting to Login...");
        org.docbook.util.AppState.setCurrentUser(null);

        try {
            // Updated to match your exact file system path and casing
            String loginPath = "/fxml/auth/login.fxml";

            java.net.URL resource = getClass().getResource(loginPath);

            if (resource == null) {
                System.err.println("Still can't find it! Ensure the path starts with / and matches casing.");
                return;
            }

            Parent root = FXMLLoader.load(resource);
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("DocBook - Authentification");
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            System.err.println("Navigation Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Exporte la liste des patients en fichier CSV.
     */
    @FXML
    private void exportPatientsCSV(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter patients en CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("patients_" + java.time.LocalDate.now() + ".csv");
        
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        java.io.File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            try {
                List<DossierMedical> patients = dossierService.getAll();
                java.io.FileWriter writer = new java.io.FileWriter(file);
                writer.write("Numéro Dossier,Nom,Prénom,Date Naissance,Genre,Email,Téléphone,Adresse,Date Création\n");
                
                for (DossierMedical p : patients) {
                    writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                        escapeCSV(p.getNumeroDossier()),
                        escapeCSV(p.getPatientNom()),
                        escapeCSV(p.getPatientPrenom()),
                        p.getDateNaissance() != null ? p.getDateNaissance().toString() : "",
                        escapeCSV(p.getGenre()),
                        escapeCSV(p.getEmail()),
                        escapeCSV(p.getTelephone()),
                        escapeCSV(p.getAdresse()),
                        p.getDateCreation() != null ? p.getDateCreation().toString() : ""));
                }
                writer.close();
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export réussi");
                alert.setHeaderText(null);
                alert.setContentText("Patients exportés avec succès vers: " + file.getName());
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
     * Supprime le patient sélectionné après confirmation.
     */
    @FXML
    private void deleteDossierPanel(ActionEvent event) {
        DossierMedical selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucune sélection");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner un patient à supprimer.");
            alert.showAndWait();
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmer la suppression");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer le patient: " + 
            selected.getPatientPrenom() + " " + selected.getPatientNom() + "?\n\nCette action est irréversible.");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    dossierService.delete(selected.getId());
                    loadPatients();
                    clearDossierPanel(event);
                    
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Suppression réussie");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Patient supprimé avec succès.");
                    successAlert.showAndWait();
                } catch (Exception e) {
                    e.printStackTrace();
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Erreur");
                    errorAlert.setHeaderText(null);
                    errorAlert.setContentText("Erreur lors de la suppression: " + e.getMessage());
                    errorAlert.showAndWait();
                }
            }
        });
    }

    /**
     * Filtre les patients en fonction de la recherche.
     */
    @FXML
    private void searchPatients(KeyEvent event) {
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
                        (p.getNumeroDossier() != null && p.getNumeroDossier().toLowerCase().contains(searchQuery)) ||
                        (p.getEmail() != null && p.getEmail().toLowerCase().contains(searchQuery)))
            .collect(java.util.stream.Collectors.toList());
        
        ObservableList<DossierMedical> obsList = FXCollections.observableArrayList(filtered);
        patientTable.setItems(obsList);
    }

    /**
     * Affiche le QR code du patient sélectionné.
     */
    @FXML
    private void showPatientQrPopup(ActionEvent event) {
        DossierMedical selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucun patient sélectionné");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner un patient pour générer son QR code.");
            alert.showAndWait();
            return;
        }
        
        try {
            // Generate QR code using QrCodeService
            javafx.scene.image.Image qrImage = qrCodeService.generatePatientQrImage(selected, 200);
            
            // Get documents for this patient
            List<org.docbook.entities.records.Document> docs = documentService.getByDossierId(selected.getId());
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ai/PatientQrView.fxml"));
            Parent root = loader.load();
            
            org.docbook.controllers.ai.PatientQrController controller = loader.getController();
            controller.setData(selected, docs, qrImage);
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("QR Code Patient");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Erreur lors de la génération du QR code: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Génère une analyse initiale via l'IA lors de la création d'un patient.
     */
    @FXML
    private void generateInitialAnalysis(ActionEvent event) {
        String remarks = remarquesField.getText();
        if (remarks == null || remarks.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucune donnée");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez d'abord saisir des observations ou symptômes dans le champ 'Remarques'.");
            alert.showAndWait();
            return;
        }
        
        Alert loadingAlert = new Alert(Alert.AlertType.INFORMATION);
        loadingAlert.setTitle("Analyse IA");
        loadingAlert.setHeaderText(null);
        loadingAlert.setContentText("Génération de l'analyse en cours...");
        loadingAlert.show();
        
        new Thread(() -> {
            String analysis = org.docbook.util.GeminiService.analyzeText(
                "Tu es un assistant médical. Analyse les observations suivantes et génère un résumé médical structuré en français. " +
                "Format: 1) Antécédents plausibles 2) Observations actuelles 3) Recommandations.",
                remarks);
            
            javafx.application.Platform.runLater(() -> {
                loadingAlert.close();
                
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ai/SnapshotView.fxml"));
                    Parent root = loader.load();
                    
                    org.docbook.controllers.ai.SnapshotController controller = loader.getController();
                    controller.setData("Analyse Initiale", analysis);
                    
                    Stage stage = new Stage();
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setTitle("Analyse IA");
                    stage.setScene(new Scene(root));
                    stage.showAndWait();
                    
                    // Optionally auto-fill the remarques field with the analysis
                    // remarquesField.setText(remarks + "\n\n--- Analyse IA ---\n" + analysis);
                } catch (Exception e) {
                    e.printStackTrace();
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Erreur");
                    errorAlert.setHeaderText(null);
                    errorAlert.setContentText("Erreur lors de l'analyse: " + e.getMessage());
                    errorAlert.showAndWait();
                }
            });
        }).start();
    }

    /**
     * Obtient des suggestions IA basées sur les symptômes saisis.
     */
    @FXML
    private void getAISuggestions(ActionEvent event) {
        String symptoms = remarquesField.getText();
        if (symptoms == null || symptoms.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucune donnée");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez saisir des symptômes ou observations dans le champ 'Remarques' pour obtenir des suggestions.");
            alert.showAndWait();
            return;
        }
        
        Alert loadingAlert = new Alert(Alert.AlertType.INFORMATION);
        loadingAlert.setTitle("Suggestions IA");
        loadingAlert.setHeaderText(null);
        loadingAlert.setContentText("Génération des suggestions en cours...");
        loadingAlert.show();
        
        new Thread(() -> {
            String suggestions = org.docbook.util.GeminiService.analyzeText(
                "En tant que médecin assistant, suggère 3 questions importantes à poser au patient " +
                "et 2 examens complémentaires pertinents basés sur ces symptômes. Réponds de manière concise en français.",
                symptoms);
            
            javafx.application.Platform.runLater(() -> {
                loadingAlert.close();
                
                Alert resultAlert = new Alert(Alert.AlertType.INFORMATION);
                resultAlert.setTitle("Suggestions IA");
                resultAlert.setHeaderText("Basé sur les symptômes saisis:");
                resultAlert.setContentText(suggestions);
                resultAlert.showAndWait();
            });
        }).start();
    }



    /**
     * Charge une vue FXML et met a jour la fenetre courante.
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

    @FXML
    private void generatePatientSnapshot(ActionEvent event) {
        if (selectedDossier == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucun patient sélectionné");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner un patient dans la liste avant de générer un snapshot AI.");
            alert.showAndWait();
            return;
        }
        
        statusText.setText("Génération du snapshot AI...");
        new Thread(() -> {
            String data = "Remarques: " + selectedDossier.getRemarques();
            String snapshot = GeminiService.analyzeText(GeminiService.getSnapshotPrompt(), data);
            javafx.application.Platform.runLater(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ai/SnapshotView.fxml"));
                    Parent root = loader.load();
                    
                    SnapshotController controller = loader.getController();
                    controller.setData(selectedDossier.getPatientPrenom() + " " + selectedDossier.getPatientNom(), snapshot);

                    Stage stage = new Stage();
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setTitle("Snapshot Médical AI");
                    stage.setScene(new Scene(root));
                    stage.show();

                    statusText.setText("Snapshot généré.");
                } catch (IOException e) {
                    e.printStackTrace();
                    statusText.setText("Erreur d'ouverture du snapshot.");
                }
            });
        }).start();
    }

    @FXML
    private void runDiagnosticAssistant(ActionEvent event) {
        if (selectedDossier == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucun patient sélectionné");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner un patient dans la liste pour utiliser l'assistant AI.");
            alert.showAndWait();
            return;
        }
        String remarks = remarquesField.getText();
        if (remarks == null || remarks.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucune remarque");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez saisir des remarques dans le formulaire pour l'assistant AI.");
            alert.showAndWait();
            return;
        }

        statusText.setText("Analyse AI en cours...");
        new Thread(() -> {
            String advice = GeminiService.analyzeText(GeminiService.getDiagnosticAssistantPrompt(), remarks);
            javafx.application.Platform.runLater(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ai/AIReportView.fxml"));
                    Parent root = loader.load();
                    
                    AIReportController controller = loader.getController();
                    controller.setData(selectedDossier.getPatientPrenom() + " " + selectedDossier.getPatientNom(), advice);

                    Stage stage = new Stage();
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setTitle("Analyse Co-pilote AI");
                    stage.setScene(new Scene(root));
                    stage.show();

                    statusText.setText("Rapport AI prêt.");
                } catch (IOException e) {
                    e.printStackTrace();
                    statusText.setText("Erreur lors de l'ouverture du rapport.");
                }
            });
        }).start();
    }

    private void selectDossier(DossierMedical dm) {
        this.selectedDossier = dm;
        if (dm != null) {
            numDossierField.setText(dm.getNumeroDossier());
            nomField.setText(dm.getPatientNom());
            prenomField.setText(dm.getPatientPrenom());
            emailPatientField.setText(dm.getEmail());
            telephoneField.setText(dm.getTelephone());
            adresseField.setText(dm.getAdresse());
            remarquesField.setText(dm.getRemarques());
            genreField.setText(dm.getGenre());
        }
    }
}


