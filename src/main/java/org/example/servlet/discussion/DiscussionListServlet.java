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

@WebServlet("/discussion/list")
public class DiscussionListServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Check if user is logged in
        HttpSession session = request.getSession();
        String userID = (String) session.getAttribute("userID");
        if (userID == null) {
            response.sendRedirect(request.getContextPath() + "/auth/signin.html");
            return;
        }

        List<Question> questions = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                throw new SQLException("无法获取数据库连接");
            }
            String sql = "SELECT q.questionID, q.questionTitle, q.creationTime, u.userName " +
                         "FROM question q JOIN user u ON q.userID = u.userphone " +
                         "WHERE q.questionState = '审核通过' ORDER BY q.creationTime DESC";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Question question = new Question();
                question.setQuestionID(rs.getString("questionID"));
                question.setQuestionTitle(rs.getString("questionTitle"));
                question.setCreationTime(rs.getTimestamp("creationTime"));
                question.setUserName(rs.getString("userName"));
                questions.add(question);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ServletException("数据库查询出错: " + e.getMessage(), e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }

        request.setAttribute("questions", questions);
        request.getRequestDispatcher("/discussion/DiscussionList.jsp").forward(request, response);
    }
}