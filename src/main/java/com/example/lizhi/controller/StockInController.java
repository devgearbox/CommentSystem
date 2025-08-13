package com.example.lizhi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StockInController {
    // 处理入库管理请求
    @GetMapping("/stock")
    public String showInventory() {
        return "stock";
    }
}
