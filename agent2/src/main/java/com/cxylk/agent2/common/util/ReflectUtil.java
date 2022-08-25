package com.cxylk.agent2.common.util;

import java.lang.reflect.Method;

/**
 * @author likui
 * @date 2022/8/24 下午2:41
 **/
public class ReflectUtil {
    public static Object invoker(Method method, Object target, Object... args) {
        boolean old = method.isAccessible();
        try {
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            method.setAccessible(old);
        }
    }
}
