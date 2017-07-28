package com.github.megatronking.stringfog.plugin;

import com.google.common.collect.ImmutableSet;
import com.squareup.javawriter.JavaWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.lang.model.element.Modifier;

/**
 * Generate the <code>StringFog</code> class.
 *
 * @author Megatron King
 * @since 17/7/28 12:00
 */

public final class StringFogClassBuilder {

    public static final void buildStringFogClass(File outputFile, File outputDir, String packageName, String className, String key) throws IOException {
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new IOException("Can not mkdirs the dir: " + outputDir);
        }
        JavaWriter javaWriter = new JavaWriter(new FileWriter(outputFile));
        javaWriter.emitPackage(packageName);
        javaWriter.emitEmptyLine();
        javaWriter.emitImports("com.github.megatronking.stringfog.lib.Base64Fog");
        javaWriter.emitEmptyLine();

        javaWriter.emitJavadoc("Generated code from StringFog gradle plugin. Do not modify!");
        javaWriter.beginType(className, "class", ImmutableSet.of(Modifier.PUBLIC, Modifier.FINAL));

        javaWriter.emitEmptyLine();
        javaWriter.beginMethod(String.class.getSimpleName(), "encode",
                ImmutableSet.of(Modifier.PUBLIC, Modifier.STATIC), String.class.getSimpleName(), "value");
        javaWriter.emitStatement("return " + "Base64Fog.encode(value, \"" + key + "\")");
        javaWriter.endMethod();

        javaWriter.emitEmptyLine();
        javaWriter.beginMethod(String.class.getSimpleName(), "decode",
                ImmutableSet.of(Modifier.PUBLIC, Modifier.STATIC), String.class.getSimpleName(), "value");
        javaWriter.emitStatement("return " + "Base64Fog.decode(value, \"" + key + "\")");
        javaWriter.endMethod();

        javaWriter.emitEmptyLine();
        javaWriter.endType();

        javaWriter.close();
    }

}
