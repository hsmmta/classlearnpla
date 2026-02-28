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
import java.sql.SQLException;

@WebServlet("/changeUserInfo")
public class ChangeUserInfoServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        // 1. 校验登录状态
        HttpSession session = request.getSession();
        String userID = (String) session.getAttribute("userID");
        if (userID == null || userID.trim().isEmpty()) {
            String json = "{\"success\":false, \"msg\":\"请先登录！\"}";
            response.getWriter().write(json);
            return;
        }

        // 2. 获取前端参数
        String userName = request.getParameter("userName");
        String classID = request.getParameter("classID");
        String gender = request.getParameter("gender");
        String studentID = request.getParameter("studentID");
        String userEmail = request.getParameter("userEmail");
        boolean success = false;
        String msg = "";

        // 3. 后端校验
        if (userName == null || userName.trim().isEmpty() || userName.length() > 20) {
            msg = "昵称不能为空且长度不能超过20位！";
        } else if (classID == null || classID.trim().isEmpty() || classID.length() > 15) {
            msg = "班级编号不能为空且长度不能超过15位！";
        } else if (gender == null || gender.trim().isEmpty() || (!"male".equals(gender) && !"female".equals(gender))) {
            msg = "请选择正确的性别！";
        } else if (studentID == null || studentID.trim().isEmpty() || studentID.length() > 15) {
            msg = "学号不能为空且长度不能超过15位！";
        } else if (userEmail != null && !userEmail.trim().isEmpty() && !userEmail.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            msg = "邮箱格式不正确！";
        } else {
            // 4. 更新数据库
            if (updateUserInfo(userID, userName, classID, gender, studentID, userEmail)) {
                success = true;
                msg = "个人信息修改成功！";
                // 5. 更新Session中的信息
                session.setAttribute("userName", userName);
                session.setAttribute("classID", classID);
                session.setAttribute("gender", gender);
                session.setAttribute("studentID", studentID);
                session.setAttribute("userEmail", userEmail);
            } else {
                msg = "信息修改失败！请稍后重试";
            }
        }

        // 6. 返回JSON响应
        String jsonResponse = String.format(
                "{\"success\":%b, \"msg\":\"%s\"}",
                success,
                msg.replace("\"", "\\\"")
        );
        response.getWriter().write(jsonResponse);
    }

    /**
     * 更新用户信息到数据库
     */
    private boolean updateUserInfo(String userID, String userName, String classID, String gender, String studentID, String userEmail) {
        org.example.util.DBUtil DBUtil = new org.example.util.DBUtil();
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "UPDATE user SET userName = ?, classID = ?, gender = ?, studentID = ?, userEmail = ? WHERE userphone = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userName);
            pstmt.setString(2, classID);
            pstmt.setString(3, gender);
            pstmt.setString(4, studentID);
            pstmt.setString(5, userEmail == null ? "" : userEmail);
            pstmt.setString(6, userID);
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/personalInfo/changeUserInfo.jsp");
    }
}