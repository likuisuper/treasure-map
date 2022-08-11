package com.cxylk.agent1.jdbc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author likui
 * @date 2022/8/11 下午3:24
 **/
public class JdbcCollects {

    public static SqlInfo begin(Connection connection, String sql) {
        SqlInfo sqlInfo = new SqlInfo();
        sqlInfo.begin = System.currentTimeMillis();
        sqlInfo.sql = sql;
        try {
            sqlInfo.jdbcUrl = connection.getMetaData().getURL();
            sqlInfo.databaseName = getDbName(sqlInfo.jdbcUrl);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return sqlInfo;
    }

    public static void end(SqlInfo info) {
        info.useTime = System.currentTimeMillis() - info.begin;
        System.out.println(info);
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
