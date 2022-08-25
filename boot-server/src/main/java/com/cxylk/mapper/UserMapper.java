package com.cxylk.mapper;

import com.cxylk.bean.RemoteUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author likui
 * @date 2022/8/23 下午7:33
 **/
@Mapper
public interface UserMapper {
    @Select({"select * from merchant_user where user_name=#{name}"})
    List<RemoteUser> findUser(String name);
}
