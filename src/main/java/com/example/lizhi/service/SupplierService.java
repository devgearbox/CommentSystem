package com.example.lizhi.service;

import com.example.lizhi.entity.Supplier;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface SupplierService {
    List<Supplier> getAllSuppliers();

    // 分页方法
    Page<Supplier> getAllSuppliers(int page, int size);

    List<Supplier> searchSuppliersByName(String name);

    // 分页搜索方法
    Page<Supplier> searchSuppliersByName(String name, int page, int size);

    Supplier addSupplier(Supplier supplier);
    void batchDelete(List<Integer> ids);
    Optional<Supplier> getSupplierById(Integer id);
    Supplier updateSupplier(Supplier supplier);
    List<Supplier> getSuppliersByUserId(Long userId);

    // 分页方法
    Page<Supplier> getSuppliersByUserId(Long userId, int page, int size);

    // 按用户ID和名称搜索（分页）
    Page<Supplier> searchSuppliersByNameAndUserId(String name, Long userId, int page, int size);

    void incrementOrderCount(Integer supplierId);

    /**
     * 根据用户ID获取供应商名称
     * 基于"一个供应商用户只能对应一个供应商"的设计原则
     */
    String getSupplierNameByUserId(Long userId);

    /**
     * 根据用户ID查找供应商
     * 基于"一个供应商用户只能对应一个供应商"的设计原则
     */
    Optional<Supplier> findByUserId(Long userId);
    /**
     * 获取供应商上架的商品数量
     */
    Integer getProductCountBySupplierId(Integer supplierId);

    /**
     * 获取供应商本月收入
     */
    BigDecimal getMonthlyRevenueBySupplierId(Integer supplierId);
}