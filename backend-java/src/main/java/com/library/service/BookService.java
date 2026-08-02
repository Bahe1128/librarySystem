package com.library.service;

import com.library.common.PageResult;
import com.library.entity.Book;

import java.util.List;

/**
 * 图书服务接口
 * <p>
 * 定义图书管理相关的业务契约，包含分页查询、搜索、增删改查操作。
 * </p>
 */
public interface BookService {
    /** 分页查询所有图书 */
    PageResult<Book> findAll(int page, int pageSize);
    /** 查询全部图书（不分页，用于书架分类展示） */
    List<Book> findAll();
    /** 根据 ID 查询单本图书 */
    Book findById(Long id);
    /** 按关键词搜索图书（书名、作者、ISBN），支持分页 */
    PageResult<Book> search(String keyword, int page, int pageSize);
    /** 新增图书 */
    Book create(Book book);
    /** 更新图书信息 */
    Book update(Long id, Book book);
    /** 删除图书 */
    void delete(Long id);
}
