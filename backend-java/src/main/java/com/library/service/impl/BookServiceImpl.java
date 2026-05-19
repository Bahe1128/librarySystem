package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.entity.Book;
import com.library.mapper.BookMapper;
import com.library.service.BookService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookServiceImpl implements BookService {

    private final BookMapper bookMapper;

    public BookServiceImpl(BookMapper bookMapper) {
        this.bookMapper = bookMapper;
    }

    @Override
    public List<Book> findAll() {
        return bookMapper.selectList(null);
    }

    @Override
    public Book findById(Long id) {
        return bookMapper.selectById(id);
    }

    @Override
    public List<Book> search(String keyword) {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Book::getTitle, keyword)
               .or().like(Book::getAuthor, keyword)
               .or().like(Book::getIsbn, keyword)
               .or().like(Book::getCategory, keyword);
        return bookMapper.selectList(wrapper);
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
