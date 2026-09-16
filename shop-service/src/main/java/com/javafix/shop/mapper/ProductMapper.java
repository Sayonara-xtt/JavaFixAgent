package com.javafix.shop.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.javafix.shop.entity.Product;

/**
 * 商品持久化。
 *
 * <p>库存扣减/恢复使用 XML 显式 SQL，保证并发下不会把库存扣成负数。</p>
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 原子扣库存。
     *
     * @return 影响行数；为 1 表示扣减成功，否则表示库存不足或商品不可用
     */
    int decreaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * 取消订单时按数量恢复库存。
     *
     * @return 影响行数
     */
    int increaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}
