package com.example.lizhi.controller;

import com.example.lizhi.entity.LitchiVariety;
import com.example.lizhi.entity.Supplier;
import com.example.lizhi.service.LitchiVarietyService;
import com.example.lizhi.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ProductController {

    @Autowired
    private LitchiVarietyService varietyService;

    @Autowired
    private SupplierService supplierService;

    @PostMapping("/products/add")
    public ResponseEntity<Map<String, Object>> addProduct(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 1. 解析请求参数
            String varietyName = (String) request.get("varietyName");
            BigDecimal price = new BigDecimal(request.get("price").toString());
            Integer stock = Integer.parseInt(request.get("stock").toString());
            String description = (String) request.get("description");
            Integer supplierId = Integer.parseInt(request.get("supplierId").toString());

            // 2. 关联供应商
            Supplier supplier = supplierService.getSupplierById(supplierId)
                    .orElseThrow(() -> new IllegalArgumentException("供应商不存在"));

            // 3. 构建商品对象
            LitchiVariety variety = new LitchiVariety();
            variety.setVarietyName(varietyName);
            variety.setPrice(price);
            variety.setStock(stock);
            variety.setDescription(description);
            variety.setSupplier(supplier); // 绑定供应商

            // 4. 保存商品
            LitchiVariety saved = varietyService.addProduct(variety);
            response.put("success", true);
            response.put("data", saved);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}