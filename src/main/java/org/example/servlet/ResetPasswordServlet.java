package org.example.servlet;

import org.example.util.CodeUtil;
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

@WebServlet("/resetPassword")
public class ResetPasswordServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. 强制设置请求编码（解决中文乱码/参数接收不到）
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache"); // 防止缓存导致响应异常

        HttpSession session = request.getSession();
        // 2. 精准获取前端参数（确保和jsp中的name属性一致）
        String inputCode = request.getParameter("verifyCode") == null ? "" : request.getParameter("verifyCode").trim();
        String newPwd = request.getParameter("newPwd") == null ? "" : request.getParameter("newPwd").trim();
        String confirmPwd = request.getParameter("confirmPwd") == null ? "" : request.getParameter("confirmPwd").trim();

        // 3. 获取Session中的关键数据
        String sessionCode = (String) session.getAttribute("resetPwdCode");
        Long createTime = (Long) session.getAttribute("resetPwdCodeCreateTime");
        String userID = (String) session.getAttribute("userID");

        boolean success = false;
        String msg = "";

        // ========== 逐行校验（强化错误提示） ==========
        // 校验1：用户是否登录
        if (userID == null || userID.trim().isEmpty()) {
            msg = "请先登录账号！";
        }
        // 校验2：是否已获取验证码
        else if (sessionCode == null || createTime == null) {
            msg = "请先获取验证码！";
        }
        // 校验3：验证码是否为空
        else if (inputCode.isEmpty()) {
            msg = "请输入验证码！";
        }
        // 校验4：验证码是否正确（复用CodeUtil的verifyCode方法）
        else if (!CodeUtil.verifyCode(inputCode, sessionCode)) {
            msg = "验证码错误！";
        }
        // 校验5：验证码是否过期（5分钟）
        else if (System.currentTimeMillis() - createTime > 5 * 60 * 1000) {
            msg = "验证码已过期，请重新获取！";
            session.removeAttribute("resetPwdCode"); // 清空过期验证码
            session.removeAttribute("resetPwdCodeCreateTime");
        }
        // 校验6：新密码是否合法
        else if (newPwd.isEmpty() || newPwd.length() < 6) {
            msg = "新密码不能为空且长度不少于6位！";
        }
        // 校验7：两次密码是否一致
        else if (!newPwd.equals(confirmPwd)) {
            msg = "两次输入的密码不一致！";
        }
        // 校验8：更新数据库密码
        else {
            if (updateUserPassword(userID, newPwd)) {
                success = true;
                msg = "密码重置成功！即将返回登录页";
                // 清空验证码+退出登录（必须操作）
                session.removeAttribute("resetPwdCode");
                session.removeAttribute("resetPwdCodeCreateTime");
                session.invalidate(); // 销毁Session，强制重新登录
            } else {
                msg = "密码重置失败，请稍后重试！";
            }
        }

        // 4. 返回JSON响应（确保格式正确，无转义错误）
        String jsonResponse = String.format("{\"success\":%b, \"msg\":\"%s\"}", success, msg.replace("\"", "\\\\\""));
        response.getWriter().write(jsonResponse);
        response.getWriter().flush(); // 强制刷新输出流
        response.getWriter().close(); // 关闭流
    }

    private boolean updateUserPassword(String userID, String newPwd) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "UPDATE user SET userPassword = ? WHERE userphone = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newPwd);
            pstmt.setString(2, userID);

            // 执行更新并判断是否影响行（返回1表示更新成功）
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace(); // 打印异常，方便排查
            return false;
        } finally {
            DBUtil.close(conn, pstmt, null); // 确保关闭连接
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/personalInfo/forgetPassword.jsp");
    }
}
