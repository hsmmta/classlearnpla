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

@WebServlet("/discussion/likeComment")
public class LikeQuestionCommentServlet extends HttpServlet {
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
            conn.setAutoCommit(false); // Start transaction

            // 1. Insert into likes tracking table
            String insertLikeSql = "INSERT IGNORE INTO question_comment_likes (commentID, userID) VALUES (?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertLikeSql)) {
                pstmt.setInt(1, commentID);
                pstmt.setString(2, userID);
                int rowsAffected = pstmt.executeUpdate();

                // 2. If the like was new, increment the counter
                if (rowsAffected > 0) {
                    String updateCountSql = "UPDATE question_comment SET likes = likes + 1 WHERE commentID = ?";
                    try (PreparedStatement updatePstmt = conn.prepareStatement(updateCountSql)) {
                        updatePstmt.setInt(1, commentID);
                        updatePstmt.executeUpdate();
                    }
                }
            }
            conn.commit(); // Commit transaction
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            throw new ServletException(e);
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            DBUtil.close(conn, null, null);
        }

        response.sendRedirect(request.getContextPath() + "/discussion/view?id=" + questionID);
    }
}