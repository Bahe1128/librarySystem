package com.library.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 借阅请求 DTO（Data Transfer Object）
 * <p>
 * 接收前端提交的借阅请求数据，只包含必要的 bookId 字段。
 * 当前登录用户的信息由后端从 SecurityContext 获取，不需要前端传递。
 * </p>
 */
public class BorrowRequest {
    /** 要借阅的图书 ID，不能为空 */
    @NotNull // 校验注解：该字段不允许为 null
    private Long bookId;

    // ========== Getter / Setter ==========
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
}
