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
     * 用于分布式追踪
     */
    public static final String TRACE_ID_KEY="agent-traceId";
    public static final String PARENT_ID_KEY="agent-parentId";
    /**
     * 当前线程会话
     */
    private static ThreadLocal<AgentSession> currentSession = new ThreadLocal<>();

    /**
     * 表示需要处理的收集器数量，当为0后所有数据都处理完毕，目的是为了关闭流
     */
    public final AtomicInteger collectCount = new AtomicInteger(5);


    private static List<Class<?>> processorClassList;

    private final List<Processor> processorList;

    /**
     * 分布式系统全链路追踪ID，会一直传递至所有节点
     * 在整个系统中唯一，该值相同的事件表示同一次调用
     */
    private String traceId;

    //一个span即代表一个时间跨度下的行为动作
    //通常情况下一个span包含名称、spanId、parentId、开始与结束时间

    /**
     * 每捕捉一个事件ID+1，每传递一次，层级+1
     */
    private int nextId;

    /**
     * 表示其父span
     */
    private String parentId;

    static {
        String currentPackage = AgentSession.class.getPackage().getName();
        processorClassList = ReflectionUtils.findAllClassesInPackage(currentPackage.substring(0, currentPackage.lastIndexOf(".")),
                ClassFilter.of(o -> Processor.class.isAssignableFrom(o) && !o.isInterface()));
    }

    private AgentSession() {
        //获取所有处理器，并进行排序
        processorList = processorClassList.stream().map(clazz -> {
            try {
                return (Processor) clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        })
        .sorted(Comparator.comparing(Processor::order))
        .collect(Collectors.toList());
    }

    public static AgentSession get() {
        return currentSession.get();
    }

    /**
     * 打开会话，整个调用链中只需在入口处打开，
     * 由于请求最先经过http，所以在http采集的begin方法中打开
     */
    public static AgentSession open() {
        //每次打开会话都会触发实例化，重新生成traceId
        String traceId = UUID.randomUUID().toString().replaceAll("-", "");
        //初始化parentId为0，即从0开始
        String parentId = "0";
        return open(traceId, parentId);
    }

    /**
     * 这个打开会话用于手需要手动传递traceId和spanId的情况
     *
     * @param traceId
     * @param parentId
     */
    public static AgentSession open(String traceId, String parentId) {
        AgentSession agentSession = new AgentSession();
        currentSession.set(agentSession);
        agentSession.traceId = traceId;
        agentSession.parentId = parentId;
        return agentSession;
    }

    /**
     * 关闭会话，整个调用链中只需在结束时关闭，
     * 由于请求最后是经过http返回给调用方，所以在http采集的end方法中关闭
     */
    public static void close() {
        Assert.assertTrue(get() != null);
        try {
            get().processorList.forEach(Processor::finish);
        } finally {
            currentSession.remove();
        }
    }

    public void push(Object node) {
        collectCount.decrementAndGet();
        Objects.requireNonNull(node);
        for (Processor processor : processorList) {
            //顺序执行处理器逻辑
            //当前参数经过处理后作为下一次处理的参数
            node = processor.accept(this, node);
            if (node == null) {
                break;
            }
        }
    }

    /**
     * 获取下一个spanId
     *
     * @return
     */
    public String nextSpanId() {
        nextId++;
        return parentId + "." + nextId;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getParentId() {
        return parentId;
    }
}
