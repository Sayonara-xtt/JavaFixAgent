package com.javafix.shop.service.impl;

import org.springframework.stereotype.Service;

import com.javafix.shop.entity.Product;
import com.javafix.shop.exception.BusinessException;
import com.javafix.shop.exception.ErrorCode;
import com.javafix.shop.mapper.ProductMapper;
import com.javafix.shop.service.ProductService;
import com.javafix.shop.vo.ProductVO;

/**
 * 商品查询服务。
 *
 * <p>依赖 MyBatis-Plus 逻辑删除：已删除商品在普通查询中不可见。</p>
 */
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    /**
     * 按 ID 查询未删除商品；不存在或已逻辑删除时返回受控业务错误。
     */
    @Override
    public ProductVO getById(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "product not found");
        }
        return toVo(product);
    }

    private ProductVO toVo(Product product) {
        ProductVO vo = new ProductVO();
        vo.setId(product.getId());
        vo.setName(product.getName());
        vo.setPrice(product.getPrice());
        vo.setStock(product.getStock());
        return vo;
    }
}
