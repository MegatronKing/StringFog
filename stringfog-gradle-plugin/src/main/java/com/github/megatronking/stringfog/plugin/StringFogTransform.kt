package com.github.megatronking.stringfog.plugin

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import com.android.build.gradle.internal.tasks.mlkit.codegen.ClassNames
import com.github.megatronking.stringfog.IKeyGenerator
import com.github.megatronking.stringfog.StringFogWrapper
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.objectweb.asm.ClassVisitor
import java.io.File

abstract class StringFogTransform : AsmClassVisitorFactory<StringFogInstrumentationParams> {

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        return with(parameters.get()) {
            ClassVisitorFactory.create(
                implementation, logs, extension.fogPackages, extension.kg, className.get(),
                classContext.currentClassData.className, extension.mode, nextClassVisitor
            )
        }
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        return true
    }

}