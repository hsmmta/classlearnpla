package org.example.servlet.comment;

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

@WebServlet("/comment/add")
public class AddCommentServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();

        String userID = (String) session.getAttribute("userID");
        String userType = (String) session.getAttribute("userType");

        // Users must be logged in to comment
        if (userID == null && !"admin".equals(userType)) {
            // Redirect to login if not logged in
            response.sendRedirect(request.getContextPath() + "/auth/signin.html");
            return;
        }

        String materialID = request.getParameter("materialID");
        String commentContent = request.getParameter("commentContent");

        // Use admin ID if an admin is commenting
        String commenterID = "admin".equals(userType) ? "admin" : userID;


        if (commentContent != null && !commentContent.trim().isEmpty()) {
            Connection conn = null;
            PreparedStatement pstmt = null;
            try {
                conn = DBUtil.getConnection();
                String sql = "INSERT INTO comment (materialID, userID, commentContent) VALUES (?, ?, ?)";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, materialID);
                pstmt.setString(2, commenterID); // Use the correct ID
                pstmt.setString(3, commentContent);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DBUtil.close(conn, pstmt);
            }
        }

        // Redirect back to the material view page
        response.sendRedirect(request.getContextPath() + "/material/view?id=" + materialID);
    }
}