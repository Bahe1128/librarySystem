package com.library.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 认证过滤器
 * <p>
 * 继承 OncePerRequestFilter，确保每个请求只被该过滤器处理一次。
 * 在 SecurityConfig 中，此过滤器被插入到 UsernamePasswordAuthenticationFilter 之前，
 * 因此每次请求到达 Controller 前，都会先经过此过滤器的处理。
 * </p>
 * <p>
 * 核心逻辑：从请求头 Authorization 中提取 Bearer Token，校验有效性后，
 * 将用户信息封装成 Authentication 对象并存入 SecurityContext，
 * 后续 Controller 可以通过 @AuthenticationPrincipal 直接获取当前用户。
 * </p>
 */
@Component // 注册为 Spring Bean，以便 SecurityConfig 注入使用
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** JWT 令牌提供者，用于校验和解析 Token */
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 构造方法注入 JwtTokenProvider
     */
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * 过滤器的核心方法，每个 HTTP 请求都会经过此方法
     *
     * @param request     当前 HTTP 请求
     * @param response    HTTP 响应
     * @param filterChain 过滤器链（处理完成后需要调用 doFilter 传递到下一个过滤器）
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 1. 从请求头中提取 JWT Token
        String token = getTokenFromRequest(request);

        // 2. 如果 Token 存在且有效，则解析用户信息并设置认证状态
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            // 从 Token 中解析用户信息
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            String username = jwtTokenProvider.getUsernameFromToken(token);
            String role = jwtTokenProvider.getRoleFromToken(token);

            // 封装为用户主体对象
            UserPrincipal userPrincipal = new UserPrincipal(userId, username, role);

            // 创建 Spring Security 认证令牌
            // 参数：用户主体、凭证(null，因为 JWT 已经完成了认证)、权限列表
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userPrincipal, null, userPrincipal.getAuthorities());
            // 设置认证详情（包含 IP、Session ID 等）
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 将认证信息存入 SecurityContext（线程绑定的安全上下文）
            // 之后的代码就可以通过 SecurityContextHolder 获取当前用户信息
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 3. 无论是否认证成功，都继续执行过滤器链中的下一个过滤器
        filterChain.doFilter(request, response);
    }

    /**
     * 从 HTTP 请求的 Authorization 头中提取 JWT Token
     * 格式：Authorization: Bearer <token>
     *
     * @param request HTTP 请求
     * @return JWT 令牌字符串；如果没有或格式不对则返回 null
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization"); // 获取认证头
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // 去掉 "Bearer " 前缀（7 个字符）
        }
        return null;
    }
}
