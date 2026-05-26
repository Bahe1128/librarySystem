package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.PageResult;
import com.library.entity.Book;
import com.library.mapper.BookMapper;
import com.library.service.BookService;
import org.springframework.stereotype.Service;

/**
 * 图书服务实现类
 * <p>
 * 实现图书的增删改查业务逻辑。
 * 搜索功能支持对书名、作者、ISBN、分类进行模糊匹配。
 * 更新图书时会自动调整可用副本数。
 * </p>
 */
@Service
public class BookServiceImpl implements BookService {

    private final BookMapper bookMapper;

    /**
     * 构造方法注入 BookMapper
     */
    public BookServiceImpl(BookMapper bookMapper) {
        this.bookMapper = bookMapper;
    }

    /**
     * 分页查询所有图书
     *
     * @param page     页码
     * @param pageSize 每页条数
     * @return 图书分页结果
     */
    @Override
    public PageResult<Book> findAll(int page, int pageSize) {
        Page<Book> p = new Page<>(page, pageSize);          // 创建 MyBatis Plus 分页对象
        Page<Book> result = bookMapper.selectPage(p, null); // null 表示无查询条件，查所有
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 根据 ID 查询单本图书
     *
     * @param id 图书 ID
     * @return 图书对象，不存在则返回 null
     */
    @Override
    public Book findById(Long id) {
        return bookMapper.selectById(id);
    }

    /**
     * 按关键词搜索图书
     * 对书名、作者、ISBN、分类四个字段进行 OR 模糊匹配
     *
     * @param keyword  搜索关键词
     * @param page     页码
     * @param pageSize 每页条数
     * @return 匹配的图书分页结果
     */
    @Override
    public PageResult<Book> search(String keyword, int page, int pageSize) {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        // 构建 OR 条件链：书名 like keyword OR 作者 like keyword OR ISBN like keyword OR 分类 like keyword
        wrapper.like(Book::getTitle, keyword)
               .or().like(Book::getAuthor, keyword)
               .or().like(Book::getIsbn, keyword)
               .or().like(Book::getCategory, keyword);
        Page<Book> p = new Page<>(page, pageSize);
        Page<Book> result = bookMapper.selectPage(p, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 新增图书
     * 初始可用副本数 = 总馆藏数
     *
     * @param book 图书信息
     * @return 插入后的图书对象
     */
    @Override
    public Book create(Book book) {
        book.setAvailableCopies(book.getTotalCopies()); // 初始可用数等于总数
        bookMapper.insert(book);
        return book;
    }

    /**
     * 更新图书信息
     * 当总馆藏数变化时，按差值调整可用副本数（可用数不低于 0）
     *
     * @param id   图书 ID
     * @param book 新的图书信息
     * @return 更新后的图书对象，不存在则返回 null
     */
    @Override
    public Book update(Long id, Book book) {
        Book existing = bookMapper.selectById(id);
        if (existing == null) return null;

        // 计算总馆藏数的变化量
        int diff = book.getTotalCopies() - existing.getTotalCopies();
        // 按差值调整可用副本数，确保不低于 0
        int newAvailable = Math.max(0, existing.getAvailableCopies() + diff);

        book.setId(id);
        book.setAvailableCopies(newAvailable);
        bookMapper.updateById(book);
        return bookMapper.selectById(id); // 重新查询返回最新数据
    }

    /**
     * 删除图书
     *
     * @param id 图书 ID
     */
    @Override
    public void delete(Long id) {
        bookMapper.deleteById(id);
    }
}
