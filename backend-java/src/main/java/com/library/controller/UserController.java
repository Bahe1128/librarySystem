package com.library.controller;

import com.library.common.Result;
import com.library.entity.User;
import com.library.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 * <p>
 * 提供用户的增删改查接口。
 * 所有 /api/users 接口需要 ADMIN 角色才能访问（由 SecurityConfig 控制）。
 * </p>
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    /** 用户服务（封装用户管理的业务逻辑） */
    private final UserService userService;

    /**
     * 构造方法注入 UserService
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取所有用户列表
     * GET /api/users
     *
     * @return 所有用户的列表
     */
    @GetMapping
    public Result<List<User>> getAll() {
        return Result.success(userService.findAll());
    }

    /**
     * 根据 ID 查询单个用户
     * GET /api/users/{id}
     *
     * @param id 用户 ID
     * @return 用户详情
     */
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        return Result.success(userService.findById(id));
    }

    /**
     * 新增用户
     * POST /api/users
     *
     * @param user 用户信息（JSON 请求体）
     * @return 保存后的用户对象
     */
    @PostMapping
    public Result<User> create(@RequestBody User user) {
        try {
            return Result.success(userService.create(user));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新用户信息
     * PUT /api/users/{id}
     *
     * @param id   用户 ID
     * @param user 新的用户信息
     * @return 更新后的用户对象
     */
    @PutMapping("/{id}")
    public Result<User> update(@PathVariable Long id, @RequestBody User user) {
        try {
            return Result.success(userService.update(id, user));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除用户
     * DELETE /api/users/{id}
     *
     * @param id 要删除的用户 ID
     * @return 删除成功消息
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        try {
            userService.delete(id);
            return Result.success("删除成功", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
