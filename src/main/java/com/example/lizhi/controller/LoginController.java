package com.example.lizhi.controller;

import com.example.lizhi.entity.User;
import com.example.lizhi.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


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
            session.setAttribute("username", user.getUsername());

            // 采购人员重定向到 /work
            if (user.getRole() == 1) {
                return "redirect:/purchasework"; // 采购人员跳转
            } else {
                return "redirect:/home"; // 其他角色跳转
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
}