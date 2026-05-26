package com.library.controller;

import com.library.common.Result;
import com.library.dto.LoginRequest;
import com.library.dto.LoginResponse;
import com.library.dto.RegisterRequest;
import com.library.entity.User;
import com.library.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * <p>
 * 处理用户登录和注册请求。
 * 所有以 /api/auth 开头的请求都被 SecurityConfig 设置为无需认证即可访问。
 * </p>
 */
@RestController // 标记为 REST 控制器，方法返回值会自动转为 JSON
@RequestMapping("/api/auth") // 所有接口路径的前缀
public class AuthController {

    /** 认证服务（包含登录和注册的业务逻辑） */
    private final AuthService authService;

    /**
     * 构造方法注入 AuthService
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 用户登录接口
     * POST /api/auth/login
     *
     * @param request 包含 username 和 password 的登录请求体，使用 @Valid 进行参数校验
     * @return 成功时返回 JWT Token 和用户信息，失败时返回 401 错误
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request.getUsername(), request.getPassword());
            return Result.success(response);
        } catch (RuntimeException e) {
            return Result.error(401, e.getMessage()); // 登录失败返回 401 Unauthorized
        }
    }

    /**
     * 用户注册接口
     * POST /api/auth/register
     *
     * @param request 包含用户名、密码、个人信息等的注册请求体
     * @return 成功时返回新用户的 ID 和用户名
     */
    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = authService.register(request);
            // 使用 Map.of 构建简洁的返回数据（Java 9+ 语法）
            return Result.success("注册成功", java.util.Map.of("id", user.getId(), "username", user.getUsername()));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
