package com.library.controller;

import com.library.common.Result;
import com.library.service.BorrowService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 统计控制器
 * <p>
 * 提供系统整体统计数据和借阅趋势数据。
 * 所有 /api/stats 接口都需要登录认证。
 * </p>
 */
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    /** 借阅服务（包含统计查询的业务逻辑） */
    private final BorrowService borrowService;

    /**
     * 构造方法注入 BorrowService
     */
    public StatsController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    /**
     * 获取系统整体统计数据
     * GET /api/stats
     *
     * @return 包含图书总数、用户总数、借阅总数等统计信息的 Map
     */
    @GetMapping
    public Result<Map<String, Object>> getStats() {
        return Result.success(borrowService.getSystemStats());
    }

    /**
     * 获取借阅趋势数据（按月统计）
     * GET /api/stats/trends
     *
     * @return 按月统计的借阅量列表，用于前端绘制趋势图
     */
    @GetMapping("/trends")
    public Result<List<Map<String, Object>>> getTrends() {
        return Result.success(borrowService.getBorrowTrends());
    }
}
