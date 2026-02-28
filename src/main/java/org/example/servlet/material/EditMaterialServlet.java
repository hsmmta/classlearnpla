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

@WebServlet("/material/edit")
public class EditMaterialServlet extends HttpServlet {

    // Handles fetching the material data to display on the edit page
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String materialID = request.getParameter("id");
        HttpSession session = request.getSession();
        String userID = (String) session.getAttribute("userID");

        if (userID == null) {
            response.sendRedirect(request.getContextPath() + "/auth/signin.html");
            return;
        }

        Material material = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            // Ensure user can only edit their own material
            String sql = "SELECT materialID, materialTitle, materialSubject, materialContent FROM material WHERE materialID = ? AND userID = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, materialID);
            pstmt.setString(2, userID);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                material = new Material();
                material.setMaterialID(rs.getString("materialID"));
                material.setMaterialTitle(rs.getString("materialTitle"));
                material.setMaterialSubject(rs.getString("materialSubject"));
                material.setMaterialContent(rs.getString("materialContent"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }

        if (material != null) {
            request.setAttribute("material", material);
            request.getRequestDispatcher("/material/EditMaterial.jsp").forward(request, response);
        } else {
            response.getWriter().write("<h1>错误</h1><p>资料不存在或您没有权限编辑。</p>");
        }
    }

    // Handles updating the material data after submission
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String materialID = request.getParameter("materialID");
        String materialTitle = request.getParameter("materialTitle");
        String materialSubject = request.getParameter("materialSubject");
        String materialContent = request.getParameter("materialContent");

        HttpSession session = request.getSession();
        String userID = (String) session.getAttribute("userID");

        if (userID == null) {
            response.sendRedirect(request.getContextPath() + "/auth/signin.html");
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            // Also reset the state to 'Pending Audit' after editing
            String sql = "UPDATE material SET materialTitle = ?, materialSubject = ?, materialContent = ?, materialState = '待审核' WHERE materialID = ? AND userID = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, materialTitle);
            pstmt.setString(2, materialSubject);
            pstmt.setString(3, materialContent);
            pstmt.setString(4, materialID);
            pstmt.setString(5, userID);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle error
        } finally {
            DBUtil.close(conn, pstmt);
        }

        response.sendRedirect(request.getContextPath() + "/material/manage");
    }
}