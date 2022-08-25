package com.cxylk.agent2.collect.dubbo;

import com.cxylk.agent2.base.Agent;
import com.cxylk.agent2.base.AgentSession;
import com.cxylk.agent2.base.Collect;
import com.cxylk.agent2.common.logger.Log;
import com.cxylk.agent2.common.logger.LogFactory;
import com.cxylk.agent2.model.DubboInfo;
import javassist.*;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.logging.Logger;

/**
 * @author likui
 * @date 2022/8/23 下午4:37
 * dubbo调用采集，即客户端采集
 **/
public class DubboInvokerCollect implements Collect, ClassFileTransformer {
    private Logger logger = Logger.getLogger(DubboInvokerCollect.class.getName());

    /**
     * 插桩入口
     */
    private static final String TARGET_CLASS="org.apache.dubbo.rpc.protocol.dubbo.DubboInvoker";

    /**
     * 插桩方法
     */
    public static final String TARGET_METHOD="doInvoke";

    @Override
    public void register(Instrumentation instrumentation) {
        instrumentation.addTransformer(this);
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if(!TARGET_CLASS.replaceAll("\\.","/").equals(className)){
            return null;
        }
        try {
            byte[] bytes = buildClass(loader);
            logger.info("插桩成功："+className);
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] buildClass(ClassLoader loader) throws NotFoundException, CannotCompileException, IOException {
        ClassPool pool=new ClassPool();
        pool.insertClassPath(new LoaderClassPath(loader));
        CtClass ctClass = pool.get(TARGET_CLASS);
        CtMethod oldMethod = ctClass.getDeclaredMethod(TARGET_METHOD);
        CtMethod newMethod = CtNewMethod.copy(oldMethod, ctClass, null);
        oldMethod.setName(oldMethod.getName()+"$agent");
        String beginSrc=String.format("Object info=%s.begin($args);",getClass().getName());
        String errorSrc=String.format("%s.error(e,info);",getClass().getName());
        String endSrc=String.format("%s.end(info);",getClass().getName());
        newMethod.setBody(String.format(SOURCE_TEMPLATE,beginSrc,TARGET_METHOD,errorSrc,endSrc));
        ctClass.addMethod(newMethod);
        return ctClass.toBytecode();
    }

    public static DubboInfo begin(Object[] args){
        DubboInfo dubboInfo=new DubboInfo();
        AgentSession agentSession = AgentSession.get();
        //为了解决classLoader的问题，这里一样采用适配器方案获取参数信息
        InvocationAdapter invocationAdapter=new InvocationAdapter(args[0]);
        InvokerAdapter invokerAdapter=new InvokerAdapter(invocationAdapter.getInvoker());
        dubboInfo.setBeginTime(LocalDateTime.now());
        dubboInfo.setSpanId(agentSession.nextSpanId());
        //todo
        //dubboInfo.setRemoteIp();
        dubboInfo.setRemoteUrl(invokerAdapter.getUrl());
        dubboInfo.setServiceInterface(invokerAdapter.getInterface().getName());
        dubboInfo.setServiceMethodName(invocationAdapter.getMethodName());
        dubboInfo.setSeat(DubboInfo.SEAT_CLIENT);
        //dubbo的隐私传参
        invocationAdapter.setAttachment(AgentSession.TRACE_ID_KEY,agentSession.getTraceId());
        invocationAdapter.setAttachment(AgentSession.PARENT_ID_KEY,dubboInfo.getSpanId());
        return dubboInfo;
    }

    public static void error( Throwable e,Object node) {
        if (node == null ) {
            return;
        }
        DubboInfo info= (DubboInfo) node;
        info.setError(e.getMessage());
    }

    public static void end(Object node){
        if(node==null){
            return;
        }
        DubboInfo dubboInfo= (DubboInfo) node;
        dubboInfo.setUseTime(LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli() - dubboInfo.getBeginTime().toInstant(ZoneOffset.of("+8")).toEpochMilli());
        AgentSession session=AgentSession.get();
        session.push(dubboInfo);
    }

}
