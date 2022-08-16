/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cxylk.agent2.common.logger;


import com.cxylk.agent2.common.util.SystemUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.*;

/**
 * Hardcoded java.util.logging commons-logging implementation.
 * <p>
 * In addition, it curr
 */
class DirectJDKLog implements Log {
    private static FileHandler fileHandler;
    private static ConsoleHandler consoleHandler;

    private Level currentLevel;
    private String name;
    private Handler[] handlers;

    public DirectJDKLog(String name) {
        this.name = name;
        if (fileHandler == null && consoleHandler == null) {
            throw new IllegalStateException("未初始化日志处理器。调用DirectJDKLog.init 进行初始化");
        }
        handlers = new Handler[]{fileHandler, consoleHandler};
        currentLevel = fileHandler.getLevel();
    }

    static {
        File configFile = new File(SystemUtil.getAgentPath(), "conf/agent.conf");
        Properties prop = new Properties();
        if (configFile.exists() && configFile.isFile()) {
            try {
                FileInputStream input = new FileInputStream(configFile);
                prop.load(input);
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        init(prop);
    }

    protected static void init(Properties properties) {
        String logPath = properties.getProperty("log.path", SystemUtil.getAgentPath() + "/logs/");

        if (!new File(logPath).exists()) {
            new File(logPath).mkdirs();
        }
        //单个文件限制 单位M
        int limit = Integer.parseInt(properties.getProperty("log.limit", "100"));// 默认100M
        limit = limit * 1048576;
        int count = Integer.parseInt(properties.getProperty("log.count", "10"));
        Level level = toLevel(properties.getProperty("log.level", "info")); // off info  warn error debug
        Level consoleLevel = toLevel(properties.getProperty("log.console", "error"));// off info  warn error debug
        // 路径 项目目录名
        try {
            String pattern = logPath + "/" + SystemUtil.getPid() + "-%g.log";
            fileHandler = new FileHandler(pattern, limit, count, true);
            fileHandler.setLevel(level);//off info  warn error debug
            fileHandler.setFormatter(new SimpleFormatter());
            // 除级别外，其它采用jul原有配置
            consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(consoleLevel);

            //  写入初始日志
            String initMsg = String.format("pid= %s jvmPath=%s appPath=%s",
                            SystemUtil.getPid(),
                            System.getProperty("java.home"),
                            System.getProperty("user.dir"));
            LogRecord initLogRecord = new LogRecord(Level.INFO, initMsg);
            initLogRecord.setLoggerName("startLog");
            fileHandler.publish(initLogRecord);
        } catch (IOException e) {
            // 日志文件锁定失败
            e.printStackTrace();
        }


    }


    private static Level toLevel(String levelName) {
        if ("off".equals(levelName)) {
            return Level.OFF;
        } else if ("info".equals(levelName)) {
            return Level.INFO;
        } else if ("warn".equals(levelName)) {
            return Level.WARNING;
        } else if ("error".equals(levelName)) {
            return Level.SEVERE;
        } else if ("debug".equals(levelName)) {
            return Level.FINE;
        } else {
            throw new IllegalArgumentException("log level '" + levelName + "' is Illegality");
        }
    }

    @Override
    public final boolean isErrorEnabled() {
        return isLoggable(Level.SEVERE);
    }

    @Override
    public final boolean isWarnEnabled() {
        return isLoggable(Level.WARNING);
    }

    @Override
    public final boolean isInfoEnabled() {
        return isLoggable(Level.INFO);
    }

    @Override
    public final boolean isDebugEnabled() {
        return isLoggable(Level.FINE);
    }

    @Override
    public final boolean isFatalEnabled() {
        return isLoggable(Level.SEVERE);
    }

    @Override
    public final boolean isTraceEnabled() {
        return isLoggable(Level.FINER);
    }

    public boolean isLoggable(Level level) {
        return currentLevel.intValue() <= level.intValue();
    }

    @Override
    public final void debug(Object message) {
        log(Level.FINE, String.valueOf(message), null);
    }

    @Override
    public final void debug(Object message, Throwable t) {
        log(Level.FINE, String.valueOf(message), t);
    }

    @Override
    public final void trace(Object message) {
        log(Level.FINER, String.valueOf(message), null);
    }

    @Override
    public final void trace(Object message, Throwable t) {
        log(Level.FINER, String.valueOf(message), t);
    }

    @Override
    public final void info(Object message) {
        log(Level.INFO, String.valueOf(message), null);
    }

    @Override
    public final void info(Object message, Throwable t) {
        log(Level.INFO, String.valueOf(message), t);
    }

    @Override
    public final void warn(Object message) {
        log(Level.WARNING, String.valueOf(message), null);
    }

    @Override
    public final void warn(Object message, Throwable t) {
        log(Level.WARNING, String.valueOf(message), t);
    }

    @Override
    public final void error(Object message) {
        log(Level.SEVERE, String.valueOf(message), null);
    }

    @Override
    public final void error(Object message, Throwable t) {
        log(Level.SEVERE, String.valueOf(message), t);
    }

    @Override
    public final void fatal(Object message) {
        log(Level.SEVERE, String.valueOf(message), null);
    }

    @Override
    public final void fatal(Object message, Throwable t) {
        log(Level.SEVERE, String.valueOf(message), t);
    }

    // from commons logging. This would be my number one reason why java.util.logging
    // is bad - design by committee can be really bad ! The impact on performance of 
    // using java.util.logging - and the ugliness if you need to wrap it - is far
    // worse than the unfriendly and uncommon default format for logs. 

    private void log(Level level, String msg, Throwable ex) {
        if (!isLoggable(level)) {
            return;
        }
        // Hack (?) to get the stack trace.
        Throwable dummyException = new Throwable();
        StackTraceElement locations[] = dummyException.getStackTrace();
        // Caller will be the third element
        String cname = "unknown";
        String method = "unknown";
        if (locations != null && locations.length > 2) {
            StackTraceElement caller = locations[2];
            cname = caller.getClassName();
            method = caller.getMethodName();
        }


        LogRecord lr = new LogRecord(level, msg);
        lr.setSourceClassName(cname);
        lr.setSourceMethodName(method);
        lr.setThrown(ex);
        lr.setLoggerName(name);
        //  写入日志
        for (Handler handler : handlers) {
            handler.publish(lr);
        }
    }


    // for LogFactory
    static void release() {

    }

    static Log getInstance(String name) {
        return new DirectJDKLog(name);
    }
}


