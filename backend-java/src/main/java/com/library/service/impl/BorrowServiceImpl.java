package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.PageResult;
import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.User;
import com.library.mapper.BookMapper;
import com.library.mapper.BorrowRecordMapper;
import com.library.mapper.UserMapper;
import com.library.service.BorrowService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 借阅服务实现类
 * <p>
 * 实现借阅和归还的核心业务逻辑，包含以下安全/业务保障：
 * <ul>
 *   <li><b>悲观行锁</b>：借书时 SELECT ... FOR UPDATE 锁定图书记录，防止并发超卖</li>
 *   <li><b>借阅上限</b>：每位用户最多同时借阅指定册数（可配置）</li>
 *   <li><b>应还日期</b>：借书时自动计算应还日期，逾期未还将标记为逾期</li>
 * </ul>
 * 借阅和归还操作均使用 @Transactional 保证数据一致性。
 * </p>
 */
@Service
public class BorrowServiceImpl implements BorrowService {

    /** 借阅期限（天数），从 application.yml 注入 */
    @Value("${library.borrow-duration-days}")
    private int borrowDurationDays;

    /** 每位用户最多同时借阅册数 */
    @Value("${library.max-borrow-count}")
    private int maxBorrowCount;

    private final BorrowRecordMapper borrowRecordMapper;
    private final BookMapper bookMapper;
    private final UserMapper userMapper;

    public BorrowServiceImpl(BorrowRecordMapper borrowRecordMapper, BookMapper bookMapper, UserMapper userMapper) {
        this.borrowRecordMapper = borrowRecordMapper;
        this.bookMapper = bookMapper;
        this.userMapper = userMapper;
    }

    /**
     * 借阅图书
     * <p>
     * 执行流程：① 检查用户借阅数量上限 → ② 悲观锁锁定图书记录 →
     * ③ 校验库存 → ④ 创建借阅记录（含应还日期） → ⑤ 扣减库存。
     * 行锁在事务提交后自动释放，阻塞并发请求，防止超卖。
     * </p>
     *
     * @param userId 借阅用户 ID
     * @param bookId 图书 ID
     * @return 创建的借阅记录
     * @throws RuntimeException 超过借阅上限 / 图书不存在 / 无可用副本
     */
    @Override
    @Transactional
    public BorrowRecord borrow(Long userId, Long bookId) {
        // 检查图书是否存在
        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new RuntimeException("图书不存在或无可用副本");
        }

        // 原子扣减库存：仅当有可用副本时才成功，防止并发借阅时超借
        int affected = bookMapper.decrementAvailableCopies(bookId);
        if (affected == 0) {
            throw new RuntimeException("图书不存在或无可用副本");
        }

        // ③ 创建借阅记录，自动计算应还日期
        BorrowRecord record = new BorrowRecord();
        record.setUserId(userId);
        record.setBookId(bookId);
        record.setStatus("borrowed");
        record.setBorrowDate(LocalDateTime.now());
        record.setDueDate(LocalDateTime.now().plusDays(borrowDurationDays)); // 应还日期 = 借阅日期 + 借阅期限
        borrowRecordMapper.insert(record);

        return record;
    }

    @Override
    public PageResult<BorrowRecord> findByUserId(Long userId, int page, int pageSize) {
        Page<BorrowRecord> p = new Page<>(page, pageSize);
        Page<BorrowRecord> result = borrowRecordMapper.findByUserIdWithBookInfo(p, userId);
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public PageResult<BorrowRecord> findAll(int page, int pageSize) {
        Page<BorrowRecord> p = new Page<>(page, pageSize);
        Page<BorrowRecord> result = borrowRecordMapper.findAllWithBookInfo(p);
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 归还图书
     * <p>
     * 执行流程：① 校验借阅记录状态 → ② 更新记录为已归还（含归还日期） →
     * ③ 恢复库存 → ④ 返回结果消息（含是否逾期）。
     * </p>
     *
     * @param operatorId 操作者用户 ID
     * @param isAdmin    操作者是否为管理员
     * @param recordId   借阅记录 ID
     * @throws RuntimeException 借阅记录不存在或已归还
     */
    @Override
    @Transactional
    public void returnBook(Long operatorId, boolean isAdmin, Long recordId) {
        // 查找借阅记录并校验状态
        BorrowRecord record = borrowRecordMapper.selectById(recordId);
        if (record == null || !"borrowed".equals(record.getStatus())) {
            throw new RuntimeException("借阅记录不存在或已归还");
        }

        // 权限校验：管理员可归还任意记录，普通用户只能归还自己的
        if (!isAdmin && !record.getUserId().equals(operatorId)) {
            throw new RuntimeException("无权归还他人的借阅记录");
        }

        // 更新记录为已归还
        record.setStatus("returned");
        record.setReturnDate(now);
        borrowRecordMapper.updateById(record);

        Book book = bookMapper.selectById(record.getBookId());
        if (book != null) {
            book.setAvailableCopies(book.getAvailableCopies() + 1);
            bookMapper.updateById(book);
        }
    }

    @Override
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();

        Long totalBooks = bookMapper.selectCount(null);
        stats.put("totalBooks", totalBooks);

        LambdaQueryWrapper<Book> availableWrapper = new LambdaQueryWrapper<>();
        availableWrapper.gt(Book::getAvailableCopies, 0);
        stats.put("availableBooks", bookMapper.selectCount(availableWrapper));

        LambdaQueryWrapper<User> userCountWrapper = new LambdaQueryWrapper<>();
        stats.put("totalUsers", userMapper.selectCount(userCountWrapper));

        LambdaQueryWrapper<BorrowRecord> borrowedWrapper = new LambdaQueryWrapper<>();
        borrowedWrapper.eq(BorrowRecord::getStatus, "borrowed");
        stats.put("borrowedBooks", borrowRecordMapper.selectCount(borrowedWrapper));

        // 逾期借阅数量：状态为 borrowed 且 due_date < 当前时间
        LambdaQueryWrapper<BorrowRecord> overdueWrapper = new LambdaQueryWrapper<>();
        overdueWrapper.eq(BorrowRecord::getStatus, "borrowed")
                      .lt(BorrowRecord::getDueDate, LocalDateTime.now());
        stats.put("overdueBooks", borrowRecordMapper.selectCount(overdueWrapper));

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
