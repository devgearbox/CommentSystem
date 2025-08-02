package com.example.lizhi.controller;

import com.example.lizhi.entity.Supplier;
import com.example.lizhi.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping("/suppliers")
    public String listSuppliers(Model model) {
        List<Supplier> suppliers = supplierService.getAllSuppliers();
        model.addAttribute("suppliers", suppliers);
        return "supplier";
    }

    @GetMapping("/suppliers/search")
    @ResponseBody
    public List<Supplier> searchSuppliersByName(@RequestParam String name) {
        return supplierService.searchSuppliersByName(name);
    }

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