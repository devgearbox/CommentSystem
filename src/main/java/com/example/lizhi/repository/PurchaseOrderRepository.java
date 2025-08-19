package com.example.lizhi.repository;

import com.example.lizhi.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Integer> {

    // 自动关联查询，fetch 确保 Supplier 对象被加载（避免懒加载问题）
    @Query("select po from PurchaseOrder po left join fetch po.supplier where po.order_id = ?1")
    PurchaseOrder findByIdWithSupplier(Integer id);

    // 查全部订单（带供应商关联）
    @Query("select po from PurchaseOrder po left join fetch po.supplier")
    List<PurchaseOrder> findAllWithSupplier();

    // 同时关联查询供应商（已有）和用户（新增）
    @Query("select po from PurchaseOrder po " +
            "left join fetch po.supplier " + // 关联供应商
            "left join fetch po.user") // 关联采购人（假设 PurchaseOrder 里有 user 关联，需确保实体类正确）
    List<PurchaseOrder> findAllWithSupplierAndUser();

    @Query("select po from PurchaseOrder po left join fetch po.supplier where po.supplier.supplierName like %:supplierName%")
    List<PurchaseOrder> findBySupplierSupplierNameContaining(String supplierName);

    // 新增：通过订单编号，查询订单 + 关联的采购人（User）
    @Query("SELECT po FROM PurchaseOrder po LEFT JOIN FETCH po.user WHERE po.orderNo = ?1")
    Optional<PurchaseOrder> findByOrderNoWithUser(String orderNo);
    Optional<PurchaseOrder> findByOrderNo(String orderNo);
    // 1. 查询未删除的订单（默认业务查询用）
    List<PurchaseOrder> findByIsDeletedFalse();

    // 2. 按ID查询未删除的订单（用于校验订单是否存在）
    @Query("SELECT po FROM PurchaseOrder po WHERE po.order_id = :order_id AND po.isDeleted = false")
    Optional<PurchaseOrder> findByIdAndIsDeletedFalse(@Param("order_id") Integer order_id);

    // 3. 按状态查询未删除的订单（如需）
    @Query("SELECT po FROM PurchaseOrder po WHERE po.orderStatus = :status AND po.isDeleted = false")
    List<PurchaseOrder> findByOrderStatusAndIsDeletedFalse(@Param("status") PurchaseOrder.OrderStatus status);
    // 新增方法：关联查询 + 软删除过滤
    @Query("""
        select po from PurchaseOrder po 
        left join fetch po.supplier 
        left join fetch po.user 
        where po.isDeleted = false
    """)
    List<PurchaseOrder> findAllValidWithSupplierUser();
}