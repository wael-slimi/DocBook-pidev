package org.docbook.services;

import org.docbook.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class RatingService {

    public static boolean saveAppointmentRating(int appointmentId, int patientId, int stars, String comment) {
        if (stars < 1 || stars > 5) {
            System.err.println("Invalid rating: " + stars + " (must be 1-5)");
            return false;
        }

        try {
            String updateSql = "UPDATE appointment_rating SET stars = ?, comment = ?, rated_at = ? WHERE appointment_id = ? AND patient_id = ?";

            try (Connection conn = DBConnection.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement(updateSql)) {

                ps.setInt(1, stars);
                ps.setString(2, comment != null ? comment : "");
                ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                ps.setInt(4, appointmentId);
                ps.setInt(5, patientId);

                int rowsUpdated = ps.executeUpdate();

                if (rowsUpdated == 0) {
                    String insertSql = "INSERT INTO appointment_rating (appointment_id, patient_id, stars, comment, rated_at) VALUES (?, ?, ?, ?, ?)";

                    try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                        insertPs.setInt(1, appointmentId);
                        insertPs.setInt(2, patientId);
                        insertPs.setInt(3, stars);
                        insertPs.setString(4, comment != null ? comment : "");
                        insertPs.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));

                        insertPs.executeUpdate();
                    }
                }

                System.out.println("Rating saved: " + getStarDisplay(stars) + " for appointment #" + appointmentId);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error saving rating: " + e.getMessage());
            return false;
        }
    }

    public static int getAppointmentRating(int appointmentId) {
        String sql = "SELECT stars FROM appointment_rating WHERE appointment_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, appointmentId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("stars");
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving rating: " + e.getMessage());
        }
        return 0;
    }

    public static double getDoctorAverageRating(String doctorName) {
        String sql = "SELECT AVG(ar.stars) as avg_rating " +
                "FROM appointment_rating ar " +
                "JOIN appointment a ON ar.appointment_id = a.id " +
                "WHERE a.doctor = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, doctorName);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                double avg = rs.getDouble("avg_rating");
                if (!rs.wasNull()) {
                    System.out.println("Dr. " + doctorName + " Average Rating: " + String.format("%.1f", avg));
                    return avg;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calculating average rating: " + e.getMessage());
        }
        return 0.0;
    }

    public static Map<String, Object> getRatingStatistics(int appointmentId) {
        Map<String, Object> stats = new HashMap<>();
        String sql = "SELECT stars, comment, rated_at FROM appointment_rating WHERE appointment_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, appointmentId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int stars = rs.getInt("stars");
                String comment = rs.getString("comment");
                Timestamp ratedAt = rs.getTimestamp("rated_at");

                stats.put("stars", stars);
                stats.put("comment", comment);
                stats.put("ratedAt", ratedAt);
                stats.put("display", getStarDisplay(stars));
                stats.put("percentage", (stars * 100) / 5 + "%");
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving rating statistics: " + e.getMessage());
        }
        return stats;
    }

    public static double getClinicAverageRating() {
        String sql = "SELECT AVG(stars) as clinic_avg FROM appointment_rating";

        try (Connection conn = DBConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {
                double avg = rs.getDouble("clinic_avg");
                if (!rs.wasNull()) {
                    return avg;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calculating clinic average: " + e.getMessage());
        }
        return 0.0;
    }

    public static Map<Integer, Integer> getRatingDistribution() {
        Map<Integer, Integer> distribution = new HashMap<>();
        String sql = "SELECT stars, COUNT(*) as count FROM appointment_rating GROUP BY stars";

        try (Connection conn = DBConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                int stars = rs.getInt("stars");
                int count = rs.getInt("count");
                distribution.put(stars, count);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving rating distribution: " + e.getMessage());
        }
        return distribution;
    }

    public static String getStarDisplay(int stars) {
        String filled = "\u2B50".repeat(Math.max(0, stars));
        String empty = "\u2606".repeat(Math.max(0, 5 - stars));
        return filled + empty;
    }

    public static String getRatingDescription(int stars) {
        return switch (stars) {
            case 5 -> "Excellent - Very Satisfied";
            case 4 -> "Good - Satisfied";
            case 3 -> "Average - Neutral";
            case 2 -> "Poor - Unsatisfied";
            case 1 -> "Very Poor - Very Unsatisfied";
            default -> "No Rating";
        };
    }

    public static void createRatingTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS appointment_rating (
                    id SERIAL PRIMARY KEY,
                    appointment_id INT NOT NULL,
                    patient_id INT,
                    stars INT NOT NULL CHECK (stars >= 1 AND stars <= 5),
                    comment TEXT,
                    rated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (appointment_id) REFERENCES appointment(id) ON DELETE CASCADE,
                    UNIQUE (appointment_id, patient_id)
                )
                """;

        try (Connection conn = DBConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            System.out.println("appointment_rating table created/verified");
        } catch (SQLException e) {
            System.err.println("Error creating table: " + e.getMessage());
        }
    }
}