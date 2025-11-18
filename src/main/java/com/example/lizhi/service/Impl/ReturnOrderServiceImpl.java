package com.example.lizhi.service.Impl;

import com.example.lizhi.entity.PurchaseOrder;
import com.example.lizhi.entity.ReturnOrder;
import com.example.lizhi.entity.User;
import com.example.lizhi.repository.PurchaseOrderRepository;
import com.example.lizhi.repository.ReturnOrderRepository;
import com.example.lizhi.service.ReturnOrderService;
import com.example.lizhi.service.SupplierService;
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
    private final SupplierService supplierService;

    public ReturnOrderServiceImpl(ReturnOrderRepository returnOrderRepository,
                                  PurchaseOrderRepository purchaseOrderRepository,
                                  SupplierService supplierService) {
        this.returnOrderRepository = returnOrderRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierService = supplierService;
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

        // 使用正确的类型转换
        Long userId = order.getUser().getId();
        returnOrder.setOperatorId(userId.intValue());

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
    public Page<ReturnOrder> getAllReturnOrders(int page, int size, User currentUser) {
        Pageable pageable = PageRequest.of(page - 1, size);

        // 根据角色进行数据过滤
        if (currentUser.getRole().equals(3)) { // 供应商
            // 供应商只能看到自己相关的退货单
            String supplierName = getSupplierNameByUserId(currentUser.getId());
            if (supplierName == null || supplierName.isEmpty()) {
                // 如果没有找到供应商名称，返回空页面
                return Page.empty(pageable);
            }
            return returnOrderRepository.findBySupplierNameOrderByCreateTimeDesc(supplierName, pageable);
        } else if (currentUser.getRole().equals(2)) { // 采购员
            // 采购员只能看到自己创建的退货单
            return returnOrderRepository.findByOperatorIdOrderByCreateTimeDesc(
                    currentUser.getId().intValue(), pageable);
        } else { // 管理员查看所有
            return returnOrderRepository.findAllByOrderByCreateTimeDesc(pageable);
        }
    }

    @Override
    public Page<ReturnOrder> searchByOrderNo(String orderNo, int page, int size, User currentUser) {
        Pageable pageable = PageRequest.of(page - 1, size);

        if (currentUser.getRole().equals(3)) { // 供应商
            String supplierName = getSupplierNameByUserId(currentUser.getId());
            if (supplierName == null || supplierName.isEmpty()) {
                return Page.empty(pageable);
            }
            return returnOrderRepository.findBySupplierNameAndOrderNoContaining(supplierName, orderNo, pageable);
        } else if (currentUser.getRole().equals(2)) { // 采购员
            return returnOrderRepository.findByOperatorIdAndOrderNoContaining(
                    currentUser.getId().intValue(), orderNo, pageable);
        } else { // 管理员
            return returnOrderRepository.findByOrderNoContaining(orderNo, pageable);
        }
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

    /**
     * 根据用户ID获取供应商名称
     * 基于"一个供应商用户只能对应一个供应商"的设计原则
     */
    private String getSupplierNameByUserId(Long userId) {
        try {
            // 直接获取供应商名称，如果用户没有关联供应商会抛出异常
            return supplierService.getSupplierNameByUserId(userId);
        } catch (Exception e) {
            // 记录警告但不抛出异常，返回null让调用方处理
            System.err.println("警告：用户ID " + userId + " 没有关联的供应商");
            return null;
        }
    }
}