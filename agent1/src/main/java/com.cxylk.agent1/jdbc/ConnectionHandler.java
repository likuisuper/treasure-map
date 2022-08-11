package com.cxylk.agent1.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;

/**
 * @author likui
 * @date 2022/8/11 下午3:26
 **/
public class ConnectionHandler implements InvocationHandler {
    private final Connection connection;

    /**
     * 代理Connection中的prepareStatement方法
     */
    private static final String[] CONNECTION_AGENT_METHODS=new String[]{"prepareStatement"};

    public ConnectionHandler(Connection connection) {
        this.connection=connection;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        boolean isTargetMethod = Arrays.stream(CONNECTION_AGENT_METHODS).anyMatch(m -> m.equals(method.getName()));
        Object result=null;
        SqlInfo sqlInfo=null;
        try {
            if(isTargetMethod){
                //获取PreparedStatement开始统计
                sqlInfo = JdbcCollects.begin(connection, (String) args[0]);
            }
            //不能影响原有的调用逻辑
            result = method.invoke(connection, args);
            if(isTargetMethod&&result instanceof PreparedStatement){
                PreparedStatement ps= (PreparedStatement) result;
                result=JdbcCollects.proxyStatement(ps,sqlInfo);
            }
        } catch (Throwable e) {
            JdbcCollects.error(sqlInfo,e);
        }
        return result;
    }
}
