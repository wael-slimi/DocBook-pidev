package org.docbook.entities.records;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DossierMedical {

    private int id;
    private String numeroDossier;
    private String patientNom;
    private String patientPrenom;
    private LocalDate dateNaissance;
    private String genre;
    private String email;
    private String telephone;
    private String adresse;
    private String remarques;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;

    public DossierMedical() {
    }

    public DossierMedical(int id, String numeroDossier, String patientNom, String patientPrenom, LocalDate dateNaissance, String genre, String email, String telephone, String adresse, String remarques, LocalDateTime dateCreation, LocalDateTime dateModification) {
        this.id = id;
        this.numeroDossier = numeroDossier;
        this.patientNom = patientNom;
        this.patientPrenom = patientPrenom;
        this.dateNaissance = dateNaissance;
        this.genre = genre;
        this.email = email;
        this.telephone = telephone;
        this.adresse = adresse;
        this.remarques = remarques;
        this.dateCreation = dateCreation;
        this.dateModification = dateModification;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumeroDossier() { return numeroDossier; }
    public void setNumeroDossier(String numeroDossier) { this.numeroDossier = numeroDossier; }

    public String getPatientNom() { return patientNom; }
    public void setPatientNom(String patientNom) { this.patientNom = patientNom; }

    public String getPatientPrenom() { return patientPrenom; }
    public void setPatientPrenom(String patientPrenom) { this.patientPrenom = patientPrenom; }

    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getRemarques() { return remarques; }
    public void setRemarques(String remarques) { this.remarques = remarques; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }

    @Override
    public String toString() {
        return "DossierMedical{" +
                "id=" + id +
                ", numeroDossier='" + numeroDossier + '\'' +
                ", patientNom='" + patientNom + '\'' +
                ", patientPrenom='" + patientPrenom + '\'' +
                '}';
    }
}


