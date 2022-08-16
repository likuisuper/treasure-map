package com.cxylk.agent2.process;

import com.cxylk.agent2.base.AgentSession;
import com.cxylk.agent2.base.Processor;
import com.cxylk.agent2.common.util.JsonUtil;

/**
 * @author likui
 * @date 2022/8/16 上午11:12
 * json处理
 **/
public class JsonSerializeProcessor implements Processor<Object,String> {
    @Override
    public int order() {
        return 1;
    }

    @Override
    public String accept(AgentSession agentSession, Object o) {
        return JsonUtil.toJson(o);
    }

}
