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
import javafx.stage.Stage;
import org.docbook.entities.records.Document;
import org.docbook.services.medical.DocumentService;
import org.docbook.util.AppState;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PrescriptionController {

    @FXML
    private TableView<Document> prescriptionTable;
    @FXML
    private TableColumn<Document, String> dateCol;
    @FXML
    private TableColumn<Document, String> titleCol;
    @FXML
    private TableColumn<Document, String> contentCol;
    @FXML
    private TableColumn<Document, Void> fileCol;
    @FXML
    private TableColumn<Document, String> actionCol;

    private final DocumentService documentService = new DocumentService();

    /**
     * Initialise le tableau des documents patient et configure l'ouverture des fichiers joints.
     */
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
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Document doc = getTableView().getItems().get(getIndex());
                    if (doc.getFichierPath() != null && !doc.getFichierPath().isEmpty()) {
                        setGraphic(btn);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });


        loadPrescriptions();
    }

    /**
     * Charge les prescriptions du dossier selectionne et filtre les types affiches.
     */
    private void loadPrescriptions() {
        Integer selectedDossierId = AppState.getSelectedDossierId();
        int dossierId = (selectedDossierId != null) ? selectedDossierId : 1;

        List<Document> list = documentService.getByDossierId(dossierId);
        // Filter only prescriptions or consultations
        list.removeIf(doc -> !"consultation".equals(doc.getTypeDocument()) && !"ordonnance".equals(doc.getTypeDocument()));
        
        ObservableList<Document> obsList = FXCollections.observableArrayList(list);
        prescriptionTable.setItems(obsList);
    }


    /**
     * Revient vers le tableau de bord patient.
     */
    @FXML
    private void goBack(ActionEvent event) {
        loadView(event, "/fxml/patient/PatientDashboard.fxml", "Espace Patient");
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


