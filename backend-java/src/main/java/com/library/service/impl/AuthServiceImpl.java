package com.library.service.impl;

import com.library.dto.LoginResponse;
import com.library.dto.RegisterRequest;
import com.library.entity.User;
import com.library.mapper.UserMapper;
import com.library.security.JwtTokenProvider;
import com.library.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务实现类
 * <p>
 * 实现登录和注册的核心业务逻辑：
 * - 登录：验证用户名和密码，生成 JWT Token 返回
 * - 注册：检查用户名唯一性，密码 BCrypt 加密后存入数据库
 * </p>
 */
@Service // 声明为服务层 Bean，Spring 会自动扫描并管理其实例
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 构造方法注入所需的依赖
     */
    public AuthServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * 用户登录
     * <p>
     * 流程：
     * 1. 根据用户名从数据库查用户
     * 2. 用 BCrypt 比对输入的密码与数据库中的密文
     * 3. 验证通过则生成 JWT Token 并封装用户信息返回
     * </p>
     *
     * @param username 用户名
     * @param password 明文密码
     * @return 包含 JWT Token 和用户信息的 LoginResponse
     * @throws RuntimeException 用户名不存在或密码错误
     */
    @Override
    public LoginResponse login(String username, String password) {
        // LambdaQueryWrapper 构建类型安全的查询条件，避免硬编码字段名
        User user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)); // .eq 表示等于条件，User::getUsername 是 Lambda 方法引用

        // matches 方法将明文密码与 BCrypt 密文比对
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 生成 JWT Token
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());

        // 构建用户信息 Map（不包含密码，安全考虑）
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("username", user.getUsername());
        userMap.put("role", user.getRole());
        userMap.put("name", user.getName());
        userMap.put("idCard", user.getIdCard());
        userMap.put("phone", user.getPhone());
        userMap.put("email", user.getEmail());

        return new LoginResponse(token, userMap);
    }

    /**
     * 用户注册
     * <p>
     * 流程：
     * 1. 检查用户名是否已被占用
     * 2. 将密码用 BCrypt 加密后保存
     * 3. 新注册用户默认角色为 "user"
     * </p>
     *
     * @param request 注册请求对象
     * @return 注册成功的 User 对象
     * @throws RuntimeException 用户名已存在
     */
    @Override
    public User register(RegisterRequest request) {
        // 检查用户名唯一性
        User existing = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername()));

        if (existing != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 组装 User 对象
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // 密码加密存储
        user.setName(request.getName());
        user.setIdCard(request.getIdCard());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setRole("user"); // 新用户默认为普通用户角色
        userMapper.insert(user);
        return user;
    }
}
