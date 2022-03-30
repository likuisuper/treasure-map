package com.cxylk.agent1.service;

/**
 * @author likui
 * @date 2022/3/29 下午7:31
 **/
public class UserService {
    public void findUser(String name){
        //begin(插桩)

        System.out.println("获取用户");
        if("lk".equals(name)){
            return;
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //end(插桩)
    }
}
