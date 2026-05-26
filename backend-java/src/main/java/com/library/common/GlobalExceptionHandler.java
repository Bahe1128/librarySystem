package com.library.common;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

/**
 * 全局异常处理类
 * <p>
 * 使用 @RestControllerAdvice 注解，拦截所有 Controller 抛出的异常，
 * 将异常统一转换为前端可读的 Result JSON 响应，避免将异常堆栈直接暴露给客户端。
 * </p>
 */
@RestControllerAdvice // 组合注解，相当于 @ControllerAdvice + @ResponseBody，返回 JSON 格式的异常信息
public class GlobalExceptionHandler {

    /**
     * 处理参数校验失败的异常
     * 当前端提交的数据不符合 @Valid 注解标记的校验规则时触发此方法
     *
     * @param e 参数校验异常对象，包含所有字段校验错误信息
     * @return 400 错误码及所有校验失败的字段信息，用逗号分隔
     */
    @ExceptionHandler(MethodArgumentNotValidException.class) // 指定拦截的异常类型
    public Result<?> handleValidation(MethodArgumentNotValidException e) {
        // 遍历所有字段校验错误，拼接错误提示信息
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage) // 获取每个字段的默认错误消息
                .collect(Collectors.joining(", "));  // 用逗号连接多条错误信息
        return Result.error(400, msg);
    }

    /**
     * 处理运行时异常（兜底异常处理）
     * 捕获所有未被特定处理器拦截的 RuntimeException
     *
     * @param e 运行时异常对象
     * @return 400 错误码及异常消息
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntime(RuntimeException e) {
        return Result.error(400, e.getMessage());
    }
}
