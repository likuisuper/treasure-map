package com.cxylk.agent1.mybatis;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import org.apache.ibatis.mapping.BoundSql;

import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.security.ProtectionDomain;

/**
 * @author likui
 * @date 2022/3/29 下午8:31
 * 相比MybatisAgent2，这个版本使用适配器通过反射的方式解决jar包启动无法找到BoundSql的问题
 **/
public class MybatisAgent3 implements ClassFileTransformer {

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
                classPool.appendClassPath(new LoaderClassPath(loader));
                CtClass ctClass = classPool.get("org.apache.ibatis.executor.BaseExecutor");
                //这里监控的是第二个query方法，也就是参数最多的那个，可以查看增强后的代理类
                CtMethod ctMethod = ctClass.getDeclaredMethods("query")[1];
                //添加一个局部变量
                ctMethod.addLocalVariable("info",classPool.get(SqlInfo.class.getName()));
                ctMethod.insertBefore("info=com.cxylk.agent1.mybatis.MybatisAgent3.begin($args);");
                ctMethod.insertAfter("com.cxylk.agent1.mybatis.MybatisAgent3.end(info);");
                System.out.println("插桩成功:"+ctClass.getName());
                byte[] bytes = ctClass.toBytecode();
                Files.write(new File("/Users/likui/Workspace/github/treasure-map/boot-example/target/MybatisAgent3$proxy.class").toPath(),bytes);
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
            //使用适配器的方式
            BoundSqlAdapter boundSqlAdapter = new BoundSqlAdapter(args[5]);
            sqlInfo.setSql(boundSqlAdapter.getSql());
            //获取SQL参数，query的第2个参数
            Object para=args[1];
            sqlInfo.setParameter(para);
            return sqlInfo;
        }

        public static void end(SqlInfo sqlInfo){
            sqlInfo.setUseTime(System.currentTimeMillis()-sqlInfo.beginTime);
            System.out.println((sqlInfo.toString()));
    }


    public static class BoundSqlAdapter{
            private Object target;

            private Class aClass;

            private Method getSql;

            public BoundSqlAdapter(Object target){
                this.target=target;
                if(this.aClass==null){
                    init(target.getClass());
                }
            }

            private synchronized void init(Class clazz){
                try {
                    aClass=clazz;
                    getSql=clazz.getDeclaredMethod("getSql");
                    getSql.setAccessible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            public String getSql(){
                try {
                    return (String) getSql.invoke(target);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
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
