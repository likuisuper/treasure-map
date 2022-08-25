package com.cxylk.mapper;


import com.cxylk.bean.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author likui
 * @date 2022/3/31 下午10:29
 **/
@Mapper
public interface UserMapper {
    @Select("select * from merchant_user where user_name=#{name}")
    List<User> findUser(String name);
}
