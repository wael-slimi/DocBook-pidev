package org.docbook.entities.users;

public class Doctor extends User {
    private String specialty;
    private String licenseNumber;
    private double consultationFee;
    private int totalReviews;
    private String bio;

    public Doctor() {
        super();
        this.setDtype("doctor");
        this.setRole("ROLE_DOCTOR");
    }


    // Getters and Setters
    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public double getConsultationFee() { return consultationFee; }
    public void setConsultationFee(double consultationFee) { this.consultationFee = consultationFee; }

    public int getTotalReviews() { return totalReviews; }
    public void setTotalReviews(int totalReviews) { this.totalReviews = totalReviews; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}