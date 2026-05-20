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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * 设置最满意答案Servlet
 * 功能：
 * 1. 只有提问者可以设置
 * 2. 一个问题只能设置一个最满意答案
 * 3. 设置后不能修改
 * 4. 获得最满意答案的用户获得5积分
 * 5. 在pointop表记录积分变化
 */
@WebServlet("/discussion/setBestAnswer")
public class SetBestAnswerServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String currentUserID = (String) session.getAttribute("userID");

        // 检查用户是否登录
        if (currentUserID == null || currentUserID.trim().isEmpty()) {
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write("<h1>请先登录</h1><p><a href='" + request.getContextPath() + "/auth/signin.html'>返回登录</a></p>");
            return;
        }

        // 获取参数
        String questionID = request.getParameter("questionID");
        String commentIDStr = request.getParameter("commentID");

        if (questionID == null || commentIDStr == null) {
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write("<h1>参数错误</h1>");
            return;
        }

        try {
            int commentID = Integer.parseInt(commentIDStr);
            String result = setBestAnswer(questionID, commentID, currentUserID);

            if ("success".equals(result)) {
                // 设置成功，重定向回问题页面
                response.sendRedirect(request.getContextPath() + "/discussion/view?id=" + questionID);
            } else {
                // 失败，显示错误信息
                response.setContentType("text/html;charset=UTF-8");
                response.getWriter().write("<h1>操作失败</h1><p>" + result + "</p><p><a href='" + request.getContextPath() + "/discussion/view?id=" + questionID + "'>返回</a></p>");
            }
        } catch (NumberFormatException e) {
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write("<h1>参数错误</h1>");
        }
    }

    /**
     * 设置最满意答案的核心逻辑
     */
    private String setBestAnswer(String questionID, int commentID, String currentUserID) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                return "数据库连接失败";
            }

            // 开启事务
            conn.setAutoCommit(false);

            try {
                // 1. 检查问题是否存在，当前用户是否为提问者
                String checkQuestionSql = "SELECT userID, bestAnswerID FROM question WHERE questionID = ?";
                pstmt = conn.prepareStatement(checkQuestionSql);
                pstmt.setString(1, questionID);
                rs = pstmt.executeQuery();

                if (!rs.next()) {
                    conn.rollback();
                    return "问题不存在";
                }

                String questionOwnerID = rs.getString("userID");
                Integer existingBestAnswerID = rs.getInt("bestAnswerID");
                if (rs.wasNull()) {
                    existingBestAnswerID = null;
                }

                // 检查是否为提问者
                if (!questionOwnerID.equals(currentUserID)) {
                    conn.rollback();
                    return "只有提问者可以设置最满意答案";
                }

                // 检查是否已经设置过最满意答案
                if (existingBestAnswerID != null) {
                    conn.rollback();
                    return "该问题已设置过最满意答案，不能修改";
                }

                // 2. 检查评论是否存在，获取回答者ID
                String checkCommentSql = "SELECT userID FROM question_comment WHERE commentID = ? AND questionID = ?";
                pstmt = conn.prepareStatement(checkCommentSql);
                pstmt.setInt(1, commentID);
                pstmt.setString(2, questionID);
                rs = pstmt.executeQuery();

                if (!rs.next()) {
                    conn.rollback();
                    return "回答不存在";
                }

                String answerOwnerID = rs.getString("userID");

                // 3. 更新 question 表，设置 bestAnswerID
                String updateQuestionSql = "UPDATE question SET bestAnswerID = ? WHERE questionID = ?";
                pstmt = conn.prepareStatement(updateQuestionSql);
                pstmt.setInt(1, commentID);
                pstmt.setString(2, questionID);
                pstmt.executeUpdate();

                // 4. 更新 question_comment 表，标记为最满意答案
                String updateCommentSql = "UPDATE question_comment SET isBestAnswer = 1 WHERE commentID = ?";
                pstmt = conn.prepareStatement(updateCommentSql);
                pstmt.setInt(1, commentID);
                pstmt.executeUpdate();

                // 5. 给回答者增加5积分
                String updatePointsSql = "UPDATE points SET points = points + 5 WHERE userID = ?";
                pstmt = conn.prepareStatement(updatePointsSql);
                pstmt.setString(1, answerOwnerID);
                int rowsUpdated = pstmt.executeUpdate();

                // 如果该用户没有积分记录，创建一个
                if (rowsUpdated == 0) {
                    // 查询用户名
                    String getUserNameSql = "SELECT userName FROM user WHERE userphone = ?";
                    pstmt = conn.prepareStatement(getUserNameSql);
                    pstmt.setString(1, answerOwnerID);
                    rs = pstmt.executeQuery();
                    String userName = "";
                    if (rs.next()) {
                        userName = rs.getString("userName");
                    }

                    // 创建积分记录
                    String insertPointsSql = "INSERT INTO points (userID, userName, points) VALUES (?, ?, 5)";
                    pstmt = conn.prepareStatement(insertPointsSql);
                    pstmt.setString(1, answerOwnerID);
                    pstmt.setString(2, userName);
                    pstmt.executeUpdate();
                }

                // 6. 在 pointop 表记录积分变化
                String recordOpSql = "INSERT INTO pointop (userID, pointOP, detail, `time`) VALUES (?, ?, ?, ?)";
                pstmt = conn.prepareStatement(recordOpSql);
                pstmt.setString(1, answerOwnerID);
                pstmt.setString(2, "+5");
                pstmt.setString(3, "获得最满意答案（问题ID：" + questionID + "）");
                pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.executeUpdate();

                // 提交事务
                conn.commit();
                return "success";

            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return "操作失败：" + e.getMessage();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "数据库错误：" + e.getMessage();
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            DBUtil.close(conn, pstmt, rs);
        }
    }
}

