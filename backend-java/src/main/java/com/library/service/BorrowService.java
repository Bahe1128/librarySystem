package com.library.service;

import com.library.common.PageResult;
import com.library.entity.BorrowRecord;

import java.util.List;
import java.util.Map;

public interface BorrowService {
    BorrowRecord borrow(Long userId, Long bookId);
    PageResult<BorrowRecord> findByUserId(Long userId, int page, int pageSize);
    PageResult<BorrowRecord> findAll(int page, int pageSize);
    void returnBook(Long recordId);
    Map<String, Object> getSystemStats();
    List<Map<String, Object>> getBorrowTrends();
}
