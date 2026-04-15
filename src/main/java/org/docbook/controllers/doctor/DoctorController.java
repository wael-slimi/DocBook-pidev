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
import javafx.stage.Stage;
import org.docbook.entities.records.DossierMedical;
import org.docbook.services.medical.DossierMedicalService;

import java.io.IOException;
import java.util.List;

public class DoctorController {

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
    @FXML private TextField numDossierField;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private DatePicker dateNaissanceField;
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
                    dateNaissanceField.setValue(newSel.getDateNaissance());
                } else {
                    dateNaissanceField.setValue(null);
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

    /**
     * Charge la liste des dossiers medicaux dans le tableau principal.
     */
    private void loadPatients() {
        List<DossierMedical> list = dossierService.getAll();
        ObservableList<DossierMedical> obsList = FXCollections.observableArrayList(list);
        patientTable.setItems(obsList);
        totalPatientsLabel.setText(String.valueOf(list.size()));
    }

    /**
     * Verifie les champs du formulaire dossier avant creation ou mise a jour.
     */
    private boolean validateDossierForm() {
        boolean isValid = true;
        
        if (numDossierField.getText() == null || numDossierField.getText().trim().isEmpty()) { numDossierError.setText("Champ obligatoire."); isValid = false; } else { numDossierError.setText(""); }
        
        if (nomField.getText() == null || !nomField.getText().matches("[a-zA-ZÀ-ÿ\\s]+")) { nomError.setText("Nom invalide."); isValid = false; } else { nomError.setText(""); }
        
        if (prenomField.getText() == null || !prenomField.getText().matches("[a-zA-ZÀ-ÿ\\s]+")) { prenomError.setText("Prénom invalide."); isValid = false; } else { prenomError.setText(""); }
        
        if (dateNaissanceField.getValue() == null) { dateNaissanceError.setText("Veuillez choisir une date."); isValid = false; } else { dateNaissanceError.setText(""); }
        
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
        dm.setDateNaissance(dateNaissanceField.getValue() != null ? dateNaissanceField.getValue() : java.time.LocalDate.now());
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
        selected.setDateNaissance(dateNaissanceField.getValue());
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
        dateNaissanceField.setValue(null);
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
     * Deconnecte l'utilisateur et retourne a la page d'accueil.
     */
    @FXML
    private void logout(ActionEvent event) {
        loadView(event, "/fxml/MainView.fxml", "DocBook");
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
}


