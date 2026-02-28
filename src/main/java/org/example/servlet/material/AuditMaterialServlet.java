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

@WebServlet("/material/audit")
public class AuditMaterialServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String userType = (String) session.getAttribute("userType");

        // Authorization check - 检查是否为管理员
        if (userType == null || !"admin".equals(userType)) {
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write("<h1>无权限</h1><p>您没有权限访问此页面。</p><a href='" + request.getContextPath() + "/auth/adminSignin.html'>返回管理员登录</a>");
            return;
        }

        List<Material> pendingMaterials = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT materialID, materialTitle, materialSubject, uploaderName, uploadTime FROM material WHERE materialState = '待审核' ORDER BY uploadTime ASC";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Material material = new Material();
                material.setMaterialID(rs.getString("materialID"));
                material.setMaterialTitle(rs.getString("materialTitle"));
                material.setMaterialSubject(rs.getString("materialSubject"));
                material.setUploaderName(rs.getString("uploaderName"));
                material.setUploadTime(rs.getTimestamp("uploadTime"));
                pendingMaterials.add(material);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }

        request.setAttribute("pendingMaterials", pendingMaterials);
        request.getRequestDispatcher("/material/AuditMaterial.jsp").forward(request, response);
    }
}