package com.example.lizhi.service.Impl;

import com.example.lizhi.entity.Supplier;
import com.example.lizhi.repository.SupplierRepository;
import com.example.lizhi.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
        return supplierRepository.findBySupplierNameContaining(name);
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
    @Override
    public Optional<Supplier> getSupplierById(Integer id) {
        return supplierRepository.findById(id);
    }

    @Override
    public Supplier updateSupplier(Supplier supplier) {
        // 验证供应商是否存在
        Optional<Supplier> existingSupplier = supplierRepository.findById(supplier.getSupplier_id());
        if (!existingSupplier.isPresent()) {
            throw new IllegalArgumentException("供应商不存在");
        }

        // 保留创建时间，不允许修改
        Supplier updated = existingSupplier.get();
        updated.setSupplier_name(supplier.getSupplier_name());
        updated.setContact(supplier.getContact());
        updated.setPhone(supplier.getPhone());
        updated.setAddress(supplier.getAddress());
        updated.setVarieties(supplier.getVarieties());
        updated.setCooperation_start_date(supplier.getCooperation_start_date());
        updated.setStatus(supplier.getStatus());
        updated.setOrder_count(supplier.getOrder_count());

        // 自动更新修改时间
        updated.setUpdate_time(LocalDateTime.now());

        return supplierRepository.save(updated);
    }

}