package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.entity.Book;
import com.library.service.BookService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public Result<PageResult<Book>> getAll(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(bookService.findAll(page, pageSize));
    }

    @GetMapping("/search")
    public Result<PageResult<Book>> search(@RequestParam String keyword,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(bookService.search(keyword, page, pageSize));
    }

    @GetMapping("/{id}")
    public Result<Book> getById(@PathVariable Long id) {
        Book book = bookService.findById(id);
        if (book == null) return Result.error("图书不存在");
        return Result.success(book);
    }

    @PostMapping
    public Result<Book> create(@RequestBody Book book) {
        return Result.success(bookService.create(book));
    }

    @PutMapping("/{id}")
    public Result<Book> update(@PathVariable Long id, @RequestBody Book book) {
        Book updated = bookService.update(id, book);
        if (updated == null) return Result.error("图书不存在");
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        bookService.delete(id);
        return Result.success("删除成功", null);
    }
}
