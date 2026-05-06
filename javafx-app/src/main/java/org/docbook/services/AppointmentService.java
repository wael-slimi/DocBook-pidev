package org.docbook.services;

import org.docbook.entities.records.Appointment;
import org.docbook.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentService {

    public void create(Appointment appointment) throws Exception {
        String sql = "INSERT INTO appointment (patient_id, doctor_id, scheduled_at, status, reason, department) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, appointment.getPatientId());
            ps.setInt(2, appointment.getDoctorId());
            ps.setTimestamp(3, Timestamp.valueOf(appointment.getScheduledAt()));
            ps.setString(4, appointment.getStatus());
            ps.setString(5, appointment.getMessage());  // reason in DB
            ps.setString(6, appointment.getDepartment());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                appointment.setId(rs.getInt(1));
            }
        }
    }

    public Appointment readById(Integer id) throws Exception {
        String sql = "SELECT id, patient_id, doctor_id, scheduled_at, status, reason, department FROM appointment WHERE id = ?";
        Appointment appointment = null;

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                appointment = mapResultSetToAppointment(rs);
            }
        }
        return appointment;
    }

    public List<Appointment> readAll() throws Exception {
        String sql = "SELECT id, patient_id, doctor_id, scheduled_at, status, reason, department FROM appointment ORDER BY scheduled_at DESC";
        List<Appointment> appointments = new ArrayList<>();

        try (Connection conn = DBConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                appointments.add(mapResultSetToAppointment(rs));
            }
        }
        return appointments;
    }

    public void update(Appointment appointment) throws Exception {
        String sql = "UPDATE appointment SET patient_id = ?, doctor_id = ?, scheduled_at = ?, status = ?, reason = ?, department = ? WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, appointment.getPatientId());
            ps.setInt(2, appointment.getDoctorId());
            ps.setTimestamp(3, Timestamp.valueOf(appointment.getScheduledAt()));
            ps.setString(4, appointment.getStatus());
            ps.setString(5, appointment.getMessage());  // reason in DB
            ps.setString(6, appointment.getDepartment());
            ps.setInt(7, appointment.getId());

            ps.executeUpdate();
        }
    }

    public void delete(Integer id) throws Exception {
        String sql = "DELETE FROM appointment WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Appointment> search(String query) throws Exception {
        String sql = "SELECT id, patient_id, doctor_id, scheduled_at, status, reason, department FROM appointment " +
                "WHERE reason LIKE ? OR department LIKE ? OR status LIKE ? " +
                "ORDER BY scheduled_at DESC";
        List<Appointment> appointments = new ArrayList<>();

        try (Connection conn = DBConnection.getInstance().getConnection();
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

    public List<Appointment> getByStatus(String status) throws Exception {
        String sql = "SELECT id, patient_id, doctor_id, scheduled_at, status, reason, department FROM appointment WHERE status = ? ORDER BY scheduled_at DESC";
        List<Appointment> appointments = new ArrayList<>();

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                appointments.add(mapResultSetToAppointment(rs));
            }
        }
        return appointments;
    }

    public List<Appointment> getByPatientId(int patientId) throws Exception {
        String sql = "SELECT id, patient_id, doctor_id, scheduled_at, status, reason, department FROM appointment WHERE patient_id = ? ORDER BY scheduled_at DESC";
        List<Appointment> appointments = new ArrayList<>();

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                appointments.add(mapResultSetToAppointment(rs));
            }
        }
        return appointments;
    }

    public List<Appointment> getByDoctorId(int doctorId) throws Exception {
        String sql = "SELECT id, patient_id, doctor_id, scheduled_at, status, reason, department FROM appointment WHERE doctor_id = ? ORDER BY scheduled_at DESC";
        List<Appointment> appointments = new ArrayList<>();

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, doctorId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                appointments.add(mapResultSetToAppointment(rs));
            }
        }
        return appointments;
    }

    private Appointment mapResultSetToAppointment(ResultSet rs) throws SQLException {
        Appointment appointment = new Appointment();
        appointment.setId(rs.getInt("id"));
        appointment.setPatientId(rs.getInt("patient_id"));
        appointment.setDoctorId(rs.getInt("doctor_id"));
        appointment.setScheduledAt(rs.getTimestamp("scheduled_at").toLocalDateTime());
        appointment.setStatus(rs.getString("status"));
        appointment.setMessage(rs.getString("reason"));  // reason -> message
        appointment.setDepartment(rs.getString("department"));
        return appointment;
    }

    public double getAverageRating(int appointmentId) {
        String sql = "SELECT AVG(stars) as avg_rating FROM appointment_rating WHERE appointment_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, appointmentId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getDouble("avg_rating");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching average rating: " + e.getMessage());
        }
        return 0.0;
    }
}