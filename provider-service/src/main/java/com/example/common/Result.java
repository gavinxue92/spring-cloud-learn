package com.example.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class Result<T> implements Serializable {
    private Integer code;     // 状态码：200 表示成功，其他代表特定错误码
    private String message;   // 提示信息
    private T data;           // 具体的业务数据

    // 成功返回（带数据）
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    // 成功返回（无数据）
    public static <T> Result<T> success() {
        return success(null);
    }

    // 失败返回（自定义错误码和错误信息）
    public static <T> Result<T> fail(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(null);
        return result;
    }

    // 失败返回（默认错误码 500）
    public static <T> Result<T> fail(String message) {
        return fail(500, message);
    }
}
