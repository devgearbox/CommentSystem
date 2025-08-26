package com.example.lizhi.repository;

import com.example.lizhi.entity.LitchiVariety;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LitchiVarietyRepository extends JpaRepository<LitchiVariety, Integer> {
    @Query("select v from LitchiVariety v left join fetch v.supplier")
    List<LitchiVariety> findAllWithSupplier();
}