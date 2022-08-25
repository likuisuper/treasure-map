package com.cxylk.service;

import com.cxylk.bean.User;
import com.cxylk.mapper.UserMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author likui
 * @date 2022/3/31 下午10:41
 **/
@Service
public class UserServiceImpl implements UserService{
    @Resource
    private UserMapper userMapper;

    @Override
    public User getUserByName(String name) {
        List<User> userList = userMapper.findUser(name);
        if (userList.isEmpty()) {
            return null;
        }
        return userList.get(0);
    }
}
