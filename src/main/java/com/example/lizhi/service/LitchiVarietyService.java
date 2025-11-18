package com.example.lizhi.service;

import com.example.lizhi.entity.LitchiVariety;
import java.util.List;
import java.util.Optional;

public interface LitchiVarietyService {
    List<LitchiVariety> findAll(); // 查询所有荔枝品种
    LitchiVariety getById(Integer varietyId);
    LitchiVariety addProduct(LitchiVariety variety); // 新增商品
    // 按品种名模糊搜索
    List<LitchiVariety> searchByVarietyName(String keyword);
    // 按供应商ID列表查询商品（关联供应商）
    List<LitchiVariety> findBySupplierIds(List<Integer> supplierIds);
    // 按“品种名+供应商ID列表”查询商品（关联供应商）
    List<LitchiVariety> searchByVarietyNameAndSupplierIds(String keyword, List<Integer> supplierIds);
    // 根据商品ID删除商品
    void deleteById(Integer varietyId);
    void incrementOrderCount(Integer varietyId);
    Optional<LitchiVariety> findById(Integer varietyId);
    //筛选封禁中的供应商
    List<LitchiVariety> findBySupplierStatus(Integer status);
    List<LitchiVariety> searchByVarietyNameAndSupplierStatus(String keyword, Integer status);
}