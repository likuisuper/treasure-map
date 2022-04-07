package com.cxylk.service;

import com.cxylk.bean.User;

/**
 * @author likui
 * @date 2022/3/31 下午10:41
 **/
public interface UserService {
    User getUserByName(String name);
}
