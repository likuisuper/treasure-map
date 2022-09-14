package com.cxylk;

import org.junit.Test;
import org.objectweb.asm.*;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Arrays;

/**
 * @author likui
 * @date 2022/9/2 下午4:06
 **/
public class ClassVisitorTest implements Opcodes {
    @Test
    public void printTest() throws IOException {
        //指定要读取的类
        ClassReader reader=new ClassReader("com.cxylk.coverage.Hello");
        ClassVisitor visitor=new ClassVisitor(ASM5) {
            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                super.visit(version, access, name, signature, superName, interfaces);
                System.out.println(version);
                System.out.println(access);
                System.out.println(name);
                System.out.println(signature);
                System.out.println(superName);
                System.out.println(Arrays.toString(interfaces));
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                System.out.println(name);
                if(!"hi".equals(name)) {
                    //因为ASM就是采用责任链的方式将visitor链起来，所以需要传递给下一个链条
                    return super.visitMethod(access, name, desc, signature, exceptions);
                }
                //每次调用都要返回一个新的methodVisitor
                MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
                return new MethodVisitor(ASM5,methodVisitor) {
                    @Override
                    public void visitParameter(String name, int access) {
                        System.out.println("参数:"+name);
                        super.visitParameter(name, access);
                    }

                    @Override
                    public void visitCode() {
                        super.visitCode();
                    }

                    @Override
                    public void visitInsn(int opcode) {
                        //jvm是不知道操作码助记符的，比如aload，它只知道0x15这种数字
                        //所以我们需要将opcode转换成助记符，可以利用Printer.OPCODES工具
                        System.out.println("指令码:"+ Printer.OPCODES[opcode]);
                        super.visitInsn(opcode);
                    }

                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                        System.out.println("方法调用指令码:"+ Printer.OPCODES[opcode]);
                        System.out.println(name);
                        System.out.println(desc);
                        super.visitMethodInsn(opcode, owner, name, desc, itf);
                    }

                    @Override
                    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
                        System.out.println("方法变量名称"+name);
                        System.out.println(desc);
                        System.out.println(index);
                        super.visitLocalVariable(name, desc, signature, start, end, index);
                    }
                };
            }

            /**
             * 字段的访问顺序在方法前面，所以虽然这里的顺序是visitField在visitMethod后面，但是
             * 字段的信息会在method信息之前输出
             * @param access
             * @param name
             * @param desc
             * @param signature
             * @param value
             * @return
             */
            @Override
            public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
                System.out.println(name);
                return super.visitField(access, name, desc, signature, value);
            }
        };
        reader.accept(visitor,0);
    }

    /**
     * 读取一个指定的类赋值
     * @throws IOException
     */
    @Test
    public void writeTest1() throws IOException {
        //指定要读取的类
        ClassReader reader=new ClassReader("com.cxylk.coverage.Hello");
        //ClassWrite也是一个访问者，它是责任链中的最后一个，用于复制读取到的类
        //flags传的参数表示自动计算最大堆栈大小和最大局部变量大小，栈帧映射
        ClassWriter writerVisitor=new ClassWriter(reader,ClassWriter.COMPUTE_FRAMES|ClassWriter.COMPUTE_MAXS);
        //一定要加入访问者，不然访问不了
        reader.accept(writerVisitor,0);
        //这时候得到的就是和Hello一模一样的class 字节数组
        byte[] bytes = writerVisitor.toByteArray();
        Files.write(new File(System.getProperty("user.dir")+"/target/Hello$agent.class").toPath(),bytes);
    }

    /**
     * 对一个类进行增删改
     * @throws IOException
     */
    @Test
    public void writeTest2() throws IOException {
        //指定要读取的类
        ClassReader reader=new ClassReader("com.cxylk.coverage.Hello");
        //ClassWrite也是一个访问者，它是责任链中的最后一个，用于复制读取到的类
        //flags传的参数表示自动计算最大堆栈大小和最大局部变量大小，栈帧映射
        ClassWriter writerVisitor=new ClassWriter(reader,ClassWriter.COMPUTE_FRAMES|ClassWriter.COMPUTE_MAXS);
        //新增一个visitor，用于增删改
        //第2个参数表示下一个访问者是谁
        ClassVisitor updateVisitor=new ClassVisitor(ASM5,writerVisitor) {
            /**
             * 访问类的头部，也就是说该方法是开始的地方
             */
            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                super.visit(version, access, name, signature, superName, interfaces);
            }

            /**
             * 访问类的头部，也就是结束访问
             * 注意，这里实现方法的顺序不是访问的顺序，也就是说这里看visitEnd在visitMethod前面，
             * 但是执行结果是visitEnd在最后面
             * 添加一个字段或方法可以在开始的地方添加，也可以在结束的地方添加，比如我们在这里添加一个方法
             */
            @Override
            public void visitEnd() {
                //添加方法（这里添加的方法是没有方法体的）
                //一定要在调用父类方法之前添加，并且最后一定要调用visitEnd通知访问者已经访问完了
                this.visitMethod(ACC_PUBLIC,"isSexy","(LJava/lang/Object;)I",null,null).visitEnd();
                super.visitEnd();
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                //删除方法，直接返回null
                if("hello".equals(name)){
                    return null;
                }
                //修改指定方法的名称
                if("hi".equals(name)){
                    return super.visitMethod(access, name+"$agent", desc, signature, exceptions);
                }else {
                    return super.visitMethod(access, name, desc, signature, exceptions);
                }
            }
        };
        //一定要加入访问者，不然访问不了
        reader.accept(updateVisitor,0);
        //这时候得到的就是和Hello一模一样的class 字节数组
        byte[] bytes = writerVisitor.toByteArray();
        Files.write(new File(System.getProperty("user.dir")+"/target/Hello$agent.class").toPath(),bytes);
    }

    /**
     * 新增一个类
     */
    @Test
    public void newTest() throws IOException {
        //构造函数不指定classReader
        ClassWriter writerVisitor=new ClassWriter(ClassWriter.COMPUTE_FRAMES|ClassWriter.COMPUTE_MAXS);
        //设置类信息
        writerVisitor.visit(V1_8, ACC_PUBLIC | ACC_ABSTRACT |ACC_INTERFACE,
                "com/cxylk/NewClass", null, "java/lang/Object",
                new String[] { "java/lang/Runnable" });
        //添加字段
        writerVisitor.visitField(ACC_PUBLIC | ACC_FINAL | ACC_STATIC, "name", "Ljava/lang/String;",
                null,"lk")
                .visitEnd();
        //添加方法
        writerVisitor.visitMethod(ACC_PUBLIC | ACC_ABSTRACT, "isSexy",
                "(Ljava/lang/Object;)I", null, null)
                .visitEnd();
        writerVisitor.visitEnd();
        byte[] bytes = writerVisitor.toByteArray();
        Files.write(new File(System.getProperty("user.dir")+"/target/NewClass.class").toPath(),bytes);
    }

    /**
     * 新增一个类,并且通过CheckClassAdapter进行语法验证
     * 如果不进行语法验证的话，即使语法错误也会生成，而不会提示
     */
    @Test
    public void newTestCheck() throws IOException {
        //构造函数不指定classReader
        ClassWriter writerVisitor=new ClassWriter(ClassWriter.COMPUTE_FRAMES|ClassWriter.COMPUTE_MAXS);
        //也是一个visitor，指定下一个访问者为writerVisitor
        CheckClassAdapter checkClassAdapter=new CheckClassAdapter(writerVisitor);
        //设置类信息
        checkClassAdapter.visit(V1_8, ACC_PUBLIC | ACC_ABSTRACT |ACC_INTERFACE,
                "com/cxylk/NewClass", null, "java/lang/Object",
                new String[] { "java/lang/Runnablel" });
        //添加字段
        checkClassAdapter.visitField(ACC_PUBLIC | ACC_FINAL | ACC_STATIC, "name", "Ljava/lang/String;",
                null,"lk")
                .visitEnd();
        //添加方法
        //比如描述符后面加了f，这时就会报错
        checkClassAdapter.visitMethod(ACC_PUBLIC | ACC_ABSTRACT, "isSexy",
                "(Ljava/lang/Object;)If", null, null)
                .visitEnd();
        checkClassAdapter.visitEnd();
        byte[] bytes = writerVisitor.toByteArray();
        Files.write(new File(System.getProperty("user.dir")+"/target/NewClass.class").toPath(),bytes);
    }

    /**
     * TraceClassVisitor用于打印类的字节码
     * ASMifier用于反向生成类创建的 ASM代码
     * @throws IOException
     */
    @Test
    public void traceTest() throws IOException {
        ClassReader reader=new ClassReader("com.cxylk.coverage.Hello");
        ASMifier asMifier=new ASMifier();
        //打印到控制台
        //TraceClassVisitor traceClassVisitor=new TraceClassVisitor(new PrintWriter(System.out));

        TraceClassVisitor traceClassVisitor=new TraceClassVisitor(null,asMifier,new PrintWriter(System.out));
        reader.accept(traceClassVisitor,0);
    }
}
