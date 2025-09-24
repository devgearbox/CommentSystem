package com.example.lizhi.controller;

import com.example.lizhi.entity.ReturnOrder;
import com.example.lizhi.service.ReturnOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class ReturnOrderController {

    private final ReturnOrderService returnOrderService;

    public ReturnOrderController(ReturnOrderService returnOrderService) {
        this.returnOrderService = returnOrderService;
    }

    @GetMapping("/returns")
    public String getReturnOrdersPage(Model model) {
        List<ReturnOrder> returnOrders = returnOrderService.getAllReturnOrders();
        model.addAttribute("returnOrders", returnOrders);
        return "returns";
    }

    @PostMapping("/api/returns/create-from-rejection")
    public ResponseEntity<?> createReturnFromRejection(@RequestBody Map<String, String> request) {
        try {
            String orderNo = request.get("orderNo");
            String reason = request.get("reason");

            ReturnOrder returnOrder = returnOrderService.createReturnFromStockRejection(orderNo, reason);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "退货单创建成功",
                    "returnOrder", returnOrder
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/api/returns/{returnId}/status")
    public ResponseEntity<?> updateReturnStatus(
            @PathVariable Integer returnId,
            @RequestParam String newStatus) {
        try {
            ReturnOrder updatedReturn = returnOrderService.updateReturnStatus(returnId, newStatus);
            return ResponseEntity.ok(updatedReturn);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("修改失败：" + e.getMessage());
        }
    }

    @GetMapping("/returns/search")
    public String searchReturns(@RequestParam String orderNo, Model model) {
        List<ReturnOrder> returnOrders = returnOrderService.searchByOrderNo(orderNo);
        model.addAttribute("returnOrders", returnOrders);
        return "returns :: #return-table-body";
    }
}