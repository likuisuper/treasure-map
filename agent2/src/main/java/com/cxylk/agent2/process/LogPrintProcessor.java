package com.cxylk.agent2.process;

import com.cxylk.agent2.base.Agent;
import com.cxylk.agent2.base.AgentSession;
import com.cxylk.agent2.base.Processor;
import com.cxylk.agent2.common.logger.Log;
import com.cxylk.agent2.common.logger.LogFactory;
import org.junit.Assert;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    static final Log logger = LogFactory.getLog(LogPrintProcessor.class);

    /**
     * 采用原生的jdk日志框架，日志会打印在控制台
     */
    //Logger logger = Logger.getLogger(LogPrintProcessor.class.getName());

    private FileWriter fileWriter;

    /**
     * 这里主要是为了做成apm,将日志通过elk展示，如果不需要，可以不用这步，直接在accept中打印
     */
    public LogPrintProcessor() {
        try {
            //默认的日志文件目录为当前项目根目录下的logs目录
            String defaultLog = System.getProperty("user.dir") + "/logs/";
            //可以自定义日志文件目录，比如在VM参数中添加log=xxx参数，追加写
            fileWriter=new FileWriter(openFile(Agent.config.getProperty("log", defaultLog)),true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File openFile(String rootDir) {
        if (rootDir == null || "".equals(rootDir)) {
            throw new IllegalArgumentException("log设置错误");
        }
        try {
            File root = new File(rootDir);
            //创建目录
            if (!root.exists() || !root.isDirectory()) {
                root.mkdirs();
            }
            //创建日志文件
            File file = new File(root, "apm-agent.log");
            if (file.exists()) {
                file.createNewFile();
            }
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public int order() {
        return 2;
    }

    @Override
    public STATUS accept(AgentSession agentSession, String s) {
        //这里需要注意，这个类的构造函数我们是不会调用的
        //只会调用accept方法，那么fileWriter是怎么被赋值的呢？
        //因为我们调用了logger的方法（带static修饰符的那个），而logger是静态字段，虽然加了final是个常量，但是它的引用不是一个常量
        //所以会触发当前类的构造方法,可以通过jclasslib查看clinit的字节码：
        //putstatic #32 <com/cxylk/agent2/process/LogPrintProcessor.logger : Lcom/cxylk/agent2/common/logger/Log;>
        //可以发现其中有putstatic指令，正是它触发了类的实例化方法
        try {
            //这里写入的两个日志文件是不一样的
            //logger写入的日志默认配置的是在target目录下的logs目录
            //而fileWriter写入的日志是我们在VM参数中指定的或者默认的项目根目录
            //这个日志主要是当前项目做成APM后通过kibana来展示的
            logger.info("写入日志-----:"+s);
            fileWriter.write( s +"\r\n");
            //刷新缓冲区，此时数据还没有写入文件，只写到缓冲区，后面还可以继续写
            fileWriter.flush();
        } catch (IOException e) {
            logger.error("日志写入失败",e);
        }finally {
            try {
                //一定要关闭，通知系统后面不能再写数据了，不然日志文件中会多打印日志
                fileWriter.close();
            } catch (IOException e) {
                logger.error("文件关闭失败",e);
            }
        }
        return STATUS.OVER;
    }

}
