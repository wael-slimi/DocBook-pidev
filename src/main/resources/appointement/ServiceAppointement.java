package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.appointement;
import tn.esprit.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceAppointement implements IService<appointement> {

    @Override
    public void create(appointement appointment) throws Exception {
        String sql = "INSERT INTO appointment (scheduled_at, department, doctor, message, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setTimestamp(1, Timestamp.valueOf(appointment.getScheduledAt()));
            ps.setString(2, appointment.getDepartment());
            ps.setString(3, appointment.getDoctor());
            ps.setString(4, appointment.getMessage());
            ps.setString(5, appointment.getStatus());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                appointment.setId(rs.getInt(1));
            }
        }
    }

    @Override
    public appointement readById(Integer id) throws Exception {
        String sql = "SELECT id, scheduled_at, department, doctor, message, status FROM appointment WHERE id = ?";
        appointement appointment = null;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                appointment = mapResultSetToAppointment(rs);
            }
        }
        return appointment;
    }

    @Override
    public List<appointement> readAll() throws Exception {
        String sql = "SELECT id, scheduled_at, department, doctor, message, status FROM appointment ORDER BY scheduled_at DESC";
        List<appointement> appointments = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                appointments.add(mapResultSetToAppointment(rs));
            }
        }
        return appointments;
    }

    @Override
    public void update(appointement appointment) throws Exception {
        String sql = "UPDATE appointment SET scheduled_at = ?, department = ?, doctor = ?, message = ?, status = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(appointment.getScheduledAt()));
            ps.setString(2, appointment.getDepartment());
            ps.setString(3, appointment.getDoctor());
            ps.setString(4, appointment.getMessage());
            ps.setString(5, appointment.getStatus());
            ps.setInt(6, appointment.getId());

            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Integer id) throws Exception {
        String sql = "DELETE FROM appointment WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Find appointments with scheduling conflicts for a specific doctor at a given
     * time
     */
    public appointement findConflict(String doctor, LocalDateTime scheduledAt, Integer excludeId) throws Exception {
        String sql = "SELECT id, scheduled_at, department, doctor, message, status FROM appointment " +
                "WHERE doctor = ? AND scheduled_at = ? AND status != ? " +
                (excludeId != null ? "AND id != ?" : "");

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, doctor);
            ps.setTimestamp(2, Timestamp.valueOf(scheduledAt));
            ps.setString(3, appointement.STATUS_CANCELLED);

            if (excludeId != null) {
                ps.setInt(4, excludeId);
            }

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToAppointment(rs);
            }
        }
        return null;
    }

    /**
     * Search appointments by department or doctor name
     */
    public List<appointement> search(String query) throws Exception {
        String sql = "SELECT id, scheduled_at, department, doctor, message, status FROM appointment " +
                "WHERE department LIKE ? OR doctor LIKE ? OR status LIKE ? " +
                "ORDER BY scheduled_at DESC";
        List<appointement> appointments = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            String searchPattern = "%" + query + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                appointments.add(mapResultSetToAppointment(rs));
            }
        }
        return appointments;
    }

    /**
     * Get appointments sorted by status
     */
    public List<appointement> getByStatus(String status) throws Exception {
        String sql = "SELECT id, scheduled_at, department, doctor, message, status FROM appointment WHERE status = ? ORDER BY scheduled_at DESC";
        List<appointement> appointments = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                appointments.add(mapResultSetToAppointment(rs));
            }
        }
        return appointments;
    }

    /**
     * Helper method to map ResultSet to Appointment object
     */
    private appointement mapResultSetToAppointment(ResultSet rs) throws SQLException {
        appointement appointment = new appointement();
        appointment.setId(rs.getInt("id"));
        appointment.setScheduledAt(rs.getTimestamp("scheduled_at").toLocalDateTime());
        appointment.setDepartment(rs.getString("department"));
        appointment.setDoctor(rs.getString("doctor"));
        appointment.setMessage(rs.getString("message"));
        appointment.setStatus(rs.getString("status"));
        return appointment;
    }

    /**
     * Save appointment rating to database
     * Creates or updates rating for given appointment
     * 
     * @param appointmentId ID of appointment to rate
     * @param stars         Rating value (1-5 stars)
     * @param comment       Optional feedback comment
     * @return true if successful, false otherwise
     */
    public boolean saveRatingToDB(int appointmentId, int stars, String comment) {
        // Validate rating
        if (stars < 1 || stars > 5) {
            System.err.println("❌ Invalid rating: " + stars + " (must be 1-5)");
            return false;
        }

        try {
            // First, try to update existing rating
            String updateSql = "UPDATE appointment_rating SET stars = ?, comment = ?, rated_at = ? WHERE appointment_id = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement ps = conn.prepareStatement(updateSql)) {

                ps.setInt(1, stars);
                ps.setString(2, comment != null ? comment : "");
                ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                ps.setInt(4, appointmentId);

                int rowsUpdated = ps.executeUpdate();

                if (rowsUpdated == 0) {
                    // No existing rating, insert new
                    String insertSql = "INSERT INTO appointment_rating (appointment_id, stars, comment, rated_at) VALUES (?, ?, ?, ?)";

                    try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                        insertPs.setInt(1, appointmentId);
                        insertPs.setInt(2, stars);
                        insertPs.setString(3, comment != null ? comment : "");
                        insertPs.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));

                        insertPs.executeUpdate();
                    }
                }

                System.out.println("✅ Rating saved: " + getStarDisplay(stars) + " for appointment #" + appointmentId);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error saving rating: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get average rating for a specific appointment
     * 
     * @param appointmentId ID of appointment
     * @return Average rating or 0 if no ratings exist
     */
    public double getAverageRating(int appointmentId) {
        String sql = "SELECT AVG(stars) as avg_rating FROM appointment_rating WHERE appointment_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, appointmentId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getDouble("avg_rating");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error fetching average rating: " + e.getMessage());
        }

        return 0.0;
    }

    /**
     * Helper method to display star rating
     */
    private String getStarDisplay(int stars) {
        return "⭐".repeat(Math.max(0, stars)) + "☆".repeat(Math.max(0, 5 - stars));
    }
}
