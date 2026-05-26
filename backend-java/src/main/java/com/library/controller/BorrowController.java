package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.dto.BorrowRequest;
import com.library.dto.ReturnRequest;
import com.library.entity.BorrowRecord;
import com.library.security.UserPrincipal;
import com.library.service.BorrowService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 借阅管理控制器
 * <p>
 * 处理图书借阅、归还、借阅记录查询。
 * 所有以 /api/borrow 开头的接口都需要登录认证。
 * 管理员可以查看所有借阅记录，普通用户只能查看自己的记录。
 * </p>
 */
@RestController
@RequestMapping("/api/borrow")
public class BorrowController {

    /** 借阅服务（封装借阅和归还业务逻辑） */
    private final BorrowService borrowService;

    /**
     * 构造方法注入 BorrowService
     */
    public BorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    /**
     * 借阅图书
     * POST /api/borrow
     * <p>
     * 通过 @AuthenticationPrincipal 获取当前登录用户的信息，
     * 无需手动传递 userId，安全性更高。
     * </p>
     *
     * @param user    当前登录用户（由 JWT 过滤器解析后注入）
     * @param request 包含图书 ID 的借阅请求体
     * @return 创建的借阅记录
     */
    @PostMapping
    public Result<?> borrow(@AuthenticationPrincipal UserPrincipal user, // 从 SecurityContext 获取当前用户
                            @Valid @RequestBody BorrowRequest request) {
        try {
            BorrowRecord record = borrowService.borrow(user.getId(), request.getBookId());
            return Result.success(record);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询借阅记录（分页）
     * GET /api/borrow?page=1&pageSize=10
     * <p>
     * 管理员角色返回全库记录，普通用户只看自己的。
     * </p>
     *
     * @param user     当前登录用户
     * @param page     页码，默认第 1 页
     * @param pageSize 每页条数，默认 10 条
     * @return 借阅记录分页结果
     */
    @GetMapping
    public Result<PageResult<BorrowRecord>> getRecords(@AuthenticationPrincipal UserPrincipal user,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<BorrowRecord> records;
        if ("admin".equals(user.getRole())) {
            // 管理员可以查看所有借阅记录
            records = borrowService.findAll(page, pageSize);
        } else {
            // 普通用户只能查看自己的借阅记录
            records = borrowService.findByUserId(user.getId(), page, pageSize);
        }
        return Result.success(records);
    }

    /**
     * 归还图书
     * POST /api/borrow/return
     *
     * @param request 包含借阅记录 ID 的归还请求体
     * @return 归还成功消息
     */
    @PostMapping("/return")
    public Result<?> returnBook(@Valid @RequestBody ReturnRequest request) {
        try {
            borrowService.returnBook(request.getRecordId());
            return Result.success("归还成功", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
