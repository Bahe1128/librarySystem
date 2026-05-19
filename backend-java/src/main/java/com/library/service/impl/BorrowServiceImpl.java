package com.library.service.impl;

import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.User;
import com.library.mapper.BookMapper;
import com.library.mapper.BorrowRecordMapper;
import com.library.mapper.UserMapper;
import com.library.service.BorrowService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class BorrowServiceImpl implements BorrowService {

    private final BorrowRecordMapper borrowRecordMapper;
    private final BookMapper bookMapper;
    private final UserMapper userMapper;

    public BorrowServiceImpl(BorrowRecordMapper borrowRecordMapper, BookMapper bookMapper, UserMapper userMapper) {
        this.borrowRecordMapper = borrowRecordMapper;
        this.bookMapper = bookMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public BorrowRecord borrow(Long userId, Long bookId) {
        Book book = bookMapper.selectById(bookId);
        if (book == null || book.getAvailableCopies() <= 0) {
            throw new RuntimeException("图书不存在或无可用副本");
        }

        BorrowRecord record = new BorrowRecord();
        record.setUserId(userId);
        record.setBookId(bookId);
        record.setStatus("borrowed");
        record.setBorrowDate(LocalDateTime.now());
        borrowRecordMapper.insert(record);

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookMapper.updateById(book);

        return record;
    }

    @Override
    public List<BorrowRecord> findByUserId(Long userId) {
        return borrowRecordMapper.findByUserIdWithBookInfo(userId);
    }

    @Override
    public List<BorrowRecord> findAll() {
        return borrowRecordMapper.findAllWithBookInfo();
    }

    @Override
    @Transactional
    public void returnBook(Long recordId) {
        BorrowRecord record = borrowRecordMapper.selectById(recordId);
        if (record == null || !"borrowed".equals(record.getStatus())) {
            throw new RuntimeException("借阅记录不存在或已归还");
        }

        record.setStatus("returned");
        record.setReturnDate(LocalDateTime.now());
        borrowRecordMapper.updateById(record);

        Book book = bookMapper.selectById(record.getBookId());
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookMapper.updateById(book);
    }

    @Override
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();

        Long totalBooks = bookMapper.selectCount(null);
        stats.put("totalBooks", totalBooks);

        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Book> availableWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        availableWrapper.gt(Book::getAvailableCopies, 0);
        stats.put("availableBooks", bookMapper.selectCount(availableWrapper));

        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User> userCountWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        stats.put("totalUsers", userMapper.selectCount(userCountWrapper));

        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BorrowRecord> borrowedWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        borrowedWrapper.eq(BorrowRecord::getStatus, "borrowed");
        stats.put("borrowedBooks", borrowRecordMapper.selectCount(borrowedWrapper));

        return stats;
    }

    @Override
    public List<Map<String, Object>> getBorrowTrends() {
        List<BorrowRecord> allRecords = borrowRecordMapper.selectList(null);
        Map<String, Long> trendMap = new LinkedHashMap<>();

        for (BorrowRecord record : allRecords) {
            if (record.getBorrowDate() != null) {
                String month = record.getBorrowDate().toString().substring(0, 7);
                trendMap.merge(month, 1L, Long::sum);
            }
        }

        List<Map<String, Object>> trends = new ArrayList<>();
        trendMap.forEach((month, count) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("month", month);
            item.put("count", count);
            trends.add(item);
        });
        trends.sort((a, b) -> ((String) a.get("month")).compareTo((String) b.get("month")));

        return trends;
    }
}
