package com.cxylk.coverage.collect;

import com.cxylk.coverage.common.Assert;
import com.cxylk.coverage.common.WildcardMatcher;
import com.cxylk.coverage.jacoco.flow.ClassProbesAdapter;
import com.cxylk.coverage.jacoco.instr.ClassInfo;
import com.cxylk.coverage.jacoco.instr.ClassInstrumenter;
import com.cxylk.coverage.jacoco.instr.InstrSupport;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.logging.Logger;

/**
 * @author likui
 * @date 2022/9/8 下午2:57
 **/
public class CodeStackCollect implements ClassFileTransformer {
    static Logger logger = Logger.getLogger(CodeStackCollect.class.getName());

    private WildcardMatcher includes;

    private WildcardMatcher excludes;

    private WildcardMatcher excludeInner;

    public CodeStackCollect(String includeExpr,String excludeExpr) {
        Assert.hasText(includeExpr, "code stack include express must be not null");
        // 排除监听器内部代码堆栈的跟踪
        excludeInner = new WildcardMatcher("com.cxylk.coverage.*");
        includes = new WildcardMatcher(includeExpr);
        // 默认 全部都不排除
        excludes = new WildcardMatcher(excludeExpr == null ? "" : excludeExpr);
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (loader == null || className == null) {
            return null;
        }
        if (!doFilter(loader, className.replaceAll("/","."), protectionDomain)) {
            return null;
        }
        //通过类的原始字节流（也就是没有修改过的）构造reader
        ClassReader reader=new ClassReader(classfileBuffer);
        //接口直接返回null
        if(InstrSupport.isInterface(reader)){
            return null;
        }
        final ClassWriter writer=new ClassWriter(reader,0);
        ClassInfo classInfo=new ClassInfo(reader);
        final ClassVisitor visitor=new ClassProbesAdapter(new ClassInstrumenter(classInfo,writer),true);
        reader.accept(visitor,ClassReader.EXPAND_FRAMES);
        return writer.toByteArray();
    }


    private boolean doFilter(ClassLoader loader, String className, ProtectionDomain protectionDomain) {
        if (loader == null)
            return false;
        if (!hasSourceLocation(protectionDomain))
            return false;
        if (excludeInner.matches(className))
            return false;
        if (excludes.matches(className))
            return false;
        if (!includes.matches(className))
            return false;
        return true;
    }

    /**
     * Checks whether this protection domain is associated with a source
     * location.
     *
     * @param protectionDomain protection domain to check (or <code>null</code>)
     * @return <code>true</code> if a source location is defined
     */
    private boolean hasSourceLocation(final ProtectionDomain protectionDomain) {
        if (protectionDomain == null) {
            return false;
        }
        final CodeSource codeSource = protectionDomain.getCodeSource();
        if (codeSource == null) {
            return false;
        }
        //获取类文件的路径，比如 file:xxx/xxxx
        return codeSource.getLocation() != null;
    }
}
