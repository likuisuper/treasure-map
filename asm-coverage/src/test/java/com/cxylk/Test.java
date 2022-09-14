package com.cxylk;

import com.cxylk.coverage.Hello;
import com.cxylk.coverage.jacoco.flow.ClassProbesAdapter;
import com.cxylk.coverage.jacoco.instr.ClassInfo;
import com.cxylk.coverage.jacoco.instr.ClassInstrumenter;
import com.cxylk.coverage.model.StackSession;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.CodeSource;

/**
 * @author likui
 * @date 2022/9/6 下午7:34
 **/
public class Test {
    public void A(String name){
        B();
        if(name.equals("lk")){
            //end
            return;
        }
        C();
        //end
    }
    public void B(){
        C();
    }
    public void C(){

        System.out.println("hello");
    }

    @org.junit.Test
    public void codeCoverageTest() throws InterruptedException {
        StackSession session=new StackSession("com.cxylk.Test","codeCoverageTest");
        new Test().A("dsd");
        session.close();
        session.printStack(System.out);
        Thread.sleep(Integer.MAX_VALUE);
    }

    @org.junit.Test
    public void codeSourceTest(){
        CodeSource codeSource = Hello.class.getProtectionDomain().getCodeSource();
        System.out.println(codeSource);
        System.out.println(codeSource.getLocation());
    }

    @org.junit.Test
    public void test() throws IOException {
        ClassReader reader = new ClassReader("com.cxylk.coverage.Hello");

        final ClassWriter writer = new ClassWriter(reader, 0);
        TraceClassVisitor trace=new TraceClassVisitor(writer,new PrintWriter(System.out));
        ClassInfo info = new ClassInfo(reader);
        final ClassVisitor visitor = new ClassProbesAdapter(new ClassInstrumenter(info, trace),
                true);
        reader.accept(visitor, ClassReader.EXPAND_FRAMES);

        byte[] bytes = writer.toByteArray();
    }

}
