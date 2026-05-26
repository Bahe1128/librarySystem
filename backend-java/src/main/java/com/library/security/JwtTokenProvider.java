package com.library.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 令牌提供者
 * <p>
 * 负责 JWT 令牌的生成、解析和校验。
 * 密钥和过期时间从 application.properties 配置文件中读取（通过 @Value 注入）。
 * </p>
 * <p>
 * JWT（JSON Web Token）是一种无状态的认证机制：
 * - 用户登录成功后，服务端生成一个 JWT 返回给客户端
 * - 客户端在后续请求中携带此 Token（放在 Authorization 头中）
 * - 服务端只需验证 Token 的签名即可确认用户身份，无需查数据库
 * - Token 中包含用户 ID、用户名、角色等基本信息
 * </p>
 */
@Component // 注册为 Spring Bean
public class JwtTokenProvider {

    /** 用于签名和验证 JWT 的密钥（HMAC-SHA 算法） */
    private final SecretKey key;
    /** Token 的过期时间（毫秒） */
    private final long expiration;

    /**
     * 构造方法，从配置文件注入 JWT 参数
     *
     * @param secret     JWT 签名密钥（application.properties 中的 jwt.secret）
     * @param expiration JWT 过期时间（application.properties 中的 jwt.expiration，单位毫秒）
     */
    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,       // @Value 从配置文件读取值
            @Value("${jwt.expiration}") long expiration) {
        // 将字符串密钥转换为 HMAC-SHA 算法的密钥对象
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    /**
     * 生成 JWT Token
     *
     * @param userId   用户 ID（存储为 Token 的 subject 主题字段）
     * @param username 用户名（存储为自定义声明 claim）
     * @param role     用户角色（存储为自定义声明 claim，用于权限控制）
     * @return 生成的 JWT 字符串
     */
    public String generateToken(Long userId, String username, String role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId.toString())             // 主题（subject）：存储用户 ID
                .claim("username", username)            // 自定义声明：用户名
                .claim("role", role)                    // 自定义声明：用户角色
                .issuedAt(now)                          // 签发时间
                .expiration(new Date(now.getTime() + expiration)) // 过期时间
                .signWith(key)                          // 使用密钥签名（防篡改）
                .compact();                             // 构建为字符串
    }

    /**
     * 从 JWT Token 中提取用户 ID
     *
     * @param token JWT 字符串
     * @return 用户 ID
     */
    public Long getUserIdFromToken(String token) {
        return Long.parseLong(getClaims(token).getSubject()); // subject 字段存的是 userId
    }

    /**
     * 从 JWT Token 中提取用户名
     *
     * @param token JWT 字符串
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaims(token).get("username", String.class);
    }

    /**
     * 从 JWT Token 中提取用户角色
     *
     * @param token JWT 字符串
     * @return 角色名（admin 或 user）
     */
    public String getRoleFromToken(String token) {
        return getClaims(token).get("role", String.class);
    }

    /**
     * 验证 JWT Token 是否有效
     * 如果 Token 被篡改、格式不对或已过期，则会抛出异常，返回 false
     *
     * @param token JWT 字符串
     * @return true 表示有效，false 表示无效
     */
    public boolean validateToken(String token) {
        try {
            getClaims(token); // 尝试解析，如果失败则说明无效
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 解析 JWT Token 并获取其中的 Claims（负载数据）
     *
     * @param token JWT 字符串
     * @return JWT Claims 对象，包含所有存储的数据
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)        // 使用密钥验证签名
                .build()
                .parseSignedClaims(token) // 解析签名后的 Token
                .getPayload();           // 获取负载（Claims）
    }
}
