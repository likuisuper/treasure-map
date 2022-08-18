package com.cxylk.agent2.base;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
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

    /**
     * 表示需要处理的收集器数量，当为0后所有数据都处理完毕，目的是为了关闭流
     */
    public final AtomicInteger collectCount=new AtomicInteger(3);

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

    /**
     * 打开会话，整个调用链中只需在入口处打开，
     * 由于请求最先经过http，所以在http采集的begin方法中打开
     */
    public static void open(){
        AgentSession agentSession=new AgentSession();
        currentSession.set(agentSession);
    }

    /**
     * 关闭会话，整个调用链中只需在结束时关闭，
     * 由于请求最后是经过http返回给调用方，所以在http采集的end方法中关闭
     */
    public static void close(){
        Assert.assertTrue(get()!=null);
        try {
            get().processorList.forEach(Processor::finish);
        } finally {
            currentSession.remove();
        }
    }

    public void push(Object node){
        collectCount.decrementAndGet();
        Objects.requireNonNull(node);
        for (Processor processor : processorList) {
            //顺序执行处理器逻辑
            //当前参数经过处理后作为下一次处理的参数
            node = processor.accept(this,node);
            if(node==null){
                break;
            }
        }
    }

    public String getSession(){
        return SESSION_ID;
    }
}
