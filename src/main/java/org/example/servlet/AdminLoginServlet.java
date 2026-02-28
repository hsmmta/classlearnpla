package org.example.servlet;

import org.example.util.DBUtil;

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

/**
 * 管理员登录Servlet
 * 专门处理管理员登录请求
 */
@WebServlet("/adminLogin")
public class AdminLoginServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        // 获取参数
        String adminID = request.getParameter("adminID");
        String adminPassword = request.getParameter("adminPassword");

        // 参数验证
        if (adminID == null || adminID.trim().isEmpty()) {
            response.getWriter().write("{\"success\":false, \"msg\":\"管理员账号不能为空\"}");
            return;
        }

        if (adminPassword == null || adminPassword.trim().isEmpty()) {
            response.getWriter().write("{\"success\":false, \"msg\":\"管理员密码不能为空\"}");
            return;
        }

        // 验证管理员
        HttpSession session = request.getSession();
        if (verifyAdmin(adminID, adminPassword, session)) {
            String redirectUrl = request.getContextPath() + "/Administrator/index.jsp";
            response.getWriter().write("{\"success\":true, \"msg\":\"登录成功\", \"redirectUrl\":\"" + redirectUrl + "\"}");
        } else {
            response.getWriter().write("{\"success\":false, \"msg\":\"管理员账号或密码错误\"}");
        }
    }

    /**
     * 验证管理员账号和密码
     */
    private boolean verifyAdmin(String adminID, String adminPassword, HttpSession session) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                return false;
            }

            // 查询管理员账号
            String sql = "SELECT adminPassword FROM admin WHERE adminID = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, adminID);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                String dbPassword = rs.getString("adminPassword");
                if (dbPassword != null && dbPassword.equals(adminPassword)) {
                    // 验证成功，设置session
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
            DBUtil.close(conn, pstmt, rs);
        }
    }
}

