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
import org.docbook.entities.records.Appointment;
import org.docbook.entities.records.DossierMedical;
import org.docbook.entities.users.Patient;
import org.docbook.entities.users.User;
import org.docbook.services.AppointmentService;
import org.docbook.services.medical.DocumentService;
import org.docbook.services.medical.DossierMedicalService;
import org.docbook.services.users.PatientService;
import org.docbook.util.AppState;
import org.docbook.util.ThemeManager;
import org.docbook.util.WeatherWidget;
import org.docbook.controllers.patient.ChatWidget;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    private VBox weatherWidgetContainer;

    @FXML
    private VBox chatWidgetContainer;
    private ImageView sidebarProfileImage;

    @FXML
    private Text sidebarInitials;

    @FXML
    private VBox appointmentsContainer;

    private final DossierMedicalService dossierService = new DossierMedicalService();
    private final DocumentService documentService = new DocumentService();
    private final PatientService patientService = new PatientService();
    private final AppointmentService appointmentService = new AppointmentService();

    /**
     * Initialise le tableau de bord patient.
     */
    @FXML
    public void initialize() {
        loadPatientData();
        if (dossiersContainer != null) {
            loadDossierCards();
        }
        
        if (weatherWidgetContainer != null) {
            WeatherWidget weatherWidget = new WeatherWidget();
            weatherWidgetContainer.getChildren().add(weatherWidget);
        }
        
        if (chatWidgetContainer != null) {
            ChatWidget chatWidget = new ChatWidget();
            chatWidgetContainer.getChildren().add(chatWidget);
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

        // Get real data from database
        if (dossiersCountLabel != null || prescriptionsCountLabel != null) {
            List<DossierMedical> dossiers = dossierService.getByPatientId(currentUser.getId());
            if (dossiersCountLabel != null) {
                dossiersCountLabel.setText(String.valueOf(dossiers.size()));
            }
            if (prescriptionsCountLabel != null) {
                int count = 0;
                for (DossierMedical d : dossiers) {
                    count += documentService.getByDossierId(d.getId()).size();
                }
                prescriptionsCountLabel.setText(String.valueOf(count));
            }
        }

        // Get upcoming appointments
        if (appointmentsCountLabel != null) {
            try {
                List<Appointment> appointments = appointmentService.getByPatientId(currentUser.getId());
                long upcoming = appointments.stream()
                    .filter(a -> a.getScheduledAt() != null && 
                                a.getScheduledAt().isAfter(LocalDateTime.now()))
                    .count();
                appointmentsCountLabel.setText(String.valueOf(upcoming));
            } catch (Exception e) {
                appointmentsCountLabel.setText("0");
            }
        }
        
        // Populate upcoming appointments list
        loadUpcomingAppointments();

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
    
    private void loadUpcomingAppointments() {
        User currentUser = AppState.getCurrentUser();
        if (currentUser == null || appointmentsContainer == null) return;
        
        appointmentsContainer.getChildren().clear();
        
        try {
            List<Appointment> appointments = appointmentService.getByPatientId(currentUser.getId());
            List<Appointment> upcoming = appointments.stream()
                .filter(a -> a.getScheduledAt() != null && 
                           a.getScheduledAt().isAfter(LocalDateTime.now()))
                .sorted((a1, a2) -> a1.getScheduledAt().compareTo(a2.getScheduledAt()))
                .limit(5)
                .collect(Collectors.toList());
            
            if (upcoming.isEmpty()) {
                javafx.scene.text.Text emptyText = new javafx.scene.text.Text("Aucun rendez-vous prévu");
                emptyText.setStyle("-fx-font-size: 13px; -fx-fill: #94a3b8;");
                appointmentsContainer.getChildren().add(emptyText);
            } else {
                for (Appointment apt : upcoming) {
                    VBox aptBox = createAppointmentCard(apt);
                    appointmentsContainer.getChildren().add(aptBox);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading appointments: " + e.getMessage());
        }
    }
    
    private VBox createAppointmentCard(Appointment apt) {
        VBox box = new VBox(4);
        box.setStyle("-fx-padding: 10; -fx-background-color: #f8fafc; -fx-background-radius: 8;");
        
        javafx.scene.text.Text doctorText = new javafx.scene.text.Text("Dr. " + apt.getDoctor());
        doctorText.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-fill: #1e293b;");
        
        String dateStr = apt.getScheduledAt() != null ? 
            apt.getScheduledAt().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")) : "N/A";
        javafx.scene.text.Text dateText = new javafx.scene.text.Text(dateStr);
        dateText.setStyle("-fx-font-size: 12px; -fx-fill: #64748b;");
        
        String status = apt.getStatus() != null ? apt.getStatus() : "";
        Label statusBadge = new Label(apt.getStatus());
        String statusStyle = "-fx-font-size: 10px; -fx-padding: 4 8; " +
            ("CONFIRMED".equalsIgnoreCase(status) ? "-fx-background-color: #22c55e; -fx-text-fill: white;" :
             "PENDING".equalsIgnoreCase(status) ? "-fx-background-color: #f59e0b; -fx-text-fill: white;" :
             "-fx-background-color: #94a3b8; -fx-text-fill: white;");
        statusBadge.setStyle(statusStyle);
        
        box.getChildren().addAll(doctorText, dateText, statusBadge);
        return box;
    }
}