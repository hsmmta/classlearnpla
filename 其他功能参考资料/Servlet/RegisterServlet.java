package org.example.servlet;

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

@WebServlet("/register") // 匹配前端表单action="/register"
public class RegisterServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. 设置编码（避免中文乱码）
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        // 2. 获取前端参数
        String userID = request.getParameter("userID");
        String code = request.getParameter("code");
        String userPassword = request.getParameter("userPassword");
        String repassword = request.getParameter("repassword");
        String userName = request.getParameter("userName");
        String classID = request.getParameter("classID");
        String studentID = request.getParameter("studentID");
        String gender = request.getParameter("gender");
        String userEmail = request.getParameter("userEmail");

        // 3. 第一步校验：必填参数非空 + 密码一致性
        if (userID == null || userID.trim().isEmpty() ||
                code == null || code.trim().isEmpty() ||
                userPassword == null || userPassword.trim().isEmpty() ||
                repassword == null || repassword.trim().isEmpty() ||
                userName == null || userName.trim().isEmpty() ||
                classID == null || classID.trim().isEmpty() ||
                studentID == null || studentID.trim().isEmpty() ||
                gender == null || gender.trim().isEmpty()) {
            // 非空校验失败：弹窗提示 + 回退到注册页
            response.getWriter().write("<script>alert('注册失败！必填项不能为空'); window.history.back();</script>");
            return;
        }

        // 密码一致性校验
        if (!userPassword.equals(repassword)) {
            // 密码不一致：弹窗提示 + 回退到注册页
            response.getWriter().write("<script>alert('注册失败！两次输入的密码不一致'); window.history.back();</script>");
            return;
        }

        // 4. 第二步校验：验证码
        HttpSession session = request.getSession();
        String sessionCode = (String) session.getAttribute("code");

        if (sessionCode == null || !sessionCode.trim().equals(code.trim())) {
            // 验证码错误：弹窗提示 + 回退到注册页
            response.getWriter().write("<script>alert('注册失败！验证码错误或已过期'); window.history.back();</script>");
            return;
        }

        // 5. 第三步：插入数据库
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "INSERT INTO user (userID, userPassword, userName, classID, studentID, gender, userEmail, userType, userStatus) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, 'user', 'activate')";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userID);
            pstmt.setString(2, userPassword);
            pstmt.setString(3, userName);
            pstmt.setString(4, classID);
            pstmt.setString(5, studentID);
            pstmt.setString(6, gender);
            pstmt.setString(7, userEmail == null ? "" : userEmail);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                // 注册成功：弹窗提示 + 跳转登录页
                System.out.println("注册成功：用户ID" + userID);
                session.removeAttribute("code");
                response.getWriter().write("<script>alert('注册成功！即将跳转到登录页'); window.location.href='" + request.getContextPath() + "/auth/signin.html';</script>");
            } else {
                // 插入失败：弹窗提示 + 回退
                response.getWriter().write("<script>alert('注册失败！数据插入失败'); window.history.back();</script>");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // 区分错误类型，统一弹窗提示 + 回退到原注册页
            if (e.getMessage().contains("Duplicate entry") && e.getMessage().contains("user.PRIMARY")) {
                // 手机号重复：弹窗提示“该手机号已注册” + 回退
                response.getWriter().write("<script>alert('该手机号已注册！'); window.history.back();</script>");
            } else if (e.getMessage().contains("Column 'gender' cannot be null") || e.getMessage().contains("Column 'studentID' cannot be null")) {
                response.getWriter().write("<script>alert('注册失败！性别或学号不能为空'); window.history.back();</script>");
            } else {
                response.getWriter().write("<script>alert('注册失败！数据库错误：" + e.getMessage() + "'); window.history.back();</script>");
            }
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }
}