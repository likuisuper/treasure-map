package com.cxylk.agent2.collect.dubbo;

import com.cxylk.agent2.base.Agent;
import com.cxylk.agent2.base.AgentSession;
import com.cxylk.agent2.base.Collect;
import com.cxylk.agent2.common.logger.Log;
import com.cxylk.agent2.common.logger.LogFactory;
import com.cxylk.agent2.model.DubboInfo;
import com.cxylk.agent2.process.LogPrintProcessor;
import javassist.*;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.logging.Logger;

/**
 * @author likui
 * @date 2022/8/23 下午4:37
 * dubbo响应采集，即服务端采集
 **/
public class DubboReceiveCollect implements Collect, ClassFileTransformer {
    private Logger logger = Logger.getLogger(DubboReceiveCollect.class.getName());

    /**
     * 插桩入口
     */
    private static final String TARGET_CLASS="org.apache.dubbo.rpc.filter.GenericFilter";

    /**
     * 插桩方法
     */
    public static final String TARGET_METHOD="invoke";

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
        //为了解决classLoader的问题，这里一样采用适配器方案获取参数信息
        InvocationAdapter invocationAdapter=new InvocationAdapter(args[1]);
        InvokerAdapter invokerAdapter=new InvokerAdapter(args[0]);
        //获取上游传参
        String traceId=invocationAdapter.getAttachment(AgentSession.TRACE_ID_KEY);
        String parentId=invocationAdapter.getAttachment(AgentSession.PARENT_ID_KEY);
        //服务端要重传打开会话，以为服务端属于另外一个节点
        AgentSession.open(traceId,parentId);
        dubboInfo.setBeginTime(System.currentTimeMillis());
        //服务端的spanId就是客户端传过来的spanId
        dubboInfo.setSpanId(parentId);
        dubboInfo.setServiceInterface(invokerAdapter.getInterface().getName());
        dubboInfo.setServiceMethodName(invocationAdapter.getMethodName());
        dubboInfo.setSeat(DubboInfo.SEAT_SERVER);
        //todo
        //dubboInfo.setClientIp();
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
        dubboInfo.setUseTime(System.currentTimeMillis() - dubboInfo.getBeginTime());
        AgentSession session=AgentSession.get();
        session.push(dubboInfo);
        //关闭会话
        AgentSession.close();
    }

}
