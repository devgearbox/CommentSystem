package com.example.lizhi.controller;

import com.example.lizhi.entity.User;
import com.example.lizhi.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
// 处理注册提交
    @PostMapping("/register")
    public String doRegister(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("real_name") String realName,
            @RequestParam("phone") String phone,
            @RequestParam("gender") String gender,
            @RequestParam("role") Integer role,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setReal_name(realName);
        user.setPhone(phone);
        user.setGender(gender);
        user.setRole(role);
        user.setStatus(1);

        if (userService.register(user)) {
            redirectAttributes.addFlashAttribute("success", "用户注册成功");
            return "redirect:/login";
        } else {
            model.addAttribute("error", "用户名已存在");
            return "register";
        }
    }
}