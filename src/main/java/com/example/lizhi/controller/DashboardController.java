package com.example.lizhi.controller;

import org.springframework.ui.Model;
import com.example.lizhi.entity.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DashboardController {

//    // 处理采购订单管理请求
//    @GetMapping("/orders")
//    public String showOrders() {
//        return "orders"; // 对应 templates/orders.html
//    }

    // 处理入库管理请求
    @GetMapping("/inventory")
    public String showInventory() {
        return "inventory"; // 对应 templates/inventory.html
    }

    // 处理采购统计分析请求
    @GetMapping("/analysis")
    public String showAnalysis() {
        return "analysis"; // 对应 templates/analysis.html
    }



    @GetMapping("/feedback")
    public String feedback() {
        return "forward:/otherHtml/feedback.html" ;// 对应 templates 目录下的 feedback.html（Thymeleaf 模板）
    }

    @GetMapping("/help")
    public String help() {
        return "forward:/otherHtml/help.html";
    }

    @GetMapping("/introduction")
    public String introduction() {
        return "forward:/otherHtml/introduction.html";
    }
}