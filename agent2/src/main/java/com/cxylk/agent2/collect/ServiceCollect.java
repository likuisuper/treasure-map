package com.cxylk.agent2.collect;

import com.cxylk.agent2.base.Collect;

import java.lang.instrument.Instrumentation;

/**
 * @author likui
 * @date 2022/8/16 上午11:10
 * service采集，这里不再实现
 **/
public class ServiceCollect implements Collect {
    @Override
    public void register(Instrumentation instrumentation) {

    }
}
