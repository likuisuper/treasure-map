package com.cxylk.agent1.redis;

import javassist.*;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.nio.charset.StandardCharsets;
import java.security.ProtectionDomain;

/**
 * @author likui
 * @date 2022/8/11 下午5:08
 **/
public class RedisAgent implements ClassFileTransformer {

    /**
     * Redis客户端采用的是jedis，并且所有命令都要通过sendCommand执行
     */
    private static final String TARGET_CLASS="redis.clients.jedis.Protocol";

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if(!TARGET_CLASS.replaceAll("\\.","/").equals(className)){
            return null;
        }
        try {
            return buildClass(loader);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] buildClass(ClassLoader loader) throws NotFoundException, CannotCompileException, IOException {
        ClassPool pool=new ClassPool();
        pool.appendSystemPath();
        pool.insertClassPath(new LoaderClassPath(loader));
        CtClass ctClass = pool.get(TARGET_CLASS);
        //发送命令方法
        //sendCommand有两个方法，获取第二个真正实现的方法
        CtMethod sendMethod = ctClass.getDeclaredMethods("sendCommand")[1];
        sendMethod.insertBefore("com.cxylk.agent1.redis.RedisAgent.begin($args);");

        //返回结果方法
        CtMethod process = ctClass.getDeclaredMethods("process")[0];
        CtMethod newMethod = CtNewMethod.copy(process, ctClass, null);
        process.setName(process.getName()+"$agent");
        String endSrc="com.cxylk.agent1.redis.RedisAgent.end(result);";
        newMethod.setBody(String.format(SOURCE,"process",endSrc));
        ctClass.addMethod(newMethod);
        return ctClass.toBytecode();
    }

    static ThreadLocal<RedisInfo> redisInfoLocalStore = new ThreadLocal();

    public static Object begin(Object[] args){
        RedisInfo info=new RedisInfo();
        info.begin=System.currentTimeMillis();
        //sendcommand方法的第二个参数包含了命令信息
        info.cmd=new String((byte[]) args[1], StandardCharsets.UTF_8);
        redisInfoLocalStore.set(info);
        return info;
    }

    public static void end(Object result){
        try{
            RedisInfo info = redisInfoLocalStore.get();
            info.useTime=System.currentTimeMillis()-info.begin;
            info.result=result;
            System.out.println(info);
        }finally {
            redisInfoLocalStore.remove();
        }
    }

    public static final String SOURCE="{\n" +
            "       Object result=null;\n" +
            "       try{\n" +
            "               result=($w)%s$agent($$);\n"+
            "       }finally{\n" +
            "%s" +
            "       }\n" +
            "       return ($r)result;\n" +
            "}\n";

    public static class RedisInfo {
        public String cmd;
        public Object result;
        public long begin;
        public long useTime;

        @Override
        public String toString() {
            return "RedisInfo{" +
                    "cmd='" + cmd + '\'' +
                    ", result=" + result +
                    ", begin=" + begin +
                    ", useTime=" + useTime +
                    '}';
        }
    }
}
