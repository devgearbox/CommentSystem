package com.example.lizhi.controller;

import com.example.lizhi.service.AlipayService;
import com.example.lizhi.service.PurchaseOrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class PaymentController {

    @Autowired
    private AlipayService alipayService;

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    // 发起支付请求
    @PostMapping("/payment/create")
    @ResponseBody
    public String createPayment(@RequestBody PaymentRequest request) {
        try {
            System.out.println("=== 收到支付请求 ===");
            System.out.println("订单ID: " + request.getOrderId());
            System.out.println("金额: " + request.getTotalAmount());

            // 验证订单是否存在且未支付
            if (!purchaseOrderService.validateOrderForPayment(request.getOrderId())) {
                return "<script>alert('订单不存在或状态异常');window.history.back();</script>";
            }

            String paymentForm = alipayService.createPayment(
                    request.getOrderId(),
                    request.getTotalAmount(),
                    "荔枝批发订单-" + request.getOrderId()
            );

            System.out.println("直接返回支付宝支付表单");

            // 直接返回HTML表单，让浏览器自动提交
            return paymentForm;

        } catch (Exception e) {
            e.printStackTrace();
            return "<script>alert('支付请求创建失败: " + e.getMessage().replace("\"", "\\\"") + "');window.history.back();</script>";
        }
    }

    // 支付宝同步回调（支付成功页面）
    @GetMapping("/payment/success")
    public String paymentSuccess(
            @RequestParam String out_trade_no,
            @RequestParam String total_amount,
            Model model) {

        // 更新订单状态为已支付
        purchaseOrderService.updateOrderStatus(out_trade_no, "已支付");

        model.addAttribute("orderId", out_trade_no);
        model.addAttribute("totalPrice", total_amount);
        model.addAttribute("paymentMethod", "支付宝支付");

        return "payment-success";
    }

    // 支付宝异步回调（支付结果通知）
    @PostMapping("/payment/notify")
    @ResponseBody
    public String paymentNotify(HttpServletRequest request) {
        try {
            Map<String, String> params = convertRequestToMap(request);

            // 验证签名
            if (!alipayService.verifySignature(params)) {
                return "failure";
            }

            // 处理业务逻辑
            String tradeStatus = params.get("trade_status");
            String orderId = params.get("out_trade_no");

            if ("TRADE_SUCCESS".equals(tradeStatus)) {
                // 更新订单状态
                purchaseOrderService.updateOrderStatus(orderId, "已支付");
            }

            return "success";
        } catch (Exception e) {
            return "failure";
        }
    }

    private Map<String, String> convertRequestToMap(HttpServletRequest request) {
        // 将request参数转换为Map
        return request.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> String.join(",", entry.getValue())
                ));
    }

    static class PaymentRequest {
        private String orderId;
        private String totalAmount;

        // getters and setters
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        public String getTotalAmount() { return totalAmount; }
        public void setTotalAmount(String totalAmount) { this.totalAmount = totalAmount; }
    }
}