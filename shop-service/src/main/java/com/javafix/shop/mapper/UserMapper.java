package com.javafix.shop.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.javafix.shop.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
