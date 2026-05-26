package com.library.service;

import com.library.dto.LoginResponse;
import com.library.dto.RegisterRequest;
import com.library.entity.User;

/**
 * 认证服务接口
 * <p>
 * 定义登录和注册的业务契约，由 AuthServiceImpl 实现。
 * 接口设计遵循"面向接口编程"原则，Controller 只依赖接口，不依赖具体实现。
 * </p>
 */
public interface AuthService {
    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 明文密码
     * @return 包含 JWT Token 和用户信息的登录响应对象
     * @throws RuntimeException 当用户名不存在或密码错误时抛出
     */
    LoginResponse login(String username, String password);

    /**
     * 用户注册
     *
     * @param request 注册请求对象（包含用户名、密码、个人信息）
     * @return 注册成功后的 User 对象
     * @throws RuntimeException 当用户名已存在时抛出
     */
    User register(RegisterRequest request);
}
