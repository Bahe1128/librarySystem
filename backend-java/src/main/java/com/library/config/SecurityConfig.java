package com.library.config;

import com.library.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security 安全配置类
 * <p>
 * 核心配置职责：
 * 1. 定义 URL 访问权限规则（哪些接口需要登录、哪些需要管理员角色）
 * 2. 配置 JWT 过滤器，在每次请求时校验 Token
 * 3. 禁用 Session（使用无状态 JWT 认证）
 * 4. 配置 CORS 跨域，允许前端开发服务器访问
 * 5. 注册密码加密器（BCrypt）和认证管理器
 * </p>
 */
@Configuration // 声明为 Spring 配置类
@EnableWebSecurity // 启用 Spring Security 的 Web 安全功能
public class SecurityConfig {

    /** JWT 认证过滤器，在请求到达 Controller 之前校验 Token */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 构造方法注入 JWT 过滤器
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * 配置安全过滤链 - 定义请求拦截规则
     * 这是 Spring Security 的核心配置，决定了哪些请求可以匿名访问、哪些需要登录
     *
     * @param http Spring Security 的 HTTP 安全构建器
     * @return 构建完成的过滤链
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 启用 CORS 跨域支持（使用下方 corsConfigurationSource 的配置）
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // 禁用 CSRF 保护（前后端分离 + JWT 模式下无需 CSRF）
            .csrf(csrf -> csrf.disable())
            // 设置为无状态会话（不使用 Session，每次请求都通过 JWT 认证）
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 配置 URL 访问权限
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()    // 登录、注册接口：无需认证
                .requestMatchers(HttpMethod.GET, "/api/books/**").permitAll() // 图书查询接口：公开可读
                .requestMatchers("/api/books/**").hasRole("admin") // 图书增删改：仅管理员
                .requestMatchers("/h2-console/**").hasRole("admin") // H2 控制台：仅管理员（生产环境建议关闭）
                .requestMatchers("/api/borrow/**").authenticated() // 借阅接口：需要登录
                .requestMatchers("/api/stats/**").authenticated()  // 统计接口：需要登录
                .requestMatchers("/api/users/**").hasRole("admin") // 用户管理：仅管理员可访问
                .anyRequest().authenticated() // 其他所有请求都要认证
            )
            // H2 控制台使用 iframe 呈现界面，仅允许同源页面嵌入（比完全禁用更安全）
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            // 在 UsernamePasswordAuthenticationFilter 之前插入 JWT 过滤器
            // 这样每次请求都会先解析 JWT，然后将用户信息设置到 SecurityContext
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 密码编码器 Bean
     * 使用 BCrypt 哈希算法加密密码，保证即使数据库泄露也无法还原明文密码
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器 Bean
     * 用于在登录时验证用户名和密码是否正确
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * CORS 跨域配置
     * 允许前端开发服务器（Vite 默认端口 5173/5174）跨域访问后端接口
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许的前端源地址
        config.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://localhost:5174"));
        // 允许的 HTTP 方法
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 允许的请求头
        config.setAllowedHeaders(Arrays.asList("*"));
        // 允许携带认证信息（Cookie、Authorization header 等）
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 对所有路径生效
        return source;
    }
}
