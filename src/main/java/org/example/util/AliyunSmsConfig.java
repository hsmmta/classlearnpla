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

    public static AliyunSmsConfig fromEnv() {
        String accessKeyId = requireEnv("ALIYUN_ACCESS_KEY_ID");
        String accessKeySecret = requireEnv("ALIYUN_ACCESS_KEY_SECRET");
        String signName = requireEnv("ALIYUN_SMS_SIGN_NAME");
        String templateCode = requireEnv("ALIYUN_SMS_TEMPLATE_CODE");
        String schemeName = optionalEnv("ALIYUN_SMS_SCHEME_NAME", "DefaultScheme");
        String templateParam = optionalEnv("ALIYUN_SMS_TEMPLATE_PARAM", "{\"code\":\"${code}\",\"min\":\"5\"}");
        return new AliyunSmsConfig(accessKeyId, accessKeySecret, signName, templateCode, schemeName, templateParam);
    }

    private static String requireEnv(String key) {
        String value = readConfigValue(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Missing required env: " + key);
        }
        return value.trim();
    }

    private static String optionalEnv(String key, String defaultValue) {
        String value = readConfigValue(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }

    private static String readConfigValue(String key) {
        String value = System.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            value = System.getenv(key);
        }
        return value;
    }
}
