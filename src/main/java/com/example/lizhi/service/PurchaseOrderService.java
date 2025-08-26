package com.example.lizhi.service;

import com.example.lizhi.entity.PurchaseOrder;
import java.util.List;
import java.util.Optional;

public interface PurchaseOrderService {
//    List<PurchaseOrder> getAllPurchaseOrders();
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
}
