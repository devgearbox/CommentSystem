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
        // 查询用户（返回完整 User 对象，包含所有字段）
        return userRepository.findByUsername(username)
                .filter(u -> u.getPassword().equals(password))
                .orElse(null);
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
    public User saveUser(User user) {
        // 直接调用 JpaRepository 提供的 save 方法
        // 若 user.id 存在则更新，不存在则新增
        return userRepository.save(user);
    }
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        // 1. 根据 userId 查询用户（从数据库获取最新数据）
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return false; // 用户不存在
        }
        User user = userOpt.get();

        // 2. 校验原密码
        if (!user.getPassword().equals(oldPassword)) {
            return false; // 原密码错误
        }

        // 3. 更新新密码（不加密，仅演示）
        user.setPassword(newPassword);
        userRepository.save(user); // 调用 Repository 持久化
        return true;
    }
}
