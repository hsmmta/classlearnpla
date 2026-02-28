package org.example.util;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * 邮箱验证码发送工具类（适配QQ邮箱，可替换为163/网易邮箱）
 */
public class EmailUtil {
    // ========== 请替换为你的邮箱配置 ==========
    private static final String SENDER_EMAIL = "jtr-wstz@qq.com"; // 发件人邮箱
    private static final String SENDER_AUTH_CODE = "agcyjcsxbvgpcfij"; // SMTP授权码（不是登录密码）
    private static final String SMTP_HOST = "smtp.qq.com"; // QQ邮箱SMTP服务器
    private static final int SMTP_PORT = 587; // QQ邮箱端口

    /**
     * 发送找回密码验证码邮件
     * @param toEmail 收件人邮箱（用户绑定的邮箱）
     * @param verifyCode 6位验证码
     * @return 是否发送成功
     */
    public static boolean sendResetPwdCode(String toEmail, String verifyCode) {
        // 1. 配置SMTP参数
        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.auth", "true"); // 开启认证
        props.put("mail.smtp.starttls.enable", "true"); // 开启TLS加密（QQ邮箱必填）

        // 2. 创建邮箱认证对象
        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_AUTH_CODE);
            }
        };

        // 3. 创建邮件会话
        Session session = Session.getInstance(props, auth);
        session.setDebug(false); // 调试时可改为true，查看发送日志

        try {
            // 4. 构建邮件内容
            MimeMessage message = new MimeMessage(session);
            // 发件人（显示名称）
            message.setFrom(new InternetAddress(SENDER_EMAIL, "班级学习社区平台", "UTF-8"));
            // 收件人
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            // 邮件标题
            message.setSubject("【班级学习社区】密码找回验证码", "UTF-8");
            // 邮件内容（和平台风格统一）
            String emailContent = String.format("尊敬的用户：\n\n您正在找回账号密码，本次验证码为：%s\n该验证码5分钟内有效，请及时输入。\n\n如非本人操作，请忽略此邮件。", verifyCode);
            message.setText(emailContent, "UTF-8");

            // 5. 发送邮件
            Transport.send(message);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
