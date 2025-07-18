package com.example.lizhi.service;

import com.example.lizhi.entity.User;
import com.example.lizhi.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 原有登录逻辑（保持不变）
    public User loginWithRole(String username, String password) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null && user.getPassword().equals(password)) {
            return user; // 返回完整的 User 对象
        }
        return null;
    }

    // 新增注册逻辑
    public boolean register(User user) {
        // 校验用户名是否已存在
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return false; // 用户名已存在，注册失败
        }
        // 明文存储密码（按需求）
        userRepository.save(user);
        return true;
    }
}
