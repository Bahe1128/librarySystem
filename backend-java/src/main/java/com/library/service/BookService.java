package com.library.service;

import com.library.entity.Book;
import java.util.List;

public interface BookService {
    List<Book> findAll();
    Book findById(Long id);
    List<Book> search(String keyword);
    Book create(Book book);
    Book update(Long id, Book book);
    void delete(Long id);
}
