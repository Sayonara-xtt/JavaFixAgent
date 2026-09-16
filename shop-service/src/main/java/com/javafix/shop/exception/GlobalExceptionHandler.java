package com.javafix.shop.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.javafix.shop.common.ApiResponse;

/**
 * 将业务异常与参数校验失败转换为统一响应体。
 *
 * <p>业务错误通常仍返回 HTTP 200，由 {@code code} 区分成败，便于前端与后续 Agent 解析。</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusiness(BusinessException ex) {
        return ApiResponse.fail(ex.getCode(), ex.getMessage());
    }

    /** JSON 解析错误也遵守统一响应契约；不向调用方暴露原始请求及解析细节。 */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<Void> handleUnreadableBody(HttpMessageNotReadableException ex) {
        return ApiResponse.fail(ErrorCode.VALIDATION_ERROR, "invalid JSON request body");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, IllegalArgumentException.class})
    public ApiResponse<Void> handleValidation(Exception ex) {
        String message = ex.getMessage();
        if (ex instanceof MethodArgumentNotValidException manv && manv.getBindingResult().getFieldError() != null) {
            message = manv.getBindingResult().getFieldError().getDefaultMessage();
        } else if (ex instanceof BindException be && be.getBindingResult().getFieldError() != null) {
            message = be.getBindingResult().getFieldError().getDefaultMessage();
        }
        return ApiResponse.fail(ErrorCode.VALIDATION_ERROR, message);
    }
}
