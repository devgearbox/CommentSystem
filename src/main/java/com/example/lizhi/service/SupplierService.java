package com.example.lizhi.service;

import com.example.lizhi.entity.Supplier;
import java.util.List;

public interface SupplierService {
    List<Supplier> getAllSuppliers();
    List<Supplier> searchSuppliersByName(String name);
    Supplier addSupplier(Supplier supplier);
    void batchDelete(List<Integer> ids);
}