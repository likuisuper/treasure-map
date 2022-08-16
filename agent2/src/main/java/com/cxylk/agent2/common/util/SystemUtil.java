package com.cxylk.agent2.common.util;



import com.cxylk.agent2.base.Agent;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.URL;

/**
 * @author Tommy
 * Created by Tommy on 2019/3/3
 **/
public class SystemUtil {
    // 获取 进程ID pid
    // 获取 Agent 部署目录
    public static String getPid() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        return name.split("@")[0];
    }

    /**
     * 获取agent 部署路径
     *
     * @return
     */
    public static String getAgentPath() {
        URL u = Agent.class.getProtectionDomain().getCodeSource().getLocation();
        return new File(u.getFile()).getParentFile().getPath();
    }

    public static void main(String[] args) {
        System.out.println(getPid());
        System.out.println(getAgentPath());
    }
}
