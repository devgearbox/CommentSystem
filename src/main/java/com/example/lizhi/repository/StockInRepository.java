package com.example.lizhi.repository;

import com.example.lizhi.entity.StockIn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StockInRepository extends JpaRepository<StockIn, Integer> {
    boolean existsByOrderNo(String order_no);
    @Modifying
    @Query("delete from StockIn s where s.orderNo in :orderNos")
    void deleteByOrderNoIn(@Param("orderNos") List<String> orderNos);

    List<StockIn> findByOrderNoContaining(String orderNo);
    Page<StockIn> findByOrderNoContaining(String orderNo, Pageable pageable);

    // 根据经办人ID查询入库单
    Page<StockIn> findByOperatorIdOrderByCreateTimeDesc(Integer operatorId, Pageable pageable);

    // 根据经办人ID和订单号搜索
    Page<StockIn> findByOperatorIdAndOrderNoContaining(Integer operatorId, String orderNo, Pageable pageable);
}