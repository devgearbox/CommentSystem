package com.example.lizhi.repository;

import com.example.lizhi.entity.StockIn;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockInRepository extends JpaRepository<StockIn, Integer> {
    boolean existsByOrderNo(String order_no);
}