// AlipayService.java - 更新版本
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

    // 初始化支付宝配置
    private Config initAlipayConfig() {
        Config config = new Config();
        config.protocol = "https";
        config.gatewayHost = "openapi-sandbox.dl.alipaydev.com";
        config.signType = alipayConfig.getSignType();
        config.appId = alipayConfig.getAppId();
        config.merchantPrivateKey = alipayConfig.getPrivateKey();
        config.alipayPublicKey = alipayConfig.getAlipayPublicKey();
        config.notifyUrl = alipayConfig.getNotifyUrl();
        return config;
    }

    public String createPayment(String orderId, String totalAmount, String subject) throws Exception {
        logger.info("=== 开始创建支付宝支付请求 ===");
        logger.info("订单ID: {}, 金额: {}, 商品: {}", orderId, totalAmount, subject);

        // 验证配置
        if (alipayConfig.getAppId() == null || alipayConfig.getPrivateKey() == null) {
            throw new Exception("支付宝配置不完整");
        }

        try {
            // 初始化配置
            Factory.setOptions(initAlipayConfig());

            // 确保金额格式正确
            if (!totalAmount.contains(".")) {
                totalAmount = totalAmount + ".00";
            }

            logger.info("调用支付宝页面支付接口...");
            AlipayTradePagePayResponse response = Factory
                    .Payment
                    .Page()
                    .pay(subject, orderId, totalAmount, alipayConfig.getReturnUrl());

            logger.info("支付宝支付表单生成成功");
            return response.getBody();

        } catch (Exception e) {
            logger.error("支付宝支付请求失败", e);
            throw new Exception("支付宝支付请求失败: " + e.getMessage());
        }
    }

    public boolean verifySignature(Map<String, String> params) throws Exception {
        try {
            Factory.setOptions(initAlipayConfig());
            return Factory.Payment.Common().verifyNotify(params);
        } catch (Exception e) {
            logger.error("支付宝签名验证失败", e);
            return false;
        }
    }

    // 查询支付状态
    public boolean checkPaymentStatus(String orderId) throws Exception {
        try {
            Factory.setOptions(initAlipayConfig());
            var response = Factory.Payment.Common().query(orderId);
            logger.info("支付宝查询结果: {}", response);
            return "TRADE_SUCCESS".equals(response.getTradeStatus()) ||
                    "TRADE_FINISHED".equals(response.getTradeStatus());
        } catch (Exception e) {
            logger.error("查询支付宝支付状态失败", e);
            return false;
        }
    }
}