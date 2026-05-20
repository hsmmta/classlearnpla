package org.example.servlet.material;

import org.example.util.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write("<h1>错误</h1><p>未提供资料ID。</p>");
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            // 1. 先查询该资料是否属于当前用户，并获取filePath用于删除磁盘文件
            String querySql = "SELECT filePath FROM material WHERE materialID = ? AND userID = ?";
            pstmt = conn.prepareStatement(querySql);
            pstmt.setString(1, materialID);
            pstmt.setString(2, userID);
            rs = pstmt.executeQuery();

            if (!rs.next()) {
                System.out.println("Material not found or user not authorized: " + materialID);
                response.sendRedirect(request.getContextPath() + "/material/manage");
                return;
            }

            String filePath = rs.getString("filePath");
            rs.close();
            pstmt.close();

            // 2. 先删除关联的评论记录（防止外键约束导致删除失败）
            String deleteCommentsSql = "DELETE FROM comment WHERE materialID = ?";
            pstmt = conn.prepareStatement(deleteCommentsSql);
            pstmt.setString(1, materialID);
            pstmt.executeUpdate();
            pstmt.close();

            // 3. 删除资料记录
            String deleteSql = "DELETE FROM material WHERE materialID = ? AND userID = ?";
            pstmt = conn.prepareStatement(deleteSql);
            pstmt.setString(1, materialID);
            pstmt.setString(2, userID);
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                System.out.println("Material deleted successfully: " + materialID);
                // 4. 删除磁盘上的PDF文件（如果有）
                if (filePath != null && !filePath.trim().isEmpty()) {
                    String fullPath = getServletContext().getRealPath("") + File.separator + filePath;
                    File pdfFile = new File(fullPath);
                    if (pdfFile.exists()) {
                        pdfFile.delete();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }

        response.sendRedirect(request.getContextPath() + "/material/manage");
    }
}