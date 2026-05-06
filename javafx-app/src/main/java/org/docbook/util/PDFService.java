package org.docbook.util;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;

import java.io.File;
import java.io.FileOutputStream;

public class PDFService {

    public static void generateAIReport(String patientName, String content, String filePath) throws Exception {
        PdfWriter writer = new PdfWriter(new FileOutputStream(filePath));
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        Paragraph header = new Paragraph("Rapport d'Analyse AI - DocBook")
                .setFontSize(20)
                .setBold()
                .setFontColor(new DeviceRgb(30, 41, 59))
                .setTextAlignment(TextAlignment.CENTER);
        document.add(header);

        document.add(new LineSeparator(new SolidLine()));
        document.add(new Paragraph("\n"));

        document.add(new Paragraph("Patient: ").setBold().add(new Text(patientName)));
        document.add(new Paragraph("Date: ").setBold().add(new Text(java.time.LocalDate.now().toString())));
        document.add(new Paragraph("\n"));

        Paragraph title = new Paragraph("Analyse de l'Assistant Co-pilote")
                .setFontSize(14)
                .setBold()
                .setFontColor(new DeviceRgb(59, 130, 246));
        document.add(title);

        document.add(new Paragraph(content)
                .setFontSize(12)
                .setFontColor(new DeviceRgb(71, 85, 105)));

        document.add(new Paragraph("\n\n"));
        document.add(new LineSeparator(new SolidLine()));
        document.add(new Paragraph("Ce rapport a été généré par l'IA DocBook. Il doit être validé par un professionnel de santé.")
                .setFontSize(9)
                .setItalic()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(new DeviceRgb(148, 163, 184)));

        document.close();
    }
}