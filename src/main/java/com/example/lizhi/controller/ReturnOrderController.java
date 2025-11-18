package com.example.lizhi.controller;

import com.example.lizhi.entity.ReturnOrder;
import com.example.lizhi.entity.User;
import com.example.lizhi.service.ReturnOrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
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
    public String getReturnOrdersPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session,
            Model model) {

        User currentUser = (User) session.getAttribute("currentUser");
        Page<ReturnOrder> returnOrderPage = returnOrderService.getAllReturnOrders(page, size, currentUser);

        model.addAttribute("returnOrders", returnOrderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", returnOrderPage.getTotalPages());
        model.addAttribute("totalItems", returnOrderPage.getTotalElements());
        model.addAttribute("pageSize", size);

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
    public String searchReturns(
            @RequestParam String orderNo,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session,
            Model model) {

        User currentUser = (User) session.getAttribute("currentUser");
        Page<ReturnOrder> returnOrderPage = returnOrderService.searchByOrderNo(orderNo, page, size, currentUser);

        model.addAttribute("returnOrders", returnOrderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", returnOrderPage.getTotalPages());
        model.addAttribute("totalItems", returnOrderPage.getTotalElements());
        model.addAttribute("pageSize", size);

        return "returns :: #return-table-body";
    }

    //批量删除退货单
    @DeleteMapping("/returns/delete/batch")
    public ResponseEntity<Map<String, Object>> batchDeleteReturns(@RequestBody Map<String, List<Integer>> request) {
        List<Integer> ids = request.get("ids");
        try {
            returnOrderService.batchDelete(ids);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "批量删除成功"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "系统错误：" + e.getMessage()
            ));
        }
    }
}