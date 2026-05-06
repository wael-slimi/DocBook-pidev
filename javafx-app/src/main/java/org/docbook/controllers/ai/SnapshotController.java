package org.docbook.controllers.ai;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import org.docbook.util.PDFService;

import java.io.File;

public class SnapshotController {

    @FXML private Text patientNameText;
    @FXML private Label snapshotLabel;

    private String patientName;
    private String snapshotContent;

    public void setData(String patientName, String content) {
        this.patientName = patientName;
        this.snapshotContent = content;
        patientNameText.setText("Patient: " + patientName);
        snapshotLabel.setText(content);
    }

    @FXML
    private void downloadPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le Snapshot");
        fileChooser.setInitialFileName("Snapshot_AI_" + patientName.replace(" ", "_") + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File file = fileChooser.showSaveDialog(snapshotLabel.getScene().getWindow());
        if (file != null) {
            try {
                PDFService.generateAIReport(patientName, "RÉSUMÉ SNAPSHOT:\n" + snapshotContent, file.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void close() {
        ((Stage) snapshotLabel.getScene().getWindow()).close();
    }
}