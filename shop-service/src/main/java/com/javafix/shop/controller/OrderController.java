package com.javafix.shop.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.javafix.shop.common.ApiResponse;
import com.javafix.shop.dto.CreateOrderRequest;
import com.javafix.shop.service.OrderService;
import com.javafix.shop.vo.OrderVO;
import com.javafix.shop.vo.PageResult;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * 订单 REST 入口：创建、详情、分页、取消。
 *
 * <p>Controller 只做参数校验与响应包装，业务规则集中在 {@link com.javafix.shop.service.OrderService}。</p>
 */
@Tag(name = "订单", description = "创建 / 查询 / 分页 / 取消")
@Validated
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /** 创建订单：请求体包含 userId 与多商品明细 items。 */
    @Operation(summary = "创建订单", description = "校验用户与库存后原子扣减，并按明细汇总金额")
    @PostMapping
    public ApiResponse<OrderVO> create(@Valid @RequestBody CreateOrderRequest request) {
        return ApiResponse.ok(orderService.create(request));
    }

    /** 订单详情（含明细）。 */
    @Operation(summary = "订单详情")
    @GetMapping("/{id}")
    public ApiResponse<OrderVO> getById(@PathVariable Long id) {
        return ApiResponse.ok(orderService.getById(id));
    }

    /** 订单分页列表，默认 page=1、size=20。 */
    @Operation(summary = "订单分页")
    @GetMapping
    public ApiResponse<PageResult<OrderVO>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size) {
        return ApiResponse.ok(orderService.page(page, size));
    }

    /** 取消订单：成功后恢复库存；重复取消返回业务错误。 */
    @Operation(summary = "取消订单", description = "仅 CREATED 可取消；成功后精确还库存一次")
    @PostMapping("/{id}/cancel")
    public ApiResponse<OrderVO> cancel(@PathVariable Long id) {
        return ApiResponse.ok(orderService.cancel(id));
    }
}
