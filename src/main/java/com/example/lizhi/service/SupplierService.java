package com.example.lizhi.service;

import com.example.lizhi.entity.Supplier;

import java.util.List;

public interface SupplierService {
    List<Supplier> getAllSuppliers();
    Supplier getSupplierById(Integer id);
    void saveSupplier(Supplier supplier);
    void deleteSupplier(Integer id);
}