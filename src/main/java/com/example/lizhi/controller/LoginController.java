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
import java.util.concurrent.ConcurrentHashMap;


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
        private static Map<String, LoginAttempt> loginAttempts = new HashMap<>();

        // 处理登录提交（修改后的方法）
        @PostMapping("/login")
        public String login(
                @RequestParam String username,
                @RequestParam String password,
                HttpSession session,
                RedirectAttributes redirectAttributes
        ) {
            // 1. 检查账号是否被锁定
            LoginAttempt attempt = loginAttempts.get(username);
            if (attempt != null && attempt.getLockUntilTime() > System.currentTimeMillis()) {
                // 计算剩余锁定时间（分钟）
                long remainingTime = (attempt.getLockUntilTime() - System.currentTimeMillis()) / (60 * 1000);
                redirectAttributes.addFlashAttribute("error",
                        "账号已锁定，请 " + (remainingTime + 1) + " 分钟后再试");
                return "redirect:/login";
            }

            User user = userService.loginWithRole(username, password);
            if (user != null) {
                // 登录成功：清除错误计数
                if (attempt != null) {
                    attempt.reset();
                }

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
                    return "redirect:/purchasework?target=/suppliers";
                } else {
                    // 未知角色，返回登录页
                    redirectAttributes.addFlashAttribute("error", "角色异常，请联系管理员");
                    return "redirect:/login";
                }
            } else {
                // 登录失败：更新错误计数
                if (attempt == null) {
                    attempt = new LoginAttempt();
                    loginAttempts.put(username, attempt);
                }
                attempt.incrementAttemptCount();

                // 检查是否达到锁定阈值（5次）
                if (attempt.getAttemptCount() >= 5) {
                    // 锁定5分钟
                    attempt.setLockUntilTime(System.currentTimeMillis() + 5 * 60 * 1000);
                    redirectAttributes.addFlashAttribute("error",
                            "连续5次登录失败，账号已锁定5分钟，请稍后再试");
                } else {
                    // 显示剩余尝试次数
                    int remainingAttempts = 5 - attempt.getAttemptCount();
                    redirectAttributes.addFlashAttribute("error",
                            "登录失败：账号或密码错误，剩余尝试次数：" + remainingAttempts);
                }
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
    public Map<String, String> checkPhone(@RequestParam String phone) {
        Map<String, String> result = new HashMap<>();

        // 手机号格式校验
        if (!isValidPhoneNumber(phone)) {
            result.put("status", "invalid");
            result.put("message", "手机号格式不正确");
            return result;
        }

        // 检查手机号是否已注册
        User user = userService.findByPhone(phone);
        if (user != null) {
            result.put("status", "exists");
            result.put("message", "手机号已注册");
        } else {
            result.put("status", "not_exists");
            result.put("message", "手机号未注册");
        }

        return result;
    }

    private static Map<String, PhoneCodeInfo> phoneCodeMap = new ConcurrentHashMap<>();

    // 验证码信息内部类
    private static class PhoneCodeInfo {
        private String code;
        private long expireTime; // 过期时间戳

        public PhoneCodeInfo(String code, long expireTime) {
            this.code = code;
            this.expireTime = expireTime;
        }

        public String getCode() { return code; }
        public long getExpireTime() { return expireTime; }
        public boolean isExpired() { return System.currentTimeMillis() > expireTime; }
    }

    // 手机号格式校验方法
    private boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        // 中国手机号格式校验：1开头，11位数字
        String phoneRegex = "^1[3-9]\\d{9}$";
        return phone.matches(phoneRegex);
    }

    @PostMapping("/sendCode")
    @ResponseBody
    public Map<String, String> sendCode(@RequestParam String phone) {
        Map<String, String> result = new HashMap<>();

        // 1. 手机号格式校验
        if (!isValidPhoneNumber(phone)) {
            result.put("status", "invalid_phone");
            result.put("message", "手机号格式不正确");
            return result;
        }

        // 2. 检查手机号是否已注册
        User user = userService.findByPhone(phone);
        if (user == null) {
            result.put("status", "not_exists");
            result.put("message", "手机号未注册");
            return result;
        }

        // 3. 检查是否发送过于频繁（60秒内不能重复发送）
        PhoneCodeInfo existingCode = phoneCodeMap.get(phone);
        if (existingCode != null && !existingCode.isExpired()) {
            long remainingTime = (existingCode.getExpireTime() - System.currentTimeMillis()) / 1000;
            if (remainingTime > 4 * 60) { // 如果剩余时间大于4分钟，说明60秒内刚发送过
                result.put("status", "too_frequent");
                result.put("message", "请求过于频繁，请稍后再试");
                return result;
            }
        }

        // 4. 生成验证码并设置过期时间（5分钟）
        String code = String.format("%04d", new Random().nextInt(9000) + 1000);
        long expireTime = System.currentTimeMillis() + 2 * 60 * 1000; // 2分钟过期

        // 5. 存储验证码信息（包含过期时间）
        phoneCodeMap.put(phone, new PhoneCodeInfo(code, expireTime));

        // 6. 模拟发送验证码（生产环境应调用短信服务商API）
        System.out.println("【模拟短信】手机号 " + phone + " 的验证码：" + code + "，5分钟内有效");

        // 7. 返回结果（不再返回验证码明文）
        result.put("status", "success");
        result.put("message", "验证码已发送");
        return result;
    }

    @PostMapping("/phoneLogin")
    @ResponseBody
    public Map<String, String> phoneLogin(@RequestParam String phone,
                                          @RequestParam String code,
                                          HttpSession session) {
        Map<String, String> result = new HashMap<>();

        // 1. 基本校验
        if (phone == null || phone.trim().isEmpty()) {
            result.put("status", "fail");
            result.put("message", "手机号不能为空");
            return result;
        }

        if (code == null || code.trim().isEmpty()) {
            result.put("status", "fail");
            result.put("message", "验证码不能为空");
            return result;
        }

        // 2. 获取验证码信息
        PhoneCodeInfo codeInfo = phoneCodeMap.get(phone);
        if (codeInfo == null) {
            result.put("status", "fail");
            result.put("message", "验证码不存在或已过期");
            return result;
        }

        // 3. 检查验证码是否过期
        if (codeInfo.isExpired()) {
            phoneCodeMap.remove(phone); // 清除过期验证码
            result.put("status", "fail");
            result.put("message", "验证码已过期");
            return result;
        }

        // 4. 验证码校验
        if (!codeInfo.getCode().equals(code)) {
            result.put("status", "fail");
            result.put("message", "验证码错误");
            return result;
        }

        // 5. 查找用户
        User user = userService.findByPhone(phone);
        if (user != null) {
            // 登录成功
            session.setAttribute("currentUser", user);
            phoneCodeMap.remove(phone); // 验证码使用后立即清除
            result.put("status", "success");
            result.put("message", "登录成功");
        } else {
            result.put("status", "fail");
            result.put("message", "用户不存在");
        }

        return result;
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

        // 4.查询当前用户的地址列表（核心修改）
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
            HttpSession session //  Session 获取当前用户
    ) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "fail"; // 未登录
        }
        // 校验地址的 userId 是否属于当前用户
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

    // 内部类记录登录尝试信息
    private static class LoginAttempt {
        private int attemptCount;
        private long lockUntilTime;

        public LoginAttempt() {
            this.attemptCount = 0;
            this.lockUntilTime = 0;
        }

        // Getter和Setter方法
        public int getAttemptCount() { return attemptCount; }
        public void setAttemptCount(int attemptCount) { this.attemptCount = attemptCount; }
        public long getLockUntilTime() { return lockUntilTime; }
        public void setLockUntilTime(long lockUntilTime) { this.lockUntilTime = lockUntilTime; }
        public void incrementAttemptCount() { this.attemptCount++; }
        public void reset() {
            this.attemptCount = 0;
            this.lockUntilTime = 0;
        }
    }
}