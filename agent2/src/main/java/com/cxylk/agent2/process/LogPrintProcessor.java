package com.cxylk.agent2.process;

import com.cxylk.agent2.base.AgentSession;
import com.cxylk.agent2.base.Processor;
import com.cxylk.agent2.common.logger.Log;
import com.cxylk.agent2.common.logger.LogFactory;

import java.util.logging.Logger;

/**
 * @author likui
 * @date 2022/8/16 上午11:12
 * 日志处理
 **/
public class LogPrintProcessor implements Processor<String, Processor.STATUS> {
    /**
     * 采用经过处理的LogFactory，会将日志存储到target/logs目录下的日志文件
     */
    //static Log logger = LogFactory.getLog(LogPrintProcessor.class);

    /**
     * 采用原生的jdk日志框架，日志会打印在控制台
      */
    Logger logger = Logger.getLogger(LogPrintProcessor.class.getName());

    @Override
    public int order() {
        return 2;
    }

    @Override
    public STATUS accept(AgentSession agentSession, String s) {
        logger.info(s);
        return STATUS.OVER;
    }

}
