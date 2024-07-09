package com.github.megatronking.stringfog.plugin

import com.android.build.api.instrumentation.InstrumentationParameters
import com.github.megatronking.stringfog.StringFogWrapper
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import java.lang.ref.WeakReference
import java.util.WeakHashMap

abstract class StringFogInstrumentationParams : InstrumentationParameters {
    @get:Input
    abstract val applicationId: Property<String>

    @get:Input
    abstract val className: Property<String>
}

private class NonSerializableParams(
    val logs: MutableList<String>,
    val implementation: StringFogWrapper
)

private val extensionForApplicationId = mutableMapOf<String, WeakReference<StringFogExtension>>()
private val extensionNonSerializableParams = WeakHashMap<StringFogExtension, NonSerializableParams>()

internal val StringFogInstrumentationParams.extension
    get() = extensionForApplicationId[applicationId.get()]?.get()
        ?: throw IllegalStateException("Extension has not been registered with setParameters")

private val StringFogInstrumentationParams.nonSerializableParameters
    get() = extension.let { extensionNonSerializableParams[it] }
        ?: throw IllegalStateException("runtimeParameters have not been registered with setParameters")

internal val StringFogInstrumentationParams.logs get() = nonSerializableParameters.logs

internal val StringFogInstrumentationParams.implementation get() = nonSerializableParameters.implementation

internal fun StringFogInstrumentationParams.setParameters(
    applicationId: String,
    extension: StringFogExtension,
    logs: MutableList<String>,
    className: String
) {
    this.applicationId.set(applicationId)
    this.className.set(className)
    extensionForApplicationId[applicationId] = WeakReference(extension)
    extensionNonSerializableParams[extension] = NonSerializableParams(
        logs = logs,
        implementation = StringFogWrapper(extension.implementation)
    )
    logs.add("stringfog impl: " + extension.implementation)
    logs.add("stringfog mode: " + extension.mode)
}