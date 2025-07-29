package com.example.lizhi.repository;

import com.example.lizhi.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplierRepository  extends JpaRepository<Supplier, Integer> {
    List<Supplier> findBySupplierNameContaining(String name);
}