package org.example.servlet;

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
 * 商品兑换Servlet
 * 用于处理用户兑换积分商城中的商品
 */
@WebServlet("/exchangeGoods")
public class ExchangeGoodsServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        // 获取session中的用户信息
        HttpSession session = request.getSession();
        String userID = (String) session.getAttribute("userID");

        if (userID == null || userID.trim().isEmpty()) {
            response.getWriter().write("{\"success\":false, \"msg\":\"请先登录\"}");
            return;
        }

        // 获取参数
        String itemIDStr = request.getParameter("itemID");
        String needPointsStr = request.getParameter("needPoints");

        if (itemIDStr == null || needPointsStr == null) {
            response.getWriter().write("{\"success\":false, \"msg\":\"参数不完整\"}");
            return;
        }

        try {
            int itemID = Integer.parseInt(itemIDStr);
            int needPoints = Integer.parseInt(needPointsStr);

            if (exchangeGoods(userID, itemID, needPoints)) {
                response.getWriter().write("{\"success\":true, \"msg\":\"兑换成功\"}");
            } else {
                response.getWriter().write("{\"success\":false, \"msg\":\"兑换失败，请确保积分足够\"}");
            }
        } catch (NumberFormatException e) {
            response.getWriter().write("{\"success\":false, \"msg\":\"参数错误\"}");
        }
    }

    /**
     * 执行商品兑换逻辑
     */
    private boolean exchangeGoods(String userID, int itemID, int needPoints) {
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
                // 1. 检查用户是否有足够的积分
                String checkSql = "SELECT points FROM points WHERE userID = ?";
                pstmt = conn.prepareStatement(checkSql);
                pstmt.setString(1, userID);
                rs = pstmt.executeQuery();

                int currentPoints = 0;
                if (rs.next()) {
                    currentPoints = rs.getInt("points");
                } else {
                    conn.rollback();
                    return false;
                }

                // 2. 检查积分是否足够
                if (currentPoints < needPoints) {
                    conn.rollback();
                    return false;
                }

                // 3. 扣除用户积分
                String updatePointsSql = "UPDATE points SET points = points - ? WHERE userID = ?";
                pstmt = conn.prepareStatement(updatePointsSql);
                pstmt.setInt(1, needPoints);
                pstmt.setString(2, userID);
                pstmt.executeUpdate();

                // 4. 记录交易操作到pointop表
                String recordOpSql = "INSERT INTO pointop (userID, pointOP, detail, `time`) VALUES (?, ?, ?, ?)";
                pstmt = conn.prepareStatement(recordOpSql);
                pstmt.setString(1, userID);
                pstmt.setString(2, "-" + needPoints);
                pstmt.setString(3, "兑换" + itemID);
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

