package com.example.lizhi.service.Impl;

import com.example.lizhi.entity.Supplier;
import com.example.lizhi.entity.User;
import com.example.lizhi.repository.SupplierRepository;
import com.example.lizhi.service.SupplierService;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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

    // 新增分页实现
    @Override
    public Page<Supplier> getAllSuppliers(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return supplierRepository.findAll(pageable);
    }

    @Override
    public List<Supplier> searchSuppliersByName(String name){
        return supplierRepository.findBySupplierNameContaining(name);
    }

    // 新增分页搜索实现
    @Override
    public Page<Supplier> searchSuppliersByName(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return supplierRepository.findBySupplierNameContaining(name, pageable);
    }

    public Supplier addSupplier(Supplier supplier) {
        // 获取当前登录用户
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpSession session = attributes.getRequest().getSession();
        User currentUser = (User) session.getAttribute("currentUser");

        // 如果当前用户是供应商角色（role=3），关联用户ID
        if (currentUser != null && currentUser.getRole() == 3) {
            supplier.setUser_id(currentUser.getId());
        }
        // 可在此处补充业务逻辑（如默认值设置）
        supplier.setStatus(2); // 示例：默认状态为 1
        return supplierRepository.save(supplier);
    }

    @Override
    public List<Supplier> getSuppliersByUserId(Long userId) {
        return supplierRepository.findByUserId(userId);
    }

    // 新增分页实现
    @Override
    public Page<Supplier> getSuppliersByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return supplierRepository.findByUserId(userId, pageable);
    }

    // 新增：按用户ID和名称搜索（分页）
    @Override
    public Page<Supplier> searchSuppliersByNameAndUserId(String name, Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return supplierRepository.findByUserIdAndSupplierNameContaining(userId, name, pageable);
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
        try {
            // 验证供应商是否存在
            Optional<Supplier> existingSupplier = supplierRepository.findById(supplier.getSupplier_id());
            if (!existingSupplier.isPresent()) {
                throw new IllegalArgumentException("供应商不存在");
            }

            Supplier existing = existingSupplier.get();

            // 只更新非空字段，保护关键字段不被意外修改
            if (supplier.getSupplier_name() != null) {
                existing.setSupplier_name(supplier.getSupplier_name());
            }
            if (supplier.getContact() != null) {
                existing.setContact(supplier.getContact());
            }
            if (supplier.getPhone() != null) {
                existing.setPhone(supplier.getPhone());
            }
            if (supplier.getAddress() != null) {
                existing.setAddress(supplier.getAddress());
            }
            if (supplier.getVarieties() != null) {
                existing.setVarieties(supplier.getVarieties());
            }
            if (supplier.getCooperation_start_date() != null) {
                existing.setCooperation_start_date(supplier.getCooperation_start_date());
            }

            //状态字段 - 只有明确传入值时才更新
            //防止状态被意外设置为 null
            if (supplier.getStatus() != null) {
                if (supplier.getStatus() >= 0 && supplier.getStatus() <= 2) {
                    existing.setStatus(supplier.getStatus());
                } else {
                    throw new IllegalArgumentException("无效的状态值");
                }
            }
            // 否则保持原有的状态值不变
            // 自动更新修改时间
            existing.setUpdate_time(LocalDateTime.now());

            return supplierRepository.save(existing);
        } catch (Exception e) {
            System.err.println("更新供应商失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("更新供应商失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void incrementOrderCount(Integer supplierId) {
        try {
            Supplier supplier = supplierRepository.findById(supplierId)
                    .orElseThrow(() -> new RuntimeException("供应商不存在：" + supplierId));

            // 处理 null 值情况
            Integer currentCount = supplier.getOrder_count();
            if (currentCount == null) {
                supplier.setOrder_count(1);
            } else {
                supplier.setOrder_count(currentCount + 1);
            }

            supplierRepository.save(supplier);
            System.out.println("供应商 " + supplierId + " 订单数量已增加至: " + supplier.getOrder_count());
        } catch (Exception e) {
            System.err.println("增加供应商订单数量失败: " + e.getMessage());
            throw e; // 重新抛出异常以确保事务回滚
        }
    }
}