package com.example.lizhi.repository;

import com.example.lizhi.entity.LitchiVariety;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LitchiVarietyRepository extends JpaRepository<LitchiVariety, Integer> {
    @Query("select v from LitchiVariety v left join fetch v.supplier")
    List<LitchiVariety> findAllWithSupplier();
    // 新增：按variety_name模糊查询（关联供应商）
    @Query("select v from LitchiVariety v left join fetch v.supplier where v.variety_name like %:keyword%")
    List<LitchiVariety> findByVarietyNameContainingWithSupplier(String keyword);
    // 新增1：按供应商ID列表查询商品（关联供应商）
    @Query("select v from LitchiVariety v left join fetch v.supplier where v.supplier.supplier_id in :supplierIds")
    List<LitchiVariety> findBySupplier_SupplierIdIn(@Param("supplierIds") List<Integer> supplierIds);

    // 新增2：按“品种名+供应商ID列表”查询商品（关联供应商）
    @Query("select v from LitchiVariety v left join fetch v.supplier where v.variety_name like %:keyword% and v.supplier.supplier_id in :supplierIds")
    List<LitchiVariety> findByVarietyNameContainingAndSupplier_SupplierIdIn(
            @Param("keyword") String keyword,
            @Param("supplierIds") List<Integer> supplierIds
    );
    //筛选封禁中的供应商
    @Query("select v from LitchiVariety v left join fetch v.supplier where v.supplier.status = :status")
    List<LitchiVariety> findBySupplierStatus(@Param("status") Integer status);

    // 根据商品名称和供应商状态搜索
    @Query("select v from LitchiVariety v left join fetch v.supplier where v.variety_name like %:keyword% and v.supplier.status = :status")
    List<LitchiVariety> findByVarietyNameContainingAndSupplierStatus(
            @Param("keyword") String keyword,
            @Param("status") Integer status
    );
}