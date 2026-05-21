package com.library.service;

import com.library.common.PageResult;
import com.library.entity.Book;

public interface BookService {
    PageResult<Book> findAll(int page, int pageSize);
    Book findById(Long id);
    PageResult<Book> search(String keyword, int page, int pageSize);
    Book create(Book book);
    Book update(Long id, Book book);
    void delete(Long id);
}
