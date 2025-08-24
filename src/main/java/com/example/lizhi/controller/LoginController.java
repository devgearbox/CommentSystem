package com.example.lizhi.controller;

import com.example.lizhi.entity.Address;
import com.example.lizhi.entity.User;
import com.example.lizhi.service.AddressService;
import com.example.lizhi.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


@Controller
public class LoginController {
    @Autowired
    private UserService userService;
    @Autowired
    private AddressService addressService;

    @GetMapping("/login")
    public String login() {
        return "login"; // 跳转登录页面
    }

    // 处理登录提交
    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        User user = userService.loginWithRole(username, password);
        if (user != null) {
            // 登录成功：存入完整 User 对象和 userId 到 Session
            session.setAttribute("currentUser", user);
            session.setAttribute("currentUserId", user.getId());

            // 核心：根据角色分流
            if (user.getRole() == 1) {
                // 系统管理员，假设后续跳转到管理员页面（如 /adminDashboard）
                return "redirect:/purchasework";
            } else if (user.getRole() == 2) {
                // 普通采购员：跳转到采购人员端主页面
                return "redirect:/purchasework";
            } else if (user.getRole() == 3) {
                // 供应商：后续扩展，当前可先跳转到临时页面或采购人员端（根据需求）
                return "redirect:/supplier";
            } else {
                // 未知角色，返回登录页
                redirectAttributes.addFlashAttribute("error", "角色异常，请联系管理员");
                return "redirect:/login";
            }
        } else {
            // 登录失败
            redirectAttributes.addFlashAttribute("error", "登录失败：账号或密码错误");
            return "redirect:/login";
        }
    }
    //登录成功后跳转的主页接口
    @GetMapping("/home")
    public String home() {
        return "home"; // 需创建 home.html 作为登录后页面
    }

    @GetMapping("/purchasework")
    public String purchasework() {
        return "purchasework";
    }

    //手机号登录
    @PostMapping("/checkPhone")
    @ResponseBody
    public String checkPhone(@RequestParam String phone) {
        // 调用 UserService 校验手机号
        User user = userService.findByPhone(phone);
        return user != null ? "exists" : "not_exists";
    }

    private static Map<String, String> phoneCodeMap = new HashMap<>();
    @PostMapping("/sendCode")
    @ResponseBody
    public Map<String, String> sendCode(@RequestParam String phone) {
        User user = userService.findByPhone(phone);
        Map<String, String> result = new HashMap<>();

        if (user == null) {
            result.put("status", "not_exists");
            return result;
        }

        String code = String.format("%04d", new Random().nextInt(9000) + 1000);
        phoneCodeMap.put(phone, code);
        System.out.println("验证码：" + code); // 控制台输出

        result.put("status", "success");
        result.put("code", code); // 将验证码返回给前端
        return result;
    }

    @PostMapping("/phoneLogin")
    @ResponseBody
    public String phoneLogin(@RequestParam String phone, HttpSession session) {
        String code = phoneCodeMap.get(phone);
        if (code == null) {
            return "invalid_code";
        }
        User user = userService.findByPhone(phone);
        if (user != null) {
            session.setAttribute("currentUser", user); // 存入 Session
            phoneCodeMap.remove(phone); // 验证码使用后移除
            return "success";
        }
        return "fail";
    }

    // 删除原有的第二个 @GetMapping("/settings") 方法，保留并修改第一个：
    @GetMapping("/settings")
    public String showSettings(HttpSession session, Model model) {
        // 1. 从 Session 获取登录用户（登录时已存入 "currentUser"）
        User user = (User) session.getAttribute("currentUser");

        // 2. 未登录拦截
        if (user == null) {
            return "redirect:/login";
        }

        // 3. 补充用户信息默认值（原有逻辑）
        if (user.getPhone() == null || user.getPhone().trim().isEmpty()) {
            user.setPhone("待填写");
        }
        if (user.getSignature() == null || user.getSignature().trim().isEmpty()) {
            user.setSignature("该用户很懒，什么都没有留下...");
        }

        // 4. 新增：查询当前用户的地址列表（核心修改）
        List<Address> addresses = addressService.getAddressesByUserId(user.getId()); // 使用登录用户的真实ID
        model.addAttribute("addresses", addresses);

        // 5. 传递用户数据到前端
        model.addAttribute("user", user);

        return "settings"; // 返回 settings.html
    }

    @PostMapping("/settings/save")
    public String saveSettings(HttpSession session,
                               @RequestParam("real_name") String realName,
                               @RequestParam("gender") String gender,
                               @RequestParam("phone") String phone,
                               @RequestParam("signature") String signature) {
        User user = (User) session.getAttribute("currentUser");
        if (user != null) {
            // 更新用户属性
            user.setReal_name(realName);
            user.setGender(gender);
            user.setPhone(phone);
            user.setSignature(signature);

            // 调用 Service 保存（内部调用 UserRepository.save）
            userService.saveUser(user);

            // 可选：更新 Session 中的用户对象
            session.setAttribute("currentUser", user);
        }
        return "redirect:/settings";
    }

    @PostMapping("/settings/changePassword")
    public String changePassword(
            HttpSession session,
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model
    ) {
        // 1. 非空校验（后端唯一校验点）
        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            model.addAttribute("passwordError", "原密码不能为空！");
            return "settings";
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            model.addAttribute("passwordError", "新密码不能为空！");
            return "settings";
        }
        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            model.addAttribute("passwordError", "确认新密码不能为空！");
            return "settings";
        }

        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/login";
        }

        // 2. 密码一致性校验
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("passwordError", "两次输入的新密码不一致！");
            return "settings";
        }

        // 3. 密码修改逻辑
        if (userService.changePassword(user.getId(), oldPassword, newPassword)) {
            model.addAttribute("passwordChanged", true);
            return "redirect:/settings";
        } else {
            model.addAttribute("passwordError", "原密码错误或修改失败！");
            model.addAttribute("passwordChanged", false);
            return "settings";
        }
    }


    // 保存地址
    @PostMapping("/address/save")
    @ResponseBody
    public String saveAddress(
            @RequestBody Address address, // 用 @RequestBody 接收 JSON
            HttpSession session
    ) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "fail";
        }
        address.setUserId(currentUser.getId());
        addressService.saveAddress(address);
        return "success";
    }

    // 删除地址
    @PostMapping("/address/delete")
    @ResponseBody
    public String deleteAddress(
            @RequestParam Long id,
            HttpSession session // 新增 Session 获取当前用户
    ) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "fail"; // 未登录
        }
        // 校验地址的 userId 是否属于当前用户（需 AddressService 新增方法）
        boolean isOwned = addressService.isAddressOwnedByUser(id, currentUser.getId());
        if (!isOwned) {
            return "fail"; // 无权删除
        }
        addressService.deleteAddress(id);
        return "success";
    }
    @PostMapping("/address/setDefault")
    @ResponseBody
    public String setDefaultAddress(
            @RequestParam Long addressId,
            HttpSession session
    ) {
        // 1. 获取当前登录用户
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "fail"; // 未登录
        }

        // 2. 校验地址归属（防止越权）
        Address address = addressService.getAddressById(addressId); // 语义修正
        if (address == null || !address.getUserId().equals(currentUser.getId())) {
            return "fail"; // 地址不存在或不属于当前用户
        }

        // 3. 先清除当前用户原有默认地址
        addressService.clearDefaultAddress(currentUser.getId());

        // 4. 设置新默认地址
        address.setDefault(true);
        addressService.saveAddress(address);

        return "success";
    }
}