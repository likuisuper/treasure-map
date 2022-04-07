package com.cxylk.controller;

import com.cxylk.bean.User;
import com.cxylk.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author likui
 * @date 2022/3/31 下午10:48
 **/
@RestController
@RequestMapping("/user")
public class MyController {
    @Resource
    private UserService userService;

    @GetMapping("/find")
    public User getUser(String name){
        return userService.getUserByName(name);
    }
}
