package com.library.service;

import com.library.entity.BorrowRecord;
import java.util.List;
import java.util.Map;

public interface BorrowService {
    BorrowRecord borrow(Long userId, Long bookId);
    List<BorrowRecord> findByUserId(Long userId);
    List<BorrowRecord> findAll();
    void returnBook(Long recordId);
    Map<String, Object> getSystemStats();
    List<Map<String, Object>> getBorrowTrends();
}
