package com.example.lizhi.repository;

import com.example.lizhi.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository  extends JpaRepository<Supplier, Integer> {
    @Query("SELECT s FROM Supplier s WHERE s.userId = :userId")
    Optional<Supplier> findByUserId(@Param("userId") Long userId);

    @Query("SELECT s FROM Supplier s WHERE s.supplierName LIKE %:name%")
    List<Supplier> findBySupplierNameContaining(@Param("name") String name);

    // 分页查询方法
    @Query("SELECT s FROM Supplier s WHERE s.supplierName LIKE %:name%")
    Page<Supplier> findBySupplierNameContaining(@Param("name") String name, Pageable pageable);

    // 按用户ID查询供应商
    @Query("SELECT s FROM Supplier s WHERE s.userId = :userId")
    List<Supplier> findListByUserId(@Param("userId") Long userId);

    // 分页按用户ID查询供应商
    @Query("SELECT s FROM Supplier s WHERE s.userId = :userId")
    Page<Supplier> findPageByUserId(@Param("userId") Long userId, Pageable pageable);

    // 页查询所有供应商
    Page<Supplier> findAll(Pageable pageable);

    // 按用户ID和名称搜索（分页）
    @Query("SELECT s FROM Supplier s WHERE s.userId = :userId AND s.supplierName LIKE %:name%")
    Page<Supplier> findByUserIdAndSupplierNameContaining(@Param("userId") Long userId, @Param("name") String name, Pageable pageable);
}