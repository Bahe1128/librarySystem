package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.Book;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 图书表数据访问接口（Mapper）
 * <p>
 * 继承 MyBatis Plus 的 BaseMapper&lt;Book&gt;，自动获得 CRUD 方法。
 * 额外定义了带行锁的查询方法，用于借书时防止并发超卖。
 * </p>
 */
@Mapper
public interface BookMapper extends BaseMapper<Book> {
    // BaseMapper 已提供了常用 CRUD 方法：insert、deleteById、updateById、selectById、selectList 等

    /**
     * 原子扣减可用副本数
     * <p>
     * 使用条件 UPDATE 保证并发安全：仅当库存大于 0 时才扣减成功，
     * 返回值为受影响行数（0 表示无可用副本），避免并发借阅导致超借。
     * </p>
     *
     * @param bookId 图书 ID
     * @return 扣减成功的行数（0 表示扣减失败）
     */
    @Update("UPDATE books SET available_copies = available_copies - 1 " +
            "WHERE id = #{bookId} AND available_copies > 0")
    int decrementAvailableCopies(@Param("bookId") Long bookId);

    /**
     * 原子恢复可用副本数
     * <p>
     * 归还图书时使用，避免“先查询再写回”导致的并发丢失更新；
     * 同时通过 available_copies &lt; total_copies 条件防止库存被加到超过总馆藏。
     * </p>
     *
     * @param bookId 图书 ID
     * @return 更新成功的行数（0 表示图书不存在或库存已满）
     */
    @Update("UPDATE books SET available_copies = available_copies + 1 " +
            "WHERE id = #{bookId} AND available_copies < total_copies")
    int incrementAvailableCopies(@Param("bookId") Long bookId);
}
