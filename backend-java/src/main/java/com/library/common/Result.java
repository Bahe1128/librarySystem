package com.library.common;

/**
 * 通用 API 返回结果封装类
 * <p>
 * 统一了整个后端 API 的响应格式，所有接口都通过此类返回 JSON 数据。
 * 包含三个字段：状态码 code（200 成功、400 失败等）、提示消息 message、以及任意类型的业务数据 data。
 * 提供静态工厂方法快速创建成功或失败的响应，无需手动 new 对象。
 * </p>
 *
 * @param <T> 返回数据的类型，可以是任意类型（如对象、列表、分页结果等）
 */
public class Result<T> {
    /** HTTP 状态码，200 表示成功，400 表示客户端错误 */
    private int code;
    /** 返回的提示消息，用于前端展示 */
    private String message;
    /** 返回的业务数据，泛型类型，失败时为 null */
    private T data;

    /**
     * 私有构造方法，防止外部直接实例化
     * 请使用静态工厂方法 success() 或 error() 创建实例
     */
    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 创建成功响应（带数据）
     *
     * @param data 返回的业务数据
     * @param <T>  数据类型
     * @return 状态码 200，消息为 "success" 的 Result 对象
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    /**
     * 创建成功响应（自定义消息 + 数据）
     *
     * @param message 自定义成功消息
     * @param data    返回的业务数据
     * @param <T>     数据类型
     * @return 状态码 200 的 Result 对象
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    /**
     * 创建失败响应（默认 400 错误码）
     *
     * @param message 错误提示消息
     * @param <T>     数据类型
     * @return 状态码 400，数据为 null 的 Result 对象
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(400, message, null);
    }

    /**
     * 创建失败响应（自定义错误码 + 消息）
     *
     * @param code    自定义 HTTP 状态码
     * @param message 错误提示消息
     * @param <T>     数据类型
     * @return 自定义状态码，数据为 null 的 Result 对象
     */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    // ========== Getter / Setter ==========

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
