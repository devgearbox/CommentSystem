package com.example.lizhi.service.Impl;

import com.example.lizhi.entity.PurchaseOrder;
import com.example.lizhi.entity.StockIn;
import com.example.lizhi.entity.Supplier;
import com.example.lizhi.entity.User;
import com.example.lizhi.repository.PurchaseOrderRepository;
import com.example.lizhi.repository.StockInRepository;
import com.example.lizhi.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final MessageService messageService;

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
        stockIn.setOperatorId(Math.toIntExact(purchaser.getId()));

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
        StockIn.FreshnessStatus oldFreshnessStatus = stockIn.getFreshness_status();
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

        // 如果状态变为"已入库"，记录入库完成时间
        if (newStatusEnum == StockIn.StockInStatus.completed && stockIn.getStock_in_time() == null) {
            stockIn.setStock_in_time(LocalDateTime.now());
        }

        stockIn.setStock_in_status(newStatusEnum);
        stockIn.setUpdate_time(LocalDateTime.now());
        StockIn updatedStock = stockInRepository.save(stockIn);

        // 检查保鲜状态变化，如果变为紧急状态则发送消息
        StockIn.FreshnessStatus newFreshnessStatus = updatedStock.getFreshness_status();
        if (newFreshnessStatus == StockIn.FreshnessStatus.URGENT &&
                oldFreshnessStatus != StockIn.FreshnessStatus.URGENT) {
            try {
                sendFreshnessUrgentMessage(updatedStock);
            } catch (Exception e) {
                // 消息发送失败不应影响主要业务流程
                System.err.println("发送保鲜紧急提醒失败: " + e.getMessage());
            }
        }

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

    // 发送保鲜紧急提醒消息
    private void sendFreshnessUrgentMessage(StockIn stockIn) {
        try {
            String title = "库存保鲜紧急提醒";
            String content = String.format(
                    "入库单 %s 的荔枝品种 %s 数量 %s 斤已进入紧急保鲜状态，请及时处理！入库时间：%s",
                    stockIn.getOrderNo(),
                    stockIn.getLitchi_variety(),
                    stockIn.getQuantity(),
                    stockIn.getCreateTime() != null ?
                            stockIn.getCreateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "未知"
            );

            // 获取操作员ID（采购员ID）
            Long recipientId = stockIn.getOperatorId() != null ?
                    stockIn.getOperatorId().longValue() : null;

            if (recipientId != null) {
                messageService.sendFreshnessUrgentMessage(stockIn.getOrderNo(), recipientId, content);
                System.out.println("保鲜紧急提醒发送成功，入库单：" + stockIn.getOrderNo());
            } else {
                System.err.println("无法发送保鲜紧急提醒：操作员ID为空，入库单：" + stockIn.getOrderNo());
            }
        } catch (Exception e) {
            System.err.println("发送保鲜紧急提醒异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public Page<StockIn> findAllStockIn(int page, int size, User currentUser) {
        Pageable pageable = PageRequest.of(page - 1, size);

        if (currentUser.getRole() == 2) { // 采购员只能看到自己的入库单
            return stockInRepository.findByOperatorIdOrderByCreateTimeDesc(
                    Math.toIntExact(currentUser.getId()), pageable);
        } else { // 管理员和供应商查看所有（根据业务需求调整）
            Page<StockIn> stockPage = stockInRepository.findAll(pageable);
            return stockPage.map(stock -> stock);
        }
    }

    @Override
    public List<StockIn> searchByOrderNo(String orderNo) {
        return stockInRepository.findByOrderNoContaining(orderNo);
    }

    @Override
    public Page<StockIn> searchByOrderNo(String orderNo, int page, int size, User currentUser) {
        Pageable pageable = PageRequest.of(page - 1, size);

        if (currentUser.getRole() == 2) { // 采购员只能搜索自己的入库单
            return stockInRepository.findByOperatorIdAndOrderNoContaining(
                    Math.toIntExact(currentUser.getId()), orderNo, pageable);
        } else { // 管理员和供应商搜索所有
            Page<StockIn> stockPage = stockInRepository.findByOrderNoContaining(orderNo, pageable);
            return stockPage.map(stock -> stock);
        }
    }

    @Override
    @Transactional
    public void batchDelete(List<Integer> ids) {
        List<StockIn> stockRecords = stockInRepository.findAllById(ids);

        // 检查状态，只有特定状态可以删除
        for (StockIn stock : stockRecords) {
            if (stock.getStock_in_status() == StockIn.StockInStatus.completed) {
                throw new IllegalArgumentException("入库单 [" + stock.getOrderNo() + "] 状态为【已入库】，无法删除");
            }
            if (stock.getStock_in_status() == StockIn.StockInStatus.rejected) {
                throw new IllegalArgumentException("入库单 [" + stock.getOrderNo() + "] 状态为【拒收】，无法删除");
            }
        }

        stockInRepository.deleteAll(stockRecords);
    }

    @Override
    public List<StockIn> getAllStockInForExport() {
        return stockInRepository.findAll();
    }

    @Override
    public List<StockIn> getStockInForExportByOrderNo(String orderNo) {
        return stockInRepository.findByOrderNoContaining(orderNo);
    }
}