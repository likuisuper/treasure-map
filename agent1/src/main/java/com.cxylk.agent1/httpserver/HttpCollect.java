package com.cxylk.agent1.httpserver;

import javassist.*;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.security.ProtectionDomain;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

/**
 * @author likui
 * @date 2022/4/24 上午11:29
 * http信息采集
 **/
public class HttpCollect implements ClassFileTransformer {
    /**
     * 现在绝大部分http服务都是使用http规范的，所以我们在servlet层插桩
     */
    private static final String TARGET_CLASS = "javax.servlet.http.HttpServlet";

    /**
     * 插桩的方法为service，只要是http请求最后都要走这个方法
     */
    private static final String TARGET_METHOD = "service";

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        //正则表达式中，使用\将下一个字符标记为特殊字符，比如这里的.来匹配点字符，而前面又加了一个\是因为你\.在
        //Java中不是一个转义字符，所以需要使用\来引用
        if (!TARGET_CLASS.replaceAll("\\.", "/").equals(className)) {
            return null;
        }
        try {
            return buildClass(loader, className);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] buildClass(ClassLoader loader, String className) throws NotFoundException, CannotCompileException, IOException {
        //获取类池
        ClassPool classPool = new ClassPool();
        //插入加载当前类的类加载路径
        classPool.insertClassPath(new LoaderClassPath(loader));
        CtClass ctClass = classPool.get(TARGET_CLASS);
        CtMethod oldMethod = ctClass.getDeclaredMethod(TARGET_METHOD);
        CtMethod newMethod = CtNewMethod.copy(oldMethod, ctClass, null);
        oldMethod.setName(oldMethod.getName() + "$agent");
        String beginSrc = "Object info=com.cxylk.agent1.httpserver.HttpCollect.begin($args);";
        String errorSrc = "com.cxylk.agent1.httpserver.HttpCollect.error(e,info);";
        String endSrc = "com.cxylk.agent1.httpserver.HttpCollect.end(info);";
        newMethod.setBody(String.format(voidSource, beginSrc, TARGET_METHOD, errorSrc, endSrc));
        ctClass.addMethod(newMethod);
        byte[] bytes = ctClass.toBytecode();
        Files.write(new File("/Users/likui/Workspace/github/treasure-map/boot-example/target/HttpServlet$agent.class").toPath(), bytes);
        return bytes;
    }

    static final String voidSource = "{\n"
            + "%s"
            + "      try{\n"
            + "          %s$agent($$);\n"
            + "      }catch(Throwable e){\n"
            + "%s"
            + "          throw e;\n"
            + "      }finally{\n"
            + "%s"
            + "      }\n"
            + "}\n";


    public static class HttpInfo {
        public LocalDateTime beginTime;
        public String url;
        public String clientIp;
        public String error;
        public long useTime;

        @Override
        public String toString() {
            return "HttpInfo{" +
                    "beginTime=" + beginTime +
                    ", url='" + url + '\'' +
                    ", clientIp='" + clientIp + '\'' +
                    ", error='" + error + '\'' +
                    ", useTime=" + useTime +
                    '}';
        }
    }

    public static HttpInfo begin(Object[] args) {
        HttpInfo httpInfo = new HttpInfo();
        httpInfo.beginTime = LocalDateTime.now();
        HttpServletRequestAdapter requestAdapter = new HttpServletRequestAdapter(args[0]);
        httpInfo.url = requestAdapter.getRequestURL();
        httpInfo.clientIp = requestAdapter.getClientIp();
        return httpInfo;
    }

    public static void end(Object object) {
        HttpInfo httpInfo = (HttpInfo) object;
        httpInfo.useTime = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli() - httpInfo.beginTime.toInstant(ZoneOffset.of("+8")).toEpochMilli();
        System.out.println(httpInfo);
    }

    public static void error(Throwable e, Object o) {
        HttpInfo httpInfo = (HttpInfo) o;
        httpInfo.error = e.getMessage();
        System.out.println(httpInfo);
    }

    private static class HttpServletRequestAdapter {
        private final Object target;
        private Method _getRequestURI;
        private Method _getRequestURL;
        private Method _getParameterMap;
        private Method _getMethod;
        private Method _getHeader;
        private Method _getRemoteAddr;
        private final static String targetClassName = "javax.servlet.http.HttpServletRequest";

        public HttpServletRequestAdapter(Object target) {
            this.target = target;


            try {
                Class<?> targetClass = target.getClass().getClassLoader().loadClass(targetClassName);
                _getRequestURI = targetClass.getMethod("getRequestURI");
                _getRequestURL = targetClass.getMethod("getRequestURL");
                _getParameterMap = targetClass.getMethod("getParameterMap");
                _getMethod = targetClass.getMethod("getMethod");
                _getHeader = targetClass.getMethod("getHeader",String.class);
                _getRemoteAddr = targetClass.getMethod("getRemoteAddr");
            } catch (NoSuchMethodException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        public String getRequestURI() {
            try {
                return (String) _getRequestURI.invoke(target);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public String getRequestURL() {
            try {
                return _getRequestURL.invoke(target).toString();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public Map<String, String[]> getParameterMap() {
            try {
                return (Map<String, String[]>) _getParameterMap.invoke(target);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public String getMethod() {
            try {
                return (String) _getMethod.invoke(target);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public String getHeader(String name) {
            try {
                return (String) _getHeader.invoke(target, name);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public String getRemoteAddr() {
            try {
                return (String) _getRemoteAddr.invoke(target);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public String getClientIp() {
            String ip = getHeader("x-forwarded-for");
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = getRemoteAddr();
            }
            return ip;
        }
    }
}
