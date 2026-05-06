package org.docbook.controllers.patient;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.docbook.entities.records.DossierMedical;
import org.docbook.entities.users.Patient;
import org.docbook.entities.users.User;
import org.docbook.services.medical.DocumentService;
import org.docbook.services.medical.DossierMedicalService;
import org.docbook.services.users.PatientService;
import org.docbook.util.AppState;
import org.docbook.util.ThemeManager;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PatientController {

    @FXML
    private VBox dossiersContainer;

    @FXML
    private StackPane contentArea;

    @FXML
    private Label dossiersCountLabel;

    @FXML
    private Label prescriptionsCountLabel;

    @FXML
    private Label appointmentsCountLabel;

    @FXML
    private Label bloodTypeLabel;

    @FXML
    private Label allergiesLabel;

    @FXML
    private Label patientNameText;

    @FXML
    private Label profileNameText;

    @FXML
    private ImageView sidebarProfileImage;

    @FXML
    private Text sidebarInitials;

    @FXML
    private VBox appointmentsContainer;

    private final DossierMedicalService dossierService = new DossierMedicalService();
    private final DocumentService documentService = new DocumentService();
    private final PatientService patientService = new PatientService();

    /**
     * Initialise le tableau de bord patient.
     */
    @FXML
    public void initialize() {
        loadPatientData();
        if (dossiersContainer != null) {
            loadDossierCards();
        }
    }

    /**
     * Load real patient data from database.
     */
    private void loadPatientData() {
        User currentUser = AppState.getCurrentUser();
        if (currentUser == null) return;

        String name = currentUser.getName() != null ? currentUser.getName() : "Patient";
        if (patientNameText != null) patientNameText.setText(name);
        if (profileNameText != null) profileNameText.setText(name);

        loadSidebarProfile(currentUser);

        if (dossiersCountLabel != null) {
            List<DossierMedical> dossiers = dossierService.getByPatientId(currentUser.getId());
            dossiersCountLabel.setText(String.valueOf(dossiers.size()));
        }

        if (prescriptionsCountLabel != null) {
            int count = 0;
            List<DossierMedical> dossiers = dossierService.getByPatientId(currentUser.getId());
            for (DossierMedical d : dossiers) {
                count += documentService.getByDossierId(d.getId()).size();
            }
            prescriptionsCountLabel.setText(String.valueOf(count));
        }

        if (appointmentsCountLabel != null) {
            appointmentsCountLabel.setText("0");
        }

        if (bloodTypeLabel != null) {
            bloodTypeLabel.setText("-");
        }

        if (allergiesLabel != null) {
            allergiesLabel.setText("Aucune");
        }
    }

    private void loadSidebarProfile(User user) {
        if (user == null) return;

        if (sidebarInitials != null && user.getName() != null) {
            String[] parts = user.getName().split(" ");
            if (parts.length >= 2) {
                sidebarInitials.setText(parts[0].substring(0, 1) + parts[1].substring(0, 1));
            } else if (parts.length == 1 && parts[0].length() > 0) {
                sidebarInitials.setText(parts[0].substring(0, Math.min(2, parts[0].length())));
            }
        }

        if (sidebarProfileImage != null) {
            String photoPath = user.getAvatarUrl();
            if (photoPath != null && !photoPath.isEmpty()) {
                File file = new File(photoPath);
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString());
                    sidebarProfileImage.setImage(image);
                    if (sidebarInitials != null) sidebarInitials.setVisible(false);
                } else {
                    sidebarProfileImage.setImage(null);
                    if (sidebarInitials != null) sidebarInitials.setVisible(true);
                }
            } else {
                sidebarProfileImage.setImage(null);
                if (sidebarInitials != null) sidebarInitials.setVisible(true);
            }
        }
    }

    /**
     * Navigation vers la recherche de docteurs.
     */
    @FXML
    private void handleSearchNav(ActionEvent event) {
        loadView(event, "/fxml/patient/search_doctors.fxml", "Recherche Médecins");
    }

    /**
     * Retourne à la vue d'accueil du dashboard.
     */
    @FXML
    private void showHome(ActionEvent event) {
        loadView(event, "/fxml/patient/PatientDashboard.fxml", "Dashboard");
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
            Scene scene = new Scene(root);
            ThemeManager.applyTheme(scene);
            stage.setScene(scene);
            stage.setTitle("DocBook - Authentification");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleProfileNav(ActionEvent event) {
        loadView(event, "/fxml/profile.fxml", "Mon Profil");
    }

    @FXML
    private void openDocuments(ActionEvent event) {
        loadView(event, "/fxml/patient/PatientDocuments.fxml", "Mes Documents");
    }

    @FXML
    private void openAppointments(ActionEvent event) {
        loadView(event, "/fxml/records/AppointmentView.fxml", "Mes Rendez-vous");
    }

    @FXML
    private void openMap(ActionEvent event) {
        loadView(event, "/fxml/records/MapView.fxml", "Carte");
    }

    /**
     * Charge une vue FXML et remplace la scène courante.
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