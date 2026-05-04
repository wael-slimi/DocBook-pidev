package org.docbook.controllers.ai;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.docbook.entities.records.Document;
import org.docbook.entities.records.DossierMedical;

import java.util.List;

public class PatientQrController {

    @FXML private Text patientNameText;
    @FXML private Text dossierNumText;
    @FXML private ImageView qrImageView;
    @FXML private VBox detailsContainer;

    public void setData(DossierMedical dossier, List<Document> documents, Image qrImage) {
        patientNameText.setText(dossier.getPatientPrenom() + " " + dossier.getPatientNom());
        dossierNumText.setText("Dossier N° " + dossier.getNumeroDossier());
        qrImageView.setImage(qrImage);

        addDetail("Email", dossier.getEmail());
        addDetail("Téléphone", dossier.getTelephone());
        addDetail("Remarques", dossier.getRemarques());
        
        if (!documents.isEmpty()) {
            addHeader("DOCUMENTS & ORDONNANCES");
            for (Document doc : documents) {
                addDocItem(doc.getTitre(), doc.getTypeDocument());
            }
        }
    }

    private void addDetail(String label, String value) {
        Text l = new Text(label + ": ");
        l.setStyle("-fx-fill: #94a3b8; -fx-font-weight: bold;");
        Text v = new Text(value == null || value.isBlank() ? "N/A" : value);
        v.setStyle("-fx-fill: white;");
        
        VBox row = new VBox(2);
        row.getChildren().addAll(l, v);
        detailsContainer.getChildren().add(row);
    }

    private void addHeader(String title) {
        Text t = new Text("\n" + title);
        t.setStyle("-fx-fill: #3b82f6; -fx-font-weight: bold; -fx-font-size: 11px;");
        detailsContainer.getChildren().add(t);
    }

    private void addDocItem(String title, String type) {
        Text t = new Text("• " + title + " (" + type + ")");
        t.setStyle("-fx-fill: #e2e8f0; -fx-font-size: 13px;");
        detailsContainer.getChildren().add(t);
    }

    @FXML
    private void close() {
        ((Stage) qrImageView.getScene().getWindow()).close();
    }
}