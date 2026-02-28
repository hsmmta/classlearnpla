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

@WebServlet("/changePassword")
public class ChangePasswordServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. 设置编码
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        // 2. 校验用户是否登录（Session中是否有userID）
        HttpSession session = request.getSession();
        String userID = (String) session.getAttribute("userID");
        if (userID == null || userID.trim().isEmpty()) {
            // 未登录：提示 + 跳转登录页
            String json = "{\"success\":false, \"msg\":\"请先登录！\", \"redirectUrl\":\"/auth/signin.html\"}";
            response.getWriter().write(json);
            return;
        }

        // 3. 获取前端参数
        String oldPassword = request.getParameter("oldPassword");
        String newPassword = request.getParameter("newPassword");
        boolean success = false;
        String msg = "";

        // 4. 非空校验
        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            msg = "原密码不能为空！";
        } else if (newPassword == null || newPassword.trim().isEmpty()) {
            msg = "新密码不能为空！";
        } else if (newPassword.length() < 6) {
            msg = "新密码长度不能少于6位！";
        } else {
            // 5. 校验原密码是否正确
            if (verifyOldPassword(userID, oldPassword)) {
                // 6. 原密码正确：修改密码
                if (updateNewPassword(userID, newPassword)) {
                    success = true;
                    msg = "密码修改成功！请重新登录";
                    // 清空Session，强制重新登录
                    session.invalidate();
                } else {
                    msg = "密码修改失败！数据库更新出错";
                }
            } else {
                msg = "原密码输入错误！"; // 核心：原密码错误弹窗提示
            }
        }

        // 7. 构造JSON响应
        String jsonResponse = String.format(
                "{\"success\":%b, \"msg\":\"%s\", \"redirectUrl\":\"%s\"}",
                success,
                msg.replace("\"", "\\\""),
                success ? "/auth/signin.html" : ""
        );
        response.getWriter().write(jsonResponse);
    }

    /**
     * 校验原密码是否正确
     */
    private boolean verifyOldPassword(String userID, String oldPassword) {
        org.example.util.DBUtil DBUtil = new org.example.util.DBUtil();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT userPassword FROM user WHERE userphone = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userID);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                String dbPassword = rs.getString("userPassword");
                // 对比原密码（测试阶段明文，后续可加密）
                return dbPassword.equals(oldPassword);
            } else {
                return false; // 用户不存在
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            org.example.util.DBUtil.close(conn, pstmt, rs);
        }
    }

    /**
     * 更新新密码到数据库
     */
    private boolean updateNewPassword(String userID, String newPassword) {
        org.example.util.DBUtil DBUtil = new org.example.util.DBUtil();
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "UPDATE user SET userPassword = ? WHERE userphone = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newPassword); // 测试阶段明文，后续建议加密
            pstmt.setString(2, userID);
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            org.example.util.DBUtil.close(conn, pstmt, null);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/personalInfo/changePassword.html");
    }
}