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
import java.util.UUID;

@WebServlet("/material/upload")
public class UploadMaterialServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession();
        String userID = (String) session.getAttribute("userID");
        String userName = (String) session.getAttribute("userName");

        if (userID == null || userID.trim().isEmpty() || userName == null || userName.trim().isEmpty()) {
            response.getWriter().write("<h1>错误</h1><p>用户未登录，请先<a href='/auth/signin.html'>登录</a></p>");
            return;
        }

        String materialTitle = request.getParameter("materialTitle");
        String materialSubject = request.getParameter("materialSubject");
        String materialContent = request.getParameter("materialContent");

        if (materialTitle == null || materialTitle.trim().isEmpty() ||
            materialSubject == null || materialSubject.trim().isEmpty() ||
            materialContent == null || materialContent.trim().isEmpty()) {
            response.getWriter().write("<h1>错误</h1><p>所有字段均为必填项。</p>");
            return;
        }

        String materialID = UUID.randomUUID().toString();
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();

            // 首先验证userID是否存在于user表中
            String checkUserSql = "SELECT userphone FROM user WHERE userphone = ?";
            pstmt = conn.prepareStatement(checkUserSql);
            pstmt.setString(1, userID);
            java.sql.ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                // userID不存在，返回错误提示
                response.getWriter().write("<h1>错误</h1><p>用户不存在，请重新登录。</p><a href='/auth/signin.html'>返回登录</a>");
                return;
            }
            rs.close();
            pstmt.close();

            // 插入资料
            String sql = "INSERT INTO material (materialID, materialTitle, materialContent, userID, uploaderName, materialSubject, materialState) VALUES (?, ?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, materialID);
            pstmt.setString(2, materialTitle);
            pstmt.setString(3, materialContent);
            pstmt.setString(4, userID);
            pstmt.setString(5, userName);
            pstmt.setString(6, materialSubject);
            pstmt.setString(7, "待审核"); // Default state

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                // 上传成功
                response.getWriter().write("<h1>上传成功</h1><p>您的资料已提交审核，请耐心等待。</p><a href='/material/AddMaterial.jsp'>继续上传</a>");
            } else {
                response.getWriter().write("<h1>上传失败</h1><p>无法将资料保存到数据库，请重试。</p>");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("上传资料出错：" + e.getMessage());
            System.out.println("userID: " + userID);
            System.out.println("userName: " + userName);
            response.getWriter().write("<h1>数据库错误</h1><p>上传过程中发生错误。</p><p>错误详情：" + e.getMessage() + "</p><p>userID: " + userID + "</p><p>请确保已登录且用户信息正确。</p><a href='/auth/signin.html'>重新登录</a>");
        } finally {
            DBUtil.close(conn, pstmt);
        }
    }
}