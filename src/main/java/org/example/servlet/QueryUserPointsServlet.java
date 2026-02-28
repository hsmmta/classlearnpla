package org.example.servlet;

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

/**
 * 查询用户积分Servlet
 * 管理员用于查询用户当前积分
 */
@WebServlet("/queryUserPoints")
public class QueryUserPointsServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        // 获取参数
        String userID = request.getParameter("userID");

        if (userID == null || userID.trim().isEmpty()) {
            response.getWriter().write("{\"success\":false, \"msg\":\"用户ID不能为空\"}");
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                response.getWriter().write("{\"success\":false, \"msg\":\"数据库连接失败\"}");
                return;
            }

            // 查询用户积分
            String sql = "SELECT points FROM points WHERE userID = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userID);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                int points = rs.getInt("points");
                response.getWriter().write("{\"success\":true, \"points\":" + points + ", \"msg\":\"查询成功\"}");
            } else {
                response.getWriter().write("{\"success\":false, \"msg\":\"用户不存在或未初始化积分\"}");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().write("{\"success\":false, \"msg\":\"查询失败：" + e.getMessage() + "\"}");
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }
}

