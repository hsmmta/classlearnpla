package org.example.util;

import java.util.Random;

/**
 * 验证码工具类：生成、验证验证码
 */
public class CodeUtil {
    // 生成6位数字验证码
    public static String generateCode() {
        Random random = new Random();
        // 100000 ~ 999999 之间的随机数
        int codeNum = 100000 + random.nextInt(900000);
        return String.valueOf(codeNum);
    }

    // 验证验证码是否正确（忽略大小写）
    public static boolean verifyCode(String inputCode, String sessionCode) {
        if (inputCode == null || sessionCode == null) {
            return false;
        }
        return inputCode.equalsIgnoreCase(sessionCode);
    }
}