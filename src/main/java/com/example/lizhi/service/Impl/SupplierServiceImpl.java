package com.example.lizhi.service.Impl;

import com.example.lizhi.entity.Supplier;
import com.example.lizhi.repository.SupplierRepository;
import com.example.lizhi.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SupplierServiceImpl implements SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;

    @Override
    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    @Override
    public Supplier getSupplierById(Integer id) {
        return supplierRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void saveSupplier(Supplier supplier) {
        if (supplier.getSupplierId() == null) {
            // 设置创建时间
            supplier.setCreateTime(new java.util.Date());
        }
        // 设置更新时间
        supplier.setUpdateTime(new java.util.Date());
        supplierRepository.save(supplier);
    }

    @Override
    @Transactional
    public void deleteSupplier(Integer id) {
        supplierRepository.deleteById(id);
    }
}
