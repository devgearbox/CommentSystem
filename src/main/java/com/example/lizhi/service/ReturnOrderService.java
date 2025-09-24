package com.example.lizhi.service;

import com.example.lizhi.entity.ReturnOrder;

import java.util.List;

public interface ReturnOrderService {
    ReturnOrder createReturnOrder(ReturnOrder returnOrder);
    List<ReturnOrder> getAllReturnOrders();
    ReturnOrder updateReturnStatus(Integer returnId, String newStatus);
    ReturnOrder createReturnFromStockRejection(String orderNo, String reason);
    List<ReturnOrder> searchByOrderNo(String orderNo);
}