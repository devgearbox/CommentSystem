package com.example.lizhi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    // 处理采购工作台请求
    @GetMapping("/work")
    public String showDashboard() {
        return "work"; // 对应 templates/work.html
    }

    // 处理采购订单管理请求
    @GetMapping("/orders")
    public String showOrders() {
        return "orders"; // 对应 templates/orders.html
    }

    // 处理入库管理请求
    @GetMapping("/inventory")
    public String showInventory() {
        return "inventory"; // 对应 templates/inventory.html
    }

    // 处理供应商管理请求
    @GetMapping("/dashboard/suppliers") // 修改映射路径
    public String showSuppliers() {
        return "suppliers"; // 对应 templates/suppliers.html
    }

    // 处理采购统计分析请求
    @GetMapping("/analysis")
    public String showAnalysis() {
        return "analysis"; // 对应 templates/analysis.html
    }

    // 处理个人设置请求
    @GetMapping("/settings")
    public String showSettings() {
        return "settings"; // 对应 templates/settings.html
    }
}