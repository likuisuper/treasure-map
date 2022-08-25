package com.cxylk.agent2.collect.dubbo;

import com.cxylk.agent2.common.util.ReflectUtil;

import java.lang.reflect.Method;

/**
 * @author likui
 * @date 2022/8/23 下午4:55
 * invoker适配器，利用反射获取Invoker信息
 **/
public class InvokerAdapter {
    private Object target;

    private final Method _getInterface;

    private final Method _getUrl;


    public InvokerAdapter(Object target) {
        this.target=target;
        try {
            _getInterface = target.getClass().getMethod("getInterface");
            _getUrl =target.getClass().getMethod("getUrl");
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("error :" + e.getMessage() + ". probable cause the target is not belong com.alibaba.dubbo.rpc.RpcInvocation ");
        }
    }

    public Class getInterface(){
        try {
            return (Class) ReflectUtil.invoker(_getInterface,target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getUrl(){
        try {
            return ReflectUtil.invoker(_getUrl,target).toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
