package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 借阅记录实体类
 * <p>
 * 对应数据库中的 borrow_records 表，记录每次借阅和归还的信息。
 * 包含三个"虚拟字段"（title、author、username）：它们不存于 borrow_records 表中，
 * 而是通过 SQL 多表联查从 books 和 users 表中获取，前端展示时更方便。
 * </p>
 */
@TableName("borrow_records")
public class BorrowRecord {
    /** 借阅记录 ID，主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 借阅用户的 ID */
    private Long userId;
    /** 被借阅图书的 ID */
    private Long bookId;
    /** 借阅日期 */
    private LocalDateTime borrowDate;
    /** 归还日期（null 表示尚未归还） */
    private LocalDateTime returnDate;
    /** 状态：borrowed（借阅中）或 returned（已归还） */
    private String status;
    /** 创建时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    // ========== 虚拟字段：不存在于 borrow_records 表中，通过联表查询填充 ==========

    /** 图书书名（虚拟字段，来自 books 表） */
    @TableField(exist = false) // 告诉 MyBatis Plus：数据库表中不存在此列
    private String title;
    /** 图书作者（虚拟字段，来自 books 表） */
    @TableField(exist = false)
    private String author;
    /** 借阅用户名（虚拟字段，来自 users 表） */
    @TableField(exist = false)
    private String username;

    // ========== Getter / Setter ==========
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
    public LocalDateTime getBorrowDate() { return borrowDate; }
    public void setBorrowDate(LocalDateTime borrowDate) { this.borrowDate = borrowDate; }
    public LocalDateTime getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDateTime returnDate) { this.returnDate = returnDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
