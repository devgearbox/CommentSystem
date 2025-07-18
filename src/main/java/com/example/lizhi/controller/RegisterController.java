package com.example.lizhi.controller;

import com.example.lizhi.entity.User;
import com.example.lizhi.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegisterController {
    private final UserService userService;

    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    // 访问注册页面
    @GetMapping("/register")
    public String showRegisterPage() {
        return "register"; // 对应 templates/register.html
    }

    // 处理注册提交
    @PostMapping("/register")
    public String doRegister(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            // 按需添加其他字段：real_name/phone/role/status
            @RequestParam("real_name") String realName,
            Model model
    ) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setReal_name(realName);
        // 其他字段默认值（可根据需求调整）
        user.setRole(2);    // 假设普通用户角色为2
        user.setStatus(1);  // 假设状态1为启用

        if (userService.register(user)) {
            model.addAttribute("message", "注册成功，请登录");
            return "redirect:/login"; // 跳转到登录页
        } else {
            model.addAttribute("error", "用户名已存在");
            return "register"; // 注册失败，留在注册页
        }
    }
}