package com.example.lizhi.service;

import com.example.lizhi.entity.LitchiVariety;
import java.util.List;

public interface LitchiVarietyService {
    List<LitchiVariety> findAll(); // 查询所有荔枝品种
    LitchiVariety getById(Integer varietyId);
    LitchiVariety addProduct(LitchiVariety variety); // 新增商品
    // 新增：按品种名模糊搜索
    List<LitchiVariety> searchByVarietyName(String keyword);
}