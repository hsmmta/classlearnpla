package org.example.util;

import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.teautil.models.RuntimeOptions;

public class AliyunSmsUtil {
    private static final String ENDPOINT = "dypnsapi.aliyuncs.com";
    private static final String COUNTRY_CODE = "86";

    public static boolean sendVerifyCode(String phoneNumber, String code) {
        AliyunSmsConfig cfg = AliyunSmsConfig.fromEnv();
        String templateParam = cfg.getTemplateParam()
                .replace("${code}", code)
                .replace("##code##", code);

        try {
            com.aliyun.teaopenapi.models.Config clientConfig = new com.aliyun.teaopenapi.models.Config()
                    .setAccessKeyId(cfg.getAccessKeyId())
                    .setAccessKeySecret(cfg.getAccessKeySecret());
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
        } catch (Exception ex) {
            System.err.println("[AliyunSmsUtil] sendVerifyCode failed: " + ex.getMessage());
            return false;
        }
    }
}
