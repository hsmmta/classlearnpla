package org.example.servlet;

import org.example.util.AliyunSmsUtil;
import org.example.util.CodeUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/sendCode") // 匹配前端请求的/sendCode
public class SendCodeServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. 关键：设置编码（避免验证码含空格/乱码）
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        // 2. 获取手机号（前端传递的phone参数）
        String phone = request.getParameter("phone");
        if (phone == null || phone.trim().isEmpty()) {
            response.getWriter().write("手机号不能为空！");
            return;
        }

        // 3. 生成6位数字验证码
        String code = CodeUtil.generateCode();

        // 4. 发送短信
        boolean sent = AliyunSmsUtil.sendVerifyCode(phone.trim(), code);
        if (!sent) {
            response.getWriter().write("验证码发送失败，请稍后再试！");
            return;
        }

        // 5. 核心：存入Session
        HttpSession session = request.getSession();
        session.setAttribute("code", code);
        session.setMaxInactiveInterval(300);
        System.out.println("【验证码发送】手机号：" + phone + " | 生成的验证码：" + code);

        // 6. 向前端返回提示
        response.getWriter().write("验证码已发送，请注意查收！");
    }
}