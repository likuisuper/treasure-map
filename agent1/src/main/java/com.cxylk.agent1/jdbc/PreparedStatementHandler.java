package com.cxylk.agent1.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;

/**
 * @author likui
 * @date 2022/8/11 下午3:52
 **/
public class PreparedStatementHandler implements InvocationHandler {
    private final PreparedStatement preparedStatement;

    private final SqlInfo sqlInfo;

    /**
     * 代理方法
     */
    private final String[] TARGET_METHODS=new String[]{"execute","executeQuery","executeUpdate"};

    public PreparedStatementHandler(PreparedStatement preparedStatement,SqlInfo sqlInfo) {
        this.preparedStatement=preparedStatement;
        this.sqlInfo=sqlInfo;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        boolean isTargetMethod=false;
        for (String targetMethod : TARGET_METHODS) {
            if(targetMethod.equals(method.getName())){
                isTargetMethod=true;
                break;
            }
        }
        //拦截赋值的set方法
        if(method.getName().startsWith("set")&&method.getParameterCount()==2){
            sqlInfo.params.add(new SqlInfo.ParamValues((Integer) args[0],args[1]));
        }
        Object result=null;
        try {
            result = method.invoke(preparedStatement, args);
        } catch (Throwable e) {
            if(isTargetMethod){
                JdbcCollects.error(sqlInfo,e);
            }
            throw e;
        }finally {
            if(isTargetMethod){
                JdbcCollects.end(sqlInfo);
            }
        }
        return result;
    }
}
