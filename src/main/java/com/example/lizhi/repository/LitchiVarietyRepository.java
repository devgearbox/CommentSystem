package com.example.lizhi.repository;

import com.example.lizhi.entity.LitchiVariety;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LitchiVarietyRepository extends JpaRepository<LitchiVariety, Integer> {
    @Query("select v from LitchiVariety v left join fetch v.supplier")
    List<LitchiVariety> findAllWithSupplier();
    // 新增：按variety_name模糊查询（关联供应商）
    @Query("select v from LitchiVariety v left join fetch v.supplier where v.variety_name like %:keyword%")
    List<LitchiVariety> findByVarietyNameContainingWithSupplier(String keyword);
}