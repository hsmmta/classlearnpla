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
import java.sql.SQLException;

/**
 * 积分更新Servlet
 * 用于更新用户积分
 */
@WebServlet("/updatePoints")
public class UpdatePointsServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        // 获取参数
        String userID = request.getParameter("userID");
        String pointsStr = request.getParameter("points");

        if (userID == null || userID.trim().isEmpty() || pointsStr == null || pointsStr.trim().isEmpty()) {
            response.getWriter().write("{\"success\":false, \"msg\":\"参数不完整\"}");
            return;
        }

        try {
            int points = Integer.parseInt(pointsStr);
            if (updatePoints(userID, points)) {
                response.getWriter().write("{\"success\":true, \"msg\":\"积分更新成功\"}");
            } else {
                response.getWriter().write("{\"success\":false, \"msg\":\"积分更新失败\"}");
            }
        } catch (NumberFormatException e) {
            response.getWriter().write("{\"success\":false, \"msg\":\"积分参数错误\"}");
        }
    }

    /**
     * 更新用户积分
     * @param userID 用户ID（手机号）
     * @param points 新的积分值
     * @return 是否更新成功
     */
    private boolean updatePoints(String userID, int points) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                return false;
            }

            // 先尝试更新，如果没有记录则插入
            String updateSql = "UPDATE points SET points = ? WHERE userID = ?";
            pstmt = conn.prepareStatement(updateSql);
            pstmt.setInt(1, points);
            pstmt.setString(2, userID);
            int rows = pstmt.executeUpdate();

            if (rows == 0) {
                // 没有更新任何记录，说明该用户不存在，需要插入
                String userName = getUserNameByUserID(userID);
                String insertSql = "INSERT INTO points (userID, userName, points) VALUES (?, ?, ?)";
                pstmt = conn.prepareStatement(insertSql);
                pstmt.setString(1, userID);
                pstmt.setString(2, userName);
                pstmt.setInt(3, points);
                pstmt.executeUpdate();
            }

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    /**
     * 根据userID获取用户名
     */
    private String getUserNameByUserID(String userID) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        java.sql.ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT userName FROM user WHERE userphone = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userID);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("userName");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return "";
    }
}

