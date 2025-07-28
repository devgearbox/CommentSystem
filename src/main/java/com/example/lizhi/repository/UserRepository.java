package com.example.lizhi.repository;

import com.example.lizhi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // 优化：返回 Optional<User>，避免空指针，兼容登录校验
    Optional<User> findByUsername(String username);
    User findByPhone(String phone);
}
