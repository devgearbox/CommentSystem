package com.example.lizhi.service;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.kernel.Config;
import com.alipay.easysdk.payment.page.models.AlipayTradePagePayResponse;
import com.example.lizhi.config.AlipayConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AlipayService {

    private static final Logger logger = LoggerFactory.getLogger(AlipayService.class);

    @Autowired
    private AlipayConfig alipayConfig;

    public String createPayment(String orderId, String totalAmount, String subject) throws Exception {
        logger.info("=== 开始创建支付请求 ===");
        logger.info("订单ID: {}, 金额: {}, 商品: {}", orderId, totalAmount, subject);

        // 验证配置
        if (alipayConfig.getAppId() == null || alipayConfig.getPrivateKey() == null) {
            throw new Exception("支付宝配置不完整");
        }

        // 初始化配置
        Config config = new Config();
        config.protocol = "https";
        config.gatewayHost = "openapi-sandbox.dl.alipaydev.com";
        config.signType = "RSA2";
        config.appId = alipayConfig.getAppId();
        config.merchantPrivateKey = alipayConfig.getPrivateKey();
        config.alipayPublicKey = alipayConfig.getAlipayPublicKey();
        config.notifyUrl = alipayConfig.getNotifyUrl();

        logger.info("配置初始化完成 - APP_ID: {}", config.appId.substring(0, 8) + "***");

        try {
            Factory.setOptions(config);

            // 确保金额格式正确
            if (!totalAmount.contains(".")) {
                totalAmount = totalAmount + ".00";
            }

            AlipayTradePagePayResponse response = Factory
                    .Payment
                    .Page()
                    .pay(subject, orderId, totalAmount, alipayConfig.getReturnUrl());

            logger.info("支付宝支付表单生成成功，响应长度: {}", response.getBody().length());
            return response.getBody();

        } catch (Exception e) {
            logger.error("支付宝支付请求失败", e);
            throw new Exception("支付宝支付请求失败: " + e.getMessage());
        }
    }

    public boolean verifySignature(Map<String, String> params) throws Exception {
        Config config = new Config();
        config.protocol = "https";
        config.gatewayHost = "openapi.alipaydev.com";
        config.signType = "RSA2";
        config.appId = alipayConfig.getAppId();
        config.merchantPrivateKey = alipayConfig.getPrivateKey();
        config.alipayPublicKey = alipayConfig.getAlipayPublicKey();

        Factory.setOptions(config);
        return Factory.Payment.Common().verifyNotify(params);
    }
}