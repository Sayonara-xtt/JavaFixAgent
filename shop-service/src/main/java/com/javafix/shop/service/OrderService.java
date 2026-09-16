package com.javafix.shop.service;

import com.javafix.shop.dto.CreateOrderRequest;
import com.javafix.shop.vo.OrderVO;
import com.javafix.shop.vo.PageResult;

public interface OrderService {

    OrderVO create(CreateOrderRequest request);

    OrderVO getById(Long id);

    PageResult<OrderVO> page(long page, long size);

    OrderVO cancel(Long id);
}
