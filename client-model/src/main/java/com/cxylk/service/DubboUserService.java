package com.cxylk.service;

import com.cxylk.bean.RemoteUser;

/**
 * @author likui
 * @date 2022/8/23 下午7:20
 **/
public interface DubboUserService {
    RemoteUser getUserByName(String name);
}
