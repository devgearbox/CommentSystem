package com.example.lizhi.service.Impl;

import com.example.lizhi.entity.PurchaseOrder;
import com.example.lizhi.entity.StockIn;
import com.example.lizhi.entity.Supplier;
import com.example.lizhi.entity.User;
import com.example.lizhi.repository.PurchaseOrderRepository;
import com.example.lizhi.repository.StockInRepository;
import com.example.lizhi.service.LitchiVarietyService;
import com.example.lizhi.service.ReturnOrderService;
import com.example.lizhi.service.StockInService;
import com.example.lizhi.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StockInServiceImpl implements StockInService {
    private final StockInRepository stockInRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierService supplierService;
    private final LitchiVarietyService litchiVarietyService;
    private final ReturnOrderService returnOrderService;

    @Override
    @Transactional
    public StockIn createStockInFromOrder(PurchaseOrder order) {
        if (stockInRepository.existsByOrderNo(order.getOrderNo())) {
            throw new RuntimeException("订单 " + order.getOrderNo() + " 已生成入库单，无需重复创建");
        }

        Optional<PurchaseOrder> orderWithUserOpt = purchaseOrderRepository.findByOrderNoWithUser(order.getOrderNo());
        PurchaseOrder orderWithUser = orderWithUserOpt.orElseThrow(() -> new RuntimeException("订单不存在：" + order.getOrderNo()));

        User purchaser = orderWithUser.getUser();
        if (purchaser == null) {
            throw new RuntimeException("采购订单未关联采购人，无法生成入库单经办人信息");
        }

        StockIn stockIn = new StockIn();
        stockIn.setOrderNo(order.getOrderNo());
        stockIn.setLitchi_variety(order.getPurchase_variety());
        stockIn.setQuantity(order.getPurchase_quantity());
        stockIn.setOperator_name(purchaser.getReal_name());
        stockIn.setOperator_id(Math.toIntExact(purchaser.getId()));

        return stockInRepository.save(stockIn);
    }

    @Override
    public List<StockIn> findAllStockIn() {
        return stockInRepository.findAll();
    }

    @Override
    @Transactional
    public StockIn updateStatus(Integer stockId, String newStatus, String rejectionReason) {
        StockIn stockIn = stockInRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("入库单不存在：" + stockId));

        StockIn.StockInStatus newStatusEnum;
        try {
            newStatusEnum = StockIn.StockInStatus.valueOf(newStatus);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("非法状态：" + newStatus, e);
        }

        StockIn.StockInStatus oldStatus = stockIn.getStock_in_status();
        int oldIndex = Arrays.asList(StockIn.StockInStatus.values()).indexOf(oldStatus);
        int newIndex = Arrays.asList(StockIn.StockInStatus.values()).indexOf(newStatusEnum);

        if (newIndex < oldIndex) {
            throw new RuntimeException("非法状态流转：" + oldStatus.getLabel() + " -> " + newStatusEnum.getLabel());
        }

        stockIn.setStock_in_status(newStatusEnum);
        StockIn updatedStock = stockInRepository.save(stockIn);

        String orderNo = stockIn.getOrderNo();
        Optional<PurchaseOrder> orderOptional = purchaseOrderRepository.findByOrderNo(orderNo);
        if (orderOptional.isPresent()) {
            PurchaseOrder order = orderOptional.get();
            if (newStatusEnum == StockIn.StockInStatus.completed) {
                order.setOrderStatus(PurchaseOrder.OrderStatus.received);

                // 增加供应商订单数量
                Supplier supplier = order.getSupplier();
                if (supplier != null) {
                    try {
                        supplierService.incrementOrderCount(supplier.getSupplier_id());
                        System.out.println("供应商订单数量增加成功: " + supplier.getSupplier_id());
                    } catch (Exception e) {
                        System.err.println("供应商订单数量增加失败: " + e.getMessage());
                    }
                }

                // 增加商品订单数量
                if (order.getLitchiVariety() != null) {
                    try {
                        litchiVarietyService.incrementOrderCount(order.getLitchiVariety().getId());
                        System.out.println("商品订单数量增加成功: " + order.getLitchiVariety().getId());
                    } catch (Exception e) {
                        System.err.println("商品订单数量增加失败: " + e.getMessage());
                    }
                } else {
                    System.err.println("订单未关联商品: " + orderNo);
                }

            } else if (newStatusEnum == StockIn.StockInStatus.rejected) {
                order.setOrderStatus(PurchaseOrder.OrderStatus.rejected);
                // 自动生成退货单
                try {
                    returnOrderService.createReturnFromStockRejection(orderNo,
                            rejectionReason != null ? rejectionReason : "入库验收拒收");
                    System.out.println("自动生成退货单成功，订单号：" + orderNo + "，原因：" + rejectionReason);
                } catch (Exception e) {
                    System.err.println("生成退货单失败：" + e.getMessage());
                }
            }
            purchaseOrderRepository.save(order);
        } else {
            System.out.println("未找到关联订单：" + orderNo);
        }

        return updatedStock;
    }
}