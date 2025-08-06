package com.example.lizhi.service.Impl;

import com.example.lizhi.entity.LitchiVariety;
import com.example.lizhi.repository.LitchiVarietyRepository;
import com.example.lizhi.service.LitchiVarietyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class LitchiVarietyServiceImpl implements LitchiVarietyService {
    private final LitchiVarietyRepository repository;

    // 构造注入 Repository
    public LitchiVarietyServiceImpl(LitchiVarietyRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<LitchiVariety> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true) // 正确导包后，使用只读事务优化性能
    public LitchiVariety getById(Integer varietyId) {
        // 校验参数
        if (varietyId == null || varietyId <= 0) {
            throw new IllegalArgumentException("商品ID无效：" + varietyId);
        }

        // 查询数据库（使用正确的变量名 repository）
        Optional<LitchiVariety> varietyOptional = repository.findById(varietyId);

        // 若不存在，抛出业务异常（需自定义异常类，这里先使用 RuntimeException 示例，实际建议自定义业务异常）
        return varietyOptional.orElseThrow(() ->
                new RuntimeException("未找到ID为" + varietyId + "的荔枝品种"));
    }
}