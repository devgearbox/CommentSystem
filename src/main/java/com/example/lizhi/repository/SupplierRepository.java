package com.example.lizhi.repository;

import com.example.lizhi.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SupplierRepository  extends JpaRepository<Supplier, Integer> {
    @Query("SELECT s FROM Supplier s WHERE s.supplierName LIKE %:name%")
    List<Supplier> findBySupplierNameContaining(@Param("name") String name);
    // 新增：按用户ID查询供应商
    List<Supplier> findByUserId(Long userId);
}