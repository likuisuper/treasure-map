package com.cxylk.coverage.model;

import java.io.PrintStream;

/**
 * @author likui
 * @date 2022/9/6 下午8:19
 * 采集整个会话，这个会话由多个StackNode组成
 **/
public class StackSession {
    static final ThreadLocal<StackSession> sessions =new ThreadLocal<>();

    /**
     * 当前整个会话的根节点
     */
    private StackNode rootNode;

    /**
     * 最重要的一个属性，它指向的是栈帧中的栈顶元素，
     * 会话期间用于连接StackNode的父子关系
     */
    private StackNode hotNode;

    /**
     * 调用的次数
     */
    private int invokeCount=0;

    /**
     * 一个session期间执行的栈帧数量
     * 用来限制方法调用深度
     */
    private int nodeSize=0;

    private int errorSize=0;

    /**
     * 虚拟机方法栈最大的容量
     */
    static final int MAX_SIZE = 5000;

    /**
     * 构造该对象即表示开启会话
     * 一个线程只能开启一次
     */
    public StackSession(String originClass,String originMethod) {
        if(sessions.get()!=null){
            throw new RuntimeException("open code stack session fail,because current session already exists!");
        }
        //开启会话即构建一个根节点
        StackNode rootNode=new StackNode(-1L,originClass,originMethod);
        //根节点从0开始
        rootNode.setId("0");
        //调用次数+1
        invokeCount++;
        //hotNode可以理解为头指针，指向头节点
        hotNode=rootNode;
        this.rootNode=rootNode;
        rootNode.setBeginTime(System.nanoTime());
        sessions.set(this);
    }

    /**
     * 每个方法入口处都要调用
     * @param classId
     * @param className
     * @param methodName
     * @return 返回一个Object或StackNode
     */
    public static Object $begin(long classId,String className,String methodName){
        StackSession session = sessions.get();
        if(session==null){
            //注意这里不能返回null，因为begin方法的返回结果要作为end方法的入参
            return new Object();
        }
        StackNode node = session.addNode(new StackNode(classId, className, methodName));
        return node==null?new Object():node;
    }

    /**
     * 在每一个return指令之前都要调用，因为会存在分支流程语句
     * @param node
     */
    public static void $end(Object node){
        StackSession session = sessions.get();
        if(session!=null&&node instanceof StackNode){
            //结束调用返回上一级
            session.doneNode((StackNode) node);
        }
    }

    /**
     * 关闭会话，谁开启的会话会关闭
     */
    public void close(){
        if(sessions.get()==this){
            sessions.remove();
        }else {
            throw new RuntimeException("code stack session close fail,because this not current session");
        }
    }

    /**
     * 添加节点（栈帧）
     * @param node
     * @return
     */
    public StackNode addNode(StackNode node){
        //调用次数+1
        invokeCount++;
        //限制方法栈的大小
        if(nodeSize>=MAX_SIZE){
            return null;
        }
        //还需要考虑调用重复方法的情况
        boolean exist=false;
        for (StackNode child : hotNode.getChilds()) {
            //类名和方法名相同即为同一个方法
            if(node.getClassName().equals(child.getClassName())&&node.getMethodName().equals(child.getMethodName())){
                node=child;
                exist=true;
                break;
            }
        }
        //没有调用相同方法的情况
        if(!exist){
            nodeSize++;
            //将当前节点加入hotNode子节点
            hotNode.getChilds().add(node);
            //设置节点id,因为进入这里表示方法没有重复，所以节点id是唯一的
            node.setId(hotNode.getId()+"."+hotNode.getChilds().size());
            node.size=1;
            node.setStackSession(this);
        }else {
            //出现调用相同方法的情况，只需要将node.size+1，表示重复方法的调用次数
            node.size++;
        }
        //当前节点的父节点设置为hotNode
        node.setParent(hotNode);
        //hotNode指向node，也就是指向栈顶
        hotNode=node;
        node.setBeginTime(System.nanoTime());
        return node;
    }

    /**
     * 表示一个栈帧执行完成
     * @param node
     */
    public void doneNode(StackNode node){
        //1、设置节点状态
        node.setDone(true);
        //2、hotNode回退到父节点
        hotNode=node.getParent();
        //3、统计栈帧执行用时
        //因为一个方法存在重复调用情况，所以这里还要加上上一个重复方法的用时
        node.setUseTime(node.getUseTime()+(System.nanoTime()-node.getBeginTime()));
    }

    public StackNode getHotStack() {
        return hotNode;
    }

    protected StackNode setHotStack(StackNode hot) {
        return hotNode = hot;
    }

    public void printStack(PrintStream out) {
        print(rootNode, out);
    }

    /**
     * 递归打印堆栈节点
     * @param node
     * @param out
     */
    private void print(StackNode node, PrintStream out) {
        out.println(node.toString());
        for (StackNode n : node.getChilds()) {
            print(n, out);
        }
    }
}
