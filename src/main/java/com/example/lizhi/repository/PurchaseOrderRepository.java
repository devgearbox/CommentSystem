package com.example.lizhi.repository;

import com.example.lizhi.entity.PurchaseOrder;
import com.example.lizhi.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder,Integer> {
    // 自动关联查询，fetch 确保Supplier对象被加载（避免懒加载问题）
    @Query("select po from PurchaseOrder po left join fetch po.supplier where po.order_id = ?1")
    PurchaseOrder findByIdWithSupplier(Integer id);

    // 查全部订单（带供应商关联）
    @Query("select po from PurchaseOrder po left join fetch po.supplier")
    List<PurchaseOrder> findAllWithSupplier();
    // 同时关联查询供应商（已有）和用户（新增）
    @Query("select po from PurchaseOrder po " +
            "left join fetch po.supplier " + // 关联供应商
            "left join fetch po.user") // 关联采购人（新增）
    List<PurchaseOrder> findAllWithSupplierAndUser();
    @Query("select po from PurchaseOrder po left join fetch po.supplier where po.supplier.supplierName like %:supplierName%")
    List<PurchaseOrder> findBySupplierSupplierNameContaining(@Param("supplierName") String supplierName);
}
