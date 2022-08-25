package com.cxylk.agent2.collect.jdbc;

import com.cxylk.agent2.base.Collect;
import com.cxylk.agent2.collect.HttpCollect;
import com.cxylk.agent2.collect.dubbo.DubboInvokerCollect;
import com.cxylk.agent2.common.logger.Log;
import com.cxylk.agent2.common.logger.LogFactory;
import javassist.*;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.logging.Logger;

/**
 * @author likui
 * @date 2022/8/11 上午11:56
 * sql执行流程：SQL请求-->JDBC template或orm框架-->JDBC-->data source-->driver-->db
 * 不对场景做假设，所以在jdbc层进行插桩
 **/
public class JdbcCollect implements ClassFileTransformer, Collect {
    private Logger logger = Logger.getLogger(JdbcCollect.class.getName());

    @Override
    public void register(Instrumentation instrumentation) {
        instrumentation.addTransformer(this);
    }

    /**
     * mysql驱动
     */
    private static final String TARGET_CLASS="com.mysql.cj.jdbc.NonRegisteringDriver";

    /**
     * 插桩方法为connect方法
     */
    private static final String TARGET_METHOD="connect";

    /**
     * 方法描述符
     */
    private static final String TARGET_METHOD_DESC="(Ljava/lang/String;Ljava/util/Properties;)Ljava/sql/Connection;";

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if(!TARGET_CLASS.replaceAll("\\.","/").equalsIgnoreCase(className)){
            return null;
        }
        try {
            logger.info("插桩成功："+className);
            return buildClass(loader);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private byte[] buildClass(ClassLoader loader) throws NotFoundException, CannotCompileException, IOException {
        ClassPool classPool=new ClassPool();
        classPool.appendSystemPath();
        classPool.insertClassPath(new LoaderClassPath(loader));
        CtClass ctClass = classPool.get(TARGET_CLASS);
        CtMethod oldMethod = ctClass.getMethod(TARGET_METHOD, TARGET_METHOD_DESC);
        CtMethod newMethod = CtNewMethod.copy(oldMethod, ctClass, null);
        oldMethod.setName(oldMethod.getName()+"$agent");
        //通过connect得到Connection对象后，对它进行代理，执行代理后的逻辑
        String endSrc="result=com.cxylk.agent2.collect.jdbc.JdbcCollects.proxyConnection((java.sql.Connection)result);";
        newMethod.setBody(String.format(SOURCE,TARGET_METHOD,endSrc));
        ctClass.addMethod(newMethod);
        return ctClass.toBytecode();
    }

    private static final String SOURCE="{\n" +
            "Object result=null;\n" +
            "       try{\n" +
            "           result=($w)%s$agent($$);\n" +
            "       }catch(Throwable e) {\n" +
            "           throw e;\n" +
            "       }\n" +
            "       finally{\n" +
            "%s" +
            "       }\n" +
            "       return ($r)result;\n" +
            "}\n";
}
