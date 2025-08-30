package com.example.lizhi.controller;

import com.example.lizhi.entity.Address;
import com.example.lizhi.entity.LitchiVariety;
import com.example.lizhi.entity.User;
import com.example.lizhi.service.AddressService;
import com.example.lizhi.service.LitchiVarietyService;
import com.example.lizhi.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class WorkController {
    @Autowired
    private UserService userService;
    @Autowired
    private LitchiVarietyService litchiVarietyService;
    // 注入 AddressService
    @Autowired
    private AddressService addressService;

    @GetMapping("/work")
    public String workPage(Model model,
                           HttpSession session,
                           @RequestParam(required = false) String keyword) { // 接收搜索关键词
        // 1. 登录校验（原有逻辑不变）
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);

        // 2. 商品查询：根据是否有关键词，选择“搜索”或“查询全部”
        List<LitchiVariety> varieties;
        if (keyword != null && !keyword.trim().isEmpty()) {
            varieties = litchiVarietyService.searchByVarietyName(keyword); // 搜索逻辑
        } else {
            varieties = litchiVarietyService.findAll(); // 原有：查询全部
        }
        model.addAttribute("varieties", varieties);

        // 3. 默认地址校验（原有逻辑不变）
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


}
