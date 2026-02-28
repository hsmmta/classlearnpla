package org.example.servlet;

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

@WebServlet("/deleteAccount")
public class DeleteAccountServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. 设置编码和响应格式
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        // 2. 校验用户是否登录
        HttpSession session = request.getSession();
        String userID = (String) session.getAttribute("userID");
        // 新增：获取前端传入的用户输入密码（注意参数名要和前端一致）
        String inputPassword = request.getParameter("password") == null ? "" : request.getParameter("password").trim();

        boolean success = false;
        String msg = "";

        // 步骤1：校验登录状态
        if (userID == null || userID.trim().isEmpty()) {
            msg = "请先登录后再注销账号！";
        }
        // 步骤2：校验输入密码是否为空
        else if (inputPassword.isEmpty()) {
            msg = "请输入账号密码进行验证！";
        }
        // 步骤3：校验输入密码是否正确
        else if (!verifyPassword(userID, inputPassword)) {
            msg = "输入的密码错误，注销失败！";
        }
        // 步骤4：密码验证通过，更新用户状态
        else {
            if (updateUserStatus(userID, "closed")) {
                success = true;
                msg = "账号注销成功！";
                // 清空Session，强制退出登录
                session.invalidate();
            } else {
                msg = "账号注销失败！请稍后重试";
            }
        }

        // 4. 构造JSON响应返回前端
        String jsonResponse = String.format(
                "{\"success\":%b, \"msg\":\"%s\"}",
                success,
                msg.replace("\"", "\\\"") // 转义双引号避免JSON解析错误
        );
        response.getWriter().write(jsonResponse);
    }

    /**
     * 新增：验证用户输入的密码是否与数据库一致
     * @param userID 用户ID（手机号）
     * @param inputPassword 用户输入的密码
     * @return 密码是否正确
     */
    private boolean verifyPassword(String userID, String inputPassword) {
        org.example.util.DBUtil DBUtil = new org.example.util.DBUtil();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            // SQL：查询用户的真实密码（注意字段名：如果你的密码字段是password，改成password）
            String sql = "SELECT userPassword FROM user WHERE userID = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userID);

            rs = pstmt.executeQuery();
            // 存在该用户且密码匹配
            if (rs.next()) {
                String realPassword = rs.getString("userPassword");
                // 注意：如果你的密码是加密存储（如BCrypt），需改为加密后对比，示例：
                // return BCrypt.checkpw(inputPassword, realPassword);
                return inputPassword.equals(realPassword);
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            // 关闭数据库连接（包含ResultSet）
            org.example.util.DBUtil.close(conn, pstmt, rs);
        }
    }

    /**
     * 更新用户状态到数据库
     * @param userID 用户ID（手机号）
     * @param status 状态值（closed/active）
     * @return 是否更新成功
     */
    private boolean updateUserStatus(String userID, String status) {
        org.example.util.DBUtil DBUtil = new org.example.util.DBUtil();
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            // SQL：更新user表的userStatus字段
            String sql = "UPDATE user SET userStatus = ? WHERE userID = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setString(2, userID);

            // 执行更新，判断是否影响行数
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            // 关闭数据库连接
            org.example.util.DBUtil.close(conn, pstmt, null);
        }
    }

    // 兼容GET请求：跳转到个人中心页
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/personalInfo/getUserInfo.jsp");
    }
}