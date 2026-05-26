package com.library.service;

import com.library.entity.User;
import java.util.List;

/**
 * 用户服务接口
 * <p>
 * 定义用户管理的业务契约，包含增删改查操作。
 * 仅管理员角色可访问（由 SecurityConfig 控制）。
 * </p>
 */
public interface UserService {
    /** 查询所有用户 */
    List<User> findAll();
    /** 根据 ID 查询单个用户 */
    User findById(Long id);
    /** 新增用户（密码会被 BCrypt 加密） */
    User create(User user);
    /** 更新用户信息 */
    User update(Long id, User user);
    /** 删除用户 */
    void delete(Long id);
}
