package com.example.lizhi.service.Impl;

import com.example.lizhi.entity.LitchiVariety;
import com.example.lizhi.repository.LitchiVarietyRepository;
import com.example.lizhi.service.LitchiVarietyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
        return repository.findAllWithSupplier(); // 改用关联查询方法
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

    @Override
    public LitchiVariety addProduct(LitchiVariety variety) {
        // 可添加业务校验（如价格、库存不能为负）
        if (variety.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("价格不能为负数");
        }
        if (variety.getStock() < 0) {
            throw new IllegalArgumentException("库存不能为负数");
        }
        return repository.save(variety); // 保存商品
    }
}