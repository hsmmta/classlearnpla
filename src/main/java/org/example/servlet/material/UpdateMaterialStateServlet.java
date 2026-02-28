package org.example.servlet.material;

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
import java.sql.SQLException;

@WebServlet("/material/updateState")
public class UpdateMaterialStateServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String userType = (String) session.getAttribute("userType");

        // 检查是否为管理员
        if (userType == null || !"admin".equals(userType)) {
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write("<h1>无权限</h1><p>您没有权限执行此操作。</p>");
            return;
        }

        String materialID = request.getParameter("id");
        String newState = request.getParameter("state");

        if (materialID == null || newState == null || (!"审核通过".equals(newState) && !"审核不通过".equals(newState))) {
            response.getWriter().write("<h1>参数错误</h1>");
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            String sql = "UPDATE material SET materialState = ? WHERE materialID = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newState);
            pstmt.setString(2, materialID);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt);
        }

        response.sendRedirect(request.getContextPath() + "/material/audit");
    }
}