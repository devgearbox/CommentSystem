package com.example.lizhi.service.Impl;

import com.example.lizhi.entity.LitchiVariety;
import com.example.lizhi.entity.PurchaseOrder;
import com.example.lizhi.entity.Supplier;
import com.example.lizhi.repository.PurchaseOrderRepository;
import com.example.lizhi.service.LitchiVarietyService;
import com.example.lizhi.service.PurchaseOrderService;
import com.example.lizhi.service.StockInService;
import com.example.lizhi.service.SupplierService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PurchaseOrderServiceImpl implements PurchaseOrderService {
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderRepository orderRepository;
    private final StockInService stockInService;
    private final SupplierService supplierService;
    private final LitchiVarietyService litchiVarietyService;

    // 使用构造注入所有依赖
    public PurchaseOrderServiceImpl(
            PurchaseOrderRepository purchaseOrderRepository,
            StockInService stockInService,
            SupplierService supplierService,
            LitchiVarietyService litchiVarietyService
    ) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.orderRepository = purchaseOrderRepository; // 使用同一个repository实例
        this.stockInService = stockInService;
        this.supplierService = supplierService;
        this.litchiVarietyService = litchiVarietyService;
    }

    @Override
    public List<PurchaseOrder> getAllPurchaseOrdersSupplierUser() {
        return purchaseOrderRepository.findAllWithSupplierAndUser();
    }

    @Override
    public List<PurchaseOrder> getValidPurchaseOrdersSupplierUser() {
        return orderRepository.findAllValidWithSupplierUser();
    }

    public List<PurchaseOrder> searchBySupplierName(String supplierName) {
        return purchaseOrderRepository.findBySupplierSupplierNameContaining(supplierName);
    }

    @Override
    @Transactional
    public void batchDelete(List<Integer> ids) {
        List<PurchaseOrder> orders = new ArrayList<>();
        for (Integer id : ids) {
            PurchaseOrder order = orderRepository.findByIdAndIsDeletedFalse(id)
                    .orElseThrow(() -> new IllegalArgumentException("订单ID不存在或已被删除：" + id));
            orders.add(order);
        }

        for (PurchaseOrder order : orders) {
            if (order.getOrderStatus() != PurchaseOrder.OrderStatus.received) {
                throw new IllegalArgumentException("订单 [" + order.getOrderNo() + "] 状态非【已接收】，无法删除");
            }
        }

        orders.forEach(order -> order.setDeleted(true));
        orderRepository.saveAll(orders);
    }

    @Override
    public PurchaseOrder createPurchaseOrder(PurchaseOrder order) {
        if (order.getOrderNo() == null || order.getOrderNo().trim().isEmpty()) {
            order.setOrderNo(UUID.randomUUID().toString().replace("-", "").substring(0, 32));
        }
        if (order.getOrderStatus() == null) {
            order.setOrderStatus(PurchaseOrder.OrderStatus.pending);
        }
        if (order.getCreateTime() == null) {
            order.setCreateTime(new Date());
        }
        return purchaseOrderRepository.save(order);
    }

    @Override
    public Optional<PurchaseOrder> getOrderById(Integer orderId) {
        return orderRepository.findById(orderId);
    }

    @Override
    public List<PurchaseOrder> getOrdersByUserId(Long userId) {
        List<Integer> supplierIds = orderRepository.findSupplierIdsByUserId(userId);
        if (supplierIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<PurchaseOrder> allOrders = new ArrayList<>();
        for (Integer supplierId : supplierIds) {
            List<PurchaseOrder> orders = orderRepository.findValidOrdersBySupplierId(supplierId);
            allOrders.addAll(orders);
        }

        return allOrders.stream()
                .sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()))
                .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseOrder> searchOrdersBySupplierNameAndUserId(String supplierName, Long userId) {
        List<PurchaseOrder> myOrders = getOrdersByUserId(userId);
        return myOrders.stream()
                .filter(po -> po.getSupplier() != null
                        && po.getSupplier().getSupplier_name().contains(supplierName))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PurchaseOrder updateStatus(Integer orderId, String newStatus) {
        PurchaseOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在：" + orderId));
        PurchaseOrder.OrderStatus oldStatus = order.getOrderStatus();

        PurchaseOrder.OrderStatus newStatusEnum = PurchaseOrder.OrderStatus.valueOf(newStatus);

        // 定义允许的状态流转
        Map<PurchaseOrder.OrderStatus, List<PurchaseOrder.OrderStatus>> allowedTransitions = Map.of(
                PurchaseOrder.OrderStatus.pending, Arrays.asList(PurchaseOrder.OrderStatus.paid, PurchaseOrder.OrderStatus.cancelled),
                PurchaseOrder.OrderStatus.paid, Arrays.asList(PurchaseOrder.OrderStatus.shipping, PurchaseOrder.OrderStatus.cancelled),
                PurchaseOrder.OrderStatus.shipping, Arrays.asList(PurchaseOrder.OrderStatus.shipped, PurchaseOrder.OrderStatus.cancelled),
                PurchaseOrder.OrderStatus.shipped, Arrays.asList(PurchaseOrder.OrderStatus.received, PurchaseOrder.OrderStatus.rejected)
        );

        // 检查状态流转是否允许
        if (!allowedTransitions.getOrDefault(oldStatus, Collections.emptyList()).contains(newStatusEnum)) {
            throw new RuntimeException("非法状态流转：" + oldStatus.getLabel() + " -> " + newStatusEnum.getLabel());
        }

        order.setOrderStatus(newStatusEnum);
        PurchaseOrder updatedOrder = orderRepository.save(order);

        // 当状态变为 received 时，增加供应商和商品的 order_count
        if (newStatusEnum == PurchaseOrder.OrderStatus.received) {
            // 增加供应商订单数量
            Supplier supplier = order.getSupplier();
            if (supplier != null) {
                try {
                    supplierService.incrementOrderCount(supplier.getSupplier_id());
                    System.out.println("供应商订单数量增加成功: " + supplier.getSupplier_id());
                } catch (Exception e) {
                    System.err.println("供应商订单数量增加失败: " + e.getMessage());
                }
            }

            // 增加商品订单数量
            if (order.getLitchiVariety() != null) {
                try {
                    litchiVarietyService.incrementOrderCount(order.getLitchiVariety().getId());
                    System.out.println("商品订单数量增加成功: " + order.getLitchiVariety().getId());
                } catch (Exception e) {
                    System.err.println("商品订单数量增加失败: " + e.getMessage());
                }
            } else {
                System.err.println("订单未关联商品: " + order.getOrderNo());
            }
        }

        // 当状态变为 shipped 时，创建入库记录
        if (newStatusEnum == PurchaseOrder.OrderStatus.shipped && oldStatus != PurchaseOrder.OrderStatus.shipped) {
            stockInService.createStockInFromOrder(updatedOrder);
        }

        return updatedOrder;
    }

    @Override
    public List<PurchaseOrder> searchByOrderNo(String orderNo) {
        return purchaseOrderRepository.findByOrderNoExact(orderNo);
    }

    @Override
    public List<PurchaseOrder> searchOrdersByOrderNoAndUserId(String orderNo, Long userId) {
        List<PurchaseOrder> myOrders = getOrdersByUserId(userId);
        return myOrders.stream()
                .filter(po -> orderNo.equals(po.getOrderNo()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean checkStock(Integer varietyId, BigDecimal quantity) {
        if (varietyId == null || quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        Optional<LitchiVariety> varietyOpt = litchiVarietyService.findById(varietyId);
        if (varietyOpt.isPresent()) {
            LitchiVariety variety = varietyOpt.get();
            // 检查库存是否充足
            return variety.getStock() != null && variety.getStock() >= quantity.intValue();
        }
        return false;
    }

    @Override
    @Transactional
    public void reduceStock(Integer varietyId, BigDecimal quantity) {
        if (varietyId == null || quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("库存扣减参数无效");
        }

        Optional<LitchiVariety> varietyOpt = litchiVarietyService.findById(varietyId);
        if (varietyOpt.isPresent()) {
            LitchiVariety variety = varietyOpt.get();
            if (variety.getStock() == null || variety.getStock() < quantity.intValue()) {
                throw new RuntimeException("库存不足，无法扣减");
            }

            // 扣减库存
            variety.setStock(variety.getStock() - quantity.intValue());
            litchiVarietyService.addProduct(variety); // 使用已有的保存方法
            System.out.println("商品库存扣减成功: " + varietyId + ", 扣减数量: " + quantity);
        } else {
            throw new RuntimeException("商品不存在: " + varietyId);
        }
    }

    // 验证订单是否可以进行支付

    @Override
    public boolean validateOrderForPayment(String orderId) {
        try {
            Integer id = Integer.parseInt(orderId);
            Optional<PurchaseOrder> orderOpt = purchaseOrderRepository.findById(id);

            if (orderOpt.isPresent()) {
                PurchaseOrder order = orderOpt.get();
                // 检查订单状态是否为待审核状态（允许支付）
                return order.getOrderStatus() == PurchaseOrder.OrderStatus.pending;
            }
            return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // 更新订单状态
    @Override
    public void updateOrderStatus(String orderId, String status) {
        try {
            Integer id = Integer.parseInt(orderId);
            Optional<PurchaseOrder> orderOpt = purchaseOrderRepository.findById(id);

            if (orderOpt.isPresent()) {
                PurchaseOrder order = orderOpt.get();

                // 将字符串状态转换为枚举
                PurchaseOrder.OrderStatus newStatus = convertStringToOrderStatus(status);
                order.setOrderStatus(newStatus);
                purchaseOrderRepository.save(order);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("无效的订单ID: " + orderId);
        }
    }

    // 辅助方法：将字符串状态转换为枚举
    private PurchaseOrder.OrderStatus convertStringToOrderStatus(String status) {
        switch (status) {
            case "已支付": return PurchaseOrder.OrderStatus.paid;
            case "未支付": return PurchaseOrder.OrderStatus.pending;
            case "待发货": return PurchaseOrder.OrderStatus.shipping;
            case "已发货": return PurchaseOrder.OrderStatus.shipped;
            case "已接收": return PurchaseOrder.OrderStatus.received;
            case "已取消": return PurchaseOrder.OrderStatus.cancelled;
            case "拒收": return PurchaseOrder.OrderStatus.rejected;
            default: throw new IllegalArgumentException("未知的订单状态: " + status);
        }
    }

    @Override
    public Page<PurchaseOrder> getValidPurchaseOrdersSupplierUser(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        // 需要创建对应的分页查询方法
        return purchaseOrderRepository.findAllValidWithSupplierUser(pageable);
    }

    @Override
    public Page<PurchaseOrder> getOrdersByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        List<Integer> supplierIds = orderRepository.findSupplierIdsByUserId(userId);

        if (supplierIds.isEmpty()) {
            return Page.empty(pageable);
        }

        return purchaseOrderRepository.findValidOrdersBySupplierIds(supplierIds, pageable);
    }

    @Override
    public Page<PurchaseOrder> searchByOrderNo(String orderNo, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return purchaseOrderRepository.findByOrderNoContaining(orderNo, pageable);
    }

    @Override
    public Page<PurchaseOrder> searchOrdersByOrderNoAndUserId(String orderNo, Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        List<Integer> supplierIds = orderRepository.findSupplierIdsByUserId(userId);

        if (supplierIds.isEmpty()) {
            return Page.empty(pageable);
        }

        return purchaseOrderRepository.findByOrderNoAndSupplierIds(orderNo, supplierIds, pageable);
    }
    @Override
    public Page<PurchaseOrder> getOrdersByPurchaserId(Long purchaserId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return purchaseOrderRepository.findByUserIdAndIsDeletedFalse(purchaserId, pageable);
    }

    @Override
    public Page<PurchaseOrder> searchOrdersByOrderNoAndPurchaserId(String orderNo, Long purchaserId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return purchaseOrderRepository.findByOrderNoAndUserId(orderNo, purchaserId, pageable);
    }

    @Override
    public List<PurchaseOrder> searchOrdersByOrderNoAndPurchaserId(String orderNo, Long purchaserId) {
        return purchaseOrderRepository.findByOrderNoAndUserId(orderNo, purchaserId);
    }

    @Override
    public BigDecimal getTotalSalesQuantityByVarietyId(Integer varietyId) {
        if (varietyId == null) {
            return BigDecimal.ZERO;
        }
        // 统计所有订单状态（或特定状态）的销售数量总和
        BigDecimal result = purchaseOrderRepository.sumPurchaseQuantityByVarietyId(varietyId);
        return result != null ? result : BigDecimal.ZERO;
    }
    @Override
    public boolean isOrderPaid(String orderId) {
        try {
            Integer id = Integer.parseInt(orderId);
            Optional<PurchaseOrder> orderOpt = purchaseOrderRepository.findById(id);
            return orderOpt.isPresent() &&
                    orderOpt.get().getOrderStatus() == PurchaseOrder.OrderStatus.paid;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}