package com.example.lizhi.controller;

import com.example.lizhi.entity.LitchiVariety;
import com.example.lizhi.entity.PurchaseOrder;
import com.example.lizhi.entity.Supplier;
import com.example.lizhi.entity.User;
import com.example.lizhi.repository.UserRepository;
import com.example.lizhi.service.LitchiVarietyService;
import com.example.lizhi.service.PurchaseOrderService;
import com.example.lizhi.service.SupplierService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page; // 修正：使用 Spring Data 的 Page
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
    @Autowired
    private LitchiVarietyService litchiVarietyService;

    @GetMapping("/orders")
    public String listPurchaseOrder(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model,
            HttpSession session) {

        // 1. 获取当前登录用户
        User currentUser = (User) session.getAttribute("currentUser");
        Page<PurchaseOrder> purchaseOrderPage;

        // 2. 按角色筛选数据
        if (currentUser != null) {
            if (currentUser.getRole() == 3) {
                // 供应商角色：只显示自己关联的供应商订单（分页）
                purchaseOrderPage = orderService.getOrdersByUserId(currentUser.getId(), page, size);
            } else if (currentUser.getRole() == 2) {
                // 普通用户角色：只显示自己创建的订单（分页）
                purchaseOrderPage = orderService.getOrdersByPurchaserId(currentUser.getId(), page, size);
            } else {
                // 管理员角色：显示全部未删除的订单（分页）
                purchaseOrderPage = orderService.getValidPurchaseOrdersSupplierUser(page, size);
            }
        } else {
            // 未登录用户：显示空列表
            purchaseOrderPage = Page.empty();
        }

        // 3. 传递数据到前端
        model.addAttribute("purchaseOrders", purchaseOrderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", purchaseOrderPage.getTotalPages());
        model.addAttribute("totalItems", purchaseOrderPage.getTotalElements());
        model.addAttribute("pageSize", size);

        return "orders";
    }

    // 修改搜索接口参数和逻辑
    @GetMapping("/orders/search")
    public String searchOrders(
            @RequestParam String orderNo,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model,
            HttpSession session
    ) {
        User currentUser = (User) session.getAttribute("currentUser");
        Page<PurchaseOrder> ordersPage;

        if (currentUser != null) {
            if (currentUser.getRole() == 3) {
                // 供应商角色：搜索自己关联的订单
                ordersPage = orderService.searchOrdersByOrderNoAndUserId(orderNo, currentUser.getId(), page, size);
            } else if (currentUser.getRole() == 2) {
                // 普通用户角色：搜索自己创建的订单
                ordersPage = orderService.searchOrdersByOrderNoAndPurchaserId(orderNo, currentUser.getId(), page, size);
            } else {
                // 管理员角色：搜索全部订单
                ordersPage = orderService.searchByOrderNo(orderNo, page, size);
            }
        } else {
            ordersPage = Page.empty();
        }

        model.addAttribute("purchaseOrders", ordersPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", ordersPage.getTotalPages());
        model.addAttribute("totalItems", ordersPage.getTotalElements());
        model.addAttribute("pageSize", size);

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

    // 提交订单接口，假设前端传参为 JSON 格式
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

            // 2. 查询用户
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("用户 ID 无效：" + userId));

            // 3. 查询供应商
            Supplier supplier = supplierService.searchSuppliersByName(request.getSupplierName())
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("未找到供应商：" + request.getSupplierName()));

            // 4.查询商品并检查库存
            List<LitchiVariety> varieties = litchiVarietyService.searchByVarietyName(request.getVarietyName());
            if (varieties.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "未找到商品：" + request.getVarietyName()
                ));
            }

            LitchiVariety variety = varieties.get(0);
            BigDecimal quantity = new BigDecimal(request.getQuantity());

            // 检查库存是否充足
            if (!purchaseOrderService.checkStock(variety.getId(), quantity)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "购买数量过大，库存不足。当前库存: " + variety.getStock() + "斤，请与供应商进行联系"
                ));
            }

            // 5. 构建 PurchaseOrder 对象
            PurchaseOrder order = new PurchaseOrder();
            order.setPurchase_variety(request.getVarietyName());
            order.setPurchase_quantity(quantity);
            order.setTotalPrice(new BigDecimal(request.getTotalPrice()));
            order.setSupplier(supplier);
            order.setUser(user);
            order.setLitchiVariety(variety); // 关联商品
            order.setOrderStatus(PurchaseOrder.OrderStatus.pending); // 设置初始状态为待支付
            order.setSpecification(request.getSpecification()); // 设置规格信息

            // 6.扣减库存
            purchaseOrderService.reduceStock(variety.getId(), quantity);

            // 7. 保存订单
            PurchaseOrder savedOrder = purchaseOrderService.createPurchaseOrder(order);

            // 8. 返回成功响应
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "订单提交成功",
                    "orderId", savedOrder.getOrder_id()
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "订单提交失败：" + e.getMessage()
            ));
        } catch (Exception e) {
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

    // 状态修改接口
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
        private String specification;

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

        public String getSpecification() {return specification;}

        public void setSpecification(String specification) {this.specification = specification;}
    }
}