package com.cxylk.agent1.jdbc;

import java.util.ArrayList;

/**
 * @author likui
 * @date 2022/8/11 下午3:37
 **/
public class SqlInfo {
    public Long begin;// 时间戳
    public Long useTime;
    // jdbc url
    public String jdbcUrl;
    // sql 语句
    public String sql;
    // 数据库名称
    public String databaseName;
    // 异常信息
    public String error;
    //参数信息

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

    @Override
    public String toString() {
        return "SqlInfo{" +
                "begin=" + begin +
                ", useTime=" + useTime +
                ", jdbcUrl='" + jdbcUrl + '\'' +
                ", sql='" + sql + '\'' +
                ", databaseName='" + databaseName + '\'' +
                ", error='" + error + '\'' +
                ", params=" + params +
                '}';
    }
}
