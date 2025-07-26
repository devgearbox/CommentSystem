package com.example.lizhi.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;   // 与数据库字段对齐
    private String password;   // 明文存储（按需求）
    private String real_name;  // 补充数据库字段
    private String phone;      // 补充数据库字段
    private Integer role;      // 补充数据库字段
    private Integer status;    // 补充数据库字段
    private String gender;     // 用户性别（男/女/其他）
    private String signature;  // 个性签名

    // Getter & Setter（必须补全所有字段）
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getReal_name() { return real_name; }
    public void setReal_name(String real_name) { this.real_name = real_name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Integer getRole() { return role; }
    public void setRole(Integer role) { this.role = role; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
}