package com.example.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 捕获自定义参数校验抛出的异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("参数校验不通过: {}", e.getMessage());
        // 返回统一包装类，错误码设为 400
        return Result.fail(400, "参数校验失败: " + e.getMessage());
    }

    /**
     * 捕获其他未知全局异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统内部异常", e);
        return Result.fail(500, "系统内部错误: " + e.getMessage());
    }

}
