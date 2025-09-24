package com.example.lizhi.service;

import com.example.lizhi.entity.StockIn;

import java.util.List;

public interface StockInService {

    StockIn createStockInFromOrder(com.example.lizhi.entity.PurchaseOrder order);

    List<StockIn> findAllStockIn();
    StockIn updateStatus(Integer stockId, String newStatus, String rejectionReason);
}