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

import com.cxylk.coverage.jacoco.data.CRC64;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

import java.util.HashMap;
import java.util.Map;

/**
 * The strategy for regular classes adds a static field to hold the probe array
 * and a static initialization method requesting the probe array from the
 * runtime.
 */
public class ClassInfo {

    private final String className;
    private final long classId;
    private final boolean withFrames;
    private final int version;
    private final boolean isInterface;
    // 方法索引 ，探针数
    private final Map<Integer, Integer> methodProbeSizes;

    private int methodProbes[] = new int[0];

    public ClassInfo(final ClassReader reader) {
        className = reader.getClassName();
        version = InstrSupport.getVersion(reader);
        classId = CRC64.checksum(reader.b);
        withFrames = version >= Opcodes.V1_6;
        isInterface = InstrSupport.isInterface(reader);
        methodProbeSizes = new HashMap<Integer, Integer>();
    }

    public String getClassName() {
        return className;
    }

    public long getClassId() {
        return classId;
    }

    public boolean isWithFrames() {
        return withFrames;
    }

    public int getVersion() {
        return version;
    }

    public boolean isInterface() {
        return isInterface;
    }

    public Map<Integer, Integer> getMethodProbeSizes() {
        return methodProbeSizes;
    }

    public int[] getMethodProbes() {
        return methodProbes;
    }

    public void setMethodProbes(int[] methodProbes) {
        this.methodProbes = methodProbes;
    }

}
