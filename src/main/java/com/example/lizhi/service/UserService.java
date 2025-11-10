package com.example.lizhi.service;

import com.example.lizhi.entity.User;
import com.example.lizhi.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // 密码编码器

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder(); // 使用BCrypt加密算法
    }

    public User loginWithRole(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // 检查密码是否已加密（BCrypt加密的密码以$2a$开头）
            if (user.getPassword().startsWith("$2a$")) {
                // 密码已加密，使用加密验证
                if (passwordEncoder.matches(password, user.getPassword())) {
                    return user;
                }
            } else {
                // 密码是明文的，直接比较（兼容旧数据）
                if (user.getPassword().equals(password)) {
                    // 登录成功时自动加密密码并更新
                    user.setPassword(passwordEncoder.encode(password));
                    userRepository.save(user);
                    return user;
                }
            }
        }
        return null;
    }

    // 修改注册逻辑 - 自动加密密码
    public boolean register(User user) {
        // 校验用户名是否已存在
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return false; // 用户名已存在，注册失败
        }
        // 加密密码后再存储
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return true;
    }

    // 修改密码修改逻辑 - 使用加密验证和存储
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        // 1. 根据 userId 查询用户（从数据库获取最新数据）
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return false; // 用户不存在
        }
        User user = userOpt.get();

        // 2. 校验原密码（支持明文和加密密码）
        boolean oldPasswordCorrect;
        if (user.getPassword().startsWith("$2a$")) {
            // 密码已加密
            oldPasswordCorrect = passwordEncoder.matches(oldPassword, user.getPassword());
        } else {
            // 密码是明文的
            oldPasswordCorrect = user.getPassword().equals(oldPassword);
        }

        if (!oldPasswordCorrect) {
            return false; // 原密码错误
        }

        // 3. 更新新密码（加密存储）
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user); // 调用 Repository 持久化
        return true;
    }
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    //手机号登录功能
    public User findByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }
}
