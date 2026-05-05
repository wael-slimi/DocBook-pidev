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
import javafx.scene.layout.StackPane;
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
                numDossierField.setText(newSel.getNumeroDossier());
                nomField.setText(newSel.getPatientNom());
                prenomField.setText(newSel.getPatientPrenom());
                if (newSel.getDateNaissance() != null) {
                    dateNaissField.setValue(newSel.getDateNaissance());
                } else {
                    dateNaissField.setValue(null);
                }
                genreField.setText(newSel.getGenre());
                emailPatientField.setText(newSel.getEmail());
                telephoneField.setText(newSel.getTelephone());
                adresseField.setText(newSel.getAdresse());
                remarquesField.setText(newSel.getRemarques());
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

        if (numDossierField.getText() == null || numDossierField.getText().trim().isEmpty()) { numDossierError.setText("Champ obligatoire."); isValid = false; } else { numDossierError.setText(""); }

        if (nomField.getText() == null || !nomField.getText().matches("[a-zA-ZÀ-ÿ\\s]+")) { nomError.setText("Nom invalide."); isValid = false; } else { nomError.setText(""); }

        if (prenomField.getText() == null || !prenomField.getText().matches("[a-zA-ZÀ-ÿ\\s]+")) { prenomError.setText("Prénom invalide."); isValid = false; } else { prenomError.setText(""); }

        if (dateNaissField.getValue() == null) { dateNaissanceError.setText("Veuillez choisir une date."); isValid = false; } else { dateNaissanceError.setText(""); }

        if (genreField.getText() == null || !genreField.getText().matches("[MFmf]")) { genreError.setText("M ou F uniquement."); isValid = false; } else { genreError.setText(""); }

        if (emailPatientField.getText() == null || !emailPatientField.getText().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) { emailError.setText("Email invalide."); isValid = false; } else { emailError.setText(""); }

        if (telephoneField.getText() == null || !telephoneField.getText().matches("\\d{8,}")) { telephoneError.setText("Numéro invalide (min 8 chiffres)."); isValid = false; } else { telephoneError.setText(""); }

        if (adresseField.getText() == null || adresseField.getText().trim().isEmpty()) { adresseError.setText("Adresse obligatoire."); isValid = false; } else { adresseError.setText(""); }

        return isValid;
    }

    /**
     * Ajoute un nouveau dossier medical depuis le formulaire.
     */
    @FXML
    private void addDossierPanel(ActionEvent event) {
        if (!validateDossierForm()) return;

        DossierMedical dm = new DossierMedical();
        dm.setNumeroDossier(numDossierField.getText());
        dm.setPatientNom(nomField.getText());
        dm.setPatientPrenom(prenomField.getText());
        dm.setDateNaissance(dateNaissField.getValue() != null ? dateNaissField.getValue() : java.time.LocalDate.now());
        dm.setGenre(genreField.getText());
        dm.setEmail(emailPatientField.getText());
        dm.setTelephone(telephoneField.getText());
        dm.setAdresse(adresseField.getText());
        dm.setRemarques(remarquesField.getText());

        dossierService.add(dm);
        loadPatients();
        clearDossierPanel(event);
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
        numDossierField.clear();
        nomField.clear();
        prenomField.clear();
        dateNaissField.setValue(null);
        genreField.clear();
        emailPatientField.clear();
        telephoneField.clear();
        adresseField.clear();
        remarquesField.clear();
        patientTable.getSelectionModel().clearSelection();

        numDossierError.setText("");
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
     * Ouvre la vue de la carte.
     */
    @FXML
    private void openMapView(ActionEvent event) {
        loadView(event, "/fxml/records/MapView.fxml", "Carte");
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
            statusText.setText("Sélectionnez un patient d'abord.");
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
            statusText.setText("Sélectionnez un patient d'abord.");
            return;
        }
        String remarks = remarquesField.getText();
        if (remarks == null || remarks.isEmpty()) {
            statusText.setText("Saisissez des remarques pour l'assistant AI.");
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


