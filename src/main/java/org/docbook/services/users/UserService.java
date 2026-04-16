package org.docbook.services.users;

import org.docbook.entities.users.Doctor;
import org.docbook.entities.users.User;
import org.docbook.interfaces.ICrud;
import org.docbook.util.DBConnection;
import org.docbook.util.PasswordUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements ICrud<User> {

    public UserService() {
        // No longer pre-loading a single connection to avoid closure bugs
    }

    private Connection getConnection() throws SQLException {
        return DBConnection.getInstance().getConnection();
    }

    @Override
    public void update(User user) {
        User current = read(user.getId());
        if (current == null) return;

        String sql = "UPDATE \"user\" SET name=?, email=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, (user.getName() != null && !user.getName().trim().isEmpty())
                    ? user.getName() : current.getName());
            pst.setString(2, (user.getEmail() != null && !user.getEmail().trim().isEmpty())
                    ? user.getEmail() : current.getEmail());
            pst.setInt(3, user.getId());

            pst.executeUpdate();
            System.out.println("User account updated.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public User read(int id) {
        // We use a LEFT JOIN to get doctor details if they exist
        String sql = "SELECT u.*, d.specialty, d.consultation_fee, d.bio FROM \"user\" u " +
                "LEFT JOIN doctor d ON u.id = d.id WHERE u.id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                User u;
                String dtype = rs.getString("dtype");

                if ("doctor".equalsIgnoreCase(dtype)) {
                    Doctor d = new Doctor();
                    d.setSpecialty(rs.getString("specialty"));
                    d.setConsultationFee(rs.getDouble("consultation_fee"));
                    d.setBio(rs.getString("bio"));
                    u = d;
                } else {
                    u = new User();
                }

                u.setId(rs.getInt("id"));
                u.setName(rs.getString("name"));
                u.setEmail(rs.getString("email"));
                u.setRole(rs.getString("role"));
                u.setDtype(dtype);
                return u;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public User login(String email, String password) {
        String sql = "SELECT u.*, d.specialty, d.consultation_fee, d.bio FROM \"user\" u " +
                "LEFT JOIN doctor d ON u.id = d.id WHERE u.email = ?";
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                String hashedFromDb = rs.getString("password");
                if (PasswordUtil.verify(password, hashedFromDb)) {
                    User u;
                    String dtype = rs.getString("dtype");

                    if ("doctor".equalsIgnoreCase(dtype)) {
                        Doctor d = new Doctor();
                        d.setSpecialty(rs.getString("specialty"));
                        d.setConsultationFee(rs.getDouble("consultation_fee"));
                        d.setBio(rs.getString("bio"));
                        u = d;
                    } else {
                        u = new User();
                    }

                    u.setId(rs.getInt("id"));
                    u.setName(rs.getString("name"));
                    u.setEmail(rs.getString("email"));
                    u.setDtype(dtype);
                    u.setRole(rs.getString("role"));
                    return u;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public void create(User user) {
        String userSql = "INSERT INTO \"user\" (name, email, password, role, is_active, is_verified, is2fa_enabled, creation_date, dtype) " +
                "VALUES (?, ?, ?, ?, true, true, false, NOW(), ?) RETURNING id";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pst = conn.prepareStatement(userSql)) {
                pst.setString(1, user.getName());
                pst.setString(2, user.getEmail());
                pst.setString(3, PasswordUtil.hash(user.getPassword()));
                pst.setString(4, user.getRole());
                pst.setString(5, user.getDtype());

                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    int userId = rs.getInt(1);
                    if ("doctor".equalsIgnoreCase(user.getDtype())) {
                        try (PreparedStatement pstDoc = conn.prepareStatement("INSERT INTO doctor (id, total_reviews) VALUES (?, 0)")) {
                            pstDoc.setInt(1, userId);
                            pstDoc.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement pstPat = conn.prepareStatement("INSERT INTO patient (id) VALUES (?)")) {
                            pstPat.setInt(1, userId);
                            pstPat.executeUpdate();
                        }
                    }
                    conn.commit();
                    System.out.println("User and sub-role record created successfully.");
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void delete(int id) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement pst1 = conn.prepareStatement("DELETE FROM doctor WHERE id = ?")) {
                    pst1.setInt(1, id);
                    pst1.executeUpdate();
                }
                try (PreparedStatement pst2 = conn.prepareStatement("DELETE FROM patient WHERE id = ?")) {
                    pst2.setInt(1, id);
                    pst2.executeUpdate();
                }
                try (PreparedStatement pst3 = conn.prepareStatement("DELETE FROM \"user\" WHERE id = ?")) {
                    pst3.setInt(1, id);
                    pst3.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override public List<User> getAll() { return new ArrayList<>(); }

    public List<Doctor> getAllDoctors() {
        List<Doctor> doctors = new ArrayList<>();
        // JOIN is necessary to get the consultationFee from the doctor table
        String sql = "SELECT u.*, d.consultation_fee, d.specialty FROM \"user\" u " +
                "JOIN doctor d ON u.id = d.id WHERE u.dtype = 'doctor'";

        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Doctor d = new Doctor();
                d.setId(rs.getInt("id"));
                d.setName(rs.getString("name"));
                d.setEmail(rs.getString("email"));
                d.setSpecialty(rs.getString("specialty"));
                d.setConsultationFee(rs.getDouble("consultation_fee"));
                doctors.add(d);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return doctors;
    }

    public boolean emailExists(String email) {
        String query = "SELECT COUNT(*) FROM \"user\" WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
        return false;
    }

    public void updateProfile(User user) throws SQLException {
        String sqlUser = "UPDATE \"user\" SET name = ?, password = ? WHERE id = ?";
        try (Connection conn = getConnection()) {
            PreparedStatement st = conn.prepareStatement(sqlUser);
            st.setString(1, user.getName());
            st.setString(2, user.getPassword());
            st.setInt(3, user.getId());
            st.executeUpdate();

            if (user instanceof Doctor) {
                Doctor d = (Doctor) user;
                String sqlDoc = "UPDATE doctor SET specialty = ?, consultation_fee = ?, bio = ? WHERE id = ?";
                PreparedStatement stDoc = conn.prepareStatement(sqlDoc);
                stDoc.setString(1, d.getSpecialty());
                stDoc.setDouble(2, d.getConsultationFee());
                stDoc.setString(3, d.getBio());
                stDoc.setInt(4, d.getId());
                stDoc.executeUpdate();
            }
        }
    }
}