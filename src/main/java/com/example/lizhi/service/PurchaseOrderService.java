package com.example.lizhi.service;

import com.example.lizhi.entity.PurchaseOrder;
import java.util.List;

public interface PurchaseOrderService {
//    List<PurchaseOrder> getAllPurchaseOrders();
    List<PurchaseOrder> getAllPurchaseOrdersSupplierUser();
    List<PurchaseOrder> searchBySupplierName(String supplierName);
    void batchDelete(List<Integer> ids);
}
