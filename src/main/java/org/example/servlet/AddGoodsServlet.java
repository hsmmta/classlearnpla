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
 * 添加商品Servlet
 * 管理员用于添加积分商城商品
 */
@WebServlet("/addGoods")
public class AddGoodsServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        // 获取参数
        String itemName = request.getParameter("itemName");
        String needPointStr = request.getParameter("needPoint");
        String desc = request.getParameter("desc");

        // 参数验证
        if (itemName == null || itemName.trim().isEmpty() || needPointStr == null || needPointStr.trim().isEmpty()) {
            response.getWriter().write("{\"success\":false, \"msg\":\"商品名称和所需积分不能为空\"}");
            return;
        }

        try {
            int needPoint = Integer.parseInt(needPointStr);
            if (needPoint < 1) {
                response.getWriter().write("{\"success\":false, \"msg\":\"所需积分必须大于0\"}");
                return;
            }

            if (addGoods(itemName, needPoint, desc)) {
                response.getWriter().write("{\"success\":true, \"msg\":\"商品添加成功\"}");
            } else {
                response.getWriter().write("{\"success\":false, \"msg\":\"商品添加失败\"}");
            }
        } catch (NumberFormatException e) {
            response.getWriter().write("{\"success\":false, \"msg\":\"所需积分必须是数字\"}");
        }
    }

    /**
     * 添加商品到数据库
     */
    private boolean addGoods(String itemName, int needPoint, String desc) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                return false;
            }

            // itemID是自增的，不需要手动指定
            String sql = "INSERT INTO goods (itemName, needPoint, `desc`) VALUES (?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, itemName);
            pstmt.setInt(2, needPoint);
            pstmt.setString(3, desc == null || desc.isEmpty() ? "" : desc);

            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }
}

