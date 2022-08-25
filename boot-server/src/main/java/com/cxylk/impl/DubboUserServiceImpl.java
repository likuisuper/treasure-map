package com.cxylk.impl;

import com.cxylk.bean.RemoteUser;
import com.cxylk.mapper.UserMapper;
import com.cxylk.service.DubboUserService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author likui
 * @date 2022/8/23 下午7:32
 **/
@DubboService
public class DubboUserServiceImpl implements DubboUserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public RemoteUser getUserByName(String name) {
        List<RemoteUser> userList = userMapper.findUser(name);
        if(CollectionUtils.isEmpty(userList)){
            return null;
        }
        return userList.get(0);
    }
}
