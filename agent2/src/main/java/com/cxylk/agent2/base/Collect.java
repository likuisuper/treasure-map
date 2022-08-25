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

    /**
     * 无返回参数方法模板
     */
    String VOID_SOURCE_TEMPLATE = "{\n"
            + "%s"
            + "        try {\n"
            + "            %s$agent($$);\n"
            + "        } catch (Throwable e) {\n"
            + "%s"
            + "            throw e;\n"
            + "        }finally{\n"
            + "%s"
            + "        }\n"
            + "}\n";

    /**
     * 有返回参数方法模板
     */
    String SOURCE_TEMPLATE = "{\n"
            + "%s"
            + "        Object result=null;\n"
            + "       try {\n"
            + "            result=($w)%s$agent($$);\n"
            + "        } catch (Throwable e) {\n"
            + "%s"
            + "            throw e;\n"
            + "        }finally{\n"
            + "%s"
            + "        }\n"
            + "        return ($r) result;\n"
            + "}\n";
}
