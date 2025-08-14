package com.example.lizhi.service;

import com.example.lizhi.entity.PurchaseOrder;
import com.example.lizhi.entity.StockIn;

public interface StockInService {
    StockIn createStockInFromOrder(PurchaseOrder order);
}