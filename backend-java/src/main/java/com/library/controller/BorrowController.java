package com.library.controller;

import com.library.common.Result;
import com.library.dto.BorrowRequest;
import com.library.dto.ReturnRequest;
import com.library.entity.BorrowRecord;
import com.library.security.UserPrincipal;
import com.library.service.BorrowService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/borrow")
public class BorrowController {

    private final BorrowService borrowService;

    public BorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    @PostMapping
    public Result<?> borrow(@AuthenticationPrincipal UserPrincipal user,
                            @Valid @RequestBody BorrowRequest request) {
        try {
            BorrowRecord record = borrowService.borrow(user.getId(), request.getBookId());
            return Result.success(record);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping
    public Result<List<BorrowRecord>> getRecords(@AuthenticationPrincipal UserPrincipal user) {
        List<BorrowRecord> records;
        if ("admin".equals(user.getRole())) {
            records = borrowService.findAll();
        } else {
            records = borrowService.findByUserId(user.getId());
        }
        return Result.success(records);
    }

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
