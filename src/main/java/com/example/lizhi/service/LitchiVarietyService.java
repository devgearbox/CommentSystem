package com.example.lizhi.service;

import com.example.lizhi.entity.LitchiVariety;
import java.util.List;

public interface LitchiVarietyService {
    List<LitchiVariety> findAll(); // 查询所有荔枝品种
    LitchiVariety getById(Integer varietyId);
}