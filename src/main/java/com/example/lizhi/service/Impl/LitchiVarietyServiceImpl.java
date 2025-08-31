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

    // 新增：按品种名模糊搜索（关联供应商）
    @Override
    @Transactional(readOnly = true) // 只读事务优化性能
    public List<LitchiVariety> searchByVarietyName(String keyword) {
        // 关键词去空（避免无效查询）
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll(); // 关键词为空时，返回所有品种
        }
        return repository.findByVarietyNameContainingWithSupplier(keyword.trim());
    }

    // 实现新增1：按供应商ID列表查询商品
    @Override
    @Transactional(readOnly = true)
    public List<LitchiVariety> findBySupplierIds(List<Integer> supplierIds) {
        if (supplierIds == null || supplierIds.isEmpty()) {
            return List.of(); // 无关联供应商时，返回空列表
        }
        // 调用Repository新增的方法（需在LitchiVarietyRepository中定义）
        return repository.findBySupplier_SupplierIdIn(supplierIds);
    }

    // 实现新增2：按“品种名+供应商ID列表”查询商品
    @Override
    @Transactional(readOnly = true)
    public List<LitchiVariety> searchByVarietyNameAndSupplierIds(String keyword, List<Integer> supplierIds) {
        if (keyword == null || keyword.trim().isEmpty() || supplierIds.isEmpty()) {
            return findBySupplierIds(supplierIds); // 关键词为空时，按供应商ID列表查
        }
        // 调用Repository新增的方法（需在LitchiVarietyRepository中定义）
        return repository.findByVarietyNameContainingAndSupplier_SupplierIdIn(keyword.trim(), supplierIds);
    }

    // 实现：根据商品ID删除商品
    @Override
    @Transactional // 事务管理，确保删除操作原子性
    public void deleteById(Integer varietyId) {
        // 1. 校验商品ID
        if (varietyId == null || varietyId <= 0) {
            throw new IllegalArgumentException("商品ID无效：" + varietyId);
        }
        // 2. 校验商品是否存在
        if (!repository.existsById(varietyId)) {
            throw new RuntimeException("商品不存在或已被删除！");
        }
        // 3. 执行删除
        repository.deleteById(varietyId);
    }
}