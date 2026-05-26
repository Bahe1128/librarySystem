package com.library.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * MyBatis Plus 配置类
 * <p>
 * 实现两个核心功能：
 * 1. 注册分页插件 - 让 MyBatis Plus 支持物理分页查询
 * 2. 实现 MetaObjectHandler - 自动填充实体类中的公共字段（如 createdAt）
 * </p>
 */
@Configuration // 声明这是一个 Spring 配置类
public class MyBatisPlusConfig implements MetaObjectHandler {

    /**
     * 注册 MyBatis Plus 拦截器链
     * 添加分页内部拦截器，指定数据库类型为 H2
     *
     * @return MybatisPlusInterceptor 拦截器实例，Spring 会自动注入到 MyBatis 执行链中
     */
    @Bean // 该方法返回的对象将被 Spring 管理
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加分页插件，DbType.H2 表示使用 H2 数据库的分页语法
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.H2));
        return interceptor;
    }

    /**
     * 插入数据时自动填充 createdAt 字段为当前时间
     * 配合实体类上的 @TableField(fill = FieldFill.INSERT) 使用
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        // 严格模式填充：只有当目标字段为 null 时才填充
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
    }

    /**
     * 更新数据时的自动填充（当前系统中不需要额外填充逻辑）
     */
    @Override
    public void updateFill(MetaObject metaObject) {}
}
