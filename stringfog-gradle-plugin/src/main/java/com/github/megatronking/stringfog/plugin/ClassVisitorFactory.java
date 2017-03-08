package com.github.megatronking.stringfog.plugin;


import com.github.megatronking.stringfog.lib.Base64Fog;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

/**
 * A factory creates {@link ClassVisitor}.
 *
 * @author Megatron King
 * @since 2017/3/7 19:56
 */

public final class ClassVisitorFactory {

    private ClassVisitorFactory() {
    }

    public static ClassVisitor create(String className, String key, ClassWriter cw) {
        if (Base64Fog.class.getName().replace('.', '/').equals(className)) {
            return new Base64FogClassVisitor(key, cw);
        }
        if (WhiteLists.inWhiteList(className, WhiteLists.FLAG_PACKAGE) || WhiteLists.inWhiteList(className, WhiteLists.FLAG_CLASS)) {
            return createEmpty(cw);
        }
        return new StringFogClassVisitor(key, cw);
    }

    public static ClassVisitor createEmpty(ClassWriter cw) {
        return new ClassVisitor(Opcodes.ASM5, cw) {
        };
    }

}
