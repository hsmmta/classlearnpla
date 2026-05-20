package org.example.util;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * 邮箱验证码发送工具类（配置来自环境变量 / .env）。
 */
public class EmailUtil {

    public static boolean sendResetPwdCode(String toEmail, String verifyCode) {
        String senderEmail = EnvUtil.require("MAIL_SENDER_EMAIL");
        String senderAuthCode = EnvUtil.require("MAIL_SMTP_AUTH_CODE");
        String smtpHost = EnvUtil.get("MAIL_SMTP_HOST", "smtp.qq.com");
        int smtpPort = Integer.parseInt(EnvUtil.get("MAIL_SMTP_PORT", "587"));

        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", String.valueOf(smtpPort));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderAuthCode);
            }
        };

        Session session = Session.getInstance(props, auth);
        session.setDebug(false);

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail, "班级学习社区平台", "UTF-8"));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject("【班级学习社区】密码找回验证码", "UTF-8");
            String emailContent = String.format(
                    "尊敬的用户：\n\n您正在找回账号密码，本次验证码为：%s\n该验证码5分钟内有效，请及时输入。\n\n如非本人操作，请忽略此邮件。",
                    verifyCode);
            message.setText(emailContent, "UTF-8");
            Transport.send(message);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
