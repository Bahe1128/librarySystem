package com.library.common;

import java.util.List;

/**
 * 分页查询结果封装类
 * <p>
 * 泛型类，用于封装 MyBatis Plus 分页查询后的结果数据，
 * 包含当前页的记录列表和分页元数据（总记录数、当前页码、每页大小）。
 * </p>
 *
 * @param <T> 分页记录的数据类型（如 Book、User、BorrowRecord 等）
 */
public class PageResult<T> {
    /** 当前页的记录列表 */
    private List<T> records;
    /** 数据库中的总记录数 */
    private long total;
    /** 当前页码（从1开始） */
    private long page;
    /** 每页显示的记录条数 */
    private long pageSize;

    /**
     * 分页结果构造方法
     *
     * @param records  当前页的数据列表
     * @param total    总记录数
     * @param page     当前页码
     * @param pageSize 每页大小
     */
    public PageResult(List<T> records, long total, long page, long pageSize) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
    }

    // ========== Getter / Setter ==========

    public List<T> getRecords() { return records; }
    public void setRecords(List<T> records) { this.records = records; }
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public long getPage() { return page; }
    public void setPage(long page) { this.page = page; }
    public long getPageSize() { return pageSize; }
    public void setPageSize(long pageSize) { this.pageSize = pageSize; }
}
