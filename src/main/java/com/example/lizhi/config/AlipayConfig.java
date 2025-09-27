package com.example.lizhi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "alipay")
public class AlipayConfig {
    private String appId;
    private String privateKey;
    private String publicKey;
    private String alipayPublicKey;
    private String serverUrl = "https://openapi-sandbox.dl.alipaydev.com/gateway.do"; // 沙箱环境
    private String returnUrl = "http://localhost:8080/otherHtml/payment-success.html";
    private String notifyUrl = "http://localhost:8080/payment/notify";
}