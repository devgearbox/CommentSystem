package com.example.lizhi.controller;

import com.example.lizhi.entity.StockIn;
import com.example.lizhi.service.StockInService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class StockInController {

    private final StockInService stockInService;

    public StockInController(StockInService stockInService) {
        this.stockInService = stockInService;
    }

    @GetMapping("/stock")
    public String getStockInPage(Model model) {
        List<StockIn> stockRecords = stockInService.findAllStockIn();
        model.addAttribute("stockRecords", stockRecords);
        return "stock"; // 这里返回的视图名要和你的 stock.html 实际前缀等匹配，假设放在 templates 目录下直接返回 "stock"
    }

    @PutMapping("/api/stock/{stockId}/status")
    public ResponseEntity<?> updateStockStatus(
            @PathVariable Integer stockId,
            @RequestParam String newStatus) {
        try {
            StockIn updatedStock = stockInService.updateStatus(stockId, newStatus);
            return ResponseEntity.ok(updatedStock);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("修改失败：商品已接收");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("服务器错误：" + e.getMessage());
        }
    }
}