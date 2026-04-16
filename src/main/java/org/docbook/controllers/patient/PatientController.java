package org.docbook.controllers.patient;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
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

    @FXML
    private StackPane contentArea; // Matches fx:id in PatientDashboard.fxml

    private final DossierMedicalService dossierService = new DossierMedicalService();
    private final DocumentService documentService = new DocumentService();

    /**
     * Initialise le tableau de bord patient.
     */
    @FXML
    public void initialize() {
        // Only load cards if we are currently looking at the dashboard home
        if (dossiersContainer != null) {
            loadDossierCards();
        }
    }

    /**
     * Navigation vers la recherche de docteurs.
     * Injecte la vue dans le contentArea (StackPane).
     */
    @FXML
    private void handleSearchNav() {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/fxml/patient/search_doctors.fxml"));
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("Error loading search_doctors.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Retourne à la vue d'accueil du dashboard (Bonjour !).
     */
    @FXML
    private void showHome(ActionEvent event) {
        try {
            // Reload the dashboard shell to reset the content area
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/patient/PatientDashboard.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Récupère tous les dossiers et construit dynamiquement les cartes d'affichage.
     */
    private void loadDossierCards() {
        dossiersContainer.getChildren().clear();

        if (AppState.getCurrentUser() != null) {
            int myId = AppState.getCurrentUser().getId();
            List<DossierMedical> myDossiers = dossierService.getByPatientId(myId);

            for (DossierMedical d : myDossiers) {
                int docCount = documentService.getByDossierId(d.getId()).size();
                dossiersContainer.getChildren().add(createDossierCard(d, docCount));
            }
        }
    }

    /**
     * Construit une carte visuelle pour un dossier.
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

    private void openDocumentsForDossier(ActionEvent event, int dossierId) {
        AppState.setSelectedDossierId(dossierId);
        loadView(event, "/fxml/doctor/PrescriptionView.fxml", "Mes Documents");
    }

    @FXML
    private void openPrescriptions(ActionEvent event) {
        AppState.setSelectedDossierId(null);
        loadView(event, "/fxml/doctor/PrescriptionView.fxml", "Mes Prescriptions");
    }

    @FXML
    private void logout(ActionEvent event) {
        AppState.setCurrentUser(null);
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/auth/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("DocBook - Authentification");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleProfileNav() {
        try {
            // Path must match exactly!
            // Use "/fxml/profile.fxml" if it's in resources/fxml/
            Parent view = FXMLLoader.load(getClass().getResource("/fxml/profile.fxml"));

            if (contentArea != null) {
                contentArea.getChildren().setAll(view);
            } else {
                System.err.println("Critical Error: contentArea StackPane is null!");
            }
        } catch (IOException e) {
            System.err.println("Error: Could not load profile.fxml. check path and casing.");
            e.printStackTrace();
        }
    }

    /**
     * Charge une vue FXML et remplace la scène courante (utilisée pour logout/prescriptions).
     */
    private void loadView(ActionEvent event, String fxmlPath, String title) {
        try {
            java.net.URL resource = getClass().getResource(fxmlPath);
            if (resource == null) return;

            Parent root = FXMLLoader.load(resource);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}