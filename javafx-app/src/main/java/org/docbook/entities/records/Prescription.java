package org.docbook.entities.records;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Prescription extends Document {

    private String medicaments;
    private String dosageInstructions;
    private String duree;

    public Prescription() {
        super();
        this.setTypeDocument("ordonnance");
    }

    public Prescription(int id, int dossierMedicalId, String titre, LocalDate dateDocument, String contenu, String fichierPath, LocalDateTime dateCreation, LocalDateTime dateModification, String medicaments, String dosageInstructions, String duree) {
        super(id, dossierMedicalId, titre, "ordonnance", dateDocument, contenu, fichierPath, dateCreation, dateModification);
        this.medicaments = medicaments;
        this.dosageInstructions = dosageInstructions;
        this.duree = duree;
    }

    public String getMedicaments() { return medicaments; }
    public void setMedicaments(String medicaments) { this.medicaments = medicaments; }

    public String getDosageInstructions() { return dosageInstructions; }
    public void setDosageInstructions(String dosageInstructions) { this.dosageInstructions = dosageInstructions; }

    public String getDuree() { return duree; }
    public void setDuree(String duree) { this.duree = duree; }
}