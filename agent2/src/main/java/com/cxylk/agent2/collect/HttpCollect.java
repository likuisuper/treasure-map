package com.cxylk.agent2.collect;

import com.cxylk.agent2.base.AgentSession;
import com.cxylk.agent2.base.Collect;
import com.cxylk.agent2.collect.jdbc.JdbcCollect;
import com.cxylk.agent2.common.logger.Log;
import com.cxylk.agent2.common.logger.LogFactory;
import com.cxylk.agent2.common.util.JsonUtil;
import com.cxylk.agent2.model.HttpInfo;
import com.cxylk.coverage.model.StackNode;
import com.cxylk.coverage.model.StackSession;
import javassist.*;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author likui
 * @date 2022/8/12 下午5:48
 * HTTP采集器
 **/
public class HttpCollect implements Collect, ClassFileTransformer {
    private Logger logger = Logger.getLogger(HttpCollect.class.getName());

    @Override
    public void register(Instrumentation instrumentation) {
        instrumentation.addTransformer(this);
    }

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
            logger.info("插桩成功："+className);
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
        String beginSrc = "Object info=com.cxylk.agent2.collect.HttpCollect.begin($args);";
        String errorSrc = "com.cxylk.agent2.collect.HttpCollect.error(e,info);";
        String endSrc = "com.cxylk.agent2.collect.HttpCollect.end(info);";
        newMethod.setBody(String.format(voidSource, beginSrc, TARGET_METHOD, errorSrc, endSrc));
        ctClass.addMethod(newMethod);
        byte[] bytes = ctClass.toBytecode();
        //Files.write(new File("/Users/likui/Workspace/github/treasure-map/boot-example/target/HttpServlet$agent.class").toPath(), bytes);
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


    public static HttpInfo begin(Object[] args) {
        //开启会话，一次会话表示收集http、jdbc、service等信息，一次开启即可
        AgentSession agentSession = AgentSession.open();
        HttpInfo httpInfo = new HttpInfo();
        httpInfo.setBeginTime(LocalDateTime.now());
        HttpServletRequestAdapter requestAdapter = new HttpServletRequestAdapter(args[0]);
        httpInfo.setUrl(requestAdapter.getRequestURL());
        httpInfo.setClientIp(requestAdapter.getClientIp());
        httpInfo.setSpanId(agentSession.getParentId());
        //http是我们的采集入口，所以在这里开启动态代码覆盖率采集
        if(DynamicCodeCollect.isActive()){
            StackSession session=new StackSession(TARGET_CLASS,TARGET_METHOD);
        }
        return httpInfo;
    }

    public static void end(Object object) {
        HttpInfo httpInfo = (HttpInfo) object;
        httpInfo.setUseTime(LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli() - httpInfo.getBeginTime().toInstant(ZoneOffset.of("+8")).toEpochMilli());
        //关闭动态代码采集会话
        StackSession stackSession = StackSession.getCurrent();
        if(stackSession !=null){
            List<StackNode> allNodes = stackSession.getAllNodes();
            httpInfo.setCodeStack(JsonUtil.toJson(allNodes));
            stackSession.close();
        }
        //处理收集数据（json处理，日志打印）
        AgentSession agentSession = AgentSession.get();
        agentSession.push(httpInfo);
        //关闭当前会话，整个会话结束后（即http、jdbc等数据都采集完）
        AgentSession.close();

    }

    public static void error(Throwable e, Object o) {
        HttpInfo httpInfo = (HttpInfo) o;
        httpInfo.setError(e.getMessage());
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
