package com.cxylk.agent2.base;

import java.lang.instrument.Instrumentation;

/**
 * @author likui
 * @date 2022/8/12 下午5:34
 * 采集器统一实现，用于不同的采集器注册
 **/
public interface Collect {
    /**
     * 采集器统一注册
     * @param instrumentation
     */
    void register(Instrumentation instrumentation);
}
