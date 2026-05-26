package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 图书实体类
 * <p>
 * 对应数据库中的 books 表，每个字段映射一个表列。
 * MyBatis Plus 会根据字段名自动映射（驼峰命名自动转为下划线命名），
 * 如 totalCopies 对应 total_copies 列。
 * </p>
 */
@TableName("books") // 指定实体对应的数据库表名
public class Book {
    /** 图书 ID，主键，自增 */
    @TableId(type = IdType.AUTO) // 主键注解，AUTO 表示数据库自增
    private Long id;
    /** 书名 */
    private String title;
    /** 作者 */
    private String author;
    /** ISBN 国际标准书号 */
    private String isbn;
    /** 图书分类（计算机、文学、科幻等） */
    private String category;
    /** 馆藏总副本数 */
    private Integer totalCopies;
    /** 当前可供借阅的副本数（借出时会减少，归还时会增加） */
    private Integer availableCopies;
    /** 创建时间，插入时由 MyBatis Plus 自动填充当前时间 */
    @TableField(fill = FieldFill.INSERT) // 标记为插入时自动填充，配合 MyBatisPlusConfig 的 insertFill 方法
    private LocalDateTime createdAt;

    // ========== Getter / Setter ==========
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Integer getTotalCopies() { return totalCopies; }
    public void setTotalCopies(Integer totalCopies) { this.totalCopies = totalCopies; }
    public Integer getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(Integer availableCopies) { this.availableCopies = availableCopies; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
