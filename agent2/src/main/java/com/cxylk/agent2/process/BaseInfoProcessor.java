package com.cxylk.agent2.process;

import com.cxylk.agent2.base.Agent;
import com.cxylk.agent2.base.AgentSession;
import com.cxylk.agent2.base.Processor;
import com.cxylk.agent2.common.util.NetUtils;
import com.cxylk.agent2.model.BaseDataNode;
import jdk.nashorn.internal.ir.BaseNode;

/**
 * @author likui
 * @date 2022/8/16 下午2:39
 * 通信信息处理器
 **/
public class BaseInfoProcessor implements Processor {
    @Override
    public Object accept(AgentSession agentSession,Object o) {
        if(o instanceof BaseDataNode){
            ((BaseDataNode)o).setTraceId(agentSession.getTraceId());
            ((BaseDataNode)o).setAppName(Agent.config.getProperty("app.name","未定义"));
            ((BaseDataNode)o).setHost(NetUtils.getLocalHost());
            ((BaseDataNode)o).setModeType(o.getClass().getSimpleName());
            if(((BaseDataNode) o).getEndTime()==0){
                ((BaseDataNode) o).setEndTime(System.currentTimeMillis());
            }
        }
        return o;
    }
}
