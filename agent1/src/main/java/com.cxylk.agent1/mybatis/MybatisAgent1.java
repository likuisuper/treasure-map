package com.cxylk.agent1.mybatis;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;

import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.nio.file.Files;
import java.security.ProtectionDomain;

/**
 * @author likui
 * @date 2022/3/29 下午8:31
 **/
public class MybatisAgent1 implements ClassFileTransformer {
    @Override
    public byte[] transform(
            ClassLoader loader,
            String className,
            Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain,
            byte[] classfileBuffer) throws IllegalClassFormatException {
        if(!"org/apache/ibatis/executor/BaseExecutor".equals(className)){
            return null;
        }
        try{
            ClassPool classPool=new ClassPool();
            classPool.appendSystemPath();
            CtClass ctClass = classPool.get("org.apache.ibatis.executor.BaseExecutor");
            CtMethod ctMethod = ctClass.getDeclaredMethods("query")[0];
            //添加一个局部变量
            ctMethod.addLocalVariable("info",classPool.get("com.cxylk.agent1.mybatis.MybatisAgent.SqlInfo"));
            ctMethod.insertBefore("info=com.cxylk.agent1.mybatis.MybatisAgent.begin;");
            ctMethod.insertAfter("com.cxylk.agent1.mybatis.MybatisAgent.end($$);");
            System.out.println("插桩成功:+ctClass.getName()");
            byte[] bytes = ctClass.toBytecode();
            Files.write(new File("/Users/likui/Workspace/github/treasure-map/agent1/target/MybatisAgent1$proxy.class").toPath(),bytes);
            return bytes;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 开始时间
     * 因为执行SQL 时候可能是多个线程并发执行
     * 这样的话就会导致begin和end方法执行混乱
     * 所以通过传递一个局部变量的方式来确保线程安全
     * @return
     */
    public static SqlInfo begin(){
        SqlInfo sqlInfo=new SqlInfo();
        sqlInfo.setBeginTime(System.currentTimeMillis());
        return sqlInfo;
    }

    public static void end(SqlInfo sqlInfo){
        sqlInfo.setUseTime(System.currentTimeMillis());
        System.out.println(sqlInfo);
    }


    public static class SqlInfo{
        /**
         * SQL开始执行时间
         */
        private long beginTime;

        /**
         * SQL执行时长
         */
        private long useTime;

        /**
         * sql语句
         */
        private String sql;

        public long getBeginTime() {
            return beginTime;
        }

        public void setBeginTime(long beginTime) {
            this.beginTime = beginTime;
        }

        public long getUseTime() {
            return useTime;
        }

        public void setUseTime(long useTime) {
            this.useTime = useTime;
        }

        public String getSql() {
            return sql;
        }

        public void setSql(String sql) {
            this.sql = sql;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("SqlInfo{");
            sb.append("beginTime=").append(beginTime);
            sb.append(", useTime=").append(useTime);
            sb.append(", sql='").append(sql).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }
}
