package com.example.lizhi.controller;

import com.example.lizhi.entity.StockIn;
import com.example.lizhi.service.ReturnOrderService;
import com.example.lizhi.service.StockInService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class StockInController {

    private final StockInService stockInService;
    private final ReturnOrderService returnOrderService;

    public StockInController(StockInService stockInService, ReturnOrderService returnOrderService) {
        this.stockInService = stockInService;
        this.returnOrderService = returnOrderService;
    }

    @GetMapping("/stock")
    public String getStockInPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Page<StockIn> stockRecordPage = stockInService.findAllStockIn(page, size);

        model.addAttribute("stockRecords", stockRecordPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", stockRecordPage.getTotalPages());
        model.addAttribute("totalItems", stockRecordPage.getTotalElements());
        model.addAttribute("pageSize", size);

        return "stock";
    }

    @PutMapping("/api/stock/{stockId}/status")
    public ResponseEntity<?> updateStockStatus(
            @PathVariable Integer stockId,
            @RequestBody Map<String, Object> request) {
        try {
            String newStatus = (String) request.get("newStatus");
            String rejectionReason = (String) request.get("rejectionReason");

            StockIn updatedStock = stockInService.updateStatus(stockId, newStatus, rejectionReason);

            return ResponseEntity.ok(updatedStock);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("修改失败：" + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("服务器错误：" + e.getMessage());
        }
    }
}