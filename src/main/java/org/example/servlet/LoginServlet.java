package org.example.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        String userID = request.getParameter("userID");
        String loginType = request.getParameter("loginType");
        HttpSession session = request.getSession();
        String contextPath = request.getContextPath();
        boolean loginSuccess = false;
        String msg = "";
        String redirectUrl = ""; // 新增：根据用户类型存储跳转路径

        // 1. 手机号非空校验（前置）
        if (userID == null || userID.trim().isEmpty()) {
            msg = "登录失败！手机号不能为空";
        }
        // 2. 密码登录逻辑
        else if ("password".equals(loginType)) {
            String userPassword = request.getParameter("userPassword");
            if (userPassword == null || userPassword.trim().isEmpty()) {
                msg = "登录失败！密码不能为空";
            } else {
                // 1. 先尝试在普通用户表中查找状态
                String statusCheck = checkUserStatus(userID);

                // 2. 如果是普通用户（状态不是 not_exist）
                if (!"not_exist".equals(statusCheck)) {
                    if ("closed".equals(statusCheck)) {
                        msg = "登录失败！该账号已注销，无法登录";
                    } else if ("inactivated".equals(statusCheck)) {
                        msg = "登录失败！该账号未激活，请先完成激活";
                    } else if ("error".equals(statusCheck)) {
                        msg = "登录失败！系统异常，请稍后重试";
                    } else {
                        // 状态正常，验证普通用户密码
                        loginSuccess = verifyPassword(userID, userPassword, session);
                        if (!loginSuccess) {
                            msg = "登录失败！手机号或密码错误";
                        } else {
                            // 普通用户登录成功
                            redirectUrl = contextPath + "/home/index.jsp";
                        }
                    }
                }
                // 3. 如果普通用户表中没找到，尝试在管理员表中查找
                else {
                    if (verifyAdmin(userID, userPassword, session)) {
                        loginSuccess = true;
                        redirectUrl = contextPath + "/Administrator/index.jsp";
                    } else {
                        // 管理员表也没找到，或者密码错误
                        msg = "登录失败！手机号或密码错误";
                    }
                }
            }
        }
        // 3. 验证码登录逻辑
        else if ("code".equals(loginType)) {
            String code = request.getParameter("code");
            String sessionCode = (String) session.getAttribute("code");
            if (sessionCode == null || code == null || code.trim().isEmpty()) {
                msg = "登录失败！验证码不能为空";
            } else if (!sessionCode.trim().equals(code.trim())) {
                msg = "登录失败！验证码错误或已过期";
            } else {
                String statusCheck = checkUserStatus(userID);
                if ("closed".equals(statusCheck)) {
                    msg = "登录失败！该账号已注销，无法登录";
                } else if ("inactivated".equals(statusCheck)) {
                    msg = "登录失败！该账号未激活，请先完成激活";
                } else if ("error".equals(statusCheck)) {
                    msg = "登录失败！系统异常，请稍后重试";
                } else {
                    // 状态为activate（已激活），再填充用户信息
                    loginSuccess = fillUserInfoToSession(userID, session);
                    if (!loginSuccess) {
                        msg = "登录失败！该手机号未注册";
                    } else {
                        // 验证码登录默认为普通用户
                        redirectUrl = contextPath + "/home/index.jsp";
                    }
                }
            }
        }
        // 4. 未知登录类型
        else {
            msg = "登录失败！请选择登录方式";
        }

        // 5. 构造JSON响应
        String jsonResponse = String.format(
                "{\"success\":%b, \"msg\":\"%s\", \"redirectUrl\":\"%s\"}",
                loginSuccess,
                msg.replace("\"", "\\\""),
                redirectUrl
        );

        // 6. 返回JSON
        response.getWriter().write(jsonResponse);
    }


    // 新增：验证管理员的方法
    private boolean verifyAdmin(String adminID, String adminPassword, HttpSession session) {
        org.example.util.DBUtil DBUtil = new org.example.util.DBUtil();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT * FROM admin WHERE adminID = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, adminID);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                String dbPassword = rs.getString("adminPassword");
                if (dbPassword != null && dbPassword.equals(adminPassword)) {
                    session.setAttribute("userID", adminID);
                    session.setAttribute("userType", "admin");
                    session.setAttribute("userName", "管理员");
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            org.example.util.DBUtil.close(conn, pstmt, rs);
        }
    }

    // 以下原有方法保持不变
    private String checkUserStatus(String userID) {
        org.example.util.DBUtil DBUtil = new org.example.util.DBUtil();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT userStatus FROM user WHERE userphone = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userID);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                int status = rs.getInt("userStatus");
                if (status == 1) return "activate";
                if (status == 0) return "inactivated"; // 假设0为未激活
                if (status == -1) return "closed"; // 假设-1为注销
                return String.valueOf(status);
            } else {
                return "not_exist";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "error";
        } finally {
            org.example.util.DBUtil.close(conn, pstmt, rs);
        }
    }

    private boolean verifyPassword(String userID, String userPassword, HttpSession session) {
        org.example.util.DBUtil DBUtil = new org.example.util.DBUtil();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT userName, userPassword, classID, gender, studentID, userEmail FROM user WHERE userphone = ? AND userStatus = 1";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userID);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                String dbPassword = rs.getString("userPassword");
                if (dbPassword.equals(userPassword)) {
                    // 补全Session字段
                    String userName = rs.getString("userName");
                    String classID = rs.getString("classID");
                    String gender = rs.getString("gender");
                    String studentID = rs.getString("studentID");
                    String userEmail = rs.getString("userEmail");

                    session.setAttribute("userName", userName);
                    session.setAttribute("userID", userID);
                    session.setAttribute("classID", classID);
                    session.setAttribute("gender", gender);
                    session.setAttribute("studentID", studentID);
                    session.setAttribute("userEmail", userEmail);
                    session.setAttribute("userType", "user"); // 默认为普通用户
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            org.example.util.DBUtil.close(conn, pstmt, rs);
        }
    }

    private boolean fillUserInfoToSession(String userID, HttpSession session) {
        org.example.util.DBUtil DBUtil = new org.example.util.DBUtil();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT userName, classID, gender, studentID, userEmail FROM user WHERE userphone = ? AND userStatus = 1";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userID);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                session.setAttribute("userName", rs.getString("userName"));
                session.setAttribute("userID", userID);
                session.setAttribute("classID", rs.getString("classID"));
                session.setAttribute("gender", rs.getString("gender"));
                session.setAttribute("studentID", rs.getString("studentID"));
                session.setAttribute("userEmail", rs.getString("userEmail"));
                session.setAttribute("userType", "user"); // 默认为普通用户
                session.removeAttribute("code");
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            org.example.util.DBUtil.close(conn, pstmt, rs);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/auth/signin.html");
    }
}


