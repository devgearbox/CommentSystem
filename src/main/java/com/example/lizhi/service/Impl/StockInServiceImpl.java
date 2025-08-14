package com.example.lizhi.service.Impl;

import com.example.lizhi.entity.PurchaseOrder;
import com.example.lizhi.entity.StockIn;
import com.example.lizhi.repository.StockInRepository;
import com.example.lizhi.service.StockInService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockInServiceImpl implements StockInService {
    private final StockInRepository stockInRepository;

    @Override
    @Transactional
    public StockIn createStockInFromOrder(PurchaseOrder order) {
        if (stockInRepository.existsByOrderNo(order.getOrder_no())) {
            throw new RuntimeException("订单 " + order.getOrder_no() + " 已生成入库单，无需重复创建");
        }

        StockIn stockIn = new StockIn();
        stockIn.setOrderNo(order.getOrder_no());
        stockIn.setLitchi_variety(order.getPurchase_variety());
        stockIn.setQuantity(order.getPurchase_quantity());

        return stockInRepository.save(stockIn);
    }
}