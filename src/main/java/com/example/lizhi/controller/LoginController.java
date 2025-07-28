package com.example.lizhi.controller;

import com.example.lizhi.entity.User;
import com.example.lizhi.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


@Controller
public class LoginController {
    @Autowired
    private UserService userService;

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
            // 登录成功：将完整 User 对象存入 Session，key 为 "currentUser"
            session.setAttribute("currentUser", user);

            // 根据角色跳转不同页面（原有逻辑）
            if (user.getRole() == 1) {
                return "redirect:/purchasework";
            } else {
                return "redirect:/home";
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "登录失败");
            return "redirect:/login";
        }
    }
    // 示例：登录成功后跳转的主页接口
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

    @GetMapping("/settings")
    public String showSettings(HttpSession session, Model model) {
        // 1. 从 Session 获取登录用户（登录时已存入 "currentUser"）
        User user = (User) session.getAttribute("currentUser");

        // 2. 未登录拦截（可选，登录逻辑已处理可省略）
        if (user == null) {
            return "redirect:/login";
        }

        // 3. 补充默认值（数据库字段为 null 时显示）
        if (user.getPhone() == null || user.getPhone().trim().isEmpty()) {
            user.setPhone("待填写"); // 联系电话为空时显示“待填写”
        }
        if (user.getSignature() == null || user.getSignature().trim().isEmpty()) {
            user.setSignature("该用户很懒，什么都没有留下..."); // 个性签名为空时显示默认文案
        }

        // 4. 将用户数据传递到前端（Thymeleaf 通过 ${user} 渲染）
        model.addAttribute("user", user);

        return "settings"; // 返回 settings.html 并携带 user 数据
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
}