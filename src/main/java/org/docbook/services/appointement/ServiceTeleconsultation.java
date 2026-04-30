package org.docbook.services.appointement;

import tn.esprit.interfaces.IService;
import tn.esprit.models.appointement;
import tn.esprit.models.teleconsultation;
import tn.esprit.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceTeleconsultation implements IService<teleconsultation> {
    private final ServiceAppointement serviceAppointement;

    public ServiceTeleconsultation() {
        this.serviceAppointement = new ServiceAppointement();
    }

    @Override
    public void create(teleconsultation teleconsultation) throws Exception {
        String sql = "INSERT INTO teleconsultation (duration, meeting_url, mode, appointment_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, teleconsultation.getDuration());
            ps.setString(2, teleconsultation.getMeetingUrl());
            ps.setString(3, teleconsultation.getMode());
            ps.setInt(4, teleconsultation.getAppointment().getId());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                teleconsultation.setId(rs.getInt(1));
            }
        }
    }

    @Override
    public teleconsultation readById(Integer id) throws Exception {
        String sql = "SELECT id, duration, meeting_url, mode, appointment_id FROM teleconsultation WHERE id = ?";
        teleconsultation teleconsultation = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                teleconsultation = mapResultSetToTeleconsultation(rs);
            }
        }
        return teleconsultation;
    }

    @Override
    public List<teleconsultation> readAll() throws Exception {
        String sql = "SELECT id, duration, meeting_url, mode, appointment_id FROM teleconsultation ORDER BY id DESC";
        List<teleconsultation> teleconsultations = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                teleconsultations.add(mapResultSetToTeleconsultation(rs));
            }
        }
        return teleconsultations;
    }

    @Override
    public void update(teleconsultation teleconsultation) throws Exception {
        String sql = "UPDATE teleconsultation SET duration = ?, meeting_url = ?, mode = ?, appointment_id = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, teleconsultation.getDuration());
            ps.setString(2, teleconsultation.getMeetingUrl());
            ps.setString(3, teleconsultation.getMode());
            ps.setInt(4, teleconsultation.getAppointment().getId());
            ps.setInt(5, teleconsultation.getId());

            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Integer id) throws Exception {
        String sql = "DELETE FROM teleconsultation WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Search and filter teleconsultations by query and mode
     */
    public List<teleconsultation> searchAndFilter(String query, String mode) throws Exception {
        StringBuilder sql = new StringBuilder(
            "SELECT t.id, t.duration, t.meeting_url, t.mode, t.appointment_id " +
            "FROM teleconsultation t " +
            "JOIN appointment a ON t.appointment_id = a.id " +
            "WHERE 1=1 "
        );

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(buildFilterQuery(sql, query, mode))) {

            int paramIndex = 1;

            if (query != null && !query.trim().isEmpty()) {
                String searchPattern = "%" + query + "%";
                ps.setString(paramIndex++, searchPattern);
                ps.setString(paramIndex++, searchPattern);
                ps.setString(paramIndex++, searchPattern);
            }

            if (mode != null && !mode.trim().isEmpty()) {
                ps.setString(paramIndex, mode);
            }

            ResultSet rs = ps.executeQuery();
            List<teleconsultation> teleconsultations = new ArrayList<>();

            while (rs.next()) {
                teleconsultations.add(mapResultSetToTeleconsultation(rs));
            }

            return teleconsultations;
        }
    }

    /**
     * Get teleconsultation by appointment ID
     */
    public teleconsultation getByAppointmentId(Integer appointmentId) throws Exception {
        String sql = "SELECT id, duration, meeting_url, mode, appointment_id FROM teleconsultation WHERE appointment_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, appointmentId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToTeleconsultation(rs);
            }
        }
        return null;
    }

    /**
     * Get teleconsultations by mode (video, chat, audio)
     */
    public List<teleconsultation> getByMode(String mode) throws Exception {
        String sql = "SELECT id, duration, meeting_url, mode, appointment_id FROM teleconsultation WHERE mode = ? ORDER BY id DESC";
        List<teleconsultation> teleconsultations = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, mode);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                teleconsultations.add(mapResultSetToTeleconsultation(rs));
            }
        }
        return teleconsultations;
    }

    /**
     * Helper method to build filter query
     */
    private String buildFilterQuery(StringBuilder sql, String query, String mode) {
        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (a.doctor LIKE ? OR a.department LIKE ? OR t.mode LIKE ?) ");
        }

        if (mode != null && !mode.trim().isEmpty()) {
            sql.append("AND t.mode = ? ");
        }

        sql.append("ORDER BY t.id DESC");
        return sql.toString();
    }

    /**
     * Helper method to map ResultSet to Teleconsultation object
     */
    private teleconsultation mapResultSetToTeleconsultation(ResultSet rs) throws SQLException, Exception {
        teleconsultation teleconsultation = new teleconsultation();
        teleconsultation.setId(rs.getInt("id"));
        teleconsultation.setDuration(rs.getInt("duration"));
        teleconsultation.setMeetingUrl(rs.getString("meeting_url"));
        teleconsultation.setMode(rs.getString("mode"));

        // Load the related appointment
        Integer appointmentId = rs.getInt("appointment_id");
        appointement appointment = serviceAppointement.readById(appointmentId);
        teleconsultation.setAppointment(appointment);

        return teleconsultation;
    }
}
