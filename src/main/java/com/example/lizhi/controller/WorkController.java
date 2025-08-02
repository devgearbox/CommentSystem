package com.example.lizhi.controller;

import com.example.lizhi.entity.User;
import com.example.lizhi.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WorkController {
    @Autowired
    private UserService userService;

    @GetMapping("/work")
    public String workPage(Model model, HttpSession session) {
        // 模拟从Session获取当前登录用户（实际需结合登录逻辑）
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "work";
    }
}
