package com.cxylk.agent1.httpinvoker;

/**
 * @author likui
 * @date 2022/4/24 下午11:03
 **/
public class HttpProxy2 {
    private static final String PROTOCOL_HANDLER = "java.protocol.handler.pkgs";
    //继承了sun.net.www.protocol.https.Handler的Handler的类所在的包前缀
    private static final String HANDLERS_PACKAGE="com.cxylk.agent1.httpinvoker";

    public static void registerProtocol(){
        String handlers = System.getProperty(PROTOCOL_HANDLER, "");
        //handlers不为空的话，还要加上handlers，使用|拼接
        System.setProperty(PROTOCOL_HANDLER,(handlers==null||handlers.isEmpty())?HANDLERS_PACKAGE:handlers+"|"+HANDLERS_PACKAGE);

    }
}
