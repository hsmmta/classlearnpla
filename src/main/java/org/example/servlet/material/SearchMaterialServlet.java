package org.example.servlet.material;

import org.example.model.Material;
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

@WebServlet("/material/search")
public class SearchMaterialServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        // Check if user is logged in
        HttpSession session = request.getSession();
        String userID = (String) session.getAttribute("userID");
        if (userID == null) {
            response.sendRedirect(request.getContextPath() + "/auth/signin.html");
            return;
        }

        String query = request.getParameter("query");

        List<Material> results = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                throw new SQLException("无法获取数据库连接");
            }
            String sql;
            if (query != null && !query.trim().isEmpty()) {
                // Search in title and content, only for approved materials
                sql = "SELECT materialID, materialTitle, materialSubject, uploaderName, uploadTime FROM material " +
                             "WHERE materialState = '审核通过' AND (materialTitle LIKE ? OR materialContent LIKE ?) " +
                             "ORDER BY uploadTime DESC";
                pstmt = conn.prepareStatement(sql);
                String searchQuery = "%" + query + "%";
                pstmt.setString(1, searchQuery);
                pstmt.setString(2, searchQuery);
            } else {
                // List all approved materials if no query
                sql = "SELECT materialID, materialTitle, materialSubject, uploaderName, uploadTime FROM material " +
                             "WHERE materialState = '审核通过' ORDER BY uploadTime DESC";
                pstmt = conn.prepareStatement(sql);
            }

            rs = pstmt.executeQuery();

            while (rs.next()) {
                Material material = new Material();
                material.setMaterialID(rs.getString("materialID"));
                material.setMaterialTitle(rs.getString("materialTitle"));
                material.setMaterialSubject(rs.getString("materialSubject"));
                material.setUploaderName(rs.getString("uploaderName"));
                material.setUploadTime(rs.getTimestamp("uploadTime"));
                results.add(material);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ServletException("数据库查询出错: " + e.getMessage(), e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }

        request.setAttribute("results", results);
        request.getRequestDispatcher("/material/SearchMaterial.jsp").forward(request, response);
    }
}