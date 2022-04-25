package com.cxylk.controller;

import com.cxylk.bean.User;
import com.cxylk.service.UserService;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

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

    @RequestMapping("/proxy")
    public void proxy(String urlText, HttpServletResponse response){
        try {
            URL url=new URL(urlText);
            URLConnection urlConnection = url.openConnection();
            urlConnection.connect();
            InputStream inputStream = urlConnection.getInputStream();
            ServletOutputStream outputStream = response.getOutputStream();
            IOUtils.copy(inputStream,outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
