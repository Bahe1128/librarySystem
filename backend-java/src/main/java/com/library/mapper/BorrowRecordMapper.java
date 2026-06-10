package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.entity.BorrowRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 借阅记录表数据访问接口（Mapper）
 * <p>
 * 除了继承 BaseMapper 获得基础 CRUD 方法外，还定义了两个自定义查询方法，
 * 通过 JOIN 联查获取关联的图书名、作者和借阅用户名，方便前端直接展示。
 * SQL 中还通过 CASE WHEN 计算逾期状态（应还日期 < 当前时间 且 未归还）。
 * </p>
 */
@Mapper
public interface BorrowRecordMapper extends BaseMapper<BorrowRecord> {

    /**
     * 按用户 ID 分页查询借阅记录（联表查询图书和用户信息）
     *
     * @param page   分页对象
     * @param userId 用户 ID
     * @return 带图书名、作者、用户名及逾期状态的借阅记录分页结果
     */
    @Select("SELECT br.*, b.title, b.author, u.username, " +
            "CASE WHEN br.due_date < NOW() AND br.status = 'borrowed' THEN true ELSE false END AS overdue " +
            "FROM borrow_records br " +
            "JOIN books b ON br.book_id = b.id " +
            "JOIN users u ON br.user_id = u.id " +
            "WHERE br.user_id = #{userId} " +
            "ORDER BY br.borrow_date DESC")
    Page<BorrowRecord> findByUserIdWithBookInfo(Page<BorrowRecord> page, @Param("userId") Long userId);

    /**
     * 分页查询所有借阅记录（联表查询，管理员使用）
     *
     * @param page 分页对象
     * @return 所有用户的借阅记录分页结果（含图书和用户信息）
     */
    @Select("SELECT br.*, b.title, b.author, u.username, " +
            "CASE WHEN br.due_date < NOW() AND br.status = 'borrowed' THEN true ELSE false END AS overdue " +
            "FROM borrow_records br " +
            "JOIN books b ON br.book_id = b.id " +
            "JOIN users u ON br.user_id = u.id " +
            "ORDER BY br.borrow_date DESC")
    Page<BorrowRecord> findAllWithBookInfo(Page<BorrowRecord> page);
}
