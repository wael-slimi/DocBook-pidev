package org.docbook.controllers.ai;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import org.docbook.util.PDFService;

import java.io.File;

public class AIReportController {

    @FXML private Text patientNameText;
    @FXML private Label aiContentLabel;

    private String patientName;
    private String reportContent;

    public void setData(String patientName, String content) {
        this.patientName = patientName;
        this.reportContent = content;
        patientNameText.setText("Patient: " + patientName);
        aiContentLabel.setText(content);
    }

    @FXML
    private void downloadPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le rapport AI");
        fileChooser.setInitialFileName("Rapport_AI_" + patientName.replace(" ", "_") + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File file = fileChooser.showSaveDialog(aiContentLabel.getScene().getWindow());
        if (file != null) {
            try {
                PDFService.generateAIReport(patientName, reportContent, file.getAbsolutePath());
                System.out.println("PDF generated: " + file.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void close() {
        ((Stage) aiContentLabel.getScene().getWindow()).close();
    }
}