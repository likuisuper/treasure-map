package com.cxylk.agent2.base;

import com.cxylk.agent2.common.logger.Log;
import com.cxylk.agent2.common.logger.LogFactory;
import org.junit.platform.commons.util.ClassFilter;
import org.junit.platform.commons.util.ReflectionUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.List;
import java.util.Properties;

/**
 * @author likui
 * @date 2022/8/16 下午2:53
 **/
public class Agent {
    private final static Log logger = LogFactory.getLog(Agent.class);
    public static Properties config;

    public static void premain(String args, Instrumentation instrumentation){
        config = new Properties();
        // 装截agent 配置文件
        config.putAll(getAgentConfigs());
        // 基于JVM参数配置，优先级高
        if (args != null && !args.trim().equals("")) {
            try {
                //多个参数以逗号分隔
                config.load(new ByteArrayInputStream(
                        args.replaceAll(",", "\n").getBytes()));
            } catch (IOException e) {
                throw new RuntimeException(String.format("agent参数无法解析：%s", args), e);
            }
        }

        //初始化采集器
        String currentPackage = Agent.class.getPackage().getName();
        //由于agent在base目录下，所以要得到父路径，才能找到collet的子类
        List<Class<?>> collectClassList = ReflectionUtils.findAllClassesInPackage(currentPackage.substring(0,currentPackage.lastIndexOf(".")),
                ClassFilter.of(o-> Collect.class.isAssignableFrom(o)&&!o.isInterface()));
        collectClassList.stream().map(clazz->{
            try {
                return (Collect)clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }).forEach(c->c.register(instrumentation));
    }

    // 读取agent 配置
    private static Properties getAgentConfigs() {
        // 读取agnet 配置
        URL u = Agent.class.getProtectionDomain().getCodeSource().getLocation();
        File file = new File(new File(u.getFile()).getParentFile(), "conf/config.properties");
        if (!file.exists() || file.isDirectory()) {
            logger.warn("找不到配置文件:" + file.getPath());
            return new Properties();
        }
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return properties;
    }
}
