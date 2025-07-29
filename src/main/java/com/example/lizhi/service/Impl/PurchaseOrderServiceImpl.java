package com.example.lizhi.service.Impl;

import com.example.lizhi.entity.PurchaseOrder;
import com.example.lizhi.entity.Supplier;
import com.example.lizhi.repository.PurchaseOrderRepository;
import com.example.lizhi.service.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PurchaseOrderServiceImpl implements PurchaseOrderService {
    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

//    @Override
//    public List<PurchaseOrder> getAllPurchaseOrders() {return purchaseOrderRepository.findAll();}

    @Override
    public List<PurchaseOrder> getAllPurchaseOrdersSupplierUser() {
        // 调用关联查询方法
        return purchaseOrderRepository.findAllWithSupplierAndUser();
    }

    // 搜索方法
    public List<PurchaseOrder> searchBySupplierName(String supplierName) {
        return purchaseOrderRepository.findBySupplierSupplierNameContaining(supplierName);
    }

    @Override
    public void batchDelete(List<Integer> ids) {
        // JPA 批量删除（与 Supplier 逻辑一致）
        purchaseOrderRepository.deleteAllById(ids);
    }
}