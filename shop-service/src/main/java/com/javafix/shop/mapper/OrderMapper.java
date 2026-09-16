package com.javafix.shop.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.javafix.shop.entity.Order;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /** 原子状态迁移：只有一个事务能够将 CREATED 改成 CANCELLED。 */
    int cancelCreated(@Param("id") Long id);
}
