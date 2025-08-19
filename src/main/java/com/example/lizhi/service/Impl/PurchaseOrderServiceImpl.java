package com.example.lizhi.service.Impl;

import com.example.lizhi.entity.PurchaseOrder;
import com.example.lizhi.entity.Supplier;
import com.example.lizhi.repository.PurchaseOrderRepository;
import com.example.lizhi.repository.StockInRepository;
import com.example.lizhi.service.PurchaseOrderService;
import com.example.lizhi.service.StockInService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PurchaseOrderServiceImpl implements PurchaseOrderService {
    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;
    @Autowired
    private PurchaseOrderRepository orderRepository;
    @Autowired
    private StockInRepository stockInRepo;

    @Override
    public List<PurchaseOrder> getAllPurchaseOrdersSupplierUser() {
        // 调用关联查询方法
        return purchaseOrderRepository.findAllWithSupplierAndUser();
    }
    @Override
    public List<PurchaseOrder> getValidPurchaseOrdersSupplierUser() {
        return orderRepository.findAllValidWithSupplierUser();
    }

    // 搜索方法
    public List<PurchaseOrder> searchBySupplierName(String supplierName) {
        return purchaseOrderRepository.findBySupplierSupplierNameContaining(supplierName);
    }

    @Override
    @Transactional // 事务保证：要么全成功，要么全失败
    public void batchDelete(List<Integer> ids) {
        // 1. 校验订单是否存在且未被删除
        List<PurchaseOrder> orders = new ArrayList<>();
        for (Integer id : ids) {
            PurchaseOrder order = orderRepository.findByIdAndIsDeletedFalse(id)
                    .orElseThrow(() -> new IllegalArgumentException("订单ID不存在或已被删除：" + id));
            orders.add(order);
        }

        // 2. 校验订单状态是否为"received"
        for (PurchaseOrder order : orders) {
            if (order.getOrderStatus() != PurchaseOrder.OrderStatus.received) {
                throw new IllegalArgumentException("订单 [" + order.getOrderNo() + "] 状态非【已接收】，无法删除");
            }
        }

        // 3. 执行软删除：标记isDeleted为true
        orders.forEach(order -> order.setDeleted(true));
        orderRepository.saveAll(orders); // 批量更新
    }

    // 实现新增的创建订单方法
    @Override
    public PurchaseOrder createPurchaseOrder(PurchaseOrder order) {
        // 可在此处补充业务逻辑，比如设置默认订单号、订单状态等
        if (order.getOrderNo() == null || order.getOrderNo().trim().isEmpty()) {
            // 简单生成订单号示例，可根据实际需求调整（如 UUID、雪花算法等）
            order.setOrderNo(UUID.randomUUID().toString().replace("-", "").substring(0, 32));
        }
        if (order.getOrderStatus() == null) {
            order.setOrderStatus(PurchaseOrder.OrderStatus.pending); // 设置默认待审核状态
        }
        if (order.getCreate_time() == null) {
            order.setCreate_time(new Date());
        }
        return purchaseOrderRepository.save(order);
    }
    @Override
    public Optional<PurchaseOrder> getOrderById(Integer orderId) {
        return orderRepository.findById(orderId);
    }

    private final StockInService stockInService;

    // 【手动添加构造函数】显式注入依赖
    public PurchaseOrderServiceImpl(
            PurchaseOrderRepository orderRepository,
            StockInService stockInService
    ) {
        this.orderRepository = orderRepository;
        this.stockInService = stockInService;
    }
    @Override
    @Transactional
    public PurchaseOrder updateStatus(Integer orderId, String newStatus) {
        PurchaseOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在：" + orderId));
        PurchaseOrder.OrderStatus oldStatus = order.getOrderStatus();

        PurchaseOrder.OrderStatus newStatusEnum = PurchaseOrder.OrderStatus.valueOf(newStatus);
        int oldIndex = Arrays.asList(PurchaseOrder.OrderStatus.values()).indexOf(oldStatus);
        int newIndex = Arrays.asList(PurchaseOrder.OrderStatus.values()).indexOf(newStatusEnum);

        if (newIndex < oldIndex || newIndex >= 3) {
            throw new RuntimeException("非法状态流转：" + oldStatus.getLabel() + " -> " + newStatusEnum.getLabel());
        }

        order.setOrderStatus(newStatusEnum);
        PurchaseOrder updatedOrder = orderRepository.save(order);

        if (newStatusEnum == PurchaseOrder.OrderStatus.shipped && oldStatus != PurchaseOrder.OrderStatus.shipped) {
            stockInService.createStockInFromOrder(updatedOrder);
        }

        return updatedOrder;
    }
}