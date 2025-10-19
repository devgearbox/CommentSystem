package com.example.lizhi.service;

import com.example.lizhi.entity.PurchaseOrder;
import org.springframework.data.domain.Page;


import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PurchaseOrderService {
    List<PurchaseOrder> getAllPurchaseOrdersSupplierUser();
    List<PurchaseOrder> searchBySupplierName(String supplierName);
    void batchDelete(List<Integer> ids);
    // 新增创建订单方法
    PurchaseOrder createPurchaseOrder(PurchaseOrder order);
    Optional<PurchaseOrder> getOrderById(Integer orderId);
    PurchaseOrder updateStatus(Integer orderId, String newStatus);
    List<PurchaseOrder> getValidPurchaseOrdersSupplierUser();
    // 新增：按用户ID查询其关联供应商的订单（仅供应商角色用户用）
    List<PurchaseOrder> getOrdersByUserId(Long userId);

    // 新增：搜索当前用户关联供应商的订单（兼容搜索功能）
    List<PurchaseOrder> searchOrdersBySupplierNameAndUserId(String supplierName, Long userId);
    // 新增：按订单编号搜索
    List<PurchaseOrder> searchByOrderNo(String orderNo);

    // 新增：按订单编号和用户ID搜索
    List<PurchaseOrder> searchOrdersByOrderNoAndUserId(String orderNo, Long userId);
    // 新增：检查库存是否充足
    boolean checkStock(Integer varietyId, BigDecimal quantity);

    // 新增：减少商品库存
    void reduceStock(Integer varietyId, BigDecimal quantity);

    // 新增支付相关方法
    boolean validateOrderForPayment(String orderId);
    void updateOrderStatus(String orderId, String status);

    Page<PurchaseOrder> getValidPurchaseOrdersSupplierUser(int page, int size);
    Page<PurchaseOrder> getOrdersByUserId(Long userId, int page, int size);
    Page<PurchaseOrder> searchByOrderNo(String orderNo, int page, int size);
    Page<PurchaseOrder> searchOrdersByOrderNoAndUserId(String orderNo, Long userId, int page, int size);
    Page<PurchaseOrder> getOrdersByPurchaserId(Long purchaserId, int page, int size);
    // 新增：按订单编号和采购人ID搜索（分页）
    Page<PurchaseOrder> searchOrdersByOrderNoAndPurchaserId(String orderNo, Long purchaserId, int page, int size);

    // 新增：按订单编号和采购人ID搜索（非分页）
    List<PurchaseOrder> searchOrdersByOrderNoAndPurchaserId(String orderNo, Long purchaserId);

    BigDecimal getTotalSalesQuantityByVarietyId(Integer varietyId);
}
