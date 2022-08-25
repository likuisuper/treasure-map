package com.cxylk.agent2.collect.dubbo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author likui
 * @date 2022/8/23 下午4:55
 * invocation适配器，利用反射获取Invocation信息
 **/
public class InvocationAdapter {
    private Object target;

    private final Method _getMethodName;

    private final Method _getInvoker;

    private final Method _getAttachment;

    private final Method _setAttachment;


    public InvocationAdapter(Object target) {
        this.target=target;
        try {
            _getMethodName = target.getClass().getMethod("getMethodName");
            _getInvoker=target.getClass().getMethod("getInvoker");
            _getAttachment=target.getClass().getMethod("getAttachment",String.class);
            _setAttachment=target.getClass().getMethod("setAttachment",String.class,String.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("error :" + e.getMessage() + ". probable cause the target is not belong com.alibaba.dubbo.rpc.RpcInvocation ");
        }
    }

    public String getMethodName(){
        try {
            return (String) _getMethodName.invoke(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Object getInvoker(){
        try {
            return _getInvoker.invoke(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getAttachment(String key){
        try {
            return _getAttachment.invoke(target,key).toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setAttachment(String key,String value){
        try {
            _setAttachment.invoke(target,key,value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
