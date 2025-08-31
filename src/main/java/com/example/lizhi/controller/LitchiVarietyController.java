package com.example.lizhi.controller;

import com.example.lizhi.entity.LitchiVariety;
import com.example.lizhi.entity.Supplier;
import com.example.lizhi.entity.User;
import com.example.lizhi.service.LitchiVarietyService;
import com.example.lizhi.service.SupplierService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class LitchiVarietyController {
    @Autowired
    private LitchiVarietyService varietyService;
    @Autowired
    private SupplierService supplierService;

    // 商品删除接口（处理前端下架请求）
    @PostMapping("/variety/delete")
    public ResponseEntity<Map<String, Object>> deleteVariety(
            @RequestParam Integer varietyId,
            HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        // 1. 登录校验
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录！");
            return new ResponseEntity<>(result, HttpStatus.UNAUTHORIZED);
        }

        // 2. 查询商品是否存在
        LitchiVariety variety;
        try {
            variety = varietyService.getById(varietyId);
        } catch (RuntimeException e) {
            result.put("success", false);
            result.put("message", "商品不存在或已被删除！");
            return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
        }

        // 3. 权限校验（关键：防止越权删除）
        Integer userRole = currentUser.getRole();
        Integer supplierId = variety.getSupplier().getSupplier_id();
        // 若用户是供应商（角色3），需校验商品所属供应商是否为自己的
        if (userRole == 3) {
            Optional<Supplier> supplierOpt = supplierService.getSupplierById(supplierId);
            if (!supplierOpt.isPresent() || !supplierOpt.get().getUser_id().equals(currentUser.getId())) {
                result.put("success", false);
                result.put("message", "无权限下架此商品（仅能下架自己供应商的商品）！");
                return new ResponseEntity<>(result, HttpStatus.FORBIDDEN);
            }
        }

        // 4. 执行删除（需在Service中新增delete方法）
        try {
            varietyService.deleteById(varietyId);
            result.put("success", true);
            result.put("message", "商品下架成功！");
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败：" + e.getMessage());
            return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}