package agent.jvmtool;

import javassist.*;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @Author likui
 * @Date 2021-11-06 15:34
 **/
public class ToolServiceImpl extends UnicastRemoteObject implements ToolService {
    public ToolServiceImpl() throws RemoteException {
    }


    @Override
    public String findClassName(String name) throws RemoteException {
        return Arrays.stream(Agent.instrumentation.getAllLoadedClasses())
                .filter(s -> s.getName().toUpperCase().contains(name.toUpperCase()))
                .limit(20)
                .map(Class::getName)
                .collect(Collectors.joining("\r\n"));
    }

    @Override
    public String findMethod(String name) throws RemoteException{
        String[] strings = name.split("/");
        String inputClassName=strings[0];
        String methodName=strings[1];
        final String[] result = new String[1];
        Agent.instrumentation.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                if(!inputClassName.contains(className)){
                    System.out.println(className);
                    return null;
                }
                try {
                    ClassPool classPool=new ClassPool();
                    classPool.appendClassPath("com.cxylk.agent");
                    classPool.appendSystemPath();
                    CtClass ctClass = classPool.get("com.cxylk.agent" + "." + inputClassName);
                    CtMethod declaredMethod = ctClass.getDeclaredMethod(methodName);
                    result[0] =declaredMethod.getSignature();
                    return ctClass.toBytecode();
                } catch (NotFoundException | CannotCompileException | IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
        return Arrays.toString(result);
//        return Arrays.stream(Agent.instrumentation.getAllLoadedClasses())
//                .filter(s -> s.getName().toUpperCase().contains(inputClassName.toUpperCase())).limit(20)
//                .map(f-> {
//                    try {
//                        System.out.println(f.getName());
//                        return f.getDeclaredMethod(methodName, Array.newInstance(String.class,1).getClass()).getName();
//                    } catch (NoSuchMethodException e) {
//                        e.printStackTrace();
//                    }
//                    return null;
//                }).collect(Collectors.joining("\r\n"));

    }

    @Override
    public String jadClass(String className) throws RemoteException {
        try {
            return Jad.decompiler(className);
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }


}
