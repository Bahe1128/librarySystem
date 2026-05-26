package com.library.dto;

import java.util.Map;

/**
 * 登录响应 DTO（Data Transfer Object）
 * <p>
 * 封装登录成功后返回给前端的数据：
 * 1. JWT Token 字符串（前端存储后用于后续请求认证）
 * 2. 用户基本信息 Map（包含 id、username、name、role 等）
 * </p>
 */
public class LoginResponse {
    /** JWT 令牌字符串，前端需要保存在浏览器中用于后续请求 */
    private String token;
    /** 当前登录用户的基本信息（id、username、name、role 等） */
    private Map<String, Object> user;

    /**
     * 登录响应构造方法
     *
     * @param token JWT 令牌
     * @param user  用户信息 Map
     */
    public LoginResponse(String token, Map<String, Object> user) {
        this.token = token;
        this.user = user;
    }

    // ========== Getter / Setter ==========
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Map<String, Object> getUser() { return user; }
    public void setUser(Map<String, Object> user) { this.user = user; }
}
