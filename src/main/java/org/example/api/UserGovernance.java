package org.example.api;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

final class UserGovernance {
    private UserGovernance() {
    }

    static void ensurePenaltyTable(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "CREATE TABLE IF NOT EXISTS user_penalty (" +
                        "userID VARCHAR(64) PRIMARY KEY," +
                        "warningCount INT NOT NULL DEFAULT 0," +
                        "bannedUntil TIMESTAMP NULL," +
                        "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                        ")")) {
            ps.executeUpdate();
        }
    }

    static Map<String, Object> queryPenalty(Connection conn, String userID) throws SQLException {
        ensurePenaltyTable(conn);
        Map<String, Object> data = new HashMap<>();
        data.put("warningCount", 0);
        data.put("bannedUntil", null);
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT warningCount, bannedUntil FROM user_penalty WHERE userID = ?")) {
            ps.setString(1, userID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                data.put("warningCount", rs.getInt("warningCount"));
                data.put("bannedUntil", rs.getTimestamp("bannedUntil"));
            }
        }
        Timestamp bannedUntil = (Timestamp) data.get("bannedUntil");
        data.put("isBanned", bannedUntil != null && bannedUntil.after(Timestamp.valueOf(LocalDateTime.now())));
        return data;
    }

    static Timestamp queryBannedUntil(Connection conn, String userID) throws SQLException {
        ensurePenaltyTable(conn);
        try (PreparedStatement ps = conn.prepareStatement("SELECT bannedUntil FROM user_penalty WHERE userID = ?")) {
            ps.setString(1, userID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getTimestamp("bannedUntil");
            return null;
        }
    }

    static int increaseWarningAndMaybeBan(Connection conn, String userID) throws SQLException {
        ensurePenaltyTable(conn);
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO user_penalty (userID, warningCount) VALUES (?, 1) " +
                        "ON DUPLICATE KEY UPDATE warningCount = warningCount + 1")) {
            ps.setString(1, userID);
            ps.executeUpdate();
        }
        int warningCount = 0;
        try (PreparedStatement ps = conn.prepareStatement("SELECT warningCount FROM user_penalty WHERE userID = ?")) {
            ps.setString(1, userID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) warningCount = rs.getInt("warningCount");
        }
        if (warningCount >= 3) {
            Timestamp until = Timestamp.valueOf(LocalDateTime.now().plusDays(3));
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE user_penalty SET warningCount = 0, bannedUntil = ? WHERE userID = ?")) {
                ps.setTimestamp(1, until);
                ps.setString(2, userID);
                ps.executeUpdate();
            }
            ensurePointsRow(conn, userID);
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE points SET points = GREATEST(points - 30, 0) WHERE userID = ?")) {
                ps.setString(1, userID);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO pointop (userID, pointOP, detail, `time`) VALUES (?, '-30', ?, NOW())")) {
                ps.setString(1, userID);
                ps.setString(2, "警告累计达到3次，触发封禁3天并扣除30积分");
                ps.executeUpdate();
            } catch (SQLException ignored) {
            }
            warningCount = 0;
        }
        return warningCount;
    }

    static void banUser(Connection conn, String userID, int days) throws SQLException {
        ensurePenaltyTable(conn);
        int banDays = Math.max(days, 1);
        Timestamp until = Timestamp.valueOf(LocalDateTime.now().plusDays(banDays));
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO user_penalty (userID, warningCount, bannedUntil) VALUES (?, 0, ?) " +
                        "ON DUPLICATE KEY UPDATE bannedUntil = VALUES(bannedUntil)")) {
            ps.setString(1, userID);
            ps.setTimestamp(2, until);
            ps.executeUpdate();
        }
    }

    static void unbanUser(Connection conn, String userID) throws SQLException {
        ensurePenaltyTable(conn);
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO user_penalty (userID, warningCount, bannedUntil) VALUES (?, 0, NULL) " +
                        "ON DUPLICATE KEY UPDATE bannedUntil = NULL")) {
            ps.setString(1, userID);
            ps.executeUpdate();
        }
    }

    private static void ensurePointsRow(Connection conn, String userID) throws SQLException {
        String userName = "";
        try (PreparedStatement ps = conn.prepareStatement("SELECT userName FROM user WHERE userphone = ?")) {
            ps.setString(1, userID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) userName = rs.getString("userName");
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO points (userID, userName, points) VALUES (?, ?, 0) ON DUPLICATE KEY UPDATE userName = VALUES(userName)")) {
            ps.setString(1, userID);
            ps.setString(2, userName == null ? "" : userName);
            ps.executeUpdate();
        }
    }
}
