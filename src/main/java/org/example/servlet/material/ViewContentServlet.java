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
        String userType = (String) session.getAttribute("userType");

        if (userType == null || !"admin".equals(userType)) {
            response.setContentType("text/html;charset=UTF-8");
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
                material.setMaterialType(rs.getString("materialType"));
                material.setFilePath(rs.getString("filePath"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }

        request.setAttribute("material", material);
        request.getRequestDispatcher("/material/AdminViewMaterial.jsp").forward(request, response);
    }
}