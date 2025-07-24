package com.example.lizhi.service.Impl;

import com.example.lizhi.entity.Supplier;
import com.example.lizhi.repository.SupplierRepository;
import com.example.lizhi.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplierServiceImpl implements SupplierService{
    @Autowired
    private SupplierRepository supplierRepository;

    @Override
    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    @Override
    public List<Supplier> searchSuppliersByName(String name){
        return supplierRepository.findBySuppliernameContaining(name);
    }

    public Supplier addSupplier(Supplier supplier) {
        // 可在此处补充业务逻辑（如默认值设置）
        supplier.setStatus(1); // 示例：默认状态为 1
        return supplierRepository.save(supplier);
    }
    public void batchDelete(List<Integer> ids) {
        // JPA 批量删除（推荐：避免逐条删除的性能问题）
        supplierRepository.deleteAllById(ids);
    }
}