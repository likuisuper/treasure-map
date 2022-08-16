package com.cxylk.agent2.base;

/**
 * @author likui
 * @date 2022/8/12 下午5:34
 * 定义处理器规范，包括各处理器的排序、具体实现
 **/
public interface Processor<P,R> {
    /**
     * 定义处理器执行顺序，默认为0
     * @return
     */
    default int order(){
        return 0;
    }

    /**
     * 处理器具体的实现逻辑，由子类实现
     * 会话的生命周期和处理器是一样的，所以这里可以添加会话参数，方便进行会话追踪
     * @param p
     * @return
     */
    R accept(AgentSession agentSession,P p);

    default void finish(){

    }

    /**
     * 处理器状态
     */
    enum STATUS{
        /**
         * 处理完成
         */
        OVER
    }
}
