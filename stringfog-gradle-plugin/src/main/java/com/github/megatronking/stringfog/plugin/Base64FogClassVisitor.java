/*
 * Copyright (C) 2017, Megatron King
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.megatronking.stringfog.plugin;


import com.github.megatronking.stringfog.lib.Base64Fog;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * We visit the class {@link Base64Fog} to modify the value of the default key.
 *
 * @author Megatron King
 * @since 2017/3/7 18:43
 */

public class Base64FogClassVisitor extends ClassVisitor {

    private static final String CLASS_FIELD_KEY_NAME = "DEFAULT_KEY";
    private static final String INJECT_METHOD_NAME1 = "encode";
    private static final String INJECT_METHOD_NAME2 = "decode";
    private static final String INJECT_METHOD_DESC = "(Ljava/lang/String;)Ljava/lang/String;";

    private String mKey;

    public Base64FogClassVisitor(String key, ClassWriter cw) {
        super(Opcodes.ASM5, cw);
        this.mKey = key;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        if (CLASS_FIELD_KEY_NAME.equals(name)) {
            value = mKey;
        }
        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if ((INJECT_METHOD_NAME1.equals(name) || INJECT_METHOD_NAME2.equals(name)) && INJECT_METHOD_DESC.equals(desc)) {
            mv = new MethodVisitor(Opcodes.ASM5, mv) {

                @Override
                public void visitLdcInsn(Object cst) {
                    if (Base64Fog.DEFAULT_KEY.equals(cst)) {
                        cst = mKey;
                    }
                    super.visitLdcInsn(cst);
                }
            };
        }
        return mv;
    }
}
