package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.entity.User;
import com.library.mapper.UserMapper;
import com.library.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 用户服务实现类
 * <p>
 * 实现用户管理的增删改查业务逻辑。
 * 安全性措施：
 * 1. 查询用户时自动清除密码字段，避免敏感信息泄露到前端
 * 2. 创建和更新用户时，密码通过 BCrypt 加密后存储
 * 3. 不允许删除管理员账号
 * </p>
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 构造方法注入
     */
    public UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 查询所有非管理员用户
     * 按创建时间降序排列，且清除密码字段
     *
     * @return 用户列表（不含管理员，不含密码）
     */
    @Override
    public List<User> findAll() {
        List<User> users = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .ne(User::getRole, "admin")        // ne = not equal，排除管理员
                        .orderByDesc(User::getCreatedAt));  // 按创建时间降序
        // 清除密码字段，避免敏感信息泄露
        users.forEach(u -> u.setPassword(null));
        return users;
    }

    /**
     * 根据 ID 查询用户
     *
     * @param id 用户 ID
     * @return 用户对象（密码字段已清除）
     * @throws RuntimeException 用户不存在
     */
    @Override
    public User findById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) throw new RuntimeException("用户不存在");
        user.setPassword(null);
        return user;
    }

    /**
     * 新增用户
     * 检查用户名唯一性，密码加密，默认角色为 user
     *
     * @param user 用户信息
     * @return 新创建的 User 对象（密码字段已清除）
     * @throws RuntimeException 用户名已存在或密码为空
     */
    @Override
    public User create(User user) {
        // 检查用户名是否已被占用
        User existing = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername()));
        if (existing != null) throw new RuntimeException("用户名已存在");
        // 密码不能为空
        if (user.getPassword() == null || user.getPassword().isEmpty())
            throw new RuntimeException("密码不能为空");

        user.setPassword(passwordEncoder.encode(user.getPassword())); // 加密密码
        user.setRole("user"); // 默认角色为普通用户
        userMapper.insert(user);
        user.setPassword(null); // 返回前清除密码
        return user;
    }

    /**
     * 更新用户信息
     * <p>
     * 如果前端没有传密码（为空），则保留原密码；
     * 如果传了新密码，则加密后替换。
     * 如果修改了用户名，需要检查新用户名是否已被占用。
     * </p>
     *
     * @param id   用户 ID
     * @param user 新的用户信息
     * @return 更新后的用户对象
     * @throws RuntimeException 用户不存在或用户名已被占用
     */
    @Override
    public User update(Long id, User user) {
        User dbUser = userMapper.selectById(id);
        if (dbUser == null) throw new RuntimeException("用户不存在");

        // 如果用户名有变化，检查新用户名是否已被占用
        if (!dbUser.getUsername().equals(user.getUsername())) {
            User existing = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername()));
            if (existing != null) throw new RuntimeException("用户名已存在");
        }

        // 更新基本字段
        dbUser.setUsername(user.getUsername());
        // 只有密码不为空时才更新密码（允许前端不传密码，保持原密码不变）
        if (StringUtils.hasText(user.getPassword())) {
            dbUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        dbUser.setName(user.getName());
        dbUser.setIdCard(user.getIdCard());
        dbUser.setPhone(user.getPhone());
        dbUser.setEmail(user.getEmail());
        userMapper.updateById(dbUser);
        dbUser.setPassword(null); // 返回前清除密码
        return dbUser;
    }

    /**
     * 删除用户
     * 不允许删除管理员账号（保护系统安全）
     *
     * @param id 用户 ID
     * @throws RuntimeException 用户不存在或尝试删除管理员
     */
    @Override
    public void delete(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) throw new RuntimeException("用户不存在");
        if ("admin".equals(user.getRole())) throw new RuntimeException("无法删除管理员账号");
        userMapper.deleteById(id);
    }
}
