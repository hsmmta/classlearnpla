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
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * 修改用户积分Servlet
 * 管理员用于增加、减少或设置用户积分
 */
@WebServlet("/modifyUserPoints")
public class ModifyUserPointsServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        // 获取参数
        String userID = request.getParameter("userID");
        String operation = request.getParameter("operation"); // add, subtract, set
        String pointAmountStr = request.getParameter("pointAmount");
        String reason = request.getParameter("reason");

        // 参数验证
        if (userID == null || userID.trim().isEmpty()) {
            response.getWriter().write("{\"success\":false, \"msg\":\"用户ID不能为空\"}");
            return;
        }

        if (operation == null || operation.trim().isEmpty()) {
            response.getWriter().write("{\"success\":false, \"msg\":\"操作类型不能为空\"}");
            return;
        }

        if (pointAmountStr == null || pointAmountStr.trim().isEmpty()) {
            response.getWriter().write("{\"success\":false, \"msg\":\"积分数量不能为空\"}");
            return;
        }

        try {
            int pointAmount = Integer.parseInt(pointAmountStr);
            if (pointAmount < 1 && !"set".equals(operation)) {
                response.getWriter().write("{\"success\":false, \"msg\":\"积分数量必须大于0\"}");
                return;
            }

            if (modifyPoints(userID, operation, pointAmount, reason)) {
                response.getWriter().write("{\"success\":true, \"msg\":\"积分修改成功\"}");
            } else {
                response.getWriter().write("{\"success\":false, \"msg\":\"积分修改失败\"}");
            }

        } catch (NumberFormatException e) {
            response.getWriter().write("{\"success\":false, \"msg\":\"积分数量必须是数字\"}");
        }
    }

    /**
     * 修改用户积分
     */
    private boolean modifyPoints(String userID, String operation, int pointAmount, String reason) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                return false;
            }

            // 开启事务
            conn.setAutoCommit(false);

            try {
                // 1. 查询用户当前积分
                String querySql = "SELECT points FROM points WHERE userID = ?";
                pstmt = conn.prepareStatement(querySql);
                pstmt.setString(1, userID);
                rs = pstmt.executeQuery();

                if (!rs.next()) {
                    conn.rollback();
                    return false;
                }

                int currentPoints = rs.getInt("points");
                int newPoints = currentPoints;
                String pointOP = "";
                String detail = "";

                // 2. 根据操作类型计算新积分
                switch (operation) {
                    case "add":
                        newPoints = currentPoints + pointAmount;
                        pointOP = "+" + pointAmount;
                        detail = "管理员增加积分" + (reason != null && !reason.isEmpty() ? "：" + reason : "");
                        break;
                    case "subtract":
                        newPoints = currentPoints - pointAmount;
                        if (newPoints < 0) newPoints = 0; // 积分不能为负
                        pointOP = "-" + pointAmount;
                        detail = "管理员减少积分" + (reason != null && !reason.isEmpty() ? "：" + reason : "");
                        break;
                    case "set":
                        newPoints = pointAmount;
                        int diff = newPoints - currentPoints;
                        pointOP = (diff >= 0 ? "+" : "") + diff;
                        detail = "管理员设置积分为" + pointAmount + (reason != null && !reason.isEmpty() ? "：" + reason : "");
                        break;
                    default:
                        conn.rollback();
                        return false;
                }

                // 3. 更新用户积分
                String updateSql = "UPDATE points SET points = ? WHERE userID = ?";
                pstmt = conn.prepareStatement(updateSql);
                pstmt.setInt(1, newPoints);
                pstmt.setString(2, userID);
                pstmt.executeUpdate();

                // 4. 记录操作到pointop表
                String recordSql = "INSERT INTO pointop (userID, pointOP, detail, `time`) VALUES (?, ?, ?, ?)";
                pstmt = conn.prepareStatement(recordSql);
                pstmt.setString(1, userID);
                pstmt.setString(2, pointOP);
                pstmt.setString(3, detail);
                pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.executeUpdate();

                // 提交事务
                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
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

