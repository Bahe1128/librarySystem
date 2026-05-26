package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表数据访问接口（Mapper）
 * <p>
 * 继承 MyBatis Plus 的 BaseMapper<User>，自动获得用户表的全部 CRUD 方法。
 * 在服务层通过 LambdaQueryWrapper 构建类型安全的查询条件，无需手写 SQL。
 * </p>
 */
@Mapper // MyBatis Mapper 标识，Spring 启动时会扫描并生成实现
public interface UserMapper extends BaseMapper<User> {
    // BaseMapper 提供的常用方法示例：
    // selectOne(Wrapper) - 查询单条，selectList(Wrapper) - 查询多条
    // insert(User) - 插入，updateById(User) - 按 ID 更新，deleteById(Long) - 按 ID 删除
}
