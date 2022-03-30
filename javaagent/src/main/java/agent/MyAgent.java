package agent;

import javassist.*;

import java.io.IOException;
import java.lang.instrument.*;
import java.security.ProtectionDomain;

/**
 * @Classname MyAgent
 * @Description javaagent入口
 * @Author likui
 * @Date 2021-11-06 15:39
 **/
public class MyAgent {
    //加载时启动
    public static void premain(String args, Instrumentation instrumentation){
//        System.out.println("premain");
        HelloWorld helloWorld=new HelloWorld();
        //如果在下面方法执行之前加了上面这行代码，那么下面的逻辑就不会走了，因为它已经加载过了
        //拦截所有未加载的类
        instrumentation.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                //只需要拦截HelloWorld类即可
                if(!"com/cxylk/agent/HelloWorld".equals(className)){
                    return null;
                }

                //javassist
                try {
                    ClassPool classPool = new ClassPool();
                    //为了下面重定义时，能在原有的方法上进行操作，并且下面这两行的顺序不能换
//                    classPool.appendClassPath(new ByteArrayClassPath("com.cxylk.agent.HelloWorld",classfileBuffer));
                    classPool.appendSystemPath();
                    CtClass ctClass = classPool.get("com.cxylk.agent.HelloWorld");
                    CtMethod method = ctClass.getDeclaredMethod("hello");
                    method.insertBefore("System.out.println(\"插入前置逻辑\");");
                    return ctClass.toBytecode();
                } catch (NotFoundException | CannotCompileException | IOException e) {
                    e.printStackTrace();
                }
                //返回null说明按照原有的逻辑走，不进行拦截增强
                return null;
            }
        },true);
        //重写走过滤器，并且要设置两个TRUE
        try {
            instrumentation.retransformClasses(HelloWorld.class);
        } catch (UnmodifiableClassException e) {
            e.printStackTrace();
        }
        // 重新定义
        try {
            ClassPool pool=new ClassPool();
            pool.appendSystemPath();
            CtClass ctClass = pool.get("com.cxylk.agent.HelloWorld");
            CtMethod ctMethod = ctClass.getDeclaredMethod("hello");
            ctMethod.insertAfter("System.out.println(\"插入后置逻辑\");");
            instrumentation.redefineClasses(new ClassDefinition(HelloWorld.class,ctClass.toBytecode()));
        } catch (NotFoundException | CannotCompileException | IOException | UnmodifiableClassException | ClassNotFoundException e) {
            e.printStackTrace();
        }
//        helloWorld.hello();
    }

    //运行时启动
    public static void agentmain(String args,Instrumentation instrumentation){
        System.out.println("agentmain");
    }
}
