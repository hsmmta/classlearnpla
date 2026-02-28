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

@WebServlet("/discussion/comment")
public class AddQuestionCommentServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        String userID = (String) session.getAttribute("userID");

        if (userID == null) {
            response.sendRedirect(request.getContextPath() + "/auth/signin.html");
            return;
        }

        String questionID = request.getParameter("questionID");
        String commentContent = request.getParameter("commentContent");

        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "INSERT INTO question_comment (questionID, userID, commentContent) VALUES (?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, questionID);
            pstmt.setString(2, userID);
            pstmt.setString(3, commentContent);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            DBUtil.close(conn, pstmt);
        }

        response.sendRedirect(request.getContextPath() + "/discussion/view?id=" + questionID);
    }
}