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

import com.cxylk.coverage.jacoco.flow.ClassProbesVisitor;
import com.cxylk.coverage.jacoco.flow.MethodProbesVisitor;
import org.objectweb.asm.*;

import java.util.Map.Entry;

/**
 * Adapter that instruments a class for coverage tracing.
 */
public class ClassInstrumenter extends ClassProbesVisitor {

    private final ClassInfo probeArrayStrategy;

    private String className;

    /**
     * Emits a instrumented version of this class to the given class visitor.
     * 
     * @param probeArrayStrategy this strategy will be used to access the probe
     *            array
     * @param cv next delegate in the visitor chain will receive the
     *            instrumented class
     */
    public ClassInstrumenter(final ClassInfo probeArrayStrategy, final ClassVisitor cv) {
        super(cv);
        this.probeArrayStrategy = probeArrayStrategy;
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature, final String superName,
            final String[] interfaces) {
        this.className = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public FieldVisitor visitField(final int access, final String name, final String desc, final String signature,
            final Object value) {
        InstrSupport.assertNotInstrumented(name, className);
        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodProbesVisitor visitMethod(final int access, final String name, final String desc, final String signature,
                                           final String[] exceptions) {

        InstrSupport.assertNotInstrumented(name, className);

        final MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);

        if (mv == null) {
            return null;
        }
        final MethodVisitor frameEliminator = new DuplicateFrameEliminator(mv);
        final ProbeInserter probeVariableInserter = new ProbeInserter(access, name, desc, frameEliminator, probeArrayStrategy);
        return new MethodInstrumenter(probeVariableInserter, probeVariableInserter);
    }

    @Override
    public void visitTotalProbeCount(final int count) {
        // probeArrayStrategy.addMembers(cv, count);
        // cv.visitField(InstrSupport.DATAFIELD_ACC,
        // InstrSupport.DATAFIELD_NAME, InstrSupport.DATAFIELD_DESC, null,
        // null);
        // 增加method probe size 初始方法 $jacocoProbeSize
        final MethodVisitor mv = cv.visitMethod(InstrSupport.INITMETHOD_ACC, InstrSupport.METHOD_PROBE_SIZE_NAME,
                InstrSupport.METHOD_PROBE_SIZE_DESC, null, null);
        mv.visitCode();
        int methodSize = probeArrayStrategy.getMethodProbeSizes().size();
        mv.visitLabel(new Label());
        InstrSupport.push(mv, methodSize);
        mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_INT);
        mv.visitInsn(Opcodes.DUP);
        for (Entry<Integer, Integer> e : probeArrayStrategy.getMethodProbeSizes().entrySet()) {
            InstrSupport.push(mv, e.getKey());
            InstrSupport.push(mv, e.getValue());
            mv.visitInsn(Opcodes.IASTORE);
            mv.visitInsn(Opcodes.DUP);
        }

        // InstrSupport.push(mv, 0);
        // InstrSupport.push(mv, 9999);
        // mv.visitInsn(Opcodes.IASTORE);
        mv.visitVarInsn(Opcodes.ASTORE, 1);

        mv.visitLabel(new Label());
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitVarInsn(Opcodes.ILOAD, 0);
        mv.visitInsn(Opcodes.IALOAD);
        mv.visitInsn(Opcodes.IRETURN);

        if (probeArrayStrategy.isWithFrames()) {
            // mv.visitFrame(Opcodes.F_NEW, 0, new Object[0], 1, new Object[] {
            // InstrSupport.METHOD_PROBE_SIZE_DESC });
        }
        mv.visitLabel(new Label());

        mv.visitMaxs(4, 2); // Maximum local stack size is 2
        mv.visitEnd();
    }

}
