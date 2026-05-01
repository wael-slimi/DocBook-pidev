package org.docbook.entities.records;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Document {

    private int id;
    private int dossierMedicalId;
    private String titre;
    private String typeDocument;
    private LocalDate dateDocument;
    private String contenu;
    private String fichierPath;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;

    public Document() {
    }

    public Document(int id, int dossierMedicalId, String titre, String typeDocument, LocalDate dateDocument, String contenu, String fichierPath, LocalDateTime dateCreation, LocalDateTime dateModification) {
        this.id = id;
        this.dossierMedicalId = dossierMedicalId;
        this.titre = titre;
        this.typeDocument = typeDocument;
        this.dateDocument = dateDocument;
        this.contenu = contenu;
        this.fichierPath = fichierPath;
        this.dateCreation = dateCreation;
        this.dateModification = dateModification;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getDossierMedicalId() { return dossierMedicalId; }
    public void setDossierMedicalId(int dossierMedicalId) { this.dossierMedicalId = dossierMedicalId; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getTypeDocument() { return typeDocument; }
    public void setTypeDocument(String typeDocument) { this.typeDocument = typeDocument; }

    public LocalDate getDateDocument() { return dateDocument; }
    public void setDateDocument(LocalDate dateDocument) { this.dateDocument = dateDocument; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public String getFichierPath() { return fichierPath; }
    public void setFichierPath(String fichierPath) { this.fichierPath = fichierPath; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }

    @Override
    public String toString() {
        return "Document{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", type='" + typeDocument + '\'' +
                '}';
    }
}


