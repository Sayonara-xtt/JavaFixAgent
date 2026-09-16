package com.javafix.shop.exception;

/**
 * 业务错误码（与统一响应 {@code ApiResponse.code} 对应）。
 *
 * <ul>
 *   <li>40000 — 参数校验失败</li>
 *   <li>40001 — 用户不存在</li>
 *   <li>40002 — 商品不存在或已删除</li>
 *   <li>40003 — 库存不足</li>
 *   <li>40004 — 订单不存在</li>
 *   <li>40005 — 订单不可取消（含重复取消）</li>
 * </ul>
 */
public final class ErrorCode {

    public static final int VALIDATION_ERROR = 40000;
    public static final int USER_NOT_FOUND = 40001;
    public static final int PRODUCT_NOT_FOUND = 40002;
    public static final int INSUFFICIENT_STOCK = 40003;
    public static final int ORDER_NOT_FOUND = 40004;
    public static final int ORDER_NOT_CANCELLABLE = 40005;

    private ErrorCode() {
    }
}
