package com.example.lizhi.controller;

import com.example.lizhi.entity.Supplier;
import com.example.lizhi.entity.User;
import com.example.lizhi.service.SupplierService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page; // 新增导入
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    // 修改：添加分页参数
    @GetMapping("/suppliers")
    public String listSuppliers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model,
            HttpSession session) {

        // 获取当前登录用户
        User currentUser = (User) session.getAttribute("currentUser");
        Page<Supplier> supplierPage;

        // 根据角色筛选数据：角色为3（供应商）只看自己的，其他角色看全部
        if (currentUser != null && currentUser.getRole() == 3) {
            supplierPage = supplierService.getSuppliersByUserId(currentUser.getId(), page, size);
        } else {
            supplierPage = supplierService.getAllSuppliers(page, size);
        }

        model.addAttribute("suppliers", supplierPage.getContent());
        // 新增分页信息
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", supplierPage.getTotalPages());
        model.addAttribute("totalItems", supplierPage.getTotalElements());
        model.addAttribute("pageSize", size);

        return "supplier";
    }

    // 修改：添加分页参数
    @GetMapping("/suppliers/search")
    public String searchSuppliers(
            @RequestParam String name,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model,
            HttpSession session) {

        User currentUser = (User) session.getAttribute("currentUser");
        Page<Supplier> supplierPage;

        if (currentUser != null && currentUser.getRole() == 3) {
            // 供应商角色：搜索自己名下的供应商（分页）
            supplierPage = supplierService.searchSuppliersByNameAndUserId(name, currentUser.getId(), page, size);
        } else {
            // 其他角色：搜索全部（分页）
            supplierPage = supplierService.searchSuppliersByName(name, page, size);
        }

        model.addAttribute("suppliers", supplierPage.getContent());
        // 新增分页信息
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", supplierPage.getTotalPages());
        model.addAttribute("totalItems", supplierPage.getTotalElements());
        model.addAttribute("pageSize", size);

        return "supplier :: #supplier-table-body";
    }

    // 以下方法保持不变...
    @PostMapping("/suppliers/add")
    public ResponseEntity<?> addSupplier(@RequestBody Supplier supplier) {
        try {
            Supplier saved = supplierService.addSupplier(supplier);
            return ResponseEntity.ok(Map.of("success", true, "data", saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/delete/batch")
    public ResponseEntity<?> batchDelete(@RequestBody Map<String, List<Integer>> request) {
        List<Integer> ids = request.get("ids");
        try {
            supplierService.batchDelete(ids);
            return ResponseEntity.ok(Map.of("success", true, "message", "批量删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/suppliers/detail/{id}")
    public ResponseEntity<?> getSupplierDetail(@PathVariable Integer id) {
        Optional<Supplier> supplierOpt = supplierService.getSupplierById(id);
        if (supplierOpt.isPresent()) {
            return ResponseEntity.ok(supplierOpt.get());
        } else {
            return ResponseEntity.status(404).body("供应商 ID 不存在");
        }
    }

    @PutMapping("/suppliers/update")
    public ResponseEntity<Map<String, Object>> updateSupplier(@RequestBody Supplier supplier) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 调用服务层更新供应商
            Supplier updated = supplierService.updateSupplier(supplier);

            response.put("success", true);
            response.put("message", "供应商更新成功");
            response.put("data", updated);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新供应商失败");
            return ResponseEntity.status(500).body(response);
        }
    }
}