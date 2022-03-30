package com.cxylk.agent1;

import javassist.*;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.nio.file.Files;
import java.security.ProtectionDomain;

/**
 * @author likui
 * @date 2022/3/29 下午7:35
 **/
public class ServiceAgent implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {
        //监控UserService类
        //这里的类路径以斜杠分开，因为在字节码文件中类的分隔符就是斜杠，可以去看Java虚拟机规范
        if(!"com/cxylk/agent1/service/UserService".equals(className)){
            //返回null，按照原有逻辑加载，不进行增强
            return null;
        }
        try {
            ClassPool classPool=new ClassPool();
            classPool.appendSystemPath();
            CtClass ctClass = classPool.get("com.cxylk.agent1.service.UserService");
            //方法可以重载，所以这里取第一个
            CtMethod ctMethod = ctClass.getDeclaredMethods("findUser")[0];
            //1、拷贝新方法
            CtMethod newMethod = CtNewMethod.copy(ctMethod, ctClass, null);
            //2、给新方法设值名字
            newMethod.setName(newMethod.getName()+"$agent");
            //3、将新方法加到类中
            ctClass.addMethod(newMethod);
            //4、修改旧方法逻辑，调用代理方法（即上面设置的新方法）
            ctMethod.setBody("{ long begin=System.currentTimeMillis();\n" +
                    "            try{\n" +
                    "                findUser$agent($$);\n" +
                    "            }finally {\n" +
                    "                long end=System.currentTimeMillis();\n" +
                    "                System.out.println(end-begin);\n" +
                    "            }}");

            byte[] bytes = ctClass.toBytecode();
            Files.write(new File("/Users/likui/Workspace/github/treasure-map/agent1/target/server$proxy.class").toPath(),bytes);
            //返回增强后的类的字节码
            return bytes;
        } catch (NotFoundException | CannotCompileException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
