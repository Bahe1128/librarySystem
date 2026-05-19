package com.library.controller;

import com.library.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Result<Map<String, String>> home() {
        return Result.success(Map.of("message", "图书管理系统后端API (Spring Boot)"));
    }
}
