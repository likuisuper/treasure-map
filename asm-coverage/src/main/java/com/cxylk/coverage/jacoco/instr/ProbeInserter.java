/*******************************************************************************
 * Copyright (c) 2009, 2016 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package com.cxylk.coverage.jacoco.instr;

import com.cxylk.coverage.model.StackSession;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/** Internal utility to add probes into the control flow of a method. The code
 * for a probe simply sets a certain slot of a boolean array to true. In
 * addition the probe array has to be retrieved at the beginning of the method
 * and stored in a local variable.
 */
class ProbeInserter extends MethodVisitor implements IProbeInserter {

    private final ClassInfo classInfo;

    /**
     * <code>true</code> if method is a class or interface initialization
     * method.
     */
    private final boolean clinit;

    /** Position of the inserted variable. */
    private final int variable;

    /** Maximum stack usage of the code to access the probe array. */
    private int accessorStackSize = 4;

    private int currentLine = -1;

    private final String methodName;
    int id = 0;
    private static final String sessionClassName;

    static {
        sessionClassName = StackSession.class.getName().replaceAll("[.]", "/");
    }

    /**
     * Creates a new {@link ProbeInserter}.
     * 
     * @param access access flags of the adapted method
     * @param name the method's name
     * @param desc the method's descriptor
     * @param mv the method visitor to which this adapter delegates calls
     */
    ProbeInserter(final int access, final String name, final String desc, final MethodVisitor mv, final ClassInfo classInfo) {
        super(InstrSupport.ASM_API_VERSION, mv);
        this.clinit = InstrSupport.CLINIT_NAME.equals(name);
        this.methodName = name+" "+desc;
        this.classInfo = classInfo;
        //如果是静态方法，那么局部变量表的槽位从0开始，否则从1开始（0存放this）
        int pos = (Opcodes.ACC_STATIC & access) == 0 ? 1 : 0;
        //计算占用局部变量表的大小
        for (final Type t : Type.getArgumentTypes(desc)) {
            pos += t.getSize();
        }
        variable = pos;
    }

    @Override
    public void insertProbe(final int id) {
        mv.visitVarInsn(Opcodes.ALOAD, variable);
        InstrSupport.push(mv, currentLine);
        //先将int转换为Integer，否则会报 Type integer (current frame, stack[1]) is not assignable to 'java/lang/Object'
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "equals", "(Ljava/lang/Object;)Z", false);
        mv.visitInsn(Opcodes.POP);

    }

    @Override
    public void visitInsn(final int opcode) {
        // ATHROW 属异常退出方法
        //return指令之前
        if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN)/* || opcode == Opcodes.ATHROW*/) {
            //将begin方法返回的结果从局部变量表推送至栈顶
            mv.visitVarInsn(Opcodes.ALOAD, variable);
            //调用StackSession的end方法
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, sessionClassName, "$end", "(Ljava/lang/Object;)V", false);
        }
        super.visitInsn(opcode);
    }


    @Override
    public void visitLineNumber(final int line, final Label start) {
        this.currentLine = line;
        super.visitLineNumber(line, start);
    }

    @Override
    public void visitCode() {
        // 在方法开始处添加begin方法
        // 将classId从常量池推送至栈顶
        mv.visitLdcInsn(classInfo.getClassId());
        // 将className从常量池推送至栈顶
        mv.visitLdcInsn(classInfo.getClassName());
        // 将methodName从常量池推送至栈顶
        mv.visitLdcInsn(methodName);
        // 调用begin方法
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, sessionClassName, "$begin",
                "(JLjava/lang/String;Ljava/lang/String;)Ljava/lang/Object;", false);
        mv.visitVarInsn(Opcodes.ASTORE, variable);
        super.visitCode();
    }

    @Override
    public final void visitVarInsn(final int opcode, final int var) {
        mv.visitVarInsn(opcode, map(var));
    }

    @Override
    public final void visitIincInsn(final int var, final int increment) {
        mv.visitIincInsn(map(var), increment);
    }

    @Override
    public final void visitLocalVariable(final String name, final String desc, final String signature, final Label start,
            final Label end, final int index) {
        mv.visitLocalVariable(name, desc, signature, start, end, map(index));
    }

    @Override
    public void visitMaxs(final int maxStack, final int maxLocals) {
        // Max stack size of the probe code is 3 which can add to the
        // original stack size depending on the probe locations. The accessor
        // stack size is an absolute maximum, as the accessor code is inserted
        // at the very beginning of each method when the stack size is empty.
        final int increasedStack = Math.max(maxStack + 3, accessorStackSize);
        mv.visitMaxs(increasedStack, maxLocals + 1);
    }

    private int map(final int var) {
        if (var < variable) {
            return var;
        } else {
            return var + 1;
        }
    }

    @Override
    public final void visitFrame(final int type, final int nLocal, final Object[] local, final int nStack, final Object[] stack) {

        if (type != Opcodes.F_NEW) { // uncompressed frame
            throw new IllegalArgumentException("ClassReader.accept() should be called with EXPAND_FRAMES flag");
        }

        final Object[] newLocal = new Object[Math.max(nLocal, variable) + 1];
        int idx = 0; // Arrays index for existing locals
        int newIdx = 0; // Array index for new locals
        int pos = 0; // Current variable position
        while (idx < nLocal || pos <= variable) {
            if (pos == variable) {
                newLocal[newIdx++] = InstrSupport.DATAFIELD_DESC;
                pos++;
            } else {
                if (idx < nLocal) {
                    final Object t = local[idx++];
                    newLocal[newIdx++] = t;
                    pos++;
                    if (t == Opcodes.LONG || t == Opcodes.DOUBLE) {
                        pos++;
                    }
                } else {
                    // Fill unused slots with TOP
                    newLocal[newIdx++] = Opcodes.TOP;
                    pos++;
                }
            }
        }
        mv.visitFrame(type, newIdx, newLocal, nStack, stack);
    }

}
