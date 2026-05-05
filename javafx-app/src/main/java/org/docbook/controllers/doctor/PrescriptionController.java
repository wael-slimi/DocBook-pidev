package org.docbook.controllers.doctor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.docbook.controllers.ai.PatientQrController;
import org.docbook.entities.records.Document;
import org.docbook.entities.records.DossierMedical;
import org.docbook.services.medical.DocumentService;
import org.docbook.util.AppState;
import org.docbook.util.QrCodeService;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PrescriptionController {

    @FXML private TableView<Document> prescriptionTable;
    @FXML private TableColumn<Document, String> dateCol;
    @FXML private TableColumn<Document, String> titleCol;
    @FXML private TableColumn<Document, String> typeCol;
    @FXML private TableColumn<Document, String> actionsCol;
    @FXML private TableColumn<Document, String> contentCol;
    @FXML private TableColumn<Document, Void> fileCol;
    @FXML private TableColumn<Document, String> actionCol;

    private final DocumentService documentService = new DocumentService();
    private final QrCodeService qrCodeService = new QrCodeService();

    @FXML
    public void initialize() {
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateDocument"));
        titleCol.setCellValueFactory(new PropertyValueFactory<>("titre"));
        contentCol.setCellValueFactory(new PropertyValueFactory<>("contenu"));

        fileCol.setCellFactory(param -> new javafx.scene.control.TableCell<>() {
            private final javafx.scene.control.Button btn = new javafx.scene.control.Button("Ouvrir");
            {
                btn.setStyle("-fx-background-color: #f8fafc; -fx-text-fill: #3b82f6; -fx-border-color: #cbd5e1; -fx-border-radius: 5;");
                btn.setOnAction(event -> {
                    Document doc = getTableView().getItems().get(getIndex());
                    if (doc.getFichierPath() != null && !doc.getFichierPath().isEmpty()) {
                        File file = new File(doc.getFichierPath());
                        if (file.exists()) {
                            try {
                                java.awt.Desktop.getDesktop().open(file);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }

            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });

        if (AppState.getCurrentUser() != null) {
            int userId = AppState.getCurrentUser().getId();
            loadPrescriptions(userId);
        }
    }

    private void loadPrescriptions(int doctorId) {
        List<Document> list = documentService.getAll();
        list.removeIf(doc -> !"consultation".equals(doc.getTypeDocument()) && !"ordonnance".equals(doc.getTypeDocument()));
        
        ObservableList<Document> obsList = FXCollections.observableArrayList(list);
        prescriptionTable.setItems(obsList);
    }

    @FXML
    private void goBack(ActionEvent event) {
        loadView(event, "/fxml/patient/PatientDashboard.fxml", "Espace Patient");
    }

    @FXML
    private void showQrPopup(Document doc) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ai/PatientQrView.fxml"));
            Parent root = loader.load();
            
            PatientQrController controller = loader.getController();
            
            javafx.scene.image.Image qrImage = qrCodeService.generateDocumentQrImage(doc, 360);
            
            DossierMedical dossier = new DossierMedical();
            dossier.setId(doc.getDossierMedicalId());
            List<Document> docs = documentService.getByDossierId(doc.getDossierMedicalId());
            
            controller.setData(dossier, docs, qrImage);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("QR Code - " + doc.getTitre());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}