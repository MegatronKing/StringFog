package com.github.megatronking.stringfog.plugin

import com.android.build.api.dsl.AndroidSourceSet
import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.AppExtension
import com.github.megatronking.stringfog.plugin.utils.Log
import groovy.xml.XmlParser
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.configurationcache.extensions.capitalized
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

class StringFogPlugin : Plugin<Project> {

    companion object {
        private const val PLUGIN_NAME = "stringfog"
        private const val FOG_CLASS_NAME = "StringFog"
    }

    override fun apply(project: Project) {
        project.extensions.create(PLUGIN_NAME, StringFogExtension::class.java)

        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.onVariants { variant ->
            // Check stringfog extension
            val stringfog = project.extensions.getByType(StringFogExtension::class.java)
            //support  log switch
            Log.setDebug(stringfog.debug)
            if (stringfog.implementation.isNullOrEmpty()) {
                throw IllegalArgumentException("Missing stringfog implementation config")
            }
            //can add debug disable?
            Log.v("StringFog variant.buildType = " + variant.buildType)
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
            var applicationId = parsedManifest.attribute("package")?.toString()
            if (applicationId.isNullOrEmpty()) {
                applicationId = stringfog.packageName
                if (applicationId.isNullOrEmpty()) {
                    throw IllegalArgumentException("Unable to resolve applicationId")
                }
            }
            project.tasks.getByName("preBuild").doLast {
                val variantName = variant.name.capitalized()
                val javaPreCompileTasks = project.getTasksByName("generate${variantName}Resources", true)
                if (javaPreCompileTasks.isEmpty()) {
                    throw IllegalArgumentException("Unable to resolve task javaPreCompile${variantName}")
                }
                javaPreCompileTasks.first().doFirst {
                    generateStringFogClass(applicationId!!, project.buildDir, variantName.lowercase(), stringfog.implementation!!, stringfog.mode)
                }
            }
            val printFile = File(project.buildDir, "outputs/mapping/${variant.name.lowercase()}/stringfog.txt")
            StringFogTransform.setParameters(stringfog, printFile, "$applicationId.$FOG_CLASS_NAME")
            variant.instrumentation.transformClassesWith(
                StringFogTransform::class.java,
                    InstrumentationScope.PROJECT) {
            }
            variant.instrumentation.setAsmFramesComputationMode(
                FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS
            )
        }
    }

    private fun generateStringFogClass(applicationId: String, buildDir: File, variant: String, implementation: String, mode: StringFogMode) {
        val stringfogDir = File(buildDir, "generated" +
                File.separatorChar + "source" + File.separatorChar + "BuildConfig" + File.separatorChar + variant)
        val outputFile = File(stringfogDir, applicationId.replace('.', File.separatorChar) + File.separator + "StringFog.java")
        StringFogClassGenerator.generate(outputFile, applicationId, FOG_CLASS_NAME,
            implementation, mode)
    }

}