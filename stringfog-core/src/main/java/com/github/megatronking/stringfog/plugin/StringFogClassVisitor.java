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

import com.github.megatronking.stringfog.Base64;
import com.github.megatronking.stringfog.IKeyGenerator;
import com.github.megatronking.stringfog.IStringFog;
import com.github.megatronking.stringfog.plugin.utils.TextUtils;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Visit the class to execute string fog.
 *
 * @author Megatron King
 * @since 2017/3/6 20:37
 */

/* package */ class StringFogClassVisitor extends ClassVisitor {

    private static final String IGNORE_ANNOTATION = "Lcom/github/megatronking/stringfog" +
            "/annotation/StringFogIgnore;";

    private boolean isClInitExists;

    private final List<ClassStringField> mStaticFinalFields = new ArrayList<>();
    private final List<ClassStringField> mStaticFields = new ArrayList<>();
    private final List<ClassStringField> mFinalFields = new ArrayList<>();
    private final List<ClassStringField> mFields = new ArrayList<>();

    private final IStringFog mStringFogImpl;
    private final List<String> mLogs;
    private final IKeyGenerator mKeyGenerator;
    private String mClassName;
    private final InstructionWriter mInstructionWriter;

    private boolean mIgnoreClass;


    /* package */ StringFogClassVisitor(IStringFog stringFogImpl, List<String> logs,
                                        String fogClassName, ClassVisitor cv, IKeyGenerator kg, StringFogMode mode) {
        super(Opcodes.ASM9, cv);
        this.mStringFogImpl = stringFogImpl;
        this.mLogs = logs;
        this.mKeyGenerator = kg;
        this.mLogs.add(fogClassName);
        fogClassName = fogClassName.replace('.', '/');
        if (mode == StringFogMode.base64) {
            this.mInstructionWriter = new Base64InstructionWriter(fogClassName);
        } else if (mode == StringFogMode.bytes) {
            this.mInstructionWriter = new ByteArrayInstructionWriter(fogClassName);
        } else {
            throw new IllegalArgumentException("Unknown stringfog mode: " + mode);
        }
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.mClassName = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        mIgnoreClass = IGNORE_ANNOTATION.equals(desc);
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        if (ClassStringField.STRING_DESC.equals(desc) && name != null && !mIgnoreClass) {
            // static final, in this condition, the value is null or not null.
            if ((access & Opcodes.ACC_STATIC) != 0 && (access & Opcodes.ACC_FINAL) != 0) {
                mStaticFinalFields.add(new ClassStringField(name, (String) value));
                value = null;
            }
            // static, in this condition, the value is null.
            if ((access & Opcodes.ACC_STATIC) != 0 && (access & Opcodes.ACC_FINAL) == 0) {
                mStaticFields.add(new ClassStringField(name, (String) value));
                value = null;
            }

            // final, in this condition, the value is null or not null.
            if ((access & Opcodes.ACC_STATIC) == 0 && (access & Opcodes.ACC_FINAL) != 0) {
                mFinalFields.add(new ClassStringField(name, (String) value));
                value = null;
            }

            // normal, in this condition, the value is null.
            if ((access & Opcodes.ACC_STATIC) == 0 && (access & Opcodes.ACC_FINAL) == 0) {
                mFields.add(new ClassStringField(name, (String) value));
                value = null;
            }
        }
        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (mv == null || mIgnoreClass) {
            return mv;
        }
        if ("<clinit>".equals(name)) {
            isClInitExists = true;
            // If clinit exists meaning the static fields (not final) would have be inited here.
            mv = new MethodVisitor(Opcodes.ASM9, mv) {

                private String lastStashCst;

                @Override
                public void visitCode() {
                    super.visitCode();
                    // Here init static final fields.
                    for (ClassStringField field : mStaticFinalFields) {
                        if (!canEncrypted(field.value)) {
                            continue;
                        }
                        encryptAndWrite(field.value, mv);
                        super.visitFieldInsn(Opcodes.PUTSTATIC, mClassName, field.name, ClassStringField.STRING_DESC);
                    }
                }

                @Override
                public void visitLdcInsn(Object cst) {
                    // Here init static or static final fields, but we must check field name int 'visitFieldInsn'
                    if (cst instanceof String && canEncrypted((String) cst)) {
                        lastStashCst = (String) cst;
                        encryptAndWrite(lastStashCst, mv);
                    } else {
                        lastStashCst = null;
                        super.visitLdcInsn(cst);
                    }
                }

                @Override
                public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                    if (mClassName.equals(owner) && lastStashCst != null) {
                        boolean isContain = false;
                        for (ClassStringField field : mStaticFields) {
                            if (field.name.equals(name)) {
                                isContain = true;
                                break;
                            }
                        }
                        if (!isContain) {
                            for (ClassStringField field : mStaticFinalFields) {
                                if (field.name.equals(name) && field.value == null) {
                                    field.value = lastStashCst;
                                    break;
                                }
                            }
                        }
                    }
                    lastStashCst = null;
                    super.visitFieldInsn(opcode, owner, name, desc);
                }
            };

        } else if ("<init>".equals(name)) {
            // Here init final(not static) and normal fields
            mv = new MethodVisitor(Opcodes.ASM9, mv) {
                @Override
                public void visitLdcInsn(Object cst) {
                    // We don't care about whether the field is final or normal
                    if (cst instanceof String && canEncrypted((String) cst)) {
                        encryptAndWrite((String) cst, mv);
                    } else {
                        super.visitLdcInsn(cst);
                    }
                }
            };
        } else {
            mv = new MethodVisitor(Opcodes.ASM9, mv) {

                @Override
                public void visitLdcInsn(Object cst) {
                    if (cst instanceof String && canEncrypted((String) cst)) {
                        // If the value is a static final field
                        for (ClassStringField field : mStaticFinalFields) {
                            if (cst.equals(field.value)) {
                                super.visitFieldInsn(Opcodes.GETSTATIC, mClassName, field.name, ClassStringField.STRING_DESC);
                                return;
                            }
                        }
                        // If the value is a final field (not static)
                        for (ClassStringField field : mFinalFields) {
                            // if the value of a final field is null, we ignore it
                            if (cst.equals(field.value)) {
                                super.visitVarInsn(Opcodes.ALOAD, 0);
                                super.visitFieldInsn(Opcodes.GETFIELD, mClassName, field.name, "Ljava/lang/String;");
                                return;
                            }
                        }
                        // local variables
                        encryptAndWrite((String) cst, mv);
                        return;
                    }
                    super.visitLdcInsn(cst);
                }

            };
        }
        return mv;
    }

    @Override
    public void visitEnd() {
        if (!mIgnoreClass && !isClInitExists && !mStaticFinalFields.isEmpty()) {
            MethodVisitor mv = super.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
            mv.visitCode();
            // Here init static final fields.
            for (ClassStringField field : mStaticFinalFields) {
                if (!canEncrypted(field.value)) {
                    continue;
                }
                encryptAndWrite(field.value, mv);
                mv.visitFieldInsn(Opcodes.PUTSTATIC, mClassName, field.name, ClassStringField.STRING_DESC);
            }
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(1, 0);
            mv.visitEnd();
        }
        super.visitEnd();
    }

    private boolean canEncrypted(String value) {
        return !TextUtils.isEmptyAfterTrim(value) && value.length() < 65536 >> 2 && mStringFogImpl.shouldFog(value);
    }

    private void encryptAndWrite(String value, MethodVisitor mv) {
        byte[] key = mKeyGenerator.generate(value);
        byte[] encryptValue = mStringFogImpl.encrypt(value, key);
        String result = mInstructionWriter.write(key, encryptValue, mv);
        mLogs.add(value + " -> " + result);
    }

    private String getJavaClassName() {
        return mClassName != null ? mClassName.replace('/', '.') : null;
    }

    private static abstract class InstructionWriter {

        private final String mFogClassName;

        InstructionWriter(String fogClassName) {
            mFogClassName = fogClassName;
        }

        abstract String write(byte[] key, byte[] value, MethodVisitor mv);

        protected void writeClass(MethodVisitor mv, String descriptor) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, mFogClassName, "decrypt", descriptor, false);
        }

    }

    private static class Base64InstructionWriter extends InstructionWriter {

        private Base64InstructionWriter(String fogClassName) {
            super(fogClassName);
        }

        @Override
        String write(byte[] key, byte[] value, MethodVisitor mv) {
            String base64Key = new String(Base64.encode(key, Base64.DEFAULT));
            String base64Value = new String(Base64.encode(value, Base64.DEFAULT));
            mv.visitLdcInsn(base64Value);
            mv.visitLdcInsn(base64Key);
            super.writeClass(mv, "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
            return base64Value;
        }

    }

    private static class ByteArrayInstructionWriter extends InstructionWriter {

        private ByteArrayInstructionWriter(String fogClassName) {
            super(fogClassName);
        }

        @Override
        String write(byte[] key, byte[] value, MethodVisitor mv) {
            pushArray(mv, value);
            pushArray(mv, key);
            super.writeClass(mv, "([B[B)Ljava/lang/String;");
            return Arrays.toString(value);
        }

        private void pushArray(MethodVisitor mv, byte[] buffer) {
            pushNumber(mv, buffer.length);
            mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BYTE);
            mv.visitInsn(Opcodes.DUP);
            for (int i = 0; i < buffer.length; i++) {
                pushNumber(mv, i);
                pushNumber(mv, buffer[i]);
                mv.visitInsn(Type.BYTE_TYPE.getOpcode(Opcodes.IASTORE));
                if (i < buffer.length - 1) mv.visitInsn(Opcodes.DUP);
            }
        }

        private void pushNumber(MethodVisitor mv, final int value) {
            if (value >= -1 && value <= 5) {
                mv.visitInsn(Opcodes.ICONST_0 + value);
            } else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
                mv.visitIntInsn(Opcodes.BIPUSH, value);
            } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
                mv.visitIntInsn(Opcodes.SIPUSH, value);
            } else {
                mv.visitLdcInsn(value);
            }
        }

    }

}
