package org.example.servlet;

import org.example.util.CodeUtil;
import org.example.util.EmailUtil;
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

@WebServlet("/sendResetPwdCode")
public class SendResetPwdCodeServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");

        HttpSession session = request.getSession();
        String userID = (String) session.getAttribute("userID");
        boolean success = false;
        String msg = "";

        if (userID == null || userID.trim().isEmpty()) {
            msg = "请先登录账号！";
        } else {
            String userEmail = getUserEmailByUserID(userID);
            // 强化未绑定邮箱的提示：明确引导用户绑定
            if (userEmail == null || userEmail.trim().isEmpty() || "null".equals(userEmail)) {
                msg = "你尚未绑定邮箱，请先绑定邮箱后再尝试重置密码！";
            } else {
                String verifyCode = CodeUtil.generateCode();
                if (EmailUtil.sendResetPwdCode(userEmail, verifyCode)) {
                    session.setAttribute("resetPwdCode", verifyCode);
                    session.setAttribute("resetPwdCodeCreateTime", System.currentTimeMillis());
                    session.setAttribute("userEmail", userEmail);
                    success = true;
                    msg = "验证码已发送至您的邮箱：" + hideEmail(userEmail) + "，请查收！";
                } else {
                    msg = "验证码发送失败，请稍后重试！";
                }
            }
        }

        String jsonResponse = String.format("{\"success\":%b, \"msg\":\"%s\"}", success, msg.replace("\"", "\\\\\""));
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
        response.getWriter().close();
    }

    // 其他方法（getUserEmailByUserID、hideEmail、doGet）保持不变
    private String getUserEmailByUserID(String userID) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT userEmail FROM user WHERE userphone = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userID);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                String email = rs.getString("userEmail");
                return email == null ? "" : email.trim(); // 处理null值
            }
            return "";
        } catch (SQLException e) {
            e.printStackTrace();
            return "";
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    private String hideEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String prefix = parts[0];
        if (prefix.length() <= 3) {
            return prefix + "****@" + parts[1];
        }
        return prefix.substring(0, 3) + "****@" + parts[1];
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/personalInfo/forgetPassword.jsp");
    }
}