package com.example.lizhi.repository;

import com.example.lizhi.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface AnalyseRepository extends JpaRepository<PurchaseOrder, Integer> {
    // 已实现的方法
    @Query("SELECT COUNT(p) FROM PurchaseOrder p")
    Long countAllOrders();

    @Query("SELECT SUM(p.totalPrice) FROM PurchaseOrder p")
    BigDecimal sumTotalPurchaseAmount();

    // 统计已接收状态的订单数量（orderStatus = received）
    @Query("SELECT COUNT(p) FROM PurchaseOrder p WHERE p.orderStatus = 'received'")
    Long countReceivedOrders();

    // 时间范围筛选：统计采购金额（用于趋势图）
    @Query("SELECT FUNCTION('DATE_FORMAT', p.createTime, :format), SUM(p.totalPrice) " +
            "FROM PurchaseOrder p " +
            "WHERE p.isDeleted = false " +
            "AND p.createTime BETWEEN :start AND :end " +
            "GROUP BY FUNCTION('DATE_FORMAT', p.createTime, :format) " +
            "ORDER BY FUNCTION('DATE_FORMAT', p.createTime, :format) ASC") // 按时间升序
    List<Object[]> sumTotalPriceByTimeRange(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("format") String format
    );
    @Query("SELECT COUNT(p) FROM PurchaseOrder p WHERE p.isDeleted = false AND p.createTime BETWEEN :start AND :end")
    Long countOrdersByTimeRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT SUM(p.totalPrice) FROM PurchaseOrder p WHERE p.isDeleted = false AND p.createTime BETWEEN :start AND :end")
    BigDecimal sumTotalPriceByTimeRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(p) FROM PurchaseOrder p WHERE p.isDeleted = false AND p.orderStatus = 'received' AND p.createTime BETWEEN :start AND :end")
    Long countReceivedOrdersByTimeRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 按品种统计采购数量
    @Query("SELECT p.purchase_variety, SUM(p.purchase_quantity) " +
            "FROM PurchaseOrder p " +
            "WHERE p.isDeleted = false " +
            "GROUP BY p.purchase_variety")
    List<Object[]> countPurchaseQuantityByVariety();

    // 按供应商统计采购金额
    @Query("SELECT s.supplierName, SUM(p.totalPrice) " +
            "FROM PurchaseOrder p " +
            "JOIN p.supplier s " +
            "WHERE p.isDeleted = false " +
            "GROUP BY s.supplierName")
    List<Object[]> countPurchaseAmountBySupplier();
}
