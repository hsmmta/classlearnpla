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

@WebServlet("/discussion/deleteComment")
public class DeleteQuestionCommentServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String userID = (String) session.getAttribute("userID");
        String questionID = request.getParameter("questionID");
        int commentID = Integer.parseInt(request.getParameter("commentID"));

        if (userID == null) {
            response.sendRedirect(request.getContextPath() + "/auth/signin.html");
            return;
        }

        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            // Users can only delete their own comments
            String sql = "DELETE FROM question_comment WHERE commentID = ? AND userID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, commentID);
                pstmt.setString(2, userID);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            DBUtil.close(conn, null, null);
        }

        response.sendRedirect(request.getContextPath() + "/discussion/view?id=" + questionID);
    }
}