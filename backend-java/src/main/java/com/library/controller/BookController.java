package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.entity.Book;
import com.library.service.BookService;
import org.springframework.web.bind.annotation.*;

/**
 * 图书管理控制器
 * <p>
 * 提供图书的增删改查接口，支持分页查询和关键词搜索。
 * 所有以 /api/books 开头的请求在 SecurityConfig 中被设置为无需认证即可访问。
 * </p>
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    /** 图书服务（封装图书相关的业务逻辑） */
    private final BookService bookService;

    /**
     * 构造方法注入 BookService
     */
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * 分页查询所有图书
     * GET /api/books?page=1&pageSize=10
     *
     * @param page     页码，默认第 1 页
     * @param pageSize 每页条数，默认 10 条
     * @return 分页结果，包含图书列表和分页元数据
     */
    @GetMapping
    public Result<PageResult<Book>> getAll(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(bookService.findAll(page, pageSize));
    }

    /**
     * 按关键词搜索图书（支持按书名、作者、ISBN 模糊匹配）
     * GET /api/books/search?keyword=计算机&page=1&pageSize=10
     *
     * @param keyword  搜索关键词
     * @param page     页码，默认第 1 页
     * @param pageSize 每页条数，默认 10 条
     * @return 匹配的图书分页结果
     */
    @GetMapping("/search")
    public Result<PageResult<Book>> search(@RequestParam String keyword,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(bookService.search(keyword, page, pageSize));
    }

    /**
     * 根据 ID 查询单本图书
     * GET /api/books/{id}
     *
     * @param id 图书 ID（路径参数）
     * @return 图书详情，不存在则返回错误提示
     */
    @GetMapping("/{id}") // {id} 是路径变量，Spring 会自动绑定到方法参数
    public Result<Book> getById(@PathVariable Long id) {
        Book book = bookService.findById(id);
        if (book == null) return Result.error("图书不存在");
        return Result.success(book);
    }

    /**
     * 新增图书
     * POST /api/books
     *
     * @param book 图书信息（JSON 请求体）
     * @return 保存后的图书对象（包含数据库生成的 ID）
     */
    @PostMapping
    public Result<Book> create(@RequestBody Book book) { // @RequestBody 将 JSON 请求体转换为 Book 对象
        return Result.success(bookService.create(book));
    }

    /**
     * 更新图书信息
     * PUT /api/books/{id}
     *
     * @param id   要更新的图书 ID
     * @param book 新的图书信息
     * @return 更新后的图书对象，不存在则返回错误提示
     */
    @PutMapping("/{id}")
    public Result<Book> update(@PathVariable Long id, @RequestBody Book book) {
        Book updated = bookService.update(id, book);
        if (updated == null) return Result.error("图书不存在");
        return Result.success(updated);
    }

    /**
     * 删除图书
     * DELETE /api/books/{id}
     *
     * @param id 要删除的图书 ID
     * @return 删除成功消息
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        bookService.delete(id);
        return Result.success("删除成功", null);
    }
}
