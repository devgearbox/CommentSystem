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
}