package org.example.servlet.discussion;

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
import java.sql.SQLException;

@WebServlet("/discussion/updateState")
public class UpdateQuestionStateServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String userType = (String) session.getAttribute("userType");

        // 检查是否为管理员
        if (userType == null || !"admin".equals(userType)) {
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write("<h1>无权限</h1><p>您没有权限执行此操作。</p>");
            return;
        }

        String questionID = request.getParameter("id");
        String state = request.getParameter("state");

        if (questionID != null && state != null) {
            Connection conn = null;
            try {
                conn = DBUtil.getConnection();
                String sql = "UPDATE question SET questionState = ? WHERE questionID = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, state);
                    pstmt.setString(2, questionID);
                    pstmt.executeUpdate();
                }
            } catch (SQLException e) {
                throw new ServletException(e);
            } finally {
                DBUtil.close(conn, null, null);
            }
        }
        response.sendRedirect(request.getContextPath() + "/discussion/audit");
    }
}