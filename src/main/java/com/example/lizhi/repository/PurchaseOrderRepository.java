package com.example.lizhi.repository;

import com.example.lizhi.entity.PurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
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
    // 新增1：按供应商ID查询未删除的订单（关联供应商+用户信息，避免懒加载）
    @Query("""
        select po from PurchaseOrder po 
        left join fetch po.supplier 
        left join fetch po.user 
        where po.isDeleted = false 
        and po.supplier.supplier_id = :supplierId
    """)
    List<PurchaseOrder> findValidOrdersBySupplierId(@Param("supplierId") Integer supplierId);

    // 新增2：按用户ID查询关联的供应商（用于“用户→供应商”的中间查询）
    // （注：此方法也可放在SupplierRepository，此处为方便订单查询统一管理）
    @Query("select s.supplier_id from Supplier s where s.userId = :userId")
    List<Integer> findSupplierIdsByUserId(@Param("userId") Long userId);

    // 新增：按订单编号模糊查询
    @Query("select po from PurchaseOrder po " +
            "left join fetch po.supplier " +
            "left join fetch po.user " +
            "where po.orderNo = :orderNo " +  // 精确匹配
            "and po.isDeleted = false")
    List<PurchaseOrder> findByOrderNoExact(@Param("orderNo") String orderNo);  // 方法名也可改为更清晰的名称

    // 添加分页查询方法
    @Query("select po from PurchaseOrder po left join fetch po.supplier left join fetch po.user where po.isDeleted = false")
    Page<PurchaseOrder> findAllValidWithSupplierUser(Pageable pageable);

    @Query("select po from PurchaseOrder po left join fetch po.supplier left join fetch po.user where po.isDeleted = false and po.supplier.supplier_id in :supplierIds")
    Page<PurchaseOrder> findValidOrdersBySupplierIds(@Param("supplierIds") List<Integer> supplierIds, Pageable pageable);

    @Query("select po from PurchaseOrder po left join fetch po.supplier left join fetch po.user where po.orderNo like %:orderNo% and po.isDeleted = false")
    Page<PurchaseOrder> findByOrderNoContaining(@Param("orderNo") String orderNo, Pageable pageable);

    @Query("select po from PurchaseOrder po left join fetch po.supplier left join fetch po.user where po.orderNo like %:orderNo% and po.isDeleted = false and po.supplier.supplier_id in :supplierIds")
    Page<PurchaseOrder> findByOrderNoAndSupplierIds(@Param("orderNo") String orderNo, @Param("supplierIds") List<Integer> supplierIds, Pageable pageable);

    @Query("select po from PurchaseOrder po " +
            "left join fetch po.supplier " +
            "left join fetch po.user " +
            "where po.isDeleted = false " +
            "and po.user.id = :userId")
    Page<PurchaseOrder> findByUserIdAndIsDeletedFalse(@Param("userId") Long userId, Pageable pageable);

    // 新增：按订单编号和用户ID搜索（分页）
    @Query("select po from PurchaseOrder po " +
            "left join fetch po.supplier " +
            "left join fetch po.user " +
            "where po.orderNo like %:orderNo% " +
            "and po.isDeleted = false " +
            "and po.user.id = :userId")
    Page<PurchaseOrder> findByOrderNoAndUserId(@Param("orderNo") String orderNo,
                                               @Param("userId") Long userId,
                                               Pageable pageable);

    // 新增：按订单编号和用户ID搜索（非分页）
    @Query("select po from PurchaseOrder po " +
            "left join fetch po.supplier " +
            "left join fetch po.user " +
            "where po.orderNo like %:orderNo% " +
            "and po.isDeleted = false " +
            "and po.user.id = :userId")
    List<PurchaseOrder> findByOrderNoAndUserId(@Param("orderNo") String orderNo,
                                               @Param("userId") Long userId);

//    销售数量总和
    @Query("SELECT COALESCE(SUM(po.purchase_quantity), 0) FROM PurchaseOrder po " +
            "WHERE po.litchiVariety.id = :varietyId " +
            "AND po.isDeleted = false")
    BigDecimal sumPurchaseQuantityByVarietyId(@Param("varietyId") Integer varietyId);
}