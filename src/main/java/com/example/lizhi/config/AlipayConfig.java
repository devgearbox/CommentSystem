// AlipayConfig.java
package com.example.lizhi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "alipay")
public class AlipayConfig {
    private String appId;
    private String privateKey;
    private String alipayPublicKey;
    private String returnUrl;
    private String notifyUrl;
    private String gateway = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    private String charset = "UTF-8";
    private String signType = "RSA2";
    private String format = "json";

    // getters and setters
    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }

    public String getPrivateKey() { return privateKey; }
    public void setPrivateKey(String privateKey) { this.privateKey = privateKey; }

    public String getAlipayPublicKey() { return alipayPublicKey; }
    public void setAlipayPublicKey(String alipayPublicKey) { this.alipayPublicKey = alipayPublicKey; }

    public String getReturnUrl() { return returnUrl; }
    public void setReturnUrl(String returnUrl) { this.returnUrl = returnUrl; }

    public String getNotifyUrl() { return notifyUrl; }
    public void setNotifyUrl(String notifyUrl) { this.notifyUrl = notifyUrl; }

    public String getGateway() { return gateway; }
    public void setGateway(String gateway) { this.gateway = gateway; }

    public String getCharset() { return charset; }
    public void setCharset(String charset) { this.charset = charset; }

    public String getSignType() { return signType; }
    public void setSignType(String signType) { this.signType = signType; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
}