package com.javafix.shop.exception;

/**
 * 受控业务异常，由全局处理器转换为统一响应。
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
