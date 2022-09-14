package com.cxylk.coverage.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author likui
 * @date 2022/9/6 下午7:57
 * 表示一个方法的执行栈帧，用来保存当前栈帧的信息
 **/
public class StackNode implements Serializable {
    /**
     * 节点id,最后一个小数点前面的表示父节点id,小数点后面的表示子节点数量
     */
    private String id;

    /**
     * 类ID，ASM会自动生成
     */
    private Long classId;

    /**
     * 类名
     */
    private String className;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 行号
     */
    private final List<Integer> lines=new ArrayList<>();

    /**
     * 栈帧数量
     * 如果一个方法没有被多次调用，size始终为1
     * 否则它等于一个方法被重复调用了多次
     */
    protected int size;

    /**
     * 方法是否调用完成
     */
    private boolean done;

    /**
     * 耗时，因为执行速度很快，所以这里用纳秒表示
     */
    private Long useTime=0L;

    //以下字段在序列化时忽略

    /**
     * 开始时间
     */
    private transient  Long beginTime;

    /**
     * 指向调用方
     */
    private transient StackNode parent;

    /**
     * 指向被调用方，并且调用方可能有多个
     */
    private transient final List<StackNode> childs=new ArrayList<>(20);

    /**
     * 当前node所属会话
     */
    private transient StackSession stackSession;

    public StackNode() {
    }

    public StackNode(Long classId, String className, String methodName) {
        this.classId = classId;
        this.className = className;
        this.methodName = methodName;
        size=1;
    }

    /**
     * 1、记录行号
     * 2、将hotNode置为当前节点
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj){
        // 添加执行行号
        if (obj instanceof Integer) {
            if (!lines.contains(obj)) {
                lines.add((Integer) obj);
            }
            //重置hotNode节点为当前节点
            //为什么需要这么做？如果当一个方法还没执行end就出现了异常并且没有被捕获，它会一直向上抛出到根节点，
            //那么hotNode就不会回退，当再次调用下个方法时，父子关系就会错乱
            stackSession.setHotStack(this);
            return false;
        }
        return super.equals(obj);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public List<Integer> getLines() {
        return lines;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public Long getUseTime() {
        return useTime;
    }

    public void setUseTime(Long useTime) {
        this.useTime = useTime;
    }

    public Long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Long beginTime) {
        this.beginTime = beginTime;
    }

    public List<StackNode> getChilds() {
        return childs;
    }

    public StackNode getParent() {
        return parent;
    }

    public void setParent(StackNode parent) {
        this.parent = parent;
    }

    public StackSession getStackSession() {
        return stackSession;
    }

    public void setStackSession(StackSession stackSession) {
        this.stackSession = stackSession;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        if (id != null) {
            builder.append(id);
            builder.append(" ");
        }
        if (!done) {
            builder.append("[ERROR]");
        }
        if (className != null) {
            builder.append(className);
            builder.append(".");
        }
        if (methodName != null) {
            builder.append(methodName);
        }
        if (Objects.nonNull(lines)) {
            builder.append("[");
            for (int i = 0; i < lines.size(); i++) {
                builder.append(lines.get(i));
                builder.append("L");
                if(i!=lines.size()-1) {
                    builder.append(",");
                }
            }
            builder.append("]");
        }
        if (useTime != null) {
            builder.append(" time:");
            builder.append(useTime);
            builder.append(":");
        }
        builder.append(" size:");
        builder.append(size);
        builder.append(":");
        return builder.toString();
    }
}
