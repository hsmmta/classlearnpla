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

@WebServlet("/material/delete")
public class DeleteMaterialServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String materialID = request.getParameter("id");
        HttpSession session = request.getSession();
        String userID = (String) session.getAttribute("userID");

        if (userID == null) {
            response.sendRedirect(request.getContextPath() + "/auth/signin.html");
            return;
        }

        if (materialID == null || materialID.trim().isEmpty()) {
            response.getWriter().write("<h1>错误</h1><p>未提供资料ID。</p>");
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            // Ensure the user can only delete their own materials
            String sql = "DELETE FROM material WHERE materialID = ? AND userID = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, materialID);
            pstmt.setString(2, userID);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Material deleted successfully: " + materialID);
            } else {
                // This could happen if the user tries to delete material that isn't theirs
                System.out.println("Material not found or user not authorized to delete: " + materialID);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle error, maybe show an error page
        } finally {
            DBUtil.close(conn, pstmt);
        }

        // Redirect back to the management page
        response.sendRedirect(request.getContextPath() + "/material/manage");
    }
}