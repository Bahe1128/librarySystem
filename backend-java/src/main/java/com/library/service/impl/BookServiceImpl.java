package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.PageResult;
import com.library.entity.Book;
import com.library.mapper.BookMapper;
import com.library.service.BookService;
import org.springframework.stereotype.Service;

@Service
public class BookServiceImpl implements BookService {

    private final BookMapper bookMapper;

    public BookServiceImpl(BookMapper bookMapper) {
        this.bookMapper = bookMapper;
    }

    @Override
    public PageResult<Book> findAll(int page, int pageSize) {
        Page<Book> p = new Page<>(page, pageSize);
        Page<Book> result = bookMapper.selectPage(p, null);
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public Book findById(Long id) {
        return bookMapper.selectById(id);
    }

    @Override
    public PageResult<Book> search(String keyword, int page, int pageSize) {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Book::getTitle, keyword)
               .or().like(Book::getAuthor, keyword)
               .or().like(Book::getIsbn, keyword)
               .or().like(Book::getCategory, keyword);
        Page<Book> p = new Page<>(page, pageSize);
        Page<Book> result = bookMapper.selectPage(p, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public Book create(Book book) {
        book.setAvailableCopies(book.getTotalCopies());
        bookMapper.insert(book);
        return book;
    }

    @Override
    public Book update(Long id, Book book) {
        Book existing = bookMapper.selectById(id);
        if (existing == null) return null;

        int diff = book.getTotalCopies() - existing.getTotalCopies();
        int newAvailable = Math.max(0, existing.getAvailableCopies() + diff);

        book.setId(id);
        book.setAvailableCopies(newAvailable);
        bookMapper.updateById(book);
        return bookMapper.selectById(id);
    }

    @Override
    public void delete(Long id) {
        bookMapper.deleteById(id);
    }
}
