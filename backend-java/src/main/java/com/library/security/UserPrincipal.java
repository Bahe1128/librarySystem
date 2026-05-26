package com.library.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * 用户主体类（实现 Spring Security 的 UserDetails 接口）
 * <p>
 * 这是当前登录用户在 Spring Security 上下文中的"身份证"。
 * JWT 过滤器解析 Token 后，会将用户信息封装为 UserPrincipal 对象存入 SecurityContext。
 * Controller 中通过 @AuthenticationPrincipal 注解可以直接获取此对象。
 * </p>
 * <p>
 * 实现了 UserDetails 接口，这样 Spring Security 就能识别它作为认证用户。
 * 因为使用 JWT 无状态认证，所以 password 为 null，账户状态相关方法均返回 true。
 * </p>
 */
public class UserPrincipal implements UserDetails {

    /** 用户 ID */
    private Long id;
    /** 用户名 */
    private String username;
    /** 用户角色（admin 或 user） */
    private String role;

    /**
     * 构造方法
     *
     * @param id       用户 ID
     * @param username 用户名
     * @param role     用户角色
     */
    public UserPrincipal(Long id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    /** 获取用户 ID */
    public Long getId() { return id; }
    /** 获取用户角色 */
    public String getRole() { return role; }

    /**
     * 返回用户的权限列表
     * Spring Security 使用此方法的返回值进行角色判断（如 hasRole("admin")）
     * 注意：角色会自动加上 "ROLE_" 前缀，这是 Spring Security 的约定
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // SimpleGrantedAuthority 是 Spring Security 的权限对象
        // "ROLE_" + role 的结果为 "ROLE_admin" 或 "ROLE_user"
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }

    /** JWT 模式下不需要密码，返回 null */
    @Override
    public String getPassword() { return null; }

    /** 返回用户名 */
    @Override
    public String getUsername() { return username; }

    // 以下四个方法均返回 true，表示不启用账户过期、锁定等机制
    // 如需更精细的账户控制，可改为从数据库读取对应状态

    /** 账户是否未过期 */
    @Override
    public boolean isAccountNonExpired() { return true; }

    /** 账户是否未锁定 */
    @Override
    public boolean isAccountNonLocked() { return true; }

    /** 凭证（密码）是否未过期 */
    @Override
    public boolean isCredentialsNonExpired() { return true; }

    /** 账户是否启用 */
    @Override
    public boolean isEnabled() { return true; }
}
