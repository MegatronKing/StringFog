package com.github.megatronking.stringfog.plugin;

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

@CacheableTask
abstract class SourceGeneratingTask @Inject constructor(): DefaultTask() {
    companion object {
        const val FOG_CLASS_NAME = "StringFog"
    }

    @get:Input
    abstract val genDir: Property<File>
    @get:Input
    abstract val applicationId: Property<String>
    @get:Input
    abstract val implementation: Property<String>
    @get:Input
    abstract val mode: Property<StringFogMode>

    @TaskAction
    fun injectSource() {

        if (!genDir.get().exists()) {
            genDir.get().mkdirs()
        }

        val outputFile = File(genDir.get(), applicationId.get().replace('.', File.separatorChar) + File.separator + "StringFog.java")
        StringFogClassGenerator.generate(outputFile, applicationId.get(), FOG_CLASS_NAME,
            implementation.get(), mode.get())
    }

}