package com.example.lizhi.controller;

import com.example.lizhi.entity.Address;
import com.example.lizhi.entity.LitchiVariety;
import com.example.lizhi.entity.Supplier;
import com.example.lizhi.entity.User;
import com.example.lizhi.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class WorkController {
    @Autowired
    private UserService userService;
    @Autowired
    private LitchiVarietyService litchiVarietyService;
    // 注入 AddressService
    @Autowired
    private AddressService addressService;

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @Autowired
    private SupplierService supplierService;

    @GetMapping("/work")
    public String workPage(Model model,
                           HttpSession session,
                           @RequestParam(required = false) String keyword) {
        // 1. 登录校验（原有逻辑不变）
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);

        // 2. 商品查询：按角色过滤（核心修改）
        List<LitchiVariety> varieties;
        if (user.getRole() == 3) { // 角色为3（供应商）：仅查自己关联的供应商的商品
            // 2.1 获取当前用户关联的所有供应商
            List<Supplier> userSuppliers = supplierService.getSuppliersByUserId(user.getId());
            // 2.2 提取供应商ID列表（用于筛选商品）
            List<Integer> supplierIds = userSuppliers.stream()
                    .map(Supplier::getSupplier_id) // 注意：Supplier类中ID字段是supplier_id
                    .collect(Collectors.toList());

            // 2.3 结合关键词查询：有关键词则“按名称+供应商ID”筛选，无关键词则“按供应商ID”筛选
            if (keyword != null && !keyword.trim().isEmpty()) {
                // 需在LitchiVarietyService中新增“按名称+供应商ID列表”查询方法
                varieties = litchiVarietyService.searchByVarietyNameAndSupplierIds(keyword, supplierIds);
            } else {
                // 需在LitchiVarietyService中新增“按供应商ID列表”查询方法
                varieties = litchiVarietyService.findBySupplierIds(supplierIds);
            }
        } else { // 其他角色（管理员/采购部）：查询全部商品（原有逻辑不变）
            if (keyword != null && !keyword.trim().isEmpty()) {
                varieties = litchiVarietyService.searchByVarietyName(keyword);
            } else {
                varieties = litchiVarietyService.findAll();
            }
        }
        model.addAttribute("varieties", varieties);

        // 3. 为每个商品计算实际销售数量总和（修改逻辑）
        Map<Integer, BigDecimal> salesQuantityMap = new HashMap<>();
        Map<Integer, String> formattedSalesMap = new HashMap<>();
        for (LitchiVariety variety : varieties) {
            BigDecimal totalSalesQuantity = purchaseOrderService.getTotalSalesQuantityByVarietyId(variety.getId());
            salesQuantityMap.put(variety.getId(), totalSalesQuantity);

            // 格式化销量显示
            String formattedSales = formatSalesQuantity(totalSalesQuantity);
            formattedSalesMap.put(variety.getId(), formattedSales);
        }

        model.addAttribute("varieties", varieties);
        model.addAttribute("salesQuantityMap", salesQuantityMap);
        model.addAttribute("formattedSalesMap", formattedSalesMap);

        // 4. 默认地址校验（原有逻辑不变）
        Address defaultAddress = addressService.findDefaultAddressByUserId(user.getId());
        model.addAttribute("hasDefaultAddress", defaultAddress != null);

        return "work";
    }

    @GetMapping("/commodity")
    public String commodityPage(@RequestParam("varietyId") Integer varietyId, Model model, HttpSession session) {
        LitchiVariety variety = litchiVarietyService.getById(varietyId);
        model.addAttribute("variety", variety);

        User user = (User) session.getAttribute("currentUser");
        if (user != null) {
            // 查询用户的地址列表
            List<Address> addresses = addressService.getAddressesByUserId(user.getId());
            model.addAttribute("addresses", addresses);

            // 查询默认地址
            Address defaultAddress = addressService.findDefaultAddressByUserId(user.getId());
            model.addAttribute("defaultAddress", defaultAddress);
        }

        return "commodity";
    }

    //销量格式化显示
    private String formatSalesQuantity(BigDecimal quantity) {
        if (quantity == null) {
            return "0";
        }

        int sales = quantity.intValue();

        if (sales < 10) {
            // 小于10：正常显示
            return String.valueOf(sales);
        } else if (sales < 100) {
            // 10-99：显示为整十
            int rounded = (sales / 10) * 10;
            return rounded + "+";
        } else if (sales < 1000) {
            // 100-999：显示为整百
            int rounded = (sales / 100) * 100;
            return rounded + "+";
        } else if (sales < 10000) {
            // 1000-9999：显示为整千
            int rounded = (sales / 1000) * 1000;
            return rounded + "+";
        } else {
            // 10000以上：显示为整万
            int rounded = (sales / 10000) * 10000;
            return rounded + "+";
        }
    }

}
