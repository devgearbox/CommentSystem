package com.example.lizhi.repository;

import com.example.lizhi.entity.LitchiVariety;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LitchiVarietyRepository extends JpaRepository<LitchiVariety, Integer> {
    // 继承 JpaRepository，自动获得增删改查方法
}