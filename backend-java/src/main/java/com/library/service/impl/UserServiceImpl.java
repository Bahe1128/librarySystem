package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.entity.User;
import com.library.mapper.UserMapper;
import com.library.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<User> findAll() {
        List<User> users = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .ne(User::getRole, "admin")
                        .orderByDesc(User::getCreatedAt));
        users.forEach(u -> u.setPassword(null));
        return users;
    }

    @Override
    public User findById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) throw new RuntimeException("用户不存在");
        user.setPassword(null);
        return user;
    }

    @Override
    public User create(User user) {
        User existing = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername()));
        if (existing != null) throw new RuntimeException("用户名已存在");
        if (user.getPassword() == null || user.getPassword().isEmpty())
            throw new RuntimeException("密码不能为空");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("user");
        userMapper.insert(user);
        user.setPassword(null);
        return user;
    }

    @Override
    public User update(Long id, User user) {
        User dbUser = userMapper.selectById(id);
        if (dbUser == null) throw new RuntimeException("用户不存在");
        if (!dbUser.getUsername().equals(user.getUsername())) {
            User existing = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername()));
            if (existing != null) throw new RuntimeException("用户名已存在");
        }
        dbUser.setUsername(user.getUsername());
        if (StringUtils.hasText(user.getPassword())) {
            dbUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        dbUser.setName(user.getName());
        dbUser.setIdCard(user.getIdCard());
        dbUser.setPhone(user.getPhone());
        dbUser.setEmail(user.getEmail());
        userMapper.updateById(dbUser);
        dbUser.setPassword(null);
        return dbUser;
    }

    @Override
    public void delete(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) throw new RuntimeException("用户不存在");
        if ("admin".equals(user.getRole())) throw new RuntimeException("无法删除管理员账号");
        userMapper.deleteById(id);
    }
}
