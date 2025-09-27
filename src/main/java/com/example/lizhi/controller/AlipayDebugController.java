// AlipayDebugController.java
package com.example.lizhi.controller;

import com.example.lizhi.config.AlipayConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AlipayDebugController {

    @Autowired
    private AlipayConfig alipayConfig;

    @GetMapping("/payment/debug/config")
    public Map<String, Object> debugConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("appId", alipayConfig.getAppId());
        config.put("privateKeyLength", alipayConfig.getPrivateKey() != null ? alipayConfig.getPrivateKey().length() : 0);
        config.put("alipayPublicKeyLength", alipayConfig.getAlipayPublicKey() != null ? alipayConfig.getAlipayPublicKey().length() : 0);
        config.put("returnUrl", alipayConfig.getReturnUrl());
        config.put("notifyUrl", alipayConfig.getNotifyUrl());
        config.put("configStatus",
                alipayConfig.getAppId() != null &&
                        alipayConfig.getPrivateKey() != null &&
                        alipayConfig.getAlipayPublicKey() != null ? "OK" : "INCOMPLETE"
        );
        return config;
    }

    @GetMapping("/payment/debug/test")
    public String testConnection() {
        return "支付服务连接正常 - " + new java.util.Date();
    }
}