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

abstract class StringFogTransform : AsmClassVisitorFactory<InstrumentationParameters.None> {

    companion object {
        private lateinit var className: String
        private lateinit var extension: StringFogExtension
        private lateinit var logs: List<String>
        private lateinit var implementation: StringFogWrapper

        fun setParameters(extension: StringFogExtension, logs: List<String>, className: String) {
            StringFogTransform.extension = extension
            StringFogTransform.className = className
            StringFogTransform.logs = logs
            implementation = StringFogWrapper(extension.implementation)
            logs.plus("stringfog impl: " + extension.implementation)
            logs.plus("stringfog mode: " + extension.mode)
        }
    }

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        return ClassVisitorFactory.create(
            implementation, logs, extension.fogPackages, extension.kg, className,
            classContext.currentClassData.className, extension.mode, nextClassVisitor
        )
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        return true
    }

}