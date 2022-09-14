package com.cxylk.coverage.collect;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.Properties;

/**
 * @author likui
 * @date 2022/9/8 下午3:58
 **/
public class Agent {
    public static void premain(String args, Instrumentation instrumentation) throws IOException {
        Properties properties=new Properties();
        properties.load(new ByteArrayInputStream(args.replaceAll(",","\n").getBytes()));
        String include = properties.getProperty("include");
        String exclude = properties.getProperty("exclude");
        instrumentation.addTransformer(new CodeStackCollect(include,exclude));
    }
}
