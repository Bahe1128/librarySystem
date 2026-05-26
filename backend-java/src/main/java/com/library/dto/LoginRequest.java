package com.library.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求 DTO（Data Transfer Object）
 * <p>
 * 接收前端登录表单提交的用户名和密码。
 * </p>
 */
public class LoginRequest {
    /** 用户名，不能为空字符串 */
    @NotBlank // 校验注解：不允许为 null、空字符串或纯空格
    private String username;
    /** 密码，不能为空字符串 */
    @NotBlank
    private String password;

    // ========== Getter / Setter ==========
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
