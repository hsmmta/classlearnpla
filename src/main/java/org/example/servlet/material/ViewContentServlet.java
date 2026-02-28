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

@WebServlet("/material/view_content")
public class ViewContentServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");

        if (isAdmin == null || !isAdmin) {
            response.getWriter().write("<h1>无权限</h1>");
            return;
        }

        String materialID = request.getParameter("id");
        Material material = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT * FROM material WHERE materialID = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, materialID);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                material = new Material();
                material.setMaterialID(rs.getString("materialID"));
                material.setMaterialTitle(rs.getString("materialTitle"));
                material.setMaterialSubject(rs.getString("materialSubject"));
                material.setUploaderName(rs.getString("uploaderName"));
                material.setUploadTime(rs.getTimestamp("uploadTime"));
                material.setMaterialContent(rs.getString("materialContent"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }

        request.setAttribute("material", material);
        // Re-use the existing view page
        request.getRequestDispatcher("/material/ViewMaterial.jsp").forward(request, response);
    }
}