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
}
