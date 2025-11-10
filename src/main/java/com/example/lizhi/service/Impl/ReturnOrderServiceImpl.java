package com.example.lizhi.service.Impl;

import com.example.lizhi.entity.PurchaseOrder;
import com.example.lizhi.entity.ReturnOrder;
import com.example.lizhi.repository.PurchaseOrderRepository;
import com.example.lizhi.repository.ReturnOrderRepository;
import com.example.lizhi.service.ReturnOrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReturnOrderServiceImpl implements ReturnOrderService {

    private final ReturnOrderRepository returnOrderRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    public ReturnOrderServiceImpl(ReturnOrderRepository returnOrderRepository,
                                  PurchaseOrderRepository purchaseOrderRepository) {
        this.returnOrderRepository = returnOrderRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    @Override
    public ReturnOrder createReturnOrder(ReturnOrder returnOrder) {
        return returnOrderRepository.save(returnOrder);
    }

    @Override
    public List<ReturnOrder> getAllReturnOrders() {
        return returnOrderRepository.findAllByOrderByCreateTimeDesc();
    }

    @Override
    public ReturnOrder updateReturnStatus(Integer returnId, String newStatus) {
        ReturnOrder returnOrder = returnOrderRepository.findById(returnId)
                .orElseThrow(() -> new RuntimeException("退货单不存在：" + returnId));

        ReturnOrder.ReturnStatus newStatusEnum;
        try {
            newStatusEnum = ReturnOrder.ReturnStatus.valueOf(newStatus);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("非法状态：" + newStatus);
        }

        returnOrder.setReturn_status(newStatusEnum);
        return returnOrderRepository.save(returnOrder);
    }

    @Override
    public ReturnOrder createReturnFromStockRejection(String orderNo, String reason) {
        // 检查是否已存在退货单
        if (returnOrderRepository.existsByOrderNo(orderNo)) {
            throw new RuntimeException("该订单已生成退货单，无需重复创建");
        }
        // 获取原订单信息
        Optional<PurchaseOrder> orderOpt = purchaseOrderRepository.findByOrderNo(orderNo);
        if (!orderOpt.isPresent()) {
            throw new RuntimeException("原订单不存在：" + orderNo);
        }
        PurchaseOrder order = orderOpt.get();
        // 创建退货单
        ReturnOrder returnOrder = new ReturnOrder();
        returnOrder.setOrderNo(orderNo);
        returnOrder.setLitchi_variety(order.getPurchase_variety());
        returnOrder.setQuantity(order.getPurchase_quantity());
        returnOrder.setReason(reason != null ? reason : "入库验收拒收");
        returnOrder.setOperatorName(order.getUser().getReal_name());
        returnOrder.setOperatorId(Math.toIntExact(order.getUser().getId()));
        returnOrder.setRefundAmount(order.getTotalPrice());
        returnOrder.setSupplierName(order.getSupplier().getSupplier_name());
        returnOrder.setPurchaserName(order.getUser().getReal_name());

        return returnOrderRepository.save(returnOrder);
    }

    @Override
    public List<ReturnOrder> searchByOrderNo(String orderNo) {
        return returnOrderRepository.findByOrderNo(orderNo);
    }

    @Override
    public Page<ReturnOrder> getAllReturnOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        // 需要创建对应的分页查询方法（需在ReturnOrderRepository中添加）
        return returnOrderRepository.findAllByOrderByCreateTimeDesc(pageable);
    }

    @Override
    public Page<ReturnOrder> searchByOrderNo(String orderNo, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        // 需要创建对应的分页查询方法（需在ReturnOrderRepository中添加）
        return returnOrderRepository.findByOrderNoContaining(orderNo, pageable);
    }

    @Override
    @Transactional
    public void batchDelete(List<Integer> ids) {
        List<ReturnOrder> returnOrders = returnOrderRepository.findAllById(ids);

        // 检查状态，只有特定状态可以删除
        for (ReturnOrder returnOrder : returnOrders) {
            if (returnOrder.getReturn_status() == ReturnOrder.ReturnStatus.completed) {
                throw new IllegalArgumentException("退货单 [" + returnOrder.getReturnNo() + "] 状态为【已完成】，无法删除");
            }
            if (returnOrder.getReturn_status() == ReturnOrder.ReturnStatus.refunded) {
                throw new IllegalArgumentException("退货单 [" + returnOrder.getReturnNo() + "] 状态为【已退款】，无法删除");
            }
        }

        returnOrderRepository.deleteAll(returnOrders);
    }
}