package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.Book;
import org.apache.ibatis.annotations.Mapper;

/**
 * 图书表数据访问接口（Mapper）
 * <p>
 * 继承 MyBatis Plus 的 BaseMapper<Book>，自动获得 CRUD 方法（增删改查），
 * 无需编写任何 SQL 或 XML 映射文件。
 * 如需自定义 SQL，只需在接口中新增方法并使用 @Select/@Update 等注解。
 * </p>
 */
@Mapper // 告诉 MyBatis 这是一个 Mapper 接口，Spring 会自动生成代理实现类
public interface BookMapper extends BaseMapper<Book> {
    // BaseMapper 已提供了常用 CRUD 方法：insert、deleteById、updateById、selectById、selectList 等
    // 如无额外需求，接口体可以为空
}
