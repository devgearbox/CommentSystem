package com.example.lizhi.service;

import com.example.lizhi.entity.Supplier;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface SupplierService {
    List<Supplier> getAllSuppliers();

    // 新增分页方法
    Page<Supplier> getAllSuppliers(int page, int size);

    List<Supplier> searchSuppliersByName(String name);

    // 新增分页搜索方法
    Page<Supplier> searchSuppliersByName(String name, int page, int size);

    Supplier addSupplier(Supplier supplier);
    void batchDelete(List<Integer> ids);
    Optional<Supplier> getSupplierById(Integer id);
    Supplier updateSupplier(Supplier supplier);
    List<Supplier> getSuppliersByUserId(Long userId);

    // 新增分页方法
    Page<Supplier> getSuppliersByUserId(Long userId, int page, int size);

    // 新增：按用户ID和名称搜索（分页）
    Page<Supplier> searchSuppliersByNameAndUserId(String name, Long userId, int page, int size);

    void incrementOrderCount(Integer supplierId);
}