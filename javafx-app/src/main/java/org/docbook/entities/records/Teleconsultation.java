package org.docbook.entities.records;

public class Teleconsultation {
    public static final String MODE_VIDEO = "video";
    public static final String MODE_CHAT = "chat";
    public static final String MODE_AUDIO = "audio";

    private Integer id;
    private int duration;
    private String meetingUrl;
    private String mode;
    private int appointmentId;
    private Appointment appointment;

    public Teleconsultation() {
    }

    public Teleconsultation(Integer id, int duration, String meetingUrl, String mode, int appointmentId) {
        this.id = id;
        this.duration = duration;
        this.meetingUrl = meetingUrl;
        this.mode = mode;
        this.appointmentId = appointmentId;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public String getMeetingUrl() { return meetingUrl; }
    public void setMeetingUrl(String meetingUrl) { this.meetingUrl = meetingUrl; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }

    public Appointment getAppointment() { return appointment; }
    public void setAppointment(Appointment appointment) { this.appointment = appointment; }

    @Override
    public String toString() {
        return "Teleconsultation{id=" + id + ", mode='" + mode + "', duration=" + duration + "min, meetingUrl='" + meetingUrl + "'}";
    }
}