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

import com.github.megatronking.stringfog.IKeyGenerator;
import com.github.megatronking.stringfog.IStringFog;
import com.github.megatronking.stringfog.plugin.utils.TextUtils;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
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
    private final String mFogClassName;

    private boolean isClInitExists;

    private final List<ClassStringField> mStaticFinalFields = new ArrayList<>();
    private final List<ClassStringField> mStaticFields = new ArrayList<>();
    private final List<ClassStringField> mFinalFields = new ArrayList<>();
    private final List<ClassStringField> mFields = new ArrayList<>();

    private final IStringFog mStringFogImpl;
    private final StringFogMappingPrinter mMappingPrinter;
    private final IKeyGenerator mKeyGenerator;
    private String mClassName;

    private boolean mIgnoreClass;


    /* package */ StringFogClassVisitor(IStringFog stringFogImpl, StringFogMappingPrinter mappingPrinter,
                                        String fogClassName, ClassWriter cw, IKeyGenerator kg) {
        super(Opcodes.ASM9, cw);
        this.mStringFogImpl = stringFogImpl;
        this.mMappingPrinter = mappingPrinter;
        this.mFogClassName = fogClassName.replace('.', '/');
        this.mKeyGenerator = kg;
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
            if ((access & Opcodes.ACC_STATIC) == 0 && (access & Opcodes.ACC_FINAL) != 0) {
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
            mv = new StubMethodVisitor(Opcodes.ASM9, mv) {

                private String lastStashCst;

                @Override
                public void visitCode() {
                    super.visitCode();
                    // Here init static final fields.
                    for (ClassStringField field : mStaticFinalFields) {
                        if (!canEncrypted(field.value)) {
                            continue;
                        }
                        insertDecryptInstructions(field.value);
                        super.visitFieldInsn(Opcodes.PUTSTATIC, mClassName, field.name, ClassStringField.STRING_DESC);
                    }
                }

                @Override
                public void visitLdcInsn(Object cst) {
                    // Here init static or static final fields, but we must check field name int 'visitFieldInsn'
                    if (cst instanceof String && canEncrypted((String) cst)) {
                        lastStashCst = (String) cst;
                        insertDecryptInstructions(lastStashCst);
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
            mv = new StubMethodVisitor(Opcodes.ASM9, mv) {
                @Override
                public void visitLdcInsn(Object cst) {
                    // We don't care about whether the field is final or normal
                    if (cst instanceof String && canEncrypted((String) cst)) {
                        insertDecryptInstructions((String) cst);
                    } else {
                        super.visitLdcInsn(cst);
                    }
                }
            };
        } else {
            mv = new StubMethodVisitor(Opcodes.ASM9, mv) {

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
                        insertDecryptInstructions((String) cst);
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
            StubMethodVisitor mv = new StubMethodVisitor(Opcodes.ASM9, super.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null));
            mv.visitCode();
            // Here init static final fields.
            for (ClassStringField field : mStaticFinalFields) {
                if (!canEncrypted(field.value)) {
                    continue;
                }
                mv.insertDecryptInstructions(field.value);
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

    private String getJavaClassName() {
        return mClassName != null ? mClassName.replace('/', '.') : null;
    }

    /**
     * insert decrypt Instructions
     *
     * @author GreyWolf
     */
    private class StubMethodVisitor extends MethodVisitor {

        /**
         * Constructs a new {@link MethodVisitor}.
         *
         * @param api the ASM API version implemented by this visitor. Must be one
         *            of {@link Opcodes#ASM4} or {@link Opcodes#ASM5}.
         * @param mv  the method visitor to which this visitor must delegate method
         */
        public StubMethodVisitor(int api, MethodVisitor mv) {
            super(api, mv);
        }

        protected void insertDecryptInstructions(String originalValue) {
            byte[] key = mKeyGenerator.generate(originalValue);
            byte[] encryptValue = mStringFogImpl.encrypt(originalValue, key);
            pushArray(encryptValue);
            pushArray(key);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, mFogClassName, "decrypt", "([B[B)Ljava/lang/String;", false);
            mMappingPrinter.output(getJavaClassName(), originalValue, Arrays.toString(encryptValue));
        }

        private void pushArray(byte[] buffer) {
            pushNumber(buffer.length);
            mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BYTE);
            mv.visitInsn(Opcodes.DUP);
            for (int i = 0; i < buffer.length; i++) {
                pushNumber(i);
                pushNumber(buffer[i]);
                mv.visitInsn(Type.BYTE_TYPE.getOpcode(Opcodes.IASTORE));
                if (i < buffer.length - 1) mv.visitInsn(Opcodes.DUP);
            }
        }

        private void pushNumber(final int value) {
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
