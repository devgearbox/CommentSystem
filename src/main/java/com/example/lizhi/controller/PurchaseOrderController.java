package com.example.lizhi.controller;

import com.example.lizhi.entity.PurchaseOrder;
import com.example.lizhi.entity.Supplier;
import com.example.lizhi.service.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class PurchaseOrderController {
    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @GetMapping("/orders")
    public String listPurchaseOrder(Model model) {
        // 替换为关联查询的方法
        List<PurchaseOrder> purchaseOrders = purchaseOrderService.getAllPurchaseOrdersSupplierUser();
        model.addAttribute("purchaseOrders", purchaseOrders);
        return "orders";
    }

    @GetMapping("/orders/search")
    @ResponseBody
    public List<PurchaseOrder> searchOrders(
            @RequestParam String supplierName // 与前端传参名一致
    ) {
        return purchaseOrderService.searchBySupplierName(supplierName);
    }

    // 与供应商的 /delete/batch 对齐，路径可根据前端调整
    @DeleteMapping("/orders/delete/batch")
    public ResponseEntity<?> batchDeleteOrders(@RequestBody Map<String, List<Integer>> request) {
        List<Integer> ids = request.get("ids");
        try {
            purchaseOrderService.batchDelete(ids);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "批量删除订单成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}