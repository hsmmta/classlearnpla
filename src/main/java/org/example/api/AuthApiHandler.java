package org.example.api;

import org.example.util.AliyunSmsUtil;
import org.example.util.CodeUtil;
import org.example.util.DBUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class AuthApiHandler {

    void session(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> payload = SessionHelper.sessionPayload(request);
        if (payload == null) {
            JsonResponse.write(response, HttpServletResponse.SC_UNAUTHORIZED, JsonResponse.fail("未登录"));
            return;
        }
        JsonResponse.write(response, JsonResponse.ok(payload));
    }

    void handle(String path, String method, HttpServletRequest request, HttpServletResponse response) throws IOException {
        switch (path) {
            case "/auth/login" -> {
                if ("POST".equals(method)) login(request, response);
                else methodNotAllowed(response);
            }
            case "/auth/register" -> {
                if ("POST".equals(method)) register(request, response);
                else methodNotAllowed(response);
            }
            case "/auth/send-code" -> {
                if ("POST".equals(method)) sendCode(request, response);
                else methodNotAllowed(response);
            }
            case "/auth/admin-login" -> {
                if ("POST".equals(method)) adminLogin(request, response);
                else methodNotAllowed(response);
            }
            case "/auth/logout" -> {
                if ("POST".equals(method)) logout(request, response);
                else methodNotAllowed(response);
            }
            default -> JsonResponse.write(response, HttpServletResponse.SC_NOT_FOUND, JsonResponse.fail("接口不存在"));
        }
    }

    private void methodNotAllowed(HttpServletResponse response) throws IOException {
        JsonResponse.write(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED, JsonResponse.fail("方法不允许"));
    }

    private void login(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String userID = request.getParameter("userID");
        String loginType = request.getParameter("loginType");
        HttpSession session = request.getSession();
        boolean loginSuccess = false;
        String msg = "";
        String userType = null;

        if (userID == null || userID.trim().isEmpty()) {
            msg = "手机号不能为空";
        } else if ("password".equals(loginType)) {
            String userPassword = request.getParameter("userPassword");
            if (userPassword == null || userPassword.trim().isEmpty()) {
                msg = "密码不能为空";
            } else {
                String banMsg = queryBanMessage(userID);
                if (banMsg != null) {
                    msg = banMsg;
                    JsonResponse.write(response, JsonResponse.fail(msg));
                    return;
                }
                String status = checkUserStatus(userID);
                if (!"not_exist".equals(status)) {
                    if ("closed".equals(status)) msg = "该账号已注销";
                    else if ("inactivated".equals(status)) msg = "该账号未激活";
                    else if ("error".equals(status)) msg = "系统异常";
                    else {
                        loginSuccess = verifyPassword(userID, userPassword, session);
                        if (!loginSuccess) msg = "手机号或密码错误";
                        else userType = "user";
                    }
                } else if (verifyAdmin(userID, userPassword, session)) {
                    loginSuccess = true;
                    userType = "admin";
                } else {
                    msg = "手机号或密码错误";
                }
            }
        } else if ("code".equals(loginType)) {
            String code = request.getParameter("code");
            String sessionCode = (String) session.getAttribute("code");
            if (sessionCode == null || code == null || !sessionCode.trim().equals(code.trim())) {
                msg = "验证码错误或已过期";
            } else {
                String banMsg = queryBanMessage(userID);
                if (banMsg != null) {
                    msg = banMsg;
                    JsonResponse.write(response, JsonResponse.fail(msg));
                    return;
                }
                String status = checkUserStatus(userID);
                if ("closed".equals(status)) msg = "该账号已注销";
                else if ("inactivated".equals(status)) msg = "该账号未激活";
                else if ("error".equals(status)) msg = "系统异常";
                else {
                    loginSuccess = fillUserInfoToSession(userID, session);
                    if (!loginSuccess) msg = "该手机号未注册";
                    else {
                        userType = "user";
                        session.removeAttribute("code");
                    }
                }
            }
        } else {
            msg = "请选择登录方式";
        }

        if (loginSuccess) {
            Map<String, Object> data = SessionHelper.sessionPayload(request);
            JsonResponse.write(response, JsonResponse.ok(data));
        } else {
            JsonResponse.write(response, JsonResponse.fail(msg));
        }
    }

    private void register(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String userID = request.getParameter("userID");
        String code = request.getParameter("code");
        String userPassword = request.getParameter("userPassword");
        String repassword = request.getParameter("repassword");
        String userName = request.getParameter("userName");
        String classID = request.getParameter("classID");
        String studentID = request.getParameter("studentID");
        String gender = request.getParameter("gender");
        String userEmail = request.getParameter("userEmail");

        if (userID == null || userID.trim().isEmpty() || code == null || code.trim().isEmpty()
                || userPassword == null || userPassword.trim().isEmpty()
                || userName == null || userName.trim().isEmpty()) {
            JsonResponse.write(response, JsonResponse.fail("必填项不能为空"));
            return;
        }
        if (!userPassword.equals(repassword)) {
            JsonResponse.write(response, JsonResponse.fail("两次密码不一致"));
            return;
        }
        HttpSession session = request.getSession();
        String sessionCode = (String) session.getAttribute("code");
        if (sessionCode == null || !sessionCode.trim().equals(code.trim())) {
            JsonResponse.write(response, JsonResponse.fail("验证码错误或已过期"));
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                JsonResponse.write(response, JsonResponse.fail("数据库连接失败"));
                return;
            }
            // If the account was previously closed (userStatus = -1), allow re-registration and reactivate it.
            pstmt = conn.prepareStatement("SELECT userStatus FROM user WHERE userphone = ?");
            pstmt.setString(1, userID);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                int status = rs.getInt("userStatus");
                DBUtil.close(null, pstmt, rs);
                pstmt = null;
                rs = null;
                if (status == -1) {
                    pstmt = conn.prepareStatement(
                            "UPDATE user SET userPassword=?, userName=?, classID=?, studentID=?, gender=?, userEmail=?, userStatus=1 WHERE userphone=?");
                    pstmt.setString(1, userPassword);
                    pstmt.setString(2, userName);
                    pstmt.setString(3, classID == null ? "" : classID);
                    pstmt.setString(4, studentID == null ? "" : studentID);
                    pstmt.setString(5, gender == null ? "" : gender);
                    pstmt.setString(6, userEmail == null ? "" : userEmail);
                    pstmt.setString(7, userID);
                    pstmt.executeUpdate();
                    DBUtil.close(null, pstmt);
                    pstmt = null;
                    try {
                        PreparedStatement pp = conn.prepareStatement(
                                "INSERT INTO points (userID, userName, points) VALUES (?, ?, 0) " +
                                        "ON DUPLICATE KEY UPDATE userName = VALUES(userName)");
                        pp.setString(1, userID);
                        pp.setString(2, userName);
                        pp.executeUpdate();
                        pp.close();
                    } catch (SQLException ignored) {
                    }
                    session.removeAttribute("code");
                    JsonResponse.write(response, JsonResponse.ok("注册成功（已恢复原注销账号）"));
                    return;
                }
                JsonResponse.write(response, JsonResponse.fail("该手机号已注册"));
                return;
            }
            DBUtil.close(null, pstmt, rs);
            pstmt = null;
            rs = null;

            String sql = "INSERT INTO user (userphone, userPassword, userName, classID, studentID, gender, userEmail, userStatus) VALUES (?, ?, ?, ?, ?, ?, ?, 1)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userID);
            pstmt.setString(2, userPassword);
            pstmt.setString(3, userName);
            pstmt.setString(4, classID == null ? "" : classID);
            pstmt.setString(5, studentID == null ? "" : studentID);
            pstmt.setString(6, gender == null ? "" : gender);
            pstmt.setString(7, userEmail == null ? "" : userEmail);
            pstmt.executeUpdate();
            try {
                PreparedStatement pp = conn.prepareStatement("INSERT INTO points (userID, userName, points) VALUES (?, ?, 0)");
                pp.setString(1, userID);
                pp.setString(2, userName);
                pp.executeUpdate();
                pp.close();
            } catch (SQLException ignored) {
            }
            session.removeAttribute("code");
            JsonResponse.write(response, JsonResponse.ok("注册成功"));
        } catch (SQLException e) {
            JsonResponse.write(response, JsonResponse.fail("注册失败: " + e.getMessage()));
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    private void sendCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String phone = request.getParameter("phone");
        if (phone == null || phone.trim().isEmpty()) {
            JsonResponse.write(response, JsonResponse.fail("手机号不能为空"));
            return;
        }
        String code = CodeUtil.generateCode();
        if (!AliyunSmsUtil.sendVerifyCode(phone.trim(), code)) {
            JsonResponse.write(response, JsonResponse.fail(AliyunSmsUtil.getLastError()));
            return;
        }
        HttpSession session = request.getSession();
        session.setAttribute("code", code);
        session.setMaxInactiveInterval(300);
        JsonResponse.write(response, JsonResponse.ok("验证码已发送"));
    }

    private void adminLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String adminID = request.getParameter("adminID");
        String adminPassword = request.getParameter("adminPassword");
        if (adminID == null || adminPassword == null) {
            JsonResponse.write(response, JsonResponse.fail("参数不完整"));
            return;
        }
        HttpSession session = request.getSession();
        if (verifyAdmin(adminID, adminPassword, session)) {
            JsonResponse.write(response, JsonResponse.ok(SessionHelper.sessionPayload(request)));
        } else {
            JsonResponse.write(response, JsonResponse.fail("管理员账号或密码错误"));
        }
    }

    private void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        JsonResponse.write(response, JsonResponse.ok("已退出"));
    }

    private String checkUserStatus(String userID) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement("SELECT userStatus FROM user WHERE userphone = ?");
            pstmt.setString(1, userID);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                int status = rs.getInt("userStatus");
                if (status == 1) return "activate";
                if (status == 0) return "inactivated";
                if (status == -1) return "closed";
            }
            return "not_exist";
        } catch (SQLException e) {
            return "error";
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    private boolean verifyPassword(String userID, String userPassword, HttpSession session) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement("SELECT userName, userPassword, classID, gender, studentID, userEmail FROM user WHERE userphone = ? AND userStatus = 1");
            pstmt.setString(1, userID);
            rs = pstmt.executeQuery();
            if (rs.next() && userPassword.equals(rs.getString("userPassword"))) {
                session.setAttribute("userName", rs.getString("userName"));
                session.setAttribute("userID", userID);
                session.setAttribute("classID", rs.getString("classID"));
                session.setAttribute("gender", rs.getString("gender"));
                session.setAttribute("studentID", rs.getString("studentID"));
                session.setAttribute("userEmail", rs.getString("userEmail"));
                session.setAttribute("userType", "user");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return false;
    }

    private boolean fillUserInfoToSession(String userID, HttpSession session) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement("SELECT userName, classID, gender, studentID, userEmail FROM user WHERE userphone = ? AND userStatus = 1");
            pstmt.setString(1, userID);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                session.setAttribute("userName", rs.getString("userName"));
                session.setAttribute("userID", userID);
                session.setAttribute("classID", rs.getString("classID"));
                session.setAttribute("gender", rs.getString("gender"));
                session.setAttribute("studentID", rs.getString("studentID"));
                session.setAttribute("userEmail", rs.getString("userEmail"));
                session.setAttribute("userType", "user");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return false;
    }

    private boolean verifyAdmin(String adminID, String adminPassword, HttpSession session) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement("SELECT adminPassword FROM admin WHERE adminID = ?");
            pstmt.setString(1, adminID);
            rs = pstmt.executeQuery();
            if (rs.next() && adminPassword.equals(rs.getString("adminPassword"))) {
                session.setAttribute("userID", adminID);
                session.setAttribute("userType", "admin");
                session.setAttribute("userName", "管理员");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return false;
    }

    private String queryBanMessage(String userID) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            if (conn == null) return "系统异常";
            Timestamp bannedUntil = UserGovernance.queryBannedUntil(conn, userID);
            if (bannedUntil != null && bannedUntil.after(new Timestamp(System.currentTimeMillis()))) {
                return "账号已被封禁至 " + bannedUntil;
            }
            return null;
        } catch (Exception e) {
            return "系统异常";
        } finally {
            DBUtil.close(conn, null);
        }
    }
}
