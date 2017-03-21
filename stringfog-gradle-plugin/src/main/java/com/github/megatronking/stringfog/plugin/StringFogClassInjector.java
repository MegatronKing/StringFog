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

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class StringFogClassInjector {

    public static void doFog2Class(File classIn, File classOut, String key) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new BufferedInputStream(new FileInputStream(classIn));
            os = new BufferedOutputStream(new FileOutputStream(classOut));
            processClass(is, os, key);
        } finally {
            closeQuietly(os);
            closeQuietly(is);
        }
    }

    public static void doFog2Jar(File jarIn, File jarOut, String key) throws IOException {
        try {
            processJar(jarIn, jarOut, key, Charset.forName("UTF-8"), Charset.forName("UTF-8"));
        } catch (IllegalArgumentException e) {
            if ("MALFORMED".equals(e.getMessage())) {
                processJar(jarIn, jarOut, key, Charset.forName("GBK"), Charset.forName("UTF-8"));
            } else {
                throw e;
            }
        }
    }

    @SuppressWarnings("NewApi")
    private static void processJar(File jarIn, File jarOut, String key, Charset charsetIn, Charset charsetOut) throws IOException {
        ZipInputStream zis = null;
        ZipOutputStream zos = null;
        try {
            zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(jarIn)), charsetIn);
            zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(jarOut)), charsetOut);
            ZipEntry entryIn;
            Map<String, Integer> processedEntryNamesMap = new HashMap<>();
            while ((entryIn = zis.getNextEntry()) != null) {
                final String entryName = entryIn.getName();
                if (!processedEntryNamesMap.containsKey(entryName)) {
                    ZipEntry entryOut = new ZipEntry(entryIn);
                    entryOut.setCompressedSize(-1);
                    zos.putNextEntry(entryOut);
                    if (!entryIn.isDirectory()) {
                        if (entryName.endsWith(".class")) {
                            processClass(zis, zos, key);
                        } else {
                            copy(zis, zos);
                        }
                    }
                    zos.closeEntry();
                    processedEntryNamesMap.put(entryName, 1);
                }
            }
        } finally {
            closeQuietly(zos);
            closeQuietly(zis);
        }
    }

    private static void processClass(InputStream classIn, OutputStream classOut, String key) throws IOException {
        ClassReader cr = new ClassReader(classIn);
        ClassWriter cw = new ClassWriter(0);
        ClassVisitor cv = ClassVisitorFactory.create(cr.getClassName(), key, cw);
        cr.accept(cv, 0);
        classOut.write(cw.toByteArray());
        classOut.flush();
    }


    private static void closeQuietly(Closeable target) {
        if (target != null) {
            try {
                target.close();
            } catch (Exception e) {
                // Ignored.
            }
        }
    }

    private static int copy(InputStream in, OutputStream out) throws IOException {
        int total = 0;
        byte[] buffer = new byte[8192];
        int c;
        while ((c = in.read(buffer)) != -1) {
            total += c;
            out.write(buffer, 0, c);
        }
        return total;
    }
}
