package org.example.services;

import org.example.entities.User;
import org.example.services.ICrud;
import org.example.util.myDataBase;
import org.example.util.PasswordUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements ICrud<User> {
    private Connection conn;

    public UserService() {
        try {
            conn = myDataBase.getInstance().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(User user) {
        // 1. Fetch current record to avoid overwriting with nulls
        User current = read(user.getId());
        if (current == null) return;

        String sql = "UPDATE \"user\" SET name=?, email=? WHERE id=?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            // 2. Logic: (NewValue is not null/empty) ? NewValue : CurrentValue
            pst.setString(1, (user.getName() != null && !user.getName().trim().isEmpty())
                    ? user.getName() : current.getName());

            pst.setString(2, (user.getEmail() != null && !user.getEmail().trim().isEmpty())
                    ? user.getEmail() : current.getEmail());

            pst.setInt(3, user.getId());

            pst.executeUpdate();
            System.out.println("User account updated. Unchanged fields were preserved.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public User read(int id) {
        String sql = "SELECT * FROM \"user\" WHERE id = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setName(rs.getString("name"));
                u.setEmail(rs.getString("email"));
                u.setRole(rs.getString("role"));
                u.setDtype(rs.getString("dtype"));
                return u;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public User login(String email, String password) {
        String sql = "SELECT * FROM \"user\" WHERE email = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                String hashedFromDb = rs.getString("password");
                if (PasswordUtil.verify(password, hashedFromDb)) {
                    User u = new User();
                    u.setId(rs.getInt("id"));
                    u.setName(rs.getString("name"));
                    u.setEmail(rs.getString("email"));
                    u.setDtype(rs.getString("dtype"));
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
        try {
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
                        conn.prepareStatement("INSERT INTO doctor (id, total_reviews) VALUES (" + userId + ", 0)").executeUpdate();
                    } else {
                        conn.prepareStatement("INSERT INTO patient (id) VALUES (" + userId + ")").executeUpdate();
                    }
                    conn.commit();
                }
            } catch (SQLException e) { conn.rollback(); throw e; }
            finally { conn.setAutoCommit(true); }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void delete(int id) {
        // In PostgreSQL, if you have ON DELETE CASCADE, this is simple.
        // Otherwise, we delete from child tables first.
        try {
            conn.setAutoCommit(false);
            try {
                conn.prepareStatement("DELETE FROM doctor WHERE id = " + id).executeUpdate();
                conn.prepareStatement("DELETE FROM patient WHERE id = " + id).executeUpdate();
                conn.prepareStatement("DELETE FROM \"user\" WHERE id = " + id).executeUpdate();
                conn.commit();
                System.out.println("Account and associated data deleted.");
            } catch (SQLException e) { conn.rollback(); throw e; }
            finally { conn.setAutoCommit(true); }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override public List<User> getAll() { return new ArrayList<>(); }
}