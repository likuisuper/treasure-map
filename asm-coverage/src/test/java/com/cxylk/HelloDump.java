package com.cxylk;

import jdk.internal.org.objectweb.asm.*;
import org.junit.Test;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.nio.file.Files;

/**
 * @author likui
 * @date 2022/9/5 下午5:24
 * 用于验证ASMifier反向生成类创建的 ASM代码
 **/
public class HelloDump implements Opcodes {

    @Test
    public void test() throws Exception {
        byte[] bytes = dump();
        Files.write(new File(System.getProperty("user.dir")+"/target/Hello.class").toPath(),bytes);
    }

    /**
     * 生成的ASM代码
     * @return
     * @throws Exception
     */
    public static byte[] dump () throws Exception {

        ClassWriter cw = new ClassWriter(0);
        FieldVisitor fv;
        MethodVisitor mv;
        AnnotationVisitor av0;

        cw.visit(52, ACC_PUBLIC + ACC_SUPER, "com/cxylk/Hello", null, "java/lang/Object", null);

        cw.visitSource("Hello.java", null);

        {
            fv = cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, "a", "Ljava/lang/String;", null, "xxx");
            fv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitLineNumber(11, l0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLineNumber(12, l1);
            mv.visitInsn(RETURN);
            Label l2 = new Label();
            mv.visitLabel(l2);
            mv.visitLocalVariable("this", "Lcom/cxylk/Hello;", null, l0, l2, 0);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "hi", "(Ljava/lang/String;I)V", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitLineNumber(16, l0);
            mv.visitInsn(ICONST_1);
            mv.visitVarInsn(ISTORE, 3);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLineNumber(17, l1);
            mv.visitInsn(ICONST_2);
            mv.visitVarInsn(ISTORE, 4);
            Label l2 = new Label();
            mv.visitLabel(l2);
            mv.visitLineNumber(18, l2);
            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitVarInsn(ILOAD, 3);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
            Label l3 = new Label();
            mv.visitLabel(l3);
            mv.visitLineNumber(19, l3);
            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitVarInsn(ILOAD, 4);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
            Label l4 = new Label();
            mv.visitLabel(l4);
            mv.visitLineNumber(20, l4);
            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
            Label l5 = new Label();
            mv.visitLabel(l5);
            mv.visitLineNumber(21, l5);
            mv.visitInsn(RETURN);
            Label l6 = new Label();
            mv.visitLabel(l6);
            mv.visitLocalVariable("this", "Lcom/cxylk/Hello;", null, l0, l6, 0);
            mv.visitLocalVariable("name", "Ljava/lang/String;", null, l0, l6, 1);
            mv.visitLocalVariable("d", "I", null, l0, l6, 2);
            mv.visitLocalVariable("i", "I", null, l1, l6, 3);
            mv.visitLocalVariable("b", "I", null, l2, l6, 4);
            mv.visitMaxs(2, 5);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "hello", "(Ljava/lang/String;)V", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitLineNumber(25, l0);
            mv.visitInsn(ICONST_1);
            mv.visitVarInsn(ISTORE, 2);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLineNumber(26, l1);
            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitVarInsn(ILOAD, 2);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
            Label l2 = new Label();
            mv.visitLabel(l2);
            mv.visitLineNumber(27, l2);
            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
            Label l3 = new Label();
            mv.visitLabel(l3);
            mv.visitLineNumber(28, l3);
            mv.visitInsn(RETURN);
            Label l4 = new Label();
            mv.visitLabel(l4);
            mv.visitLocalVariable("this", "Lcom/cxylk/Hello;", null, l0, l4, 0);
            mv.visitLocalVariable("name", "Ljava/lang/String;", null, l0, l4, 1);
            mv.visitLocalVariable("i", "I", null, l1, l4, 2);
            mv.visitMaxs(2, 3);
            mv.visitEnd();
        }
        cw.visitEnd();

        return cw.toByteArray();
    }
}
