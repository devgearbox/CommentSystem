package com.example.lizhi.service;

import com.example.lizhi.entity.StockIn;
import org.springframework.data.domain.Page;

import java.util.List;

public interface StockInService {

    StockIn createStockInFromOrder(com.example.lizhi.entity.PurchaseOrder order);

    List<StockIn> findAllStockIn();
    StockIn updateStatus(Integer stockId, String newStatus, String rejectionReason);
    Page<StockIn> findAllStockIn(int page, int size);

    List<StockIn> searchByOrderNo(String orderNo);
    Page<StockIn> searchByOrderNo(String orderNo, int page, int size);
    void batchDelete(List<Integer> ids);
}