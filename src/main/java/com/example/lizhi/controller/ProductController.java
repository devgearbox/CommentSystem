package com.example.lizhi.controller;

import com.example.lizhi.entity.LitchiVariety;
import com.example.lizhi.entity.Supplier;
import com.example.lizhi.service.LitchiVarietyService;
import com.example.lizhi.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private LitchiVarietyService varietyService;
    @Autowired
    private SupplierService supplierService;

    // 使用相对于项目根目录的路径
    private static final String UPLOAD_DIR = "uploads/img/";

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addProduct(
            @RequestParam("varietyName") String varietyName,
            @RequestParam("price") BigDecimal price,
            @RequestParam("stock") Integer stock,
            @RequestParam("description") String description,
            @RequestParam("supplierId") Integer supplierId,
            @RequestParam("productImage") MultipartFile productImage) {

        Map<String, Object> response = new HashMap<>();
        try {
            // 关联供应商
            Supplier supplier = supplierService.getSupplierById(supplierId)
                    .orElseThrow(() -> new IllegalArgumentException("供应商不存在"));
            // 2. 检查供应商状态：1=正常，0=封禁中
            if (supplier.getStatus() != 1) {
                throw new IllegalArgumentException("您已被封禁，无法上架商品");
            }
            // 构建商品对象
            LitchiVariety variety = new LitchiVariety();
            variety.setVarietyName(varietyName);
            variety.setPrice(price);
            variety.setStock(stock);
            variety.setDescription(description);
            variety.setSupplier(supplier);

            // 保存图片并获取图片路径
            String imagePath = saveProductImage(productImage);
            variety.setImagePath("/uploads/img/" + imagePath.substring(imagePath.lastIndexOf("/") + 1));

            // 保存商品
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

    //图片保存方法
    private String saveProductImage(MultipartFile productImage) throws IOException {
        // 获取项目根目录的绝对路径
        String rootPath = System.getProperty("user.dir");
        Path uploadPath = Paths.get(rootPath, "uploads", "img");

        // 确保上传目录存在
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 生成唯一文件名
        String originalFilename = productImage.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        // 保存文件
        Path filePath = uploadPath.resolve(uniqueFilename);
        productImage.transferTo(filePath.toFile());

        // 返回前端可访问的路径
        return "/uploads/img/" + uniqueFilename;
    }
}