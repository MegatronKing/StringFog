package com.github.megatronking.stringfog.plugin

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.Instrumentation
import com.github.megatronking.stringfog.IKeyGenerator
import com.github.megatronking.stringfog.StringFogWrapper
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.objectweb.asm.ClassVisitor
import java.io.Serializable

class StringFogExtensionWrapper(
    val packageName: String,
    val implementation: String,
    val generator: IKeyGenerator,
    val mode: StringFogMode,
    val enable: Boolean,
    val debug: Boolean,
    val fogPackages: Array<String>
) : Serializable {

    companion object {
        fun get(extension: StringFogExtension): StringFogExtensionWrapper {
            return StringFogExtensionWrapper(
                extension.packageName.orEmpty(),
                extension.implementation.orEmpty(),
                extension.kg,
                extension.mode,
                extension.enable,
                extension.debug,
                extension.fogPackages
            )
        }
    }

}

interface StringForParameters : InstrumentationParameters {

    @get:Input val logs: ListProperty<String>

    @get:Input val className: Property<String>

    @get:Input val extension: Property<StringFogExtensionWrapper>

}

abstract class SimpleStringFogClassVisitorFactory : AsmClassVisitorFactory<StringForParameters> {

    override fun createClassVisitor(
        classContext: ClassContext, nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        val logs = parameters.get().logs.get()
        val extension = parameters.get().extension.get()
        val className = parameters.get().className.get()
        return ClassVisitorFactory.create(
            StringFogWrapper(extension.implementation),
            logs.toMutableList(),
            extension.fogPackages,
            extension.generator,
            className,
            classContext.currentClassData.className,
            extension.mode,
            nextClassVisitor
        )
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        return true
    }

}

fun Instrumentation.transformClassesWithStringFog(
    namespace: String, stringFog: StringFogExtension
) {
    transformClassesWith(
        SimpleStringFogClassVisitorFactory::class.java, InstrumentationScope.PROJECT
    ) {
        it.logs.set(arrayListOf())
        it.extension.set(StringFogExtensionWrapper.get(stringFog))
        it.className.set("$namespace.${SourceGeneratingTask.FOG_CLASS_NAME}")
    }
}