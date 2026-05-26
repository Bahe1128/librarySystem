package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * <p>
 * 对应数据库中的 users 表，存储系统中所有用户的信息（包括管理员和普通用户）。
 * 密码字段存储的是 BCrypt 加密后的密文，不会保存明文密码。
 * </p>
 */
@TableName("users")
public class User {
    /** 用户 ID，主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 登录用户名 */
    private String username;
    /** 登录密码（BCrypt 加密后的密文） */
    private String password;
    /** 真实姓名 */
    private String name;
    /** 身份证号码 */
    private String idCard;
    /** 手机号码 */
    private String phone;
    /** 电子邮箱 */
    private String email;
    /** 角色：admin（管理员）或 user（普通用户） */
    private String role;
    /** 创建时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    // ========== Getter / Setter ==========
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIdCard() { return idCard; }
    public void setIdCard(String idCard) { this.idCard = idCard; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
