package org.docbook.controllers.patient;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.docbook.entities.records.DossierMedical;
import org.docbook.services.medical.DocumentService;
import org.docbook.services.medical.DossierMedicalService;
import org.docbook.util.AppState;

import java.io.IOException;
import java.util.List;


public class PatientController {

    @FXML
    private VBox dossiersContainer;

    private final DossierMedicalService dossierMedicalService = new DossierMedicalService();
    private final DocumentService documentService = new DocumentService();

    /**
     * Initialise le tableau de bord patient en chargeant les cartes des dossiers.
     */
    @FXML
    public void initialize() {
        loadDossierCards();
    }

    /**
     * Recupere tous les dossiers et construit dynamiquement les cartes d'affichage.
     */
    private void loadDossierCards() {
        dossiersContainer.getChildren().clear();
        List<DossierMedical> dossiers = dossierMedicalService.getAll();

        for (DossierMedical dossier : dossiers) {
            int documentCount = documentService.getByDossierId(dossier.getId()).size();
            dossiersContainer.getChildren().add(createDossierCard(dossier, documentCount));
        }
    }

    /**
     * Construit une carte visuelle pour un dossier avec un bouton vers ses documents.
     */
    private VBox createDossierCard(DossierMedical dossier, int documentCount) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-radius: 12;");

        Text title = new Text("Dossier " + dossier.getNumeroDossier());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: #1e293b;");

        Text patientInfo = new Text("Patient: " + dossier.getPatientPrenom() + " " + dossier.getPatientNom());
        patientInfo.setStyle("-fx-fill: #475569;");

        Text docsInfo = new Text("Documents disponibles: " + documentCount);
        docsInfo.setStyle("-fx-fill: #64748b;");

        HBox actions = new HBox();
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button seeDocsBtn = new Button("Voir documents");
        seeDocsBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold;");
        seeDocsBtn.setOnAction(event -> openDocumentsForDossier(event, dossier.getId()));

        actions.getChildren().addAll(spacer, seeDocsBtn);
        card.getChildren().addAll(title, patientInfo, docsInfo, actions);
        return card;
    }

    /**
     * Memorise le dossier selectionne puis ouvre la vue des documents associes.
     */
    private void openDocumentsForDossier(ActionEvent event, int dossierId) {
        AppState.setSelectedDossierId(dossierId);
        loadView(event, "/fxml/doctor/PrescriptionView.fxml", "Mes Documents");
    }

    /**
     * Ouvre la vue des prescriptions du patient.
     */
    @FXML
    private void openPrescriptions(ActionEvent event) {
        AppState.setSelectedDossierId(null);
        loadView(event, "/fxml/doctor/PrescriptionView.fxml", "Mes Prescriptions");
    }

    /**
     * Deconnecte l'utilisateur et retourne vers la vue principale.
     */
    @FXML
    private void logout(ActionEvent event) {
        loadView(event, "/fxml/MainView.fxml", "DocBook");
    }

    /**
     * Charge une vue FXML et remplace la scene courante.
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


