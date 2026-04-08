package org.example.services;

import org.example.entities.Doctor;
import org.example.util.myDataBase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DoctorService {
    private Connection conn;

    public DoctorService() {
        try {
            conn = myDataBase.getInstance().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateDoctorProfile(Doctor doctor) {
        // 1. Fetch current data to preserve non-null fields
        Doctor current = getDoctorFullProfile(doctor.getId());
        if (current == null) return;

        String sql = "UPDATE doctor SET specialty=?, consultation_fee=?, bio=? WHERE id=?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            // 2. Only use new value if it's not null/empty, otherwise keep current
            pst.setString(1, (doctor.getSpecialty() != null && !doctor.getSpecialty().isEmpty())
                    ? doctor.getSpecialty() : current.getSpecialty());

            pst.setDouble(2, (doctor.getConsultationFee() > 0)
                    ? doctor.getConsultationFee() : current.getConsultationFee());

            pst.setString(3, (doctor.getBio() != null && !doctor.getBio().isEmpty())
                    ? doctor.getBio() : current.getBio());

            pst.setInt(4, doctor.getId());

            pst.executeUpdate();
            System.out.println("Doctor profile updated (unchanged fields preserved).");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Doctor getDoctorFullProfile(int id) {
        String sql = "SELECT u.*, d.specialty, d.consultation_fee, d.bio FROM \"user\" u JOIN doctor d ON u.id = d.id WHERE u.id = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                Doctor doc = new Doctor();
                doc.setId(rs.getInt("id"));
                doc.setName(rs.getString("name"));
                doc.setEmail(rs.getString("email"));
                doc.setSpecialty(rs.getString("specialty"));
                doc.setConsultationFee(rs.getDouble("consultation_fee"));
                doc.setBio(rs.getString("bio"));
                return doc;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Doctor> searchDoctors(String name) {
        List<Doctor> list = new ArrayList<>();
        // Joining user and doctor tables to get both identity and medical info
        String sql = "SELECT u.name, d.id, d.specialty, d.consultation_fee, d.bio " +
                "FROM \"user\" u " +
                "JOIN doctor d ON u.id = d.id " +
                "WHERE u.name ILIKE ? " +
                "ORDER BY u.name ASC";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, "%" + name + "%");
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Doctor d = new Doctor();
                d.setId(rs.getInt("id"));
                d.setName(rs.getString("name"));
                d.setSpecialty(rs.getString("specialty"));
                d.setConsultationFee(rs.getDouble("consultation_fee"));
                d.setBio(rs.getString("bio"));
                list.add(d);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}