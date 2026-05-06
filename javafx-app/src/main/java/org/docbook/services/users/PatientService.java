package org.docbook.services.users;

import org.docbook.entities.users.Patient;
import org.docbook.util.DBConnection;
import java.sql.*;

public class PatientService {
    private Connection conn;

    public PatientService() {
        try {
            conn = DBConnection.getInstance().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Patient getPatientProfile(int id) {
        String sql = "SELECT u.* FROM \"user\" u JOIN patient p ON u.id = p.id WHERE u.id = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                Patient p = new Patient();
                p.setId(rs.getInt("id"));
                p.setName(rs.getString("name"));
                p.setEmail(rs.getString("email"));
                return p;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}