package org.example.util;

public class AliyunSmsConfig {
    private final String accessKeyId;
    private final String accessKeySecret;
    private final String signName;
    private final String templateCode;
    private final String schemeName;
    private final String templateParam;

    private AliyunSmsConfig(String accessKeyId,
                            String accessKeySecret,
                            String signName,
                            String templateCode,
                            String schemeName,
                            String templateParam) {
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.signName = signName;
        this.templateCode = templateCode;
        this.schemeName = schemeName;
        this.templateParam = templateParam;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public String getSignName() {
        return signName;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public String getSchemeName() {
        return schemeName;
    }

    public String getTemplateParam() {
        return templateParam;
    }

    /**
     * 从环境变量 / .env 加载（见 .env.example）。
     */
    public static AliyunSmsConfig fromEnv() {
        return new AliyunSmsConfig(
                trimSecret(EnvUtil.require("ALIYUN_ACCESS_KEY_ID")),
                trimSecret(EnvUtil.require("ALIYUN_ACCESS_KEY_SECRET")),
                EnvUtil.get("ALIYUN_SMS_SIGN_NAME", "速通互联验证码"),
                EnvUtil.get("ALIYUN_SMS_TEMPLATE_CODE", "100001"),
                EnvUtil.get("ALIYUN_SMS_SCHEME_NAME", "默认方案"),
                EnvUtil.get("ALIYUN_SMS_TEMPLATE_PARAM", "{\"code\":\"##code##\",\"min\":\"5\"}")
        );
    }

    private static String trimSecret(String value) {
        if (value == null) {
            return "";
        }
        value = value.replace("\uFEFF", "").replace("\r", "").trim();
        if ((value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("'") && value.endsWith("'"))) {
            value = value.substring(1, value.length() - 1).trim();
        }
        return value;
    }
}
