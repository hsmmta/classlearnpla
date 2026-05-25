package org.example.api;

import org.example.util.DBUtil;
import org.example.util.EmailUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class ProfileApiHandler {

    void handle(String path, String method, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if ("/profile/send-reset-code".equals(path) && "POST".equals(method)) {
            sendResetCode(request, response);
            return;
        }
        if ("/profile/reset-password".equals(path) && "POST".equals(method)) {
            resetPassword(request, response);
            return;
        }
        if ("/profile/check-in".equals(path) && "POST".equals(method)) {
            if (!SessionHelper.isLoggedIn(request)) {
                JsonResponse.write(response, HttpServletResponse.SC_UNAUTHORIZED, JsonResponse.fail("请先登录"));
                return;
            }
            checkIn(request, response);
            return;
        }
        if (!SessionHelper.isLoggedIn(request)) {
            JsonResponse.write(response, HttpServletResponse.SC_UNAUTHORIZED, JsonResponse.fail("请先登录"));
            return;
        }
        if ("/profile".equals(path) && "GET".equals(method)) {
            getProfile(request, response);
        } else if ("/profile".equals(path) && "PUT".equals(method)) {
            updateProfile(request, response);
        } else if ("/profile/password".equals(path) && "POST".equals(method)) {
            changePassword(request, response);
        } else if ("/profile".equals(path) && "DELETE".equals(method)) {
            deleteAccount(request, response);
        } else {
            JsonResponse.write(response, HttpServletResponse.SC_NOT_FOUND, JsonResponse.fail("接口不存在"));
        }
    }

    private void getProfile(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        String userID = SessionHelper.getUserId(request);
        HttpSession session = request.getSession();
        Map<String, Object> profile = new HashMap<>();
        profile.put("userID", userID);
        profile.put("userName", session.getAttribute("userName"));
        profile.put("classID", session.getAttribute("classID"));
        profile.put("gender", session.getAttribute("gender"));
        profile.put("studentID", session.getAttribute("studentID"));
        profile.put("userEmail", session.getAttribute("userEmail"));
        int points = 0;
        Connection conn = DBUtil.getConnection();
        try {
            ensureCheckInTable(conn);
            try (PreparedStatement ps = conn.prepareStatement("SELECT points FROM points WHERE userID = ?")) {
                ps.setString(1, userID);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) points = rs.getInt("points");
            }
            Map<String, Object> penalty = UserGovernance.queryPenalty(conn, userID);
            profile.put("warningCount", penalty.get("warningCount"));
            profile.put("bannedUntil", penalty.get("bannedUntil"));
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT 1 FROM user_checkin WHERE userID = ? AND checkinDate = CURRENT_DATE LIMIT 1")) {
                ps.setString(1, userID);
                ResultSet rs = ps.executeQuery();
                profile.put("checkedInToday", rs.next());
            }
        } finally {
            conn.close();
        }
        profile.put("points", points);
        JsonResponse.write(response, JsonResponse.ok(profile));
    }

    private void checkIn(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        String userID = SessionHelper.getUserId(request);
        Connection conn = DBUtil.getConnection();
        conn.setAutoCommit(false);
        try {
            ensureCheckInTable(conn);
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT 1 FROM user_checkin WHERE userID = ? AND checkinDate = CURRENT_DATE LIMIT 1")) {
                ps.setString(1, userID);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    conn.rollback();
                    JsonResponse.write(response, JsonResponse.fail("今天已打卡，请明天再来"));
                    return;
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO user_checkin (userID, checkinDate) VALUES (?, CURRENT_DATE)")) {
                ps.setString(1, userID);
                ps.executeUpdate();
            }
            ensurePointsRow(conn, userID, (String) request.getSession().getAttribute("userName"));
            try (PreparedStatement ps = conn.prepareStatement("UPDATE points SET points = points + 1 WHERE userID = ?")) {
                ps.setString(1, userID);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO pointop (userID, pointOP, detail, `time`) VALUES (?, '+1', '每日学习打卡', NOW())")) {
                ps.setString(1, userID);
                ps.executeUpdate();
            }
            int points = 0;
            try (PreparedStatement ps = conn.prepareStatement("SELECT points FROM points WHERE userID = ?")) {
                ps.setString(1, userID);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) points = rs.getInt("points");
            }
            conn.commit();
            Map<String, Object> data = new HashMap<>();
            data.put("points", points);
            data.put("checkedInToday", true);
            JsonResponse.write(response, JsonResponse.ok(data));
        } catch (Exception e) {
            conn.rollback();
            JsonResponse.write(response, JsonResponse.fail("打卡失败"));
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    private void ensureCheckInTable(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "CREATE TABLE IF NOT EXISTS user_checkin (" +
                        "id INT PRIMARY KEY AUTO_INCREMENT," +
                        "userID VARCHAR(64) NOT NULL," +
                        "checkinDate DATE NOT NULL," +
                        "checkinTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "UNIQUE KEY uk_user_checkin_day (userID, checkinDate)" +
                        ")")) {
            ps.executeUpdate();
        }
    }

    private void ensurePointsRow(Connection conn, String userID, String userName) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO points (userID, userName, points) VALUES (?, ?, 0) ON DUPLICATE KEY UPDATE userName = VALUES(userName)")) {
            ps.setString(1, userID);
            ps.setString(2, userName == null ? "" : userName);
            ps.executeUpdate();
        }
    }

    private void updateProfile(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        String userID = SessionHelper.getUserId(request);
        String userName = trimParam(request.getParameter("userName"));
        String classID = trimOrEmpty(request.getParameter("classID"));
        String gender = trimOrEmpty(request.getParameter("gender"));
        String studentID = trimOrEmpty(request.getParameter("studentID"));
        String userEmail = trimOrEmpty(request.getParameter("userEmail"));
        if (userName == null || userName.isEmpty()) {
            JsonResponse.write(response, JsonResponse.fail("昵称不能为空"));
            return;
        }
        Connection conn = DBUtil.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE user SET userName=?, classID=?, gender=?, studentID=?, userEmail=? WHERE userphone=?")) {
            ps.setString(1, userName);
            ps.setString(2, classID);
            ps.setString(3, gender);
            ps.setString(4, studentID);
            ps.setString(5, userEmail);
            ps.setString(6, userID);
            ps.executeUpdate();
        } finally {
            conn.close();
        }
        HttpSession session = request.getSession();
        session.setAttribute("userName", userName);
        session.setAttribute("classID", classID);
        session.setAttribute("gender", gender);
        session.setAttribute("studentID", studentID);
        session.setAttribute("userEmail", userEmail);
        JsonResponse.write(response, JsonResponse.ok(SessionHelper.sessionPayload(request)));
    }

    private String trimParam(String value) {
        return value == null ? null : value.trim();
    }

    private String trimOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private void changePassword(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        String userID = SessionHelper.getUserId(request);
        String oldPassword = request.getParameter("oldPassword");
        String newPassword = request.getParameter("newPassword");
        Connection conn = DBUtil.getConnection();
        try (PreparedStatement ps = conn.prepareStatement("SELECT userPassword FROM user WHERE userphone = ?")) {
            ps.setString(1, userID);
            ResultSet rs = ps.executeQuery();
            if (!rs.next() || !oldPassword.equals(rs.getString("userPassword"))) {
                JsonResponse.write(response, JsonResponse.fail("原密码错误"));
                return;
            }
        } finally {
            conn.close();
        }
        conn = DBUtil.getConnection();
        try (PreparedStatement ps = conn.prepareStatement("UPDATE user SET userPassword=? WHERE userphone=?")) {
            ps.setString(1, newPassword);
            ps.setString(2, userID);
            ps.executeUpdate();
        } finally {
            conn.close();
        }
        JsonResponse.write(response, JsonResponse.ok("密码修改成功"));
    }

    private void deleteAccount(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        String userID = SessionHelper.getUserId(request);
        String password = request.getParameter("password");
        if (password == null || password.isEmpty()) {
            JsonResponse.write(response, JsonResponse.fail("请输入密码"));
            return;
        }
        Connection conn = DBUtil.getConnection();
        try (PreparedStatement ps = conn.prepareStatement("SELECT userPassword FROM user WHERE userphone = ?")) {
            ps.setString(1, userID);
            ResultSet rs = ps.executeQuery();
            if (!rs.next() || !password.equals(rs.getString("userPassword"))) {
                JsonResponse.write(response, JsonResponse.fail("密码错误"));
                return;
            }
        } finally {
            conn.close();
        }
        conn = DBUtil.getConnection();
        conn.setAutoCommit(false);
        try {
            UserDataPurgeUtil.purge(conn, userID);
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE user SET userStatus = -1, userName = '', classID = '', gender = '', studentID = '', userEmail = '' WHERE userphone = ?")) {
                ps.setString(1, userID);
                ps.executeUpdate();
            }
            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            JsonResponse.write(response, JsonResponse.fail("账号注销失败"));
            return;
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
        request.getSession().invalidate();
        JsonResponse.write(response, JsonResponse.ok("账号已注销，历史数据已清空"));
    }

    private void sendResetCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String email = request.getParameter("email");
        String userID = request.getParameter("userID");
        if (email == null || userID == null || email.trim().isEmpty() || userID.trim().isEmpty()) {
            JsonResponse.write(response, JsonResponse.fail("参数不完整"));
            return;
        }
        String uid = userID.trim();
        String mail = email.trim();
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            ensureResetCodeTable(conn);
            String dbEmail = null;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT userEmail, userStatus FROM user WHERE userphone = ?")) {
                ps.setString(1, uid);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    JsonResponse.write(response, JsonResponse.fail("用户不存在"));
                    return;
                }
                int status = rs.getInt("userStatus");
                if (status != 1) {
                    JsonResponse.write(response, JsonResponse.fail("该账号状态不可找回密码"));
                    return;
                }
                dbEmail = rs.getString("userEmail");
            }
            if (dbEmail == null || dbEmail.trim().isEmpty() || !dbEmail.trim().equalsIgnoreCase(mail)) {
                JsonResponse.write(response, JsonResponse.fail("手机号与邮箱不匹配"));
                return;
            }

            String code = org.example.util.CodeUtil.generateCode();
            if (!EmailUtil.sendResetPwdCode(mail, code)) {
                JsonResponse.write(response, JsonResponse.fail("邮件发送失败，请稍后重试"));
                return;
            }
            Timestamp expireAt = Timestamp.valueOf(LocalDateTime.now().plusMinutes(10));
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO reset_pwd_code (userID, code, expireAt, used, createdAt) VALUES (?, ?, ?, 0, NOW()) " +
                            "ON DUPLICATE KEY UPDATE code = VALUES(code), expireAt = VALUES(expireAt), used = 0, createdAt = NOW()")) {
                ps.setString(1, uid);
                ps.setString(2, code);
                ps.setTimestamp(3, expireAt);
                ps.executeUpdate();
            }
            JsonResponse.write(response, JsonResponse.ok("验证码已发送到邮箱（10分钟内有效）"));
        } catch (Exception e) {
            JsonResponse.write(response, JsonResponse.fail("发送失败：" + e.getMessage()));
        } finally {
            DBUtil.close(conn, null);
        }
    }

    private void resetPassword(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        String userID = request.getParameter("userID");
        String code = request.getParameter("code");
        String newPassword = request.getParameter("newPassword");
        if (userID == null || code == null || newPassword == null
                || userID.trim().isEmpty() || code.trim().isEmpty() || newPassword.trim().isEmpty()) {
            JsonResponse.write(response, JsonResponse.fail("参数不完整"));
            return;
        }
        String uid = userID.trim();
        String inputCode = code.trim();
        Connection conn = DBUtil.getConnection();
        conn.setAutoCommit(false);
        try {
            ensureResetCodeTable(conn);
            boolean valid = false;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT code, expireAt, used FROM reset_pwd_code WHERE userID = ? FOR UPDATE")) {
                ps.setString(1, uid);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String dbCode = rs.getString("code");
                    Timestamp expireAt = rs.getTimestamp("expireAt");
                    int used = rs.getInt("used");
                    boolean notExpired = expireAt != null && expireAt.after(new Timestamp(System.currentTimeMillis()));
                    valid = used == 0 && notExpired && inputCode.equalsIgnoreCase(dbCode == null ? "" : dbCode.trim());
                }
            }
            if (!valid) {
                conn.rollback();
                JsonResponse.write(response, JsonResponse.fail("验证码错误或已过期"));
                return;
            }
            try (PreparedStatement ps = conn.prepareStatement("UPDATE user SET userPassword=? WHERE userphone=?")) {
                ps.setString(1, newPassword.trim());
                ps.setString(2, uid);
                int n = ps.executeUpdate();
                if (n == 0) {
                    conn.rollback();
                    JsonResponse.write(response, JsonResponse.fail("用户不存在"));
                    return;
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE reset_pwd_code SET used = 1, usedAt = NOW() WHERE userID = ?")) {
                ps.setString(1, uid);
                ps.executeUpdate();
            }
            conn.commit();
            JsonResponse.write(response, JsonResponse.ok("密码重置成功"));
        } catch (Exception e) {
            conn.rollback();
            JsonResponse.write(response, JsonResponse.fail("重置失败：" + e.getMessage()));
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    private void ensureResetCodeTable(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "CREATE TABLE IF NOT EXISTS reset_pwd_code (" +
                        "userID VARCHAR(64) PRIMARY KEY," +
                        "code VARCHAR(16) NOT NULL," +
                        "expireAt TIMESTAMP NOT NULL," +
                        "used TINYINT(1) NOT NULL DEFAULT 0," +
                        "createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "usedAt TIMESTAMP NULL" +
                        ")")) {
            ps.executeUpdate();
        }
    }
}
