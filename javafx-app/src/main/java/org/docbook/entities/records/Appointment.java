package org.docbook.entities.records;

import java.time.LocalDateTime;

public class Appointment {
    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_CONFIRMED = "Confirmed";
    public static final String STATUS_CANCELLED = "Cancelled";
    public static final String STATUS_COMPLETED = "Completed";
    public static final String STATUS_EXPIRED = "Expired";

    private Integer id;
    private LocalDateTime scheduledAt;
    private String department;
    private String doctor;
    private String message;
    private String status;
    private int patientId;
    private int doctorId;
    private Teleconsultation teleconsultation;

    public Appointment() {
        this.status = STATUS_PENDING;
    }

    public Appointment(Integer id, LocalDateTime scheduledAt, String department, String doctor, String message, String status) {
        this.id = id;
        this.scheduledAt = scheduledAt;
        this.department = department;
        this.doctor = doctor;
        this.message = message;
        this.status = status != null ? status : STATUS_PENDING;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDoctor() { return doctor; }
    public void setDoctor(String doctor) { this.doctor = doctor; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }

    public Teleconsultation getTeleconsultation() { return teleconsultation; }
    public void setTeleconsultation(Teleconsultation teleconsultation) { this.teleconsultation = teleconsultation; }

    @Override
    public String toString() {
        return "Appointment{id=" + id + ", scheduledAt=" + scheduledAt + ", department='" + department + "', doctor='" + doctor + "', status='" + status + "'}";
    }
}