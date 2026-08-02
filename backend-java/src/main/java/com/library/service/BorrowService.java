package com.library.service;

import com.library.common.PageResult;
import com.library.entity.BorrowRecord;

import java.util.List;
import java.util.Map;

/**
 * 借阅服务接口
 * <p>
 * 定义借阅和归还相关的业务契约，还包括系统统计和趋势分析功能。
 * </p>
 */
public interface BorrowService {
    /** 借阅图书（用户借书） */
    BorrowRecord borrow(Long userId, Long bookId);
    /** 按用户 ID 分页查询借阅记录（普通用户使用） */
    PageResult<BorrowRecord> findByUserId(Long userId, int page, int pageSize);
    /** 分页查询所有借阅记录（管理员使用） */
    PageResult<BorrowRecord> findAll(int page, int pageSize);
    /** 归还图书（校验操作者身份：管理员可归还任意记录，普通用户只能归还自己的） */
    void returnBook(Long operatorId, boolean isAdmin, Long recordId);
    /** 获取系统统计数据（图书总数、用户总数、借阅总数等） */
    Map<String, Object> getSystemStats();
    /** 获取借阅趋势数据（按月统计的借阅量） */
    List<Map<String, Object>> getBorrowTrends();
}
