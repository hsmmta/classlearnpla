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

@WebServlet("/material/manage")
public class MaterialManagementServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        String userID = (String) session.getAttribute("userID");

        if (userID == null) {
            response.sendRedirect(request.getContextPath() + "/auth/signin.html");
            return;
        }

        List<Material> materials = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT materialID, materialTitle, materialSubject, uploadTime, materialState, materialType FROM material WHERE userID = ? ORDER BY uploadTime DESC";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userID);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Material material = new Material();
                material.setMaterialID(rs.getString("materialID"));
                material.setMaterialTitle(rs.getString("materialTitle"));
                material.setMaterialSubject(rs.getString("materialSubject"));
                material.setUploadTime(rs.getTimestamp("uploadTime"));
                material.setMaterialState(rs.getString("materialState"));
                material.setMaterialType(rs.getString("materialType"));
                materials.add(material);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Optionally, forward to an error page
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }

        request.setAttribute("materials", materials);
        request.getRequestDispatcher("/material/MaterialManagement.jsp").forward(request, response);
    }
}