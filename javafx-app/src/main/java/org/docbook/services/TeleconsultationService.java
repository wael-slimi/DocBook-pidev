package org.docbook.services;

import org.docbook.entities.records.Appointment;
import org.docbook.entities.records.Teleconsultation;
import org.docbook.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeleconsultationService {
    private final AppointmentService appointmentService;

    public TeleconsultationService() {
        this.appointmentService = new AppointmentService();
    }

    public void create(Teleconsultation teleconsultation) throws Exception {
        String sql = "INSERT INTO teleconsultation (appointment_id, video_link, access_code) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, teleconsultation.getAppointmentId());
            ps.setString(2, teleconsultation.getMeetingUrl());  // video_link
            ps.setString(3, teleconsultation.getMode());  // access_code

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                teleconsultation.setId(rs.getInt(1));
            }
        }
    }

    public Teleconsultation readById(Integer id) throws Exception {
        String sql = "SELECT id, appointment_id, video_link, access_code FROM teleconsultation WHERE id = ?";
        Teleconsultation teleconsultation = null;

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                teleconsultation = mapResultSetToTeleconsultation(rs);
            }
        }
        return teleconsultation;
    }

    public List<Teleconsultation> readAll() throws Exception {
        String sql = "SELECT id, appointment_id, video_link, access_code FROM teleconsultation ORDER BY id DESC";
        List<Teleconsultation> teleconsultations = new ArrayList<>();

        try (Connection conn = DBConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                teleconsultations.add(mapResultSetToTeleconsultation(rs));
            }
        }
        return teleconsultations;
    }

    public void update(Teleconsultation teleconsultation) throws Exception {
        String sql = "UPDATE teleconsultation SET appointment_id = ?, video_link = ?, access_code = ? WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, teleconsultation.getAppointmentId());
            ps.setString(2, teleconsultation.getMeetingUrl());
            ps.setString(3, teleconsultation.getMode());
            ps.setObject(4, teleconsultation.getId());

            ps.executeUpdate();
        }
    }

    public void delete(Integer id) throws Exception {
        String sql = "DELETE FROM teleconsultation WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Teleconsultation> searchAndFilter(String query, String mode) throws Exception {
        StringBuilder sql = new StringBuilder(
            "SELECT t.id, t.appointment_id, t.video_link, t.access_code " +
            "FROM teleconsultation t " +
            "JOIN appointment a ON t.appointment_id = a.id " +
            "WHERE 1=1 "
        );

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(buildFilterQuery(sql, query, mode))) {

            int paramIndex = 1;

            if (query != null && !query.trim().isEmpty()) {
                String searchPattern = "%" + query + "%";
                ps.setString(paramIndex++, searchPattern);
                ps.setString(paramIndex++, searchPattern);
            }

            if (mode != null && !mode.trim().isEmpty()) {
                ps.setString(paramIndex, mode);
            }

            ResultSet rs = ps.executeQuery();
            List<Teleconsultation> teleconsultations = new ArrayList<>();

            while (rs.next()) {
                teleconsultations.add(mapResultSetToTeleconsultation(rs));
            }

            return teleconsultations;
        }
    }

    public Teleconsultation getByAppointmentId(Integer appointmentId) throws Exception {
        String sql = "SELECT id, appointment_id, video_link, access_code FROM teleconsultation WHERE appointment_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, appointmentId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToTeleconsultation(rs);
            }
        }
        return null;
    }

    public List<Teleconsultation> getByDoctorId(int doctorId) throws Exception {
        String sql = "SELECT t.id, t.appointment_id, t.video_link, t.access_code " +
                "FROM teleconsultation t " +
                "JOIN appointment a ON t.appointment_id = a.id " +
                "WHERE a.doctor_id = ? " +
                "ORDER BY t.id DESC";
        List<Teleconsultation> teleconsultations = new ArrayList<>();

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, doctorId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                teleconsultations.add(mapResultSetToTeleconsultation(rs));
            }
        }
        return teleconsultations;
    }

    public List<Teleconsultation> getByPatientId(int patientId) throws Exception {
        String sql = "SELECT t.id, t.appointment_id, t.video_link, t.access_code " +
                "FROM teleconsultation t " +
                "JOIN appointment a ON t.appointment_id = a.id " +
                "WHERE a.patient_id = ? " +
                "ORDER BY t.id DESC";
        List<Teleconsultation> teleconsultations = new ArrayList<>();

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                teleconsultations.add(mapResultSetToTeleconsultation(rs));
            }
        }
        return teleconsultations;
    }

    public List<Teleconsultation> getByMode(String mode) throws Exception {
        String sql = "SELECT id, appointment_id, video_link, access_code FROM teleconsultation WHERE access_code = ? ORDER BY id DESC";
        List<Teleconsultation> teleconsultations = new ArrayList<>();

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, mode);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                teleconsultations.add(mapResultSetToTeleconsultation(rs));
            }
        }
        return teleconsultations;
    }

    private String buildFilterQuery(StringBuilder sql, String query, String mode) {
        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (a.reason LIKE ? OR t.video_link LIKE ?) ");
        }
        if (mode != null && !mode.trim().isEmpty()) {
            sql.append("AND t.access_code = ? ");
        }
        sql.append("ORDER BY t.id DESC");
        return sql.toString();
    }

    private Teleconsultation mapResultSetToTeleconsultation(ResultSet rs) throws SQLException, Exception {
        Teleconsultation teleconsultation = new Teleconsultation();
        teleconsultation.setId(rs.getInt("id"));
        teleconsultation.setAppointmentId(rs.getInt("appointment_id"));
        teleconsultation.setMeetingUrl(rs.getString("video_link"));
        teleconsultation.setMode(rs.getString("access_code"));

        Integer appointmentId = rs.getInt("appointment_id");
        Appointment appointment = appointmentService.readById(appointmentId);
        teleconsultation.setAppointment(appointment);

        return teleconsultation;
    }
}