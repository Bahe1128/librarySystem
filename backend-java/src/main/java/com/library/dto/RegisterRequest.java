package com.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 注册请求 DTO（Data Transfer Object）
 * <p>
 * 接收前端注册表单提交的用户注册信息。
 * username 和 password 为必填项，且有长度限制；
 * 姓名、身份证号、手机号、邮箱为选填项。
 * </p>
 */
public class RegisterRequest {
    /** 用户名：必填，3~50 字符 */
    @NotBlank
    @Size(min = 3, max = 50)
    private String username;
    /** 密码：必填，6~100 字符 */
    @NotBlank
    @Size(min = 6, max = 100)
    private String password;
    /** 真实姓名（选填） */
    private String name;
    /** 身份证号（选填） */
    private String idCard;
    /** 手机号（选填） */
    private String phone;
    /** 邮箱地址（选填） */
    private String email;

    // ========== Getter / Setter ==========
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
}
