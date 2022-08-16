package com.cxylk.agent2.common.logger;


public class LogFactory {

    private static LogFactory singleton = new LogFactory();

    private LogFactory() {

    }
    private Log getInstance(String name) {
        return DirectJDKLog.getInstance(name);
    }

    private Log getInstance(Class<?> clazz) {
        return getInstance(clazz.getName());
    }

    public static LogFactory getFactory() {
        return singleton;
    }

    public static Log getLog(Class<?> clazz) {
        return (getFactory().getInstance(clazz));
    }

    public static Log getLog(String name) {
        return (getFactory().getInstance(name));
    }
}
