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


import com.github.megatronking.stringfog.plugin.utils.TextUtils;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.File;

/**
 * A factory creates {@link ClassVisitor}.
 *
 * @author Megatron King
 * @since 2017/3/7 19:56
 */

public final class ClassVisitorFactory {

    private ClassVisitorFactory() {
    }

    public static ClassVisitor create(String[] excludePackages, String fogClassName, String className, String key, ClassWriter cw) {
        if (WhiteLists.inWhiteList(className, WhiteLists.FLAG_PACKAGE)
                || WhiteLists.inWhiteList(className, WhiteLists.FLAG_CLASS) || isInExcludePackages(excludePackages, className)) {
            System.out.println("StringFog Ignore: " + className);
            return createEmpty(cw);
        }
        return new StringFogClassVisitor(fogClassName, key, cw);
    }

    public static ClassVisitor createEmpty(ClassWriter cw) {
        return new ClassVisitor(Opcodes.ASM5, cw) {
        };
    }

    private static boolean isInExcludePackages(String[] excludePackages, String className) {
        if (excludePackages == null || excludePackages.length == 0 || TextUtils.isEmpty(className)) {
            return false;
        }
        for (String excludePackage : excludePackages) {
            if (className.replace(File.separatorChar, '.').startsWith(excludePackage + ".")) {
                return true;
            }
        }
        return false;
    }

}
