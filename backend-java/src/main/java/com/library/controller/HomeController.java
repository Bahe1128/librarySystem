package com.library.controller;

import com.library.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 首页控制器
 * <p>
 * 提供根路径 "/" 的响应，用于快速检查后端服务是否正常运行。
 * </p>
 */
@RestController // 无需 @RequestMapping，直接处理根路径
public class HomeController {

    /**
     * 后端服务健康检查端点
     * GET /
     *
     * @return 包含欢迎消息的 JSON
     */
    @GetMapping("/")
    public Result<Map<String, String>> home() {
        // Map.of 创建不可变的单条目 Map（Java 9+ 语法）
        return Result.success(Map.of("message", "图书管理系统后端API (Spring Boot)"));
    }
}
