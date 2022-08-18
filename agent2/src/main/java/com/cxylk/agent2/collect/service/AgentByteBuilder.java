package com.cxylk.agent2.collect.service;

import javassist.*;

import java.io.IOException;

/**
 * @author likui
 * @date 2022/8/17 上午11:32
 * 将service采集器中的一些模块抽取出来
 * 比如拷贝新方法，生成代理方法，给方法设值body，重新生成字节码等
 **/
public class AgentByteBuilder {
    private final CtClass ctClass;

    public AgentByteBuilder(CtClass ctClass) {
        this.ctClass = ctClass;
    }

    /**
     * 插入，监听代理方法
     * @param ctMethod
     * @param builder
     * @throws CannotCompileException
     */
    public void updateMethod(CtMethod ctMethod, MethodSrcBuilder builder) throws CannotCompileException {
        //1、拷贝新方法
        CtMethod newMethod = CtNewMethod.copy(ctMethod, ctClass, null);
        //2、给新方法设值名字（代理方法）
        newMethod.setName(ctMethod.getName() + "$agent");
        //3、将新方法加到类中
        ctClass.addMethod(newMethod);
        //原方法调用代理方法
        ctMethod.setBody(builder.buildSrc(ctMethod));
    }

    /**
     * 生成新的class 字节码 ，
     *
     * @return
     * @throws NotFoundException
     * @throws Exception
     */
    public byte[] toBytecode() throws IOException, CannotCompileException {
        return ctClass.toBytecode();
    }

    public static class MethodSrcBuilder {
        private String beginSrc;

        private String endSrc;

        private String errorSrc;

        public String getBeginSrc() {
            return beginSrc;
        }

        public void setBeginSrc(String beginSrc) {
            this.beginSrc = beginSrc;
        }

        public String getEndSrc() {
            return endSrc;
        }

        public void setEndSrc(String endSrc) {
            this.endSrc = endSrc;
        }

        public String getErrorSrc() {
            return errorSrc;
        }

        public void setErrorSrc(String errorSrc) {
            this.errorSrc = errorSrc;
        }

        public String buildSrc(CtMethod ctMethod) {
            //这里要区分方法有无返回值
            String template = null;
            try {
                template = "void".equals(ctMethod.getReturnType().getName()) ? voidSource : source;
                String bsrc = beginSrc == null ? "" : beginSrc;
                String eSrc = errorSrc == null ? "" : errorSrc;
                String enSrc = endSrc == null ? "" : endSrc;
                return String.format(template, bsrc, ctMethod.getName(), eSrc, endSrc);
            } catch (NotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 无返回值的方法
     */
    final static String voidSource = "{\n"
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

    /**
     * 有返回值的方法
     */
    final static String source = "{\n"
            + "%s"
            + "        Object result=null;\n"
            + "       try {\n"
            + "            result=($w)%s$agent($$);\n"
            + "        } catch (Throwable e) {\n"
            + "%s"
            + "            throw e;\n"
            + "        }finally{\n"
            + "%s"
            + "        }\n"
            + "        return ($r) result;\n"
            + "}\n";
}
