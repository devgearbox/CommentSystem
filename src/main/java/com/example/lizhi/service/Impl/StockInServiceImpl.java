package com.example.lizhi.service.Impl;

import com.example.lizhi.entity.PurchaseOrder;
import com.example.lizhi.entity.StockIn;
import com.example.lizhi.entity.User;
import com.example.lizhi.repository.PurchaseOrderRepository;
import com.example.lizhi.repository.StockInRepository;
import com.example.lizhi.service.StockInService;
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

    @Override
    @Transactional
    public StockIn createStockInFromOrder(PurchaseOrder order) {
        if (stockInRepository.existsByOrderNo(order.getOrder_no())) {
            throw new RuntimeException("订单 " + order.getOrder_no() + " 已生成入库单，无需重复创建");
        }

        // 关联查询：通过订单编号，获取订单 + 采购人（User）
        Optional<PurchaseOrder> orderWithUserOpt = purchaseOrderRepository.findByOrderNoWithUser(order.getOrder_no());
        PurchaseOrder orderWithUser = orderWithUserOpt.orElseThrow(() -> new RuntimeException("订单不存在：" + order.getOrder_no()));

        User purchaser = orderWithUser.getUser();
        if (purchaser == null) {
            // 这里可根据实际情况，比如从 SecurityContext 获取当前登录用户等更合理的方式，
            // 示例中简单处理，若 purchaseOrder 里 user 为 null 就抛异常，实际可扩展
            throw new RuntimeException("采购订单未关联采购人，无法生成入库单经办人信息");
        }

        StockIn stockIn = new StockIn();
        stockIn.setOrderNo(order.getOrder_no());
        stockIn.setLitchi_variety(order.getPurchase_variety());
        stockIn.setQuantity(order.getPurchase_quantity());
        // 填充经办人姓名
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
    public StockIn updateStatus(Integer stockId, String newStatus) {
        // 1. 查询入库单
        StockIn stockIn = stockInRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("入库单不存在：" + stockId));

        // 2. 转换为枚举（校验合法性）
        StockIn.StockInStatus newStatusEnum;
        try {
            newStatusEnum = StockIn.StockInStatus.valueOf(newStatus);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("非法状态：" + newStatus, e);
        }

        // 3. 状态流转校验（仿照订单逻辑，限制非法流转）
        StockIn.StockInStatus oldStatus = stockIn.getStock_in_status();
        int oldIndex = Arrays.asList(StockIn.StockInStatus.values()).indexOf(oldStatus);
        int newIndex = Arrays.asList(StockIn.StockInStatus.values()).indexOf(newStatusEnum);

        // 示例规则：只能向后流转（pending→completed→cancelled 不允许回退）
        if (newIndex < oldIndex) {
            throw new RuntimeException("非法状态流转：" + oldStatus.getLabel() + " -> " + newStatusEnum.getLabel());
        }

        // 4. 更新状态
        stockIn.setStock_in_status(newStatusEnum);
        return stockInRepository.save(stockIn);
    }
}