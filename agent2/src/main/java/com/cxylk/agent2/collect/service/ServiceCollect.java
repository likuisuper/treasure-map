package com.cxylk.agent2.collect.service;

import com.cxylk.agent2.base.Agent;
import com.cxylk.agent2.base.AgentSession;
import com.cxylk.agent2.base.Collect;
import com.cxylk.agent2.common.logger.Log;
import com.cxylk.agent2.common.logger.LogFactory;
import com.cxylk.agent2.common.util.WildcardMatcher;
import com.cxylk.agent2.model.ServiceInfo;
import javassist.*;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author likui
 * @date 2022/8/16 上午11:10
 * service采集，由于service采集范围很广，所以我们不能像以前那样写死，需要灵活配置
 * 比如可以指定采集哪些类，指定排除哪些类，这里引入WildcardMatcher来实现
 **/
public class ServiceCollect implements Collect, ClassFileTransformer {
    static final Log logger = LogFactory.getLog(ServiceCollect.class);

    /**
     * 要包含哪些类
     */
    private WildcardMatcher includeMatcher=null;

    /**
     * 要排除哪些类
     */
    private WildcardMatcher excludeMatcher=null;


    @Override
    public void register(Instrumentation instrumentation) {
        //从VM参数中获取指定类的参数和排除类的参数，在premain中我们已经将它加入到了properties中
        String include = Agent.getConfig("service.include");
        String exclude = Agent.getConfig("service.exclude");
        if(!(include==null||"".equals(include))){
            includeMatcher=new WildcardMatcher(include);
        }else {
            logger.warn("未配置 'service.include'参数，无法监控service服务方法");
        }
        if(!(exclude==null||"".equals(exclude))){
            excludeMatcher=new WildcardMatcher(exclude);
        }
        instrumentation.addTransformer(this);
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if(className==null){
            return null;
        }
        className=className.replaceAll("/",".");
        if(includeMatcher==null){
            return null;
        }else if(!includeMatcher.matches(className)){
            //未包含指定类
            return null;
        }else if(excludeMatcher!=null&&excludeMatcher.matches(className)){
            //排除指定类
            return null;
        }
        try {
            CtClass ctClass = toCtClass(loader,className);
            //排除接口
            if(ctClass.isInterface()){
                return null;
            }
            logger.info("插桩："+className);
            AgentByteBuilder byteBuilder=new AgentByteBuilder(ctClass);
            //获取所有方法
            CtMethod[] ctMethods = ctClass.getDeclaredMethods();
            for (CtMethod ctMethod : ctMethods) {
                //屏蔽非公共方法
                if(!Modifier.isPublic(ctMethod.getModifiers())){
                    continue;
                }
                //屏蔽静态方法
                if(Modifier.isStatic(ctMethod.getModifiers())){
                    continue;
                }
                //屏蔽本地方法
                if(Modifier.isNative(ctMethod.getModifiers())){
                    continue;
                }
                AgentByteBuilder.MethodSrcBuilder methodSrcBuilder=new AgentByteBuilder.MethodSrcBuilder();
                methodSrcBuilder.setBeginSrc(beginSrc);
                methodSrcBuilder.setEndSrc(endSrc);
                methodSrcBuilder.setErrorSrc(errorSrc);
                byteBuilder.updateMethod(ctMethod,methodSrcBuilder);

            }
            return byteBuilder.toBytecode();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static final String beginSrc;
    private static final String endSrc;
    private static final String errorSrc;

    private static Map<ClassLoader,ClassPool> classPoolMap=new ConcurrentHashMap<>();

    static {
        //放入静态块，只需初始化一次即可
        beginSrc="com.cxylk.agent2.model.ServiceInfo info=com.cxylk.agent2.collect.service.ServiceCollect.begin(\"%s\",\"%s\");";
        endSrc="com.cxylk.agent2.collect.service.ServiceCollect.end(info);";
        errorSrc="com.cxylk.agent2.collect.service.ServiceCollect.error(info,e);";
    }

    protected static CtClass toCtClass(ClassLoader loader,String className) throws NotFoundException {
        if(!classPoolMap.containsKey(loader)){
            ClassPool classPool=new ClassPool();
            classPool.insertClassPath(new LoaderClassPath(loader));
            classPoolMap.put(loader,classPool);
        }
        className=className.replaceAll("/",".");
        ClassPool classPool = classPoolMap.get(loader);
        return classPool.get(className);
    }

    public static ServiceInfo begin(String serviceName,String methodName){
        ServiceInfo serviceInfo=new ServiceInfo();
        serviceInfo.setBeginTime(LocalDateTime.now());
        serviceInfo.setServiceName(serviceName);
        serviceInfo.setSimpleName(serviceName.substring(serviceName.lastIndexOf(".")+1));
        serviceInfo.setMethodName(methodName);
        return serviceInfo;
    }

    public static void end(ServiceInfo serviceInfo){
        AgentSession agentSession = AgentSession.get();
        if(agentSession==null){
            return;
        }
        serviceInfo.setUseTime(LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli() - serviceInfo.getBeginTime().toInstant(ZoneOffset.of("+8")).toEpochMilli());
        //推送采集到的数据
        agentSession.push(serviceInfo);
    }

    public static void error(ServiceInfo serviceInfo,Throwable e){
        serviceInfo.setErrorType(e.getClass().getSimpleName());
        serviceInfo.setErrorMsg(e.getMessage());
    }


}
