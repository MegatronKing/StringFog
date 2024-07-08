package com.github.megatronking.stringfog.plugin

import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import groovy.xml.XmlParser
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.configurationcache.extensions.capitalized
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

class StringFogPlugin : Plugin<Project> {

    companion object {
        private const val PLUGIN_NAME = "stringfog"
    }

    private fun forEachVariant(
        extension: BaseExtension,
        action: (com.android.build.gradle.api.BaseVariant) -> Unit
    ) {
        when (extension) {
            is AppExtension -> extension.applicationVariants.all(action)
            is LibraryExtension -> {
                extension.libraryVariants.all(action)
            } else -> throw GradleException(
                "StringFog plugin must be used with android app," +
                        "library or feature plugin"
            )
        }
    }

    override fun apply(project: Project) {
        project.extensions.create(PLUGIN_NAME, StringFogExtension::class.java)
        val extension = project.extensions.findByType(BaseExtension::class.java)
            ?: throw GradleException("StringFog plugin must be used with android plugin")

        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.onVariants { variant ->
            // Check stringfog extension
            val stringfog = project.extensions.getByType(StringFogExtension::class.java)
            if (stringfog.implementation.isNullOrEmpty()) {
                throw IllegalArgumentException("Missing stringfog implementation config")
            }
            if (!stringfog.enable) {
                return@onVariants
            }
            var applicationId: String? = null
            // We must get the package name to generate <package name>.StringFog.java
            // Priority: AndroidManifest -> namespace -> stringfog.packageName
            val manifestFile = project.file("src/main/AndroidManifest.xml")
            if (manifestFile.exists()) {
                val parsedManifest = XmlParser().parse(
                    InputStreamReader(FileInputStream(manifestFile), "utf-8")
                )
                if (!manifestFile.exists()) {
                    throw IllegalArgumentException("Failed to parse file $manifestFile")
                }
                applicationId = parsedManifest.attribute("package")?.toString()
            }
            if (applicationId.isNullOrEmpty()) {
                applicationId = extension.namespace
            }
            if (applicationId.isNullOrEmpty()) {
                applicationId = stringfog.packageName
            }
            if (applicationId.isNullOrEmpty()) {
                throw IllegalArgumentException("Unable to resolve applicationId")
            }

            val logs = mutableListOf<String>()
            variant.instrumentation.transformClassesWith(
                StringFogTransform::class.java,
                InstrumentationScope.PROJECT
            ) { params ->
                params.setParameters(
                    applicationId,
                    stringfog,
                    logs,
                    "$applicationId.${SourceGeneratingTask.FOG_CLASS_NAME}"
                )
            }
            variant.instrumentation.setAsmFramesComputationMode(
                FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS
            )

            // TODO This will not work on Gradle 9.0
            forEachVariant(extension) {
                val generateTaskName = "generateStringFog${it.name.capitalized()}"
                if (project.getTasksByName(generateTaskName, true).isNotEmpty()) {
                    return@forEachVariant
                }
                val stringfogDir = File(project.buildDir, "generated" +
                        File.separatorChar + "source" + File.separatorChar + "stringFog" + File.separatorChar + it.name.capitalized().lowercase())
                val provider = project.tasks.register(generateTaskName, SourceGeneratingTask::class.java) { task ->
                    task.genDir.set(stringfogDir)
                    task.applicationId.set(applicationId)
                    task.implementation.set(stringfog.implementation)
                    task.mode.set(stringfog.mode)
                }
                it.registerJavaGeneratingTask(provider, stringfogDir)
            }
            // TODO Need a final task to write logs to file
//            val printFile = File(project.buildDir, "outputs/mapping/${variant.name.lowercase()}/stringfog.txt")
//            printFile.writeText(logs.joinToString("\n"))
        }
    }

}