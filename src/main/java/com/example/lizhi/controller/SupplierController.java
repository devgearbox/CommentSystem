package com.example.lizhi.controller;

import com.example.lizhi.entity.Supplier;
import com.example.lizhi.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/suppliers")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @GetMapping
    public String ListSupplier(Model model) {
        List<Supplier> suppliers = supplierService.getAllSuppliers();
        model.addAttribute("suppliers", suppliers);
        return "suppliers";
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Supplier> getSupplierById(@PathVariable Integer id) {
        Supplier supplier = supplierService.getSupplierById(id);
        if (supplier != null) {
            return ResponseEntity.ok(supplier);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<String> saveSupplier(@RequestBody Supplier supplier) {
        try {
            supplierService.saveSupplier(supplier);
            return ResponseEntity.ok("Supplier saved successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving supplier: " + e.getMessage());
        }
    }
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteSupplier(@PathVariable Integer id) {
        try {
            supplierService.deleteSupplier(id);
            return ResponseEntity.ok("供应商删除成功");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("供应商删除失败: " + e.getMessage());
        }
    }
}