package org.example.servlet.material;

import org.example.model.Comment;
import org.example.model.Material;
import org.example.util.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/material/view")
public class ViewMaterialServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String materialID = request.getParameter("id");

        if (materialID == null || materialID.trim().isEmpty()) {
            response.getWriter().write("<h1>错误</h1><p>未提供资料ID。</p>");
            return;
        }

        Material material = null;
        List<Comment> comments = new ArrayList<>();
        Connection conn = null;

        try {
            conn = DBUtil.getConnection();
            // 1. Get Material Details
            String materialSql = "SELECT * FROM material WHERE materialID = ? AND materialState = '审核通过'";
            try (PreparedStatement pstmt = conn.prepareStatement(materialSql)) {
                pstmt.setString(1, materialID);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        material = new Material();
                        material.setMaterialID(rs.getString("materialID"));
                        material.setMaterialTitle(rs.getString("materialTitle"));
                        material.setUserID(rs.getString("userID"));
                        material.setUploaderName(rs.getString("uploaderName"));
                        material.setUploadTime(rs.getTimestamp("uploadTime"));
                        material.setMaterialContent(rs.getString("materialContent"));
                    }
                }
            }

            // 2. Get Comments for the Material
            if (material != null) {
                String commentSql = "SELECT c.commentID, c.userID, c.commentContent, c.commentTime, u.userName " +
                                    "FROM comment c JOIN user u ON c.userID = u.userphone " +
                                    "WHERE c.materialID = ? ORDER BY c.commentTime ASC";
                try (PreparedStatement pstmt = conn.prepareStatement(commentSql)) {
                    pstmt.setString(1, materialID);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            Comment comment = new Comment();
                            comment.setCommentID(rs.getInt("commentID"));
                            comment.setUserID(rs.getString("userID"));
                            comment.setCommentContent(rs.getString("commentContent"));
                            comment.setCommentTime(rs.getTimestamp("commentTime"));
                            comment.setUserName(rs.getString("userName"));
                            comments.add(comment);
                        }
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, null, null);
        }

        request.setAttribute("material", material);
        request.setAttribute("comments", comments);
        request.getRequestDispatcher("/material/ViewMaterial.jsp").forward(request, response);
    }
}