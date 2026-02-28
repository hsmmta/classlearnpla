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
import java.util.UUID;

@WebServlet("/discussion/ask")
public class AskQuestionServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        String userID = (String) session.getAttribute("userID");

        if (userID == null || userID.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/auth/signin.html");
            return;
        }

        String questionTitle = request.getParameter("questionTitle");
        String questionContent = request.getParameter("questionContent");
        String questionID = UUID.randomUUID().toString();

        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();

            // 首先验证userID是否存在于user表中
            String checkUserSql = "SELECT userphone FROM user WHERE userphone = ?";
            pstmt = conn.prepareStatement(checkUserSql);
            pstmt.setString(1, userID);
            java.sql.ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                // userID不存在，返回错误提示
                response.setContentType("text/html;charset=UTF-8");
                response.getWriter().write("<h1>错误</h1><p>用户不存在，请重新登录。</p><a href='" + request.getContextPath() + "/auth/signin.html'>返回登录</a>");
                return;
            }
            rs.close();
            pstmt.close();

            // 插入问题
            String sql = "INSERT INTO question (questionID, questionTitle, questionContent, userID, questionState) VALUES (?, ?, ?, ?, '待审核')";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, questionID);
            pstmt.setString(2, questionTitle);
            pstmt.setString(3, questionContent);
            pstmt.setString(4, userID);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new ServletException("发布问题失败：" + e.getMessage(), e);
        } finally {
            DBUtil.close(conn, pstmt);
        }

        // 重定向到讨论列表页面
        response.sendRedirect(request.getContextPath() + "/discussion/list");
    }
}