package com.example.lizhi.controller;

import com.example.lizhi.entity.Supplier;
import com.example.lizhi.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    @GetMapping("/work")
    public String showSuppliersInWork(Model model) {
        List<Supplier> suppliers = supplierService.getAllSuppliers();
        model.addAttribute("suppliers", suppliers);
        return "work";
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
}