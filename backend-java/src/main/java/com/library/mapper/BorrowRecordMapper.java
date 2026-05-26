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
 * </p>
 */
@Mapper
public interface BorrowRecordMapper extends BaseMapper<BorrowRecord> {

    /**
     * 按用户 ID 分页查询借阅记录（联表查询图书和用户信息）
     * <p>
     * SQL 解析：br.* 取借阅记录全部字段；b.title, b.author 来自 books 表；
     * u.username 来自 users 表。这些字段映射到 BorrowRecord 的虚拟字段上。
     * </p>
     *
     * @param page   分页对象（MyBatis Plus 自动处理 LIMIT/OFFSET）
     * @param userId 用户 ID
     * @return 带图书名、作者、用户名的借阅记录分页结果
     */
    @Select("SELECT br.*, b.title, b.author, u.username " +
            "FROM borrow_records br " +
            "JOIN books b ON br.book_id = b.id " +    // 关联图书表
            "JOIN users u ON br.user_id = u.id " +    // 关联用户表
            "WHERE br.user_id = #{userId} " +         // 按用户 ID 过滤
            "ORDER BY br.borrow_date DESC")           // 按借阅日期降序排列
    Page<BorrowRecord> findByUserIdWithBookInfo(Page<BorrowRecord> page, @Param("userId") Long userId);

    /**
     * 分页查询所有借阅记录（联表查询，管理员使用）
     *
     * @param page 分页对象
     * @return 所有用户的借阅记录分页结果（含图书和用户信息）
     */
    @Select("SELECT br.*, b.title, b.author, u.username " +
            "FROM borrow_records br " +
            "JOIN books b ON br.book_id = b.id " +
            "JOIN users u ON br.user_id = u.id " +
            "ORDER BY br.borrow_date DESC")
    Page<BorrowRecord> findAllWithBookInfo(Page<BorrowRecord> page);
}
