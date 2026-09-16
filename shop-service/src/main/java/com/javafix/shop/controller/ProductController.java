package com.javafix.shop.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.javafix.shop.common.ApiResponse;
import com.javafix.shop.service.ProductService;
import com.javafix.shop.vo.ProductVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 商品 REST 入口。
 */
@Tag(name = "商品", description = "商品查询")
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /** 查询未删除商品；不存在或已逻辑删除时返回业务错误码。 */
    @Operation(summary = "查询商品", description = "逻辑删除商品不可见")
    @GetMapping("/{id}")
    public ApiResponse<ProductVO> getById(@PathVariable Long id) {
        return ApiResponse.ok(productService.getById(id));
    }
}
