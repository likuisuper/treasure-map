package com.cxylk.agent1;

import com.cxylk.agent1.httpinvoker.HttpProxy1;
import com.cxylk.agent1.httpinvoker.HttpProxy2;
import com.cxylk.agent1.httpserver.HttpCollect;
import com.cxylk.agent1.mybatis.MybatisAgent1;
import com.cxylk.agent1.mybatis.MybatisAgent2;
import com.cxylk.agent1.mybatis.MybatisAgent3;

import java.lang.instrument.Instrumentation;

/**
 * @author likui
 * @date 2022/3/29 下午7:33
 **/
public class Agent {
    public static void premain(String args, Instrumentation instrumentation){
        //instrumentation.addTransformer(new ServiceAgent());
//        instrumentation.addTransformer(new MybatisAgent1());
//        instrumentation.addTransformer(new MybatisAgent2());
        instrumentation.addTransformer(new MybatisAgent3());
        //http响应采集
        instrumentation.addTransformer(new HttpCollect());
        //http调用采集
//        HttpProxy1.registerProtocol();
        HttpProxy2.registerProtocol();
    }
}
