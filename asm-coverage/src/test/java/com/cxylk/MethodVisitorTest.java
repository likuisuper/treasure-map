package com.cxylk;

import jdk.internal.org.objectweb.asm.Opcodes;
import org.junit.Test;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.LocalVariablesSorter;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @author likui
 * @date 2022/9/6 上午10:39
 **/
public class MethodVisitorTest implements Opcodes {
    @Test
    public void newTest() throws IOException {
        ClassReader reader = new ClassReader("com.cxylk.coverage.Hello");
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES + ClassWriter.COMPUTE_MAXS);
        //添加语法校验适配器
        CheckClassAdapter checkClassAdapter = new CheckClassAdapter(writer);
        //添加classVisitor用于修改类
        ClassVisitor updateVisitor = new ClassVisitor(ASM5, checkClassAdapter) {
            /**
             * 在类的结束位置添加方法
             * 添加方法的常用做法：将需要添加的方法用正常Java代码写出来，然后在用ASMifer生成ASM代码
             */
            @Override
            public void visitEnd() {
                //调用visitMethod添加一个方法，指定名称等属性
                //其实就是调用下一个visitor的visitMethod，因为没有这个方法，所以最后会走到
                //ClassWriter的visitMethod方法，由它创建一个方法出来
                MethodVisitor mv = this.visitMethod(ACC_PUBLIC, "isSexy", "(Ljava/lang/Object;IZ)I", null, null);
                //表示方法的开始
                mv.visitCode();
                Label l0 = new Label();
                //label主要用于分支流程语句跳转
                mv.visitLabel(l0);
                //行号
                mv.visitLineNumber(33, l0);
                //调用静态方法out
                mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                //将hello从常量池推送至栈顶
                mv.visitLdcInsn("hello");
                //调用println方法
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
                Label l1 = new Label();
                mv.visitLabel(l1);
                mv.visitLineNumber(34, l1);
                //将int类型1推送至栈顶
                mv.visitInsn(ICONST_1);
                //添加return指令返回
                mv.visitInsn(IRETURN);
                Label l2 = new Label();
                mv.visitLabel(l2);
                //访问局部变量表，因为是实例方法，所以槽位为0的地方是this
                mv.visitLocalVariable("this", "Lcom/cxylk/Hello;", null, l0, l2, 0);
                mv.visitLocalVariable("a", "Ljava/lang/Object;", null, l0, l2, 1);
                mv.visitLocalVariable("b", "I", null, l0, l2, 2);
                mv.visitLocalVariable("c", "Z", null, l0, l2, 3);
                //操作数栈需要操作2次，局部变量表需要4个槽位，它们的大小都是由最大值决定
                mv.visitMaxs(2, 4);
                //该方法访问结束
                mv.visitEnd();
                //调用父类的visitEnd其实就是调用链条中下一个visitor，叫给下一个visitor处理，因为我们
                //在构造函数中指定了下一个visitor
                super.visitEnd();
            }

            //修改方法
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
                if ("hi".equals(name)) {
                    //LocalVariablesSorter自动计算局部变量表的位置
                    return new LocalVariablesSorter(ASM5, access, desc, methodVisitor) {
                        int time;

                        @Override
                        public void visitCode() {// 方法开始 加入begin
                            super.visitCode();
                            visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
                            //需要new一个局部变量
                            time = newLocal(Type.LONG_TYPE);
                            visitVarInsn(LSTORE, time);
                        }

                        @Override
                        public void visitInsn(int opcode) {
                            //在return之前添加
                            if (opcode >= IRETURN && opcode <= RETURN) {
                                visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                                visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
                                visitVarInsn(LLOAD, time);
                                visitInsn(LSUB);
                                visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(J)V", false);
                            }
                            super.visitInsn(opcode);
                        }

                        @Override
                        public void visitMaxs(int maxStack, int maxLocals) {
                            super.visitMaxs(4, 4);
                        }

                        @Override
                        public void visitEnd() {
                            //因为方法调用的顺序，所以不能在这里插入计算耗时的代码
                            super.visitEnd();
                        }

                    };
                }
                return methodVisitor;
            }
        };

        reader.accept(updateVisitor, 0);
        byte[] bytes = writer.toByteArray();
        Files.write(new File(System.getProperty("user.dir") + "/target/Hello2.class").toPath(), bytes);
    }

}
