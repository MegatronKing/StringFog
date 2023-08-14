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
import java.util.Locale

class StringFogPlugin : Plugin<Project> {

    companion object {
        private const val PLUGIN_NAME = "stringfog"
    }

    private fun forEachVariant(
        extension: BaseExtension,
        action: (com.android.build.gradle.api.BaseVariant) -> Unit
    ) {
        when {
            extension is AppExtension -> extension.applicationVariants.all(action)
            extension is LibraryExtension -> {
                extension.libraryVariants.all(action)
            }
            else -> throw GradleException(
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
            // We must get the package name to generate <package name>.StringFog.java
            val manifestFile = project.file("src/main/AndroidManifest.xml")
            if (!manifestFile.exists()) {
                throw IllegalArgumentException("Missing file $manifestFile")
            }
            val parsedManifest = XmlParser().parse(
                InputStreamReader(FileInputStream(manifestFile), "utf-8")
            )
            if (!manifestFile.exists()) {
                throw IllegalArgumentException("Failed to parse file $manifestFile")
            }
            val applicationId = parsedManifest.attribute("package")?.toString()
            if (applicationId.isNullOrEmpty()) {
                throw IllegalArgumentException("Unable to resolve applicationId")
            }

            forEachVariant(extension) { variant ->
                try {
                    val stringfogDir = File(project.buildDir, "generated" +
                            File.separatorChar + "source" + File.separatorChar + "stringFog" + File.separatorChar + variant.name.capitalized().lowercase())
                    val provider = project.tasks.register("generateStringFog${variant.name.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString()
                    }}", SourceGeneratingTask::class.java) { task ->
                        task.genDir.set(stringfogDir)
                        task.applicationId.set(applicationId)
                        task.implementation.set(stringfog.implementation)
                        task.mode.set(stringfog.mode)
                    }
                    variant.registerJavaGeneratingTask(provider, stringfogDir)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val printFile = File(project.buildDir, "outputs/mapping/${variant.name.lowercase()}/stringfog.txt")
            StringFogTransform.setParameters(stringfog, printFile, "$applicationId.${SourceGeneratingTask.FOG_CLASS_NAME}")
            variant.instrumentation.transformClassesWith(
                StringFogTransform::class.java,
                    InstrumentationScope.PROJECT) {
            }
            variant.instrumentation.setAsmFramesComputationMode(
                FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS
            )
        }
    }

}