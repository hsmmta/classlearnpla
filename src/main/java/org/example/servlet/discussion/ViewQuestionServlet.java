package org.example.servlet.discussion;

import org.example.model.Question;
import org.example.model.QuestionComment;
import org.example.util.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/discussion/view")
public class ViewQuestionServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String questionID = request.getParameter("id");
        Question question = null;
        List<QuestionComment> comments = new ArrayList<>();

        Connection conn = null;
        try {
            conn = DBUtil.getConnection();

            // Get Question Details
            String questionSql = "SELECT q.*, u.userName FROM question q JOIN user u ON q.userID = u.userphone WHERE q.questionID = ? AND q.questionState = '审核通过'";
            try (PreparedStatement pstmt = conn.prepareStatement(questionSql)) {
                pstmt.setString(1, questionID);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        question = new Question();
                        question.setQuestionID(rs.getString("questionID"));
                        question.setQuestionTitle(rs.getString("questionTitle"));
                        question.setQuestionContent(rs.getString("questionContent"));
                        question.setUserID(rs.getString("userID")); // 添加userID，用于判断是否为提问者
                        question.setCreationTime(rs.getTimestamp("creationTime"));
                        question.setUserName(rs.getString("userName"));
                        // 获取最满意答案ID
                        try {
                            int bestAnswerID = rs.getInt("bestAnswerID");
                            question.setBestAnswerID(rs.wasNull() ? null : bestAnswerID);
                        } catch (SQLException e) {
                            // 如果列不存在，设置为null
                            question.setBestAnswerID(null);
                        }
                    }
                }
            }

            // Get Comments
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
                            // 添加最满意答案标记
                            try {
                                comment.setBestAnswer(rs.getBoolean("isBestAnswer"));
                            } catch (SQLException e) {
                                // 如果列不存在，默认为false
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
        request.getRequestDispatcher("/discussion/ViewQuestion.jsp").forward(request, response);
    }
}

// Add a setter for likes in QuestionComment model
// I will assume this is done in the model file. If not, I would add:
// public void setLikes(int likes) { this.likes = likes; }
// public int getLikes() { return likes; }