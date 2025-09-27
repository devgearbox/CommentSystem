package com.example.lizhi.repository;

import com.example.lizhi.entity.ReturnOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReturnOrderRepository extends JpaRepository<ReturnOrder, Integer> {

    List<ReturnOrder> findAllByOrderByCreateTimeDesc();

    @Query("SELECT r FROM ReturnOrder r WHERE r.orderNo = :orderNo")
    List<ReturnOrder> findByOrderNo(@Param("orderNo") String orderNo);

    boolean existsByOrderNo(String orderNo);

    @Query("SELECT r FROM ReturnOrder r WHERE r.return_status = :status")
    List<ReturnOrder> findByStatus(@Param("status") ReturnOrder.ReturnStatus status);

    // 添加分页查询方法
    Page<ReturnOrder> findAllByOrderByCreateTimeDesc(Pageable pageable);
    Page<ReturnOrder> findByOrderNoContaining(String orderNo, Pageable pageable);
}