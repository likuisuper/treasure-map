package com.cxylk.agent1.mybatis;

import javassist.*;

import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.nio.file.Files;
import java.security.ProtectionDomain;
import org.apache.ibatis.mapping.BoundSql;

/**
 * @author likui
 * @date 2022/3/29 下午8:31
 * 这种方式，当使用jar的方式启动后，会报BoundSql无法找到的问题
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
                //加入系统路径
                classPool.appendSystemPath();
                //还需要加入加载当前类的类加载器路径，否则会访问不到BaseExecutor的
                classPool.appendClassPath(new LoaderClassPath(loader));
                CtClass ctClass = classPool.get("org.apache.ibatis.executor.BaseExecutor");
                //这里监控的是第二个query方法，也就是参数最多的那个，可以查看增强后的代理类
                CtMethod ctMethod = ctClass.getDeclaredMethods("query")[1];
                //添加一个局部变量
                ctMethod.addLocalVariable("info",classPool.get(SqlInfo.class.getName()));
                ctMethod.insertBefore("info=com.cxylk.agent1.mybatis.MybatisAgent1.begin($args);");
                ctMethod.insertAfter("com.cxylk.agent1.mybatis.MybatisAgent1.end(info);");
                System.out.println("插桩成功:"+ctClass.getName());
                byte[] bytes = ctClass.toBytecode();
                Files.write(new File("/Users/likui/Workspace/github/treasure-map/boot-example/target/MybatisAgent1$proxy.class").toPath(),bytes);
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
        public static SqlInfo begin(Object[] args){
            SqlInfo sqlInfo=new SqlInfo();
            sqlInfo.setBeginTime(System.currentTimeMillis());
            //获取执行的SQL,BoundSql是query的第6个参数（从0开始）
            BoundSql boundSql=(BoundSql)args[5];
            sqlInfo.setSql(boundSql.getSql());
            //获取SQL参数，query的第2个参数
            Object para=args[1];
            sqlInfo.setParameter(para);
            return sqlInfo;
        }

        public static void end(SqlInfo sqlInfo){
            sqlInfo.setUseTime(System.currentTimeMillis()-sqlInfo.beginTime);
            System.out.println((sqlInfo.toString()));
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

        /**
         * 参数
         */
        private Object parameter;

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

        public Object getParameter() {
            return parameter;
        }

        public void setParameter(Object parameter) {
            this.parameter = parameter;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("SqlInfo{");
            sb.append("beginTime=").append(beginTime);
            sb.append(", useTime=").append(useTime);
            sb.append(", sql='").append(sql).append('\'');
            sb.append(", parameter=").append(parameter);
            sb.append('}');
            return sb.toString();
        }
    }
}
