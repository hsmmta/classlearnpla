package org.example.servlet.discussion;

import org.example.model.Question;
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

@WebServlet("/discussion/view_content")
public class ViewQuestionContentServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");

        if (isAdmin == null || !isAdmin) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String questionID = request.getParameter("id");
        Question question = null;

        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT q.*, u.userName FROM question q JOIN user u ON q.userID = u.userphone WHERE q.questionID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, questionID);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        question = new Question();
                        question.setQuestionID(rs.getString("questionID"));
                        question.setQuestionTitle(rs.getString("questionTitle"));
                        question.setQuestionContent(rs.getString("questionContent"));
                        question.setCreationTime(rs.getTimestamp("creationTime"));
                        question.setUserName(rs.getString("userName"));
                    }
                }
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            DBUtil.close(conn, null, null);
        }

        request.setAttribute("question", question);
        // We can reuse the public view page for this
        request.getRequestDispatcher("/discussion/ViewQuestion.jsp").forward(request, response);
    }
}