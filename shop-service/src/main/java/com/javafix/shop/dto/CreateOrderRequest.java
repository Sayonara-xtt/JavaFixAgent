package com.javafix.shop.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class CreateOrderRequest {

    @NotNull(message = "userId must not be null")
    private Long userId;

    @NotEmpty(message = "items must not be empty")
    @Valid
    // 集合非空不代表每个元素非空；null 明细必须在进入业务层前拒绝。
    private List<@NotNull(message = "order item must not be null") OrderItemRequest> items;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }
}
