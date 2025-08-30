package com.example.lizhi.controller;

import com.example.lizhi.entity.PurchaseOrder;
import com.example.lizhi.entity.Supplier;
import com.example.lizhi.entity.User;
import com.example.lizhi.repository.UserRepository;
import com.example.lizhi.service.PurchaseOrderService;
import com.example.lizhi.service.SupplierService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class PurchaseOrderController {
    @Autowired
    private PurchaseOrderService purchaseOrderService;
    // 可注入当前登录用户相关服务，用于获取 purchaserId，这里先简单示例，实际需结合权限体系
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SupplierService supplierService;
    @Autowired
    private PurchaseOrderService orderService;

    @GetMapping("/orders")
    public String listPurchaseOrder(Model model, HttpSession session) {
        // 1. 获取当前登录用户
        User currentUser = (User) session.getAttribute("currentUser");
        List<PurchaseOrder> purchaseOrders;

        // 2. 按角色筛选数据
        if (currentUser != null && currentUser.getRole() == 3) {
            // 供应商角色：只显示自己关联的供应商订单
            purchaseOrders = orderService.getOrdersByUserId(currentUser.getId());
        } else {
            // 其他角色：显示全部未删除的订单（原有逻辑）
            purchaseOrders = orderService.getValidPurchaseOrdersSupplierUser();
        }

        // 3. 传递数据到前端
        model.addAttribute("purchaseOrders", purchaseOrders);
        return "orders";
    }

    // 修改搜索接口参数和逻辑
    @GetMapping("/orders/search")
    public String searchOrders(
            @RequestParam String orderNo,  // 参数名从supplierName改为orderNo
            Model model,
            HttpSession session
    ) {
        User currentUser = (User) session.getAttribute("currentUser");
        List<PurchaseOrder> orders;

        if (currentUser != null && currentUser.getRole() == 3) {
            // 供应商角色：搜索自己关联的订单（按订单编号）
            orders = orderService.searchOrdersByOrderNoAndUserId(orderNo, currentUser.getId());
        } else {
            // 其他角色：按订单编号搜索全部订单
            orders = orderService.searchByOrderNo(orderNo);
        }

        model.addAttribute("purchaseOrders", orders);
        return "orders :: #order-table-body";
    }

    // 与供应商的 /delete/batch 对齐，路径可根据前端调整
    @DeleteMapping("/orders/delete/batch")
    public ResponseEntity<Map<String, Object>> batchDeleteOrders(@RequestBody Map<String, List<Integer>> request) {
        List<Integer> ids = request.get("ids");
        try {
            purchaseOrderService.batchDelete(ids);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "批量删除成功（仅已接收状态订单）"
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

    // 新增提交订单接口，假设前端传参为 JSON 格式
    @PostMapping("/work/submit")
    @ResponseBody
    public ResponseEntity<?> submitOrder(
            @RequestBody PurchaseOrderRequest request,
            HttpSession session
    ) {
        try {
            // 1. 获取当前登录用户 ID
            Long userId = (Long) session.getAttribute("currentUserId");
            if (userId == null) {
                return ResponseEntity.badRequest().body("用户未登录");
            }

            // 2. 查询用户（增加空值校验）
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("用户 ID 无效：" + userId));

            // 3. 构建 PurchaseOrder 对象
            PurchaseOrder order = new PurchaseOrder();
            order.setPurchase_variety(request.getVarietyName());
            order.setPurchase_quantity(new BigDecimal(request.getQuantity()));
            // 在构建PurchaseOrder对象时添加
            order.setTotalPrice(new BigDecimal(request.getTotalPrice()));

            // 4. 查询供应商（增加空值校验）
            Supplier supplier = supplierService.searchSuppliersByName(request.getSupplierName())
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("未找到供应商：" + request.getSupplierName()));
            order.setSupplier(supplier);
            order.setUser(user); // 设置采购人

            // 5. 调用 service 保存订单
            PurchaseOrder savedOrder = purchaseOrderService.createPurchaseOrder(order);

            // 6. 返回成功响应
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "订单提交成功",
                    "orderId", savedOrder.getOrder_id()
            ));

        } catch (IllegalArgumentException e) {
            // 处理明确的业务异常（如用户/供应商不存在）
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "订单提交失败：" + e.getMessage()
            ));
        } catch (Exception e) {
            // 处理其他未知异常
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "订单提交失败：" + e.getMessage()
            ));
        }
    }


    @GetMapping("/orders/detail/{orderId}")
    public ResponseEntity<?> getOrderDetail(@PathVariable Integer orderId) {
        Optional<PurchaseOrder> orderOpt = orderService.getOrderById(orderId);
        if (orderOpt.isPresent()) {
            return ResponseEntity.ok(orderOpt.get());
        } else {
            return ResponseEntity.status(404).body("订单 ID 不存在");
        }
    }

    // 新增状态修改接口
    @PutMapping("/api/orders/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Integer orderId,
            @RequestParam String newStatus) {
        try {
            PurchaseOrder updatedOrder = purchaseOrderService.updateStatus(orderId, newStatus);
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            // 捕获服务层抛出的非法状态异常
            return ResponseEntity.badRequest().body("修改失败：" + e.getMessage());
        } catch (Exception e) {
            // 其他未知异常
            return ResponseEntity.status(500).body("服务器错误：" + e.getMessage());
        }
    }
    // 内部静态类用于接收前端提交订单的参数
    static class PurchaseOrderRequest {
        private String varietyName;
        private String supplierName;
        private String quantity;
        private String totalPrice;

        public String getTotalPrice() {
            return totalPrice;
        }

        public void setTotalPrice(String totalPrice) {
            this.totalPrice = totalPrice;
        }

        public String getVarietyName() {
            return varietyName;
        }

        public void setVarietyName(String varietyName) {
            this.varietyName = varietyName;
        }

        public String getSupplierName() {
            return supplierName;
        }

        public void setSupplierName(String supplierName) {
            this.supplierName = supplierName;
        }

        public String getQuantity() {
            return quantity;
        }

        public void setQuantity(String quantity) {
            this.quantity = quantity;
        }
    }
}