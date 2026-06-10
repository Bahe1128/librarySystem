package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.Book;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 图书表数据访问接口（Mapper）
 * <p>
 * 继承 MyBatis Plus 的 BaseMapper&lt;Book&gt;，自动获得 CRUD 方法。
 * 额外定义了带行锁的查询方法，用于借书时防止并发超卖。
 * </p>
 */
@Mapper
public interface BookMapper extends BaseMapper<Book> {

    /**
     * 使用悲观锁查询图书（SELECT ... FOR UPDATE）
     * <p>
     * 在事务内调用时，该查询会锁住目标行直到事务提交，
     * 阻止其他事务同时读取/修改库存，防止并发借书导致超卖。
     * </p>
     *
     * @param id 图书 ID
     * @return 被锁定的图书记录，或 null（不存在）
     */
    @Select("SELECT * FROM books WHERE id = #{id} FOR UPDATE")
    Book selectByIdForUpdate(@Param("id") Long id);
}
