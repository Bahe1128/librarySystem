package com.library.controller;

import com.library.common.Result;
import com.library.service.BorrowService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final BorrowService borrowService;

    public StatsController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    @GetMapping
    public Result<Map<String, Object>> getStats() {
        return Result.success(borrowService.getSystemStats());
    }

    @GetMapping("/trends")
    public Result<List<Map<String, Object>>> getTrends() {
        return Result.success(borrowService.getBorrowTrends());
    }
}
