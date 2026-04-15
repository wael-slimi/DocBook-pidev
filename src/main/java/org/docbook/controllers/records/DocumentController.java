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
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.docbook.entities.records.Document;
import org.docbook.entities.records.DossierMedical;
import org.docbook.services.medical.DocumentService;
import org.docbook.services.medical.DossierMedicalService;

import java.io.File;
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
        if (!validateDocumentForm()) return;

        try {
            Document doc = new Document();
            doc.setDossierMedicalId(dossierIdCombo.getValue().getId());
            doc.setTitre(titreField.getText());
            doc.setTypeDocument(typeCombo.getValue());
            doc.setDateDocument(dateField.getValue());
            doc.setContenu(contenuField.getText());
            doc.setFichierPath(selectedFilePath);

            documentService.add(doc);
            statusText.setText("Ajouté avec succès !");
            statusText.setStyle("-fx-fill: #10b981;");
            loadDocuments();
            clearForm();
        } catch (Exception e) {
            dossierIdError.setText("Dossier invalide !");
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
     * Deconnecte l'utilisateur et charge la vue d'accueil.
     */
    @FXML
    private void logout(ActionEvent event) {
        loadView(event, "/fxml/MainView.fxml", "DocBook");
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
}


