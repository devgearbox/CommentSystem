package com.example.lizhi.repository;

import com.example.lizhi.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserId(Long userId);
    @Query("SELECT a FROM Address a WHERE a.userId = :userId AND a.isDefault = true")
    Address findDefaultByUserId(@Param("userId") Long userId);
}