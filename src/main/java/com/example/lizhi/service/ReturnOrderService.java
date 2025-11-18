package com.example.lizhi.service;

import com.example.lizhi.entity.ReturnOrder;
import com.example.lizhi.entity.User;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ReturnOrderService {
    ReturnOrder createReturnOrder(ReturnOrder returnOrder);
    List<ReturnOrder> getAllReturnOrders();
    ReturnOrder updateReturnStatus(Integer returnId, String newStatus);
    ReturnOrder createReturnFromStockRejection(String orderNo, String reason);
    List<ReturnOrder> searchByOrderNo(String orderNo);
    Page<ReturnOrder> getAllReturnOrders(int page, int size, User currentUser);
    Page<ReturnOrder> searchByOrderNo(String orderNo, int page, int size, User currentUser);
    void batchDelete(List<Integer> ids);
}