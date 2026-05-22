package org.example.util;

import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.tea.TeaException;
import com.aliyun.teautil.models.RuntimeOptions;

public class AliyunSmsUtil {
    private static final String ENDPOINT = "dypnsapi.aliyuncs.com";
    private static final String REGION_ID = "cn-hangzhou";
    private static final String COUNTRY_CODE = "86";

    private static volatile String lastError = "验证码发送失败";

    public static String getLastError() {
        return lastError;
    }

    public static boolean sendVerifyCode(String phoneNumber, String code) {
        lastError = "验证码发送失败";
        AliyunSmsConfig cfg = AliyunSmsConfig.fromEnv();
        int secretLen = cfg.getAccessKeySecret().length();
        if (secretLen != 30) {
            lastError = "AccessKey Secret 长度异常(" + secretLen + ")，应为 30 字符，请检查 .env 是否多空格或引号";
            return false;
        }

        String templateParam = cfg.getTemplateParam()
                .replace("${code}", code)
                .replace("##code##", code);

        try {
            com.aliyun.teaopenapi.models.Config clientConfig = new com.aliyun.teaopenapi.models.Config()
                    .setAccessKeyId(cfg.getAccessKeyId())
                    .setAccessKeySecret(cfg.getAccessKeySecret())
                    .setRegionId(REGION_ID);
            clientConfig.endpoint = ENDPOINT;

            com.aliyun.dypnsapi20170525.Client client = new com.aliyun.dypnsapi20170525.Client(clientConfig);
            SendSmsVerifyCodeRequest request = new SendSmsVerifyCodeRequest()
                    .setSignName(cfg.getSignName())
                    .setTemplateCode(cfg.getTemplateCode())
                    .setPhoneNumber(phoneNumber)
                    .setSchemeName(cfg.getSchemeName())
                    .setCountryCode(COUNTRY_CODE)
                    .setTemplateParam(templateParam)
                    .setReturnVerifyCode(false);

            client.sendSmsVerifyCodeWithOptions(request, new RuntimeOptions());
            return true;
        } catch (TeaException te) {
            String codeStr = te.getCode() != null ? te.getCode() : "";
            System.err.println("[AliyunSmsUtil] TeaException code=" + codeStr + " message=" + te.getMessage());
            if (codeStr.contains("SignatureDoesNotMatch") || (te.getMessage() != null && te.getMessage().contains("signature"))) {
                lastError = "短信服务签名失败，请检查 .env 中 AccessKey 是否正确，并重启后端";
            } else if (codeStr.contains("InvalidAccessKeyId")) {
                lastError = "AccessKeyId 无效，请检查 .env 配置";
            } else {
                lastError = "验证码发送失败: " + (te.getMessage() != null ? te.getMessage() : codeStr);
            }
            return false;
        } catch (Exception ex) {
            System.err.println("[AliyunSmsUtil] sendVerifyCode failed: " + ex.getMessage());
            lastError = "验证码发送失败: " + ex.getMessage();
            return false;
        }
    }
}
