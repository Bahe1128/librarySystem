package com.library.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 归还请求 DTO（Data Transfer Object）
 * <p>
 * 接收前端提交的还书请求数据，只包含借阅记录的 ID。
 * </p>
 */
public class ReturnRequest {
    /** 要归还的借阅记录 ID，不能为空 */
    @NotNull // 校验注解：该字段不允许为 null
    private Long recordId;

    // ========== Getter / Setter ==========
    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
}
