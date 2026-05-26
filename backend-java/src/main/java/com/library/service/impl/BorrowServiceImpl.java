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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 借阅服务实现类
 * <p>
 * 实现借阅和归还的核心业务逻辑。借阅和归还操作均使用 @Transactional 注解
 * 保证数据一致性：如果借阅记录创建成功但库存扣减失败，事务会自动回滚。
 * </p>
 * <p>
 * 还实现了系统统计（图书总数、用户总数、在借数量）和借阅趋势分析功能。
 * </p>
 */
@Service
public class BorrowServiceImpl implements BorrowService {

    private final BorrowRecordMapper borrowRecordMapper;
    private final BookMapper bookMapper;
    private final UserMapper userMapper;

    /**
     * 构造方法注入所需的 Mapper
     */
    public BorrowServiceImpl(BorrowRecordMapper borrowRecordMapper, BookMapper bookMapper, UserMapper userMapper) {
        this.borrowRecordMapper = borrowRecordMapper;
        this.bookMapper = bookMapper;
        this.userMapper = userMapper;
    }

    /**
     * 借阅图书
     * <p>
     * 使用 @Transactional 确保两步操作（创建借阅记录 + 扣减库存）的原子性：
     * 如果其中任何一步失败，整个操作回滚，不会出现借阅记录已创建但库存没扣的情况。
     * </p>
     *
     * @param userId 借阅用户 ID
     * @param bookId 图书 ID
     * @return 创建的借阅记录
     * @throws RuntimeException 图书不存在或无可用副本
     */
    @Override
    @Transactional // 声明事务：该方法内的数据库操作要么全部成功，要么全部回滚
    public BorrowRecord borrow(Long userId, Long bookId) {
        // 检查图书是否存在且有可用副本
        Book book = bookMapper.selectById(bookId);
        if (book == null || book.getAvailableCopies() <= 0) {
            throw new RuntimeException("图书不存在或无可用副本");
        }

        // 创建借阅记录
        BorrowRecord record = new BorrowRecord();
        record.setUserId(userId);
        record.setBookId(bookId);
        record.setStatus("borrowed");
        record.setBorrowDate(LocalDateTime.now());
        borrowRecordMapper.insert(record);

        // 扣减图书可用库存
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookMapper.updateById(book);

        return record;
    }

    /**
     * 按用户 ID 分页查询借阅记录
     * 通过联表查询同时返回图书名、作者、用户名（普通用户只查看自己的记录）
     *
     * @param userId   用户 ID
     * @param page     页码
     * @param pageSize 每页条数
     * @return 带图书和用户信息的借阅记录分页结果
     */
    @Override
    public PageResult<BorrowRecord> findByUserId(Long userId, int page, int pageSize) {
        Page<BorrowRecord> p = new Page<>(page, pageSize);
        Page<BorrowRecord> result = borrowRecordMapper.findByUserIdWithBookInfo(p, userId);
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 分页查询所有借阅记录（管理员使用）
     *
     * @param page     页码
     * @param pageSize 每页条数
     * @return 所有用户的借阅记录分页结果
     */
    @Override
    public PageResult<BorrowRecord> findAll(int page, int pageSize) {
        Page<BorrowRecord> p = new Page<>(page, pageSize);
        Page<BorrowRecord> result = borrowRecordMapper.findAllWithBookInfo(p);
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 归还图书
     * <p>
     * 使用 @Transactional 确保两步操作（更新记录状态 + 恢复库存）的原子性。
     * </p>
     *
     * @param recordId 借阅记录 ID
     * @throws RuntimeException 借阅记录不存在或已归还
     */
    @Override
    @Transactional
    public void returnBook(Long recordId) {
        // 查找借阅记录并校验状态
        BorrowRecord record = borrowRecordMapper.selectById(recordId);
        if (record == null || !"borrowed".equals(record.getStatus())) {
            throw new RuntimeException("借阅记录不存在或已归还");
        }

        // 更新记录为已归还
        record.setStatus("returned");
        record.setReturnDate(LocalDateTime.now());
        borrowRecordMapper.updateById(record);

        // 恢复图书可用库存
        Book book = bookMapper.selectById(record.getBookId());
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookMapper.updateById(book);
    }

    /**
     * 获取系统统计数据
     * 统计图书总数、可借图书数、用户总数、在借图书数
     *
     * @return 包含统计数据的 Map
     */
    @Override
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();

        // 图书总数
        Long totalBooks = bookMapper.selectCount(null);
        stats.put("totalBooks", totalBooks);

        // 可借图书数（availableCopies > 0）
        LambdaQueryWrapper<Book> availableWrapper = new LambdaQueryWrapper<>();
        availableWrapper.gt(Book::getAvailableCopies, 0); // gt = greater than，大于
        stats.put("availableBooks", bookMapper.selectCount(availableWrapper));

        // 用户总数
        LambdaQueryWrapper<User> userCountWrapper = new LambdaQueryWrapper<>();
        stats.put("totalUsers", userMapper.selectCount(userCountWrapper));

        // 在借图书数（status = 'borrowed'）
        LambdaQueryWrapper<BorrowRecord> borrowedWrapper = new LambdaQueryWrapper<>();
        borrowedWrapper.eq(BorrowRecord::getStatus, "borrowed"); // eq = equal
        stats.put("borrowedBooks", borrowRecordMapper.selectCount(borrowedWrapper));

        return stats;
    }

    /**
     * 获取借阅趋势数据
     * <p>
     * 遍历所有借阅记录，按月聚合统计借阅量，
     * 前端可用于绘制折线图展示借阅趋势。
     * </p>
     *
     * @return 按月排序的统计数据列表，每条包含 month（月份）和 count（借阅量）
     */
    @Override
    public List<Map<String, Object>> getBorrowTrends() {
        List<BorrowRecord> allRecords = borrowRecordMapper.selectList(null);
        // LinkedHashMap 保持插入顺序，便于后续排序
        Map<String, Long> trendMap = new LinkedHashMap<>();

        // 按月统计借阅量
        for (BorrowRecord record : allRecords) {
            if (record.getBorrowDate() != null) {
                // 截取日期前 7 位（如 "2024-01"）作为月份标识
                String month = record.getBorrowDate().toString().substring(0, 7);
                // merge 方法：如果 key 不存在则置为 1，存在则累加
                trendMap.merge(month, 1L, Long::sum);
            }
        }

        // 将 Map 转换为前端需要的 List<Map> 格式
        List<Map<String, Object>> trends = new ArrayList<>();
        trendMap.forEach((month, count) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("month", month);
            item.put("count", count);
            trends.add(item);
        });
        // 按月份升序排序
        trends.sort((a, b) -> ((String) a.get("month")).compareTo((String) b.get("month")));

        return trends;
    }
}
