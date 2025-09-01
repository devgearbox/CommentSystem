package com.example.lizhi.service.Impl;

import com.example.lizhi.entity.Supplier;
import com.example.lizhi.entity.User;
import com.example.lizhi.repository.SupplierRepository;
import com.example.lizhi.service.SupplierService;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Override
    public List<Supplier> searchSuppliersByName(String name){
        return supplierRepository.findBySupplierNameContaining(name);
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
        supplier.setStatus(1); // 示例：默认状态为 1
        return supplierRepository.save(supplier);
    }

    @Override
    public List<Supplier> getSuppliersByUserId(Long userId) {
        return supplierRepository.findByUserId(userId);
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