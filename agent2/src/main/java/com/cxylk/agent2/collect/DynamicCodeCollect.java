package com.cxylk.agent2.collect;

import com.cxylk.agent2.base.Agent;
import com.cxylk.agent2.base.Collect;
import com.cxylk.coverage.collect.CodeStackCollect;
import com.cxylk.coverage.common.StringUtils;

import java.lang.instrument.Instrumentation;

/**
 * @author likui
 * @date 2022/9/14 下午3:07
 * 动态代码覆盖率采集
 **/
public class DynamicCodeCollect implements Collect {
    /**
     * 是否开启动态代码覆盖率采集
     */
    private static boolean active;

    @Override
    public void register(Instrumentation instrumentation) {
        String include = Agent.getConfig("include");
        String exclude = Agent.getConfig("exclude");
        if(StringUtils.hasText(include)){
            active =true;
            instrumentation.addTransformer(new CodeStackCollect(include,exclude));
        }
    }

    public static boolean isActive() {
        return active;
    }
}
