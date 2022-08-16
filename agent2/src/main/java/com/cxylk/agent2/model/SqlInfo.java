package com.cxylk.agent2.model;

import java.util.ArrayList;

/**
 * @author likui
 * @date 2022/8/12 下午5:43
 **/
public class SqlInfo extends BaseDataNode{
    // jdbc url
    public String jdbcUrl;
    // sql 语句
    public String sql;
    // 数据库名称
    public String databaseName;
    // 异常信息
    public String error;

    public ArrayList<ParamValues> params=new ArrayList();


    public static class ParamValues{
        public int index;
        public Object value;
        public ParamValues(int index, Object value) {
            this.index = index;
            this.value = value;
        }

        @Override
        public String toString() {
            return "ParamValues{" +
                    "index=" + index +
                    ", value=" + value +
                    '}';
        }
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public ArrayList<ParamValues> getParams() {
        return params;
    }

    public void setParams(ArrayList<ParamValues> params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return "SqlInfo{" +
                "jdbcUrl='" + jdbcUrl + '\'' +
                ", sql='" + sql + '\'' +
                ", databaseName='" + databaseName + '\'' +
                ", error='" + error + '\'' +
                ", params=" + params +
                '}';
    }
}
