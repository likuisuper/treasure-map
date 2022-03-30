package com.cxylk.agent1;

import com.cxylk.agent1.mybatis.MybatisAgent1;

import java.lang.instrument.Instrumentation;

/**
 * @author likui
 * @date 2022/3/29 下午7:33
 **/
public class Agent {
    public static void premain(String args, Instrumentation instrumentation){
        //instrumentation.addTransformer(new ServiceAgent());
        instrumentation.addTransformer(new MybatisAgent1());
    }
}
