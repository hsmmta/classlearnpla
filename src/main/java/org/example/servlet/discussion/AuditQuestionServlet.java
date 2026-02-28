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
import java.util.ArrayList;
import java.util.List;

@WebServlet("/discussion/audit")
public class AuditQuestionServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String userType = (String) session.getAttribute("userType");

        // 检查是否为管理员
        if (userType == null || !"admin".equals(userType)) {
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write("<h1>无权限</h1><p>您没有权限访问此页面。</p><a href='" + request.getContextPath() + "/auth/adminSignin.html'>返回管理员登录</a>");
            return;
        }

        List<Question> questions = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT q.*, u.userName FROM question q JOIN user u ON q.userID = u.userphone WHERE q.questionState = '待审核' ORDER BY q.creationTime ASC";
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Question question = new Question();
                    question.setQuestionID(rs.getString("questionID"));
                    question.setQuestionTitle(rs.getString("questionTitle"));
                    question.setCreationTime(rs.getTimestamp("creationTime"));
                    question.setUserName(rs.getString("userName"));
                    questions.add(question);
                }
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            DBUtil.close(conn, null, null);
        }

        request.setAttribute("pendingQuestions", questions);
        request.getRequestDispatcher("/discussion/AuditQuestion.jsp").forward(request, response);
    }
}