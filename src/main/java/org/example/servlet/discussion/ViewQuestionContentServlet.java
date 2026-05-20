package org.example.servlet.discussion;

import org.example.model.Question;
import org.example.model.QuestionComment;
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
import java.util.ArrayList;
import java.util.List;

@WebServlet("/discussion/view_content")
public class ViewQuestionContentServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String userType = (String) session.getAttribute("userType");

        if (!"admin".equals(userType)) {
            response.setContentType("text/html;charset=UTF-8");
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String questionID = request.getParameter("id");
        Question question = null;
        List<QuestionComment> comments = new ArrayList<QuestionComment>();

        Connection conn = null;
        try {
            conn = DBUtil.getConnection();

            // 管理员查看不限制 questionState，所以待审核的也能看到
            String sql = "SELECT q.*, u.userName FROM question q JOIN user u ON q.userID = u.userphone WHERE q.questionID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, questionID);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        question = new Question();
                        question.setQuestionID(rs.getString("questionID"));
                        question.setQuestionTitle(rs.getString("questionTitle"));
                        question.setQuestionContent(rs.getString("questionContent"));
                        question.setUserID(rs.getString("userID"));
                        question.setCreationTime(rs.getTimestamp("creationTime"));
                        question.setUserName(rs.getString("userName"));
                        try {
                            int bestAnswerID = rs.getInt("bestAnswerID");
                            question.setBestAnswerID(rs.wasNull() ? null : bestAnswerID);
                        } catch (SQLException e) {
                            question.setBestAnswerID(null);
                        }
                    }
                }
            }

            // 加载评论/回答
            if (question != null) {
                String commentSql = "SELECT qc.*, u.userName FROM question_comment qc JOIN user u ON qc.userID = u.userphone WHERE qc.questionID = ? ORDER BY qc.commentTime ASC";
                try (PreparedStatement pstmt = conn.prepareStatement(commentSql)) {
                    pstmt.setString(1, questionID);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            QuestionComment comment = new QuestionComment();
                            comment.setCommentID(rs.getInt("commentID"));
                            comment.setUserID(rs.getString("userID"));
                            comment.setCommentContent(rs.getString("commentContent"));
                            comment.setCommentTime(rs.getTimestamp("commentTime"));
                            comment.setUserName(rs.getString("userName"));
                            comment.setFavorable(rs.getBoolean("isFavorable"));
                            comment.setLikes(rs.getInt("likes"));
                            try {
                                comment.setBestAnswer(rs.getBoolean("isBestAnswer"));
                            } catch (SQLException e) {
                                comment.setBestAnswer(false);
                            }
                            comments.add(comment);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            DBUtil.close(conn, null, null);
        }

        request.setAttribute("question", question);
        request.setAttribute("comments", comments);
        request.getRequestDispatcher("/discussion/AdminViewQuestion.jsp").forward(request, response);
    }
}

