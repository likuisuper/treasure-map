package com.cxylk.agent2.base;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.platform.commons.util.ClassFilter;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * @author likui
 * @date 2022/8/16 上午11:22
 **/
public class AgentSession {
    /**
     * 当前线程会话
     */
    private static ThreadLocal<AgentSession> currentSession=new ThreadLocal<>();

    private String SESSION_ID;

    private static List<Class<?>> processorClassList;

    private final List<Processor> processorList;

    static {
        String currentPackage=AgentSession.class.getPackage().getName();
        processorClassList = ReflectionUtils.findAllClassesInPackage(currentPackage.substring(0,currentPackage.lastIndexOf(".")),
                ClassFilter.of(o -> Processor.class.isAssignableFrom(o) && !o.isInterface()));
    }

    private AgentSession(){
        //每次打开会话都会触发实例化，重新生成sessionId
        this.SESSION_ID= UUID.randomUUID().toString().replace("-","");
        //获取所有处理器，并进行排序
        processorList=processorClassList.stream().map(clazz->{
            try {
                return (Processor)clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }).sorted(Comparator.comparing(Processor::order)).collect(Collectors.toList());
    }

    public static AgentSession get(){
        return currentSession.get();
    }

    public static void open(){
        AgentSession agentSession=new AgentSession();
        currentSession.set(agentSession);
    }

    public static void close(){
        Assert.assertTrue(get()!=null);
        try {
            get().processorList.forEach(Processor::finish);
        } finally {
            currentSession.remove();
        }
    }

    public void push(AgentSession session,Object node){
        Objects.requireNonNull(node);
        for (Processor processor : processorList) {
            //顺序执行处理器逻辑
            //当前参数经过处理后作为下一次处理的参数
            node = processor.accept(session,node);
            if(node==null){
                break;
            }
        }
    }

    public String getSession(){
        return SESSION_ID;
    }
}
