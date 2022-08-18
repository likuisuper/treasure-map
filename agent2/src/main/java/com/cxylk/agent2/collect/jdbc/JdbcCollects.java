package com.cxylk.agent2.collect.jdbc;

import com.cxylk.agent2.base.AgentSession;
import com.cxylk.agent2.model.SqlInfo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @author likui
 * @date 2022/8/11 下午3:24
 **/
public class JdbcCollects {

    public static SqlInfo begin(Connection connection, String sql) {
        SqlInfo sqlInfo = new SqlInfo();
        sqlInfo.setBeginTime(LocalDateTime.now());
        sqlInfo.setSql(sql);
        try {
            sqlInfo.setJdbcUrl(connection.getMetaData().getURL());
            sqlInfo.setDatabaseName(getDbName(sqlInfo.getJdbcUrl()));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return sqlInfo;
    }

    public static void end(SqlInfo info) {
        info.setUseTime(LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli() - info.getBeginTime().toInstant(ZoneOffset.of("+8")).toEpochMilli());
        //采集数据处理
        AgentSession agentSession = AgentSession.get();
        agentSession.push(info);
        //System.out.println(info);
    }

    public static void error(SqlInfo stat, Throwable throwable) {
        if (stat != null) {
            if (throwable instanceof InvocationTargetException) {
                stat.error = ((InvocationTargetException) throwable).getTargetException().getMessage();
            } else {
                stat.error = throwable.getMessage();
            }
        }
    }

    private static String getDbName(String url) {
        int index = url.indexOf("?");
        if (index != -1) {
            url = url.substring(0, index);
        }
        return url.substring(url.lastIndexOf("/") + 1);
    }


    public static Connection proxyConnection(Connection connection) {
        Object proxyInstance = Proxy.newProxyInstance(JdbcCollects.class.getClassLoader(),
                new Class[]{Connection.class}, new ConnectionHandler(connection));
        return (Connection) proxyInstance;
    }

    public static PreparedStatement proxyStatement(PreparedStatement preparedStatement, SqlInfo sqlInfo) {
        Object proxyInstance = Proxy.newProxyInstance(JdbcCollects.class.getClassLoader(),
                new Class[]{PreparedStatement.class}, new PreparedStatementHandler(preparedStatement, sqlInfo));
        return (PreparedStatement) proxyInstance;
    }
}
