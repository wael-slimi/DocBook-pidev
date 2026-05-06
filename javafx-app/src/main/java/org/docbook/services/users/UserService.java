package org.docbook.services.users;

import org.docbook.entities.users.Doctor;
import org.docbook.entities.users.User;
import org.docbook.interfaces.ICrud;
import org.docbook.services.UserActivityLogService;
import org.docbook.services.users.GoogleAuthService;
import org.docbook.util.DBConnection;
import org.docbook.util.EmailService;
import org.docbook.util.PasswordUtil;

import java.io.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class UserService implements ICrud<User> {

    private final UserActivityLogService logService = new UserActivityLogService();

    public UserService() {
        // Connections are managed per-method to avoid closure bugs
    }

    private Connection getConnection() throws SQLException {
        return DBConnection.getInstance().getConnection();
    }

    private User buildUserFromResultSet(ResultSet rs) throws SQLException {
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
        u.setPassword(rs.getString("password"));
        u.setRole(rs.getString("role"));
        u.setDtype(dtype);
        u.setIsVerified(rs.getBoolean("is_verified"));
        u.setVerificationCode(rs.getString("verification_code"));
        u.setStatus(rs.getString("status"));
        u.setAvatarUrl(rs.getString("avatar_url"));
        String theme = rs.getString("theme_preference");
        if (theme != null) u.setThemePreference(theme);
        return u;
    }

    // --- HELPER METHODS ---

    /**
     * Retrieves a user ID based on their email address.
     */
    public int getIdByEmail(String email) {
        String sql = "SELECT id FROM \"user\" WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public User getUserByEmail(String email) {
        String sql = "SELECT u.*, d.specialty, d.consultation_fee, d.bio FROM \"user\" u " +
                "LEFT JOIN doctor d ON u.id = d.id WHERE u.email = ?";
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                User u = buildUserFromResultSet(rs);
                return u;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
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

    // --- ADMIN & APPROVAL LOGIC ---

    /**
     * Updates the status of a doctor to verified.
     */
    public void approveDoctor(int doctorId) {
        String sql = "UPDATE \"user\" SET is_verified = true, status = 'approved' WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, doctorId);
            pst.executeUpdate();
            System.out.println("Doctor ID " + doctorId + " has been approved.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a list of doctors who registered but haven't been approved yet.
     */
    public List<User> getPendingDoctors() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM \"user\" WHERE dtype = 'doctor' AND status = 'pending'";

        try (Connection conn = getConnection();
             PreparedStatement st = conn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {

            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setName(rs.getString("name"));
                u.setEmail(rs.getString("email"));
                u.setDtype(rs.getString("dtype"));
                u.setStatus(rs.getString("status"));
                list.add(u);
            }
        }
        return list;
    }

    // --- CORE CRUD OPERATIONS ---

    @Override
    public void create(User user) {
        String verificationCode = generateVerificationCode();
        user.setVerificationCode(verificationCode);
        user.setIsVerified(false); // New users are unverified by default

        String userSql = "INSERT INTO \"user\" (name, email, password, role, is_active, is_verified, is2fa_enabled, creation_date, dtype, verification_code, status) " +
                "VALUES (?, ?, ?, ?, true, ?, false, NOW(), ?, ?, ?) RETURNING id";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pst = conn.prepareStatement(userSql)) {
                pst.setString(1, user.getName());
                pst.setString(2, user.getEmail());
                pst.setString(3, PasswordUtil.hash(user.getPassword()));
                pst.setString(4, user.getRole());
                pst.setBoolean(5, user.isIsVerified()); // Should be false
                pst.setString(6, user.getDtype());
                pst.setString(7, user.getVerificationCode());
                // For doctors, default status is "pending", for patients "approved"
                String status = "doctor".equalsIgnoreCase(user.getDtype()) ? "pending" : "approved";
                pst.setString(8, status);

                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    int userId = rs.getInt(1);
                    user.setId(userId); // Set the ID for the user object

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
                    System.out.println("Account created successfully. Verification code sent to " + user.getEmail());
                    EmailService.sendEmail(user.getEmail(), "Verify Your Email", "Your verification code is: " + verificationCode);
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public User read(int id) {
        String sql = "SELECT u.*, d.specialty, d.consultation_fee, d.bio FROM \"user\" u " +
                "LEFT JOIN doctor d ON u.id = d.id WHERE u.id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return buildUserFromResultSet(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public void update(User user) {
        User current = read(user.getId());
        if (current == null) return;

        String sql = "UPDATE \"user\" SET name=?, email=?, is_verified=?, status=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, (user.getName() != null && !user.getName().trim().isEmpty()) ? user.getName() : current.getName());
            pst.setString(2, (user.getEmail() != null && !user.getEmail().trim().isEmpty()) ? user.getEmail() : current.getEmail());
            pst.setBoolean(3, user.isIsVerified());
            pst.setString(4, (user.getStatus() != null) ? user.getStatus() : current.getStatus());
            pst.setInt(5, user.getId());
            pst.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void delete(int id) {
        User user = read(id);
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement pst1 = conn.prepareStatement("DELETE FROM doctor WHERE id = ?")) {
                    pst1.setInt(1, id); pst1.executeUpdate();
                }
                try (PreparedStatement pst2 = conn.prepareStatement("DELETE FROM patient WHERE id = ?")) {
                    pst2.setInt(1, id); pst2.executeUpdate();
                }
                try (PreparedStatement pst3 = conn.prepareStatement("DELETE FROM \"user\" WHERE id = ?")) {
                    pst3.setInt(1, id); pst3.executeUpdate();
                }
                conn.commit();
                if (user != null) {
                    logService.logActivity(id, "ACCOUNT_DELETED", "Account deleted: " + user.getName() + " (" + user.getEmail() + ")", null);
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally { conn.setAutoCommit(true); }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override public List<User> getAll() { return new ArrayList<>(); }

    // --- DOCTOR SPECIFIC QUERIES ---

    public List<Doctor> getAllDoctors() {
        List<Doctor> doctors = new ArrayList<>();
        // Logic: Only show APPROVED doctors to patients
        String sql = "SELECT u.*, d.consultation_fee, d.specialty FROM \"user\" u " +
                "JOIN doctor d ON u.id = d.id WHERE u.dtype = 'doctor' AND u.status = 'approved'";
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                Doctor d = new Doctor();
                d.setId(rs.getInt("id"));
                d.setName(rs.getString("name"));
                d.setEmail(rs.getString("email"));
                d.setSpecialty(rs.getString("specialty"));
                d.setConsultationFee(rs.getDouble("consultation_fee"));
                doctors.add(d);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return doctors;
    }

    public List<Doctor> searchDoctors(String specialty) {
        List<Doctor> doctors = new ArrayList<>();
        String sql = "SELECT u.*, d.specialty, d.consultation_fee, d.bio FROM \"user\" u " +
                "JOIN doctor d ON u.id = d.id " +
                "WHERE u.dtype = 'doctor' AND u.status = 'approved' AND d.specialty ILIKE ?";
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, "%" + specialty + "%");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Doctor d = new Doctor();
                d.setId(rs.getInt("id"));
                d.setName(rs.getString("name"));
                d.setEmail(rs.getString("email"));
                d.setSpecialty(rs.getString("specialty"));
                d.setConsultationFee(rs.getDouble("consultation_fee"));
                d.setBio(rs.getString("bio"));
                doctors.add(d);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return doctors;
    }

    // --- AUTHENTICATION & PROFILE ---

    public User login(String email, String password) {
        String sql = "SELECT u.*, d.specialty, d.consultation_fee, d.bio FROM \"user\" u " +
                "LEFT JOIN doctor d ON u.id = d.id WHERE u.email = ?";

        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String hashedFromDb = rs.getString("password");
                System.out.println("Login attempt for: " + email);
                System.out.println("Hash from DB starts with: " + (hashedFromDb != null ? hashedFromDb.substring(0, 7) : "null"));

                // Check if password is correct first
                if (PasswordUtil.verify(password, hashedFromDb)) {
                    System.out.println("Password verified successfully");
                    if (!rs.getBoolean("is_verified")) {
                        User u = new User();
                        u.setEmail(email);
                        u.setIsVerified(false);
                        return u;
                    }

                    String dtype = rs.getString("dtype");

                    User u;
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
                    u.setPassword(hashedFromDb);
                    u.setDtype(dtype);
                    u.setRole(rs.getString("role"));
                    u.setIsVerified(true);
                    u.setStatus(rs.getString("status"));
                    u.setAvatarUrl(rs.getString("avatar_url"));
                    u.setThemePreference(rs.getString("theme_preference"));

                    logService.logActivity(u.getId(), "LOGIN", "User logged in successfully", null);

                    return u;
                } else {
                    System.out.println("Password verification FAILED for: " + email);
                }
            } else {
                System.out.println("No user found with email: " + email);
            }
        } catch (SQLException e) {
            System.err.println("SQL error during login: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public void updateProfile(User user) throws SQLException {
        User current = read(user.getId());
        if (current == null) return;

        String newPassword = user.getPassword();
        String passwordToSave = (newPassword != null && !newPassword.isEmpty())
                ? PasswordUtil.hash(newPassword)
                : current.getPassword() != null ? current.getPassword() : "";

        String sqlUser = "UPDATE \"user\" SET name = ?, password = ?, avatar_url = ? WHERE id = ?";
        try (Connection conn = getConnection()) {
            PreparedStatement st = conn.prepareStatement(sqlUser);
            st.setString(1, user.getName());
            st.setString(2, passwordToSave);
            st.setString(3, user.getAvatarUrl());
            st.setInt(4, user.getId());
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
    public void denyDoctor(int id) {
        delete(id); // Use your existing delete logic to remove the request
    }

    public List<User> getAllUsersForAdmin() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM \"user\"";
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setName(rs.getString("name"));
                u.setEmail(rs.getString("email"));
                u.setRole(rs.getString("role"));
                u.setDtype(rs.getString("dtype"));
                u.setIsVerified(rs.getBoolean("is_verified"));
                u.setStatus(rs.getString("status"));
                list.add(u);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void exportUsersToCSV(File file) throws IOException {
        List<User> users = getAllUsersForAdmin();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("ID,Name,Email,Role,Type,Status,Verified\n");
            for (User u : users) {
                writer.write(String.format("\"%d\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    u.getId(),
                    u.getName() != null ? u.getName().replace("\"", "\"\"") : "",
                    u.getEmail() != null ? u.getEmail() : "",
                    u.getRole() != null ? u.getRole() : "",
                    u.getDtype() != null ? u.getDtype() : "",
                    u.getStatus() != null ? u.getStatus() : "",
                    u.isIsVerified() ? "Yes" : "No"));
            }
            System.out.println("Exported " + users.size() + " users to " + file.getAbsolutePath());
        }
    }

    public void saveThemePreference(int userId, String theme) throws SQLException {
        String sql = "UPDATE \"user\" SET theme_preference = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, theme);
            st.setInt(2, userId);
            st.executeUpdate();
        }
    }


    public void updateStatus(int userId, String newStatus) throws SQLException {
        String sql = "UPDATE \"user\" SET status = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, newStatus);
            st.setInt(2, userId);
            st.executeUpdate();
        }
    }

    // --- PASSWORD RESET LOGIC ---

    public boolean sendPasswordResetLink(String email) {
        if (!emailExists(email)) {
            return false;
        }

        String token = UUID.randomUUID().toString().substring(0, 8); // Shorten for easier entry
        LocalDateTime expiry = LocalDateTime.now().plusHours(1);

        String sql = "UPDATE \"user\" SET reset_token = ?, reset_token_expiry = ? WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, token);
            pst.setTimestamp(2, Timestamp.valueOf(expiry));
            pst.setString(3, email);
            int updated = pst.executeUpdate();

            if (updated > 0) {
                EmailService.sendEmail(email, "Password Reset", "Your reset token is: " + token);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean validateResetToken(String token) {
        String sql = "SELECT COUNT(*) FROM \"user\" WHERE reset_token = ? AND reset_token_expiry > NOW()";
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, token);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean resetPassword(String token, String newPassword) {
        String sql = "UPDATE \"user\" SET password = ?, reset_token = NULL, reset_token_expiry = NULL WHERE reset_token = ? AND reset_token_expiry > NOW()";
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, PasswordUtil.hash(newPassword));
            pst.setString(2, token);
            int updated = pst.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // --- EMAIL VERIFICATION LOGIC ---

    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 6-digit code
        return String.valueOf(code);
    }

    public boolean verifyUserEmail(String email, String code) {
        String sql = "UPDATE \"user\" SET is_verified = true, verification_code = NULL WHERE email = ? AND verification_code = ?";
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, email);
            pst.setString(2, code);
            int updated = pst.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean resendVerificationCode(String email) {
        User user = getUserByEmail(email);
        if (user == null || user.isIsVerified()) {
            return false; // User not found or already verified
        }

        String newCode = generateVerificationCode();
        String sql = "UPDATE \"user\" SET verification_code = ? WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, newCode);
            pst.setString(2, email);
            int updated = pst.executeUpdate();
            if (updated > 0) {
                EmailService.sendEmail(email, "New Email Verification Code", "Your new verification code is: " + newCode);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public User loginOrCreateWithGoogle(GoogleAuthService.GoogleUserInfo googleUser) throws SQLException {
        User existingUser = getUserByEmail(googleUser.getEmail());
        if (existingUser != null) {
            logService.logActivity(existingUser.getId(), "LOGIN", "Google OAuth login", null);
            return existingUser;
        }

        User newUser = new User(googleUser.getName(), googleUser.getEmail(), "", "ROLE_PATIENT", "patient");
        newUser.setIsVerified(googleUser.isVerifiedEmail());
        newUser.setAvatarUrl(googleUser.getPicture());
        create(newUser);

        User createdUser = getUserByEmail(googleUser.getEmail());
        if (createdUser != null) {
            logService.logActivity(createdUser.getId(), "ACCOUNT_CREATED", "Google OAuth signup", null);
        }
        return createdUser;
    }

    public int getTotalUsers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM \"user\"";
        try (Connection conn = getConnection();
             PreparedStatement st = conn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int getUserCountByType(String dtype) throws SQLException {
        String sql = "SELECT COUNT(*) FROM \"user\" WHERE dtype = ?";
        try (Connection conn = getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, dtype);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public int getPendingDoctorCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM \"user\" WHERE dtype = 'doctor' AND status = 'pending'";
        try (Connection conn = getConnection();
             PreparedStatement st = conn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int getRecentRegistrations(int days) throws SQLException {
        String sql = "SELECT COUNT(*) FROM \"user\" WHERE creation_date >= NOW() - INTERVAL '" + days + " days'";
        try (Connection conn = getConnection();
             PreparedStatement st = conn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int getActiveUsers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM \"user\" WHERE status = 'approved' AND is_verified = true";
        try (Connection conn = getConnection();
             PreparedStatement st = conn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
}
