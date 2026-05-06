package org.docbook.services;

import org.docbook.entities.UserActivityLog;
import org.docbook.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserActivityLogService {

    private Connection getConnection() throws SQLException {
        return DBConnection.getInstance().getConnection();
    }

    public void logActivity(int userId, String actionType, String details, String ipAddress) {
        String sql = "INSERT INTO user_activity_log (user_id, action_type, details, ip_address) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, userId);
            pst.setString(2, actionType);
            pst.setString(3, details);
            pst.setString(4, ipAddress);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to log activity: " + e.getMessage());
        }
    }

    public List<UserActivityLog> getAllLogs() {
        List<UserActivityLog> list = new ArrayList<>();
        String sql = "SELECT l.*, u.name AS user_name FROM user_activity_log l LEFT JOIN \"user\" u ON l.user_id = u.id ORDER BY l.created_at DESC";
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch activity logs: " + e.getMessage());
        }
        return list;
    }

    public List<UserActivityLog> searchLogs(String searchTerm, String actionType, String userDtype) {
        List<UserActivityLog> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT l.*, u.name AS user_name, u.dtype AS user_dtype " +
            "FROM user_activity_log l " +
            "LEFT JOIN \"user\" u ON l.user_id = u.id " +
            "WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (searchTerm != null && !searchTerm.isEmpty()) {
            sql.append(" AND (u.name ILIKE ? OR u.email ILIKE ? OR l.details ILIKE ?)");
            params.add("%" + searchTerm + "%");
            params.add("%" + searchTerm + "%");
            params.add("%" + searchTerm + "%");
        }
        if (actionType != null && !actionType.isEmpty() && !"All".equals(actionType)) {
            sql.append(" AND l.action_type = ?");
            params.add(actionType);
        }
        if (userDtype != null && !userDtype.isEmpty() && !"All".equals(userDtype)) {
            sql.append(" AND u.dtype = ?");
            params.add(userDtype.toLowerCase());
        }

        sql.append(" ORDER BY l.created_at DESC");

        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pst.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to search activity logs: " + e.getMessage());
        }
        return list;
    }

    private UserActivityLog mapRow(ResultSet rs) throws SQLException {
        UserActivityLog log = new UserActivityLog();
        log.setId(rs.getInt("id"));
        log.setUserId(rs.getInt("user_id"));
        log.setUserName(rs.getString("user_name"));
        log.setActionType(rs.getString("action_type"));
        log.setDetails(rs.getString("details"));
        log.setIpAddress(rs.getString("ip_address"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) log.setCreatedAt(ts.toLocalDateTime());
        return log;
    }

    public void clearAllLogs() {
        String sql = "DELETE FROM user_activity_log";
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to clear logs: " + e.getMessage());
        }
    }
}
