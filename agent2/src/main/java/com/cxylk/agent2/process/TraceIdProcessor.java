package com.cxylk.agent2.process;

import com.cxylk.agent2.base.AgentSession;
import com.cxylk.agent2.base.Processor;
import com.cxylk.agent2.model.BaseDataNode;

/**
 * @author likui
 * @date 2022/8/16 下午2:39
 **/
public class TraceIdProcessor implements Processor {
    @Override
    public Object accept(AgentSession agentSession,Object o) {
        if(o instanceof BaseDataNode){
            ((BaseDataNode)o).setTraceId(agentSession.getSession());
        }
        return o;
    }
}
