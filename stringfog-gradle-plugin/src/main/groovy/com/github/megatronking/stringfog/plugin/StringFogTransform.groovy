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

package com.github.megatronking.stringfog.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.api.BaseVariant
import com.android.utils.FileUtils
import com.github.megatronking.stringfog.plugin.utils.MD5
import com.google.common.collect.ImmutableSet
import com.google.common.io.Files
import groovy.io.FileType
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project

public abstract class StringFogTransform extends Transform {

    private static final String TRANSFORM_NAME = 'stringFog'

    protected String mKey;
    protected boolean mEnable;

    protected StringFogClassInjector mInjector;

    public StringFogTransform(Project project, DomainObjectSet<BaseVariant> variants) {
        project.afterEvaluate {
            mKey = project.stringfog.key
            if (mKey == null || mKey.length() == 0) {
                throw new IllegalArgumentException("Key of stringfog can not be empty!");
            }
            mEnable = project.stringfog.enable
            if (mEnable) {
                def applicationId = variants.first().applicationId
                def manifestFile = project.file("src/main/AndroidManifest.xml")
                if (manifestFile.exists()) {
                    def parsedManifest = new XmlParser().parse(
                            new InputStreamReader(new FileInputStream(manifestFile), "utf-8"))
                    if (parsedManifest != null) {
                        def packageName = parsedManifest.attribute("package")
                        if (packageName != null) {
                            applicationId = packageName
                        }
                    }
                }
                createFogClass(variants, applicationId)
                createInjector(project.stringfog.exclude, variants, applicationId)
            }
        }
    }

    public void createFogClass(DomainObjectSet<BaseVariant> variants, def applicationId) {
        variants.all { variant ->
            variant.outputs.forEach { output ->
                def processResources = output.processResources
                processResources.doLast {
                    def stringfogDir = applicationId.replace((char)'.', (char)'/')
                    def stringfogFile = new File(processResources.sourceOutputDir, stringfogDir + File.separator + "StringFog.java")
                    StringFogClassBuilder.buildStringFogClass(stringfogFile, processResources.sourceOutputDir,
                            applicationId, "StringFog", mKey)
                }
            }
        }
    }

    public void createInjector(String[] excludePackages, DomainObjectSet<BaseVariant> variants, def applicationId) {
        variants.all { variant ->
            if (mInjector == null) {
                mInjector = new StringFogClassInjector(excludePackages, applicationId + ".StringFog")
            }
            return true
        }
    }

    @Override
    public String getName() {
        return TRANSFORM_NAME
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return ImmutableSet.of(QualifiedContent.DefaultContentType.CLASSES)
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return null
    }

    @Override
    boolean isIncremental() {
        return true
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        def dirInputs = new HashSet<>()
        def jarInputs = new HashSet<>()

        // Collecting inputs.
        transformInvocation.inputs.each { input ->
            input.directoryInputs.each { dirInput ->
                dirInputs.add(dirInput)
            }
            input.jarInputs.each { jarInput ->
                jarInputs.add(jarInput)
            }
        }

        if (!dirInputs.isEmpty() || !jarInputs.isEmpty()) {
            File dirOutput = transformInvocation.outputProvider.getContentLocation(
                    "classes", getOutputTypes(), getScopes(), Format.DIRECTORY)
            FileUtils.mkdirs(dirOutput)
            if (!dirInputs.isEmpty()) {
                dirInputs.each { dirInput ->
                    if (transformInvocation.incremental) {
                        dirInput.changedFiles.each { entry ->
                            File fileInput = entry.getKey()
                            File fileOutput = new File(fileInput.getAbsolutePath().replace(
                                    dirInput.file.getAbsolutePath(), dirOutput.getAbsolutePath()))
                            FileUtils.mkdirs(fileOutput.parentFile)
                            Status fileStatus = entry.getValue()
                            switch(fileStatus) {
                                case Status.ADDED:
                                case Status.CHANGED:
                                    if (fileInput.isDirectory()) {
                                        return // continue.
                                    }
                                    if (!fileInput.getName().endsWith('.class') || !mEnable || mInjector == null) {
                                        Files.copy(fileInput, fileOutput)
                                    } else {
                                        mInjector.doFog2Class(fileInput, fileOutput, mKey)
                                    }
                                    break
                                case Status.REMOVED:
                                    if (fileOutput.exists()) {
                                        if (fileOutput.isDirectory()) {
                                            fileOutput.deleteDir()
                                        } else {
                                            fileOutput.delete()
                                        }
                                    }
                                    break
                            }
                        }
                    } else {
                        if (dirOutput.exists()) {
                            dirOutput.deleteDir()
                        }

                        dirInput.file.traverse(type: FileType.FILES) { fileInput ->
                            File fileOutput = new File(fileInput.getAbsolutePath().replace(dirInput.file.getAbsolutePath(), dirOutput.getAbsolutePath()))
                            FileUtils.mkdirs(fileOutput.parentFile)
                            if (!fileInput.getName().endsWith('.class') || !mEnable || mInjector == null) {
                                Files.copy(fileInput, fileOutput)
                            } else {
                                mInjector.doFog2Class(fileInput, fileOutput, mKey)
                            }
                        }
                    }
                }
            }

            if (!jarInputs.isEmpty()) {
                jarInputs.each { jarInput ->
                    File jarInputFile = jarInput.file
                    File jarOutputFile = transformInvocation.outputProvider.getContentLocation(
                            getUniqueHashName(jarInputFile), getOutputTypes(), getScopes(), Format.JAR
                    )

                    FileUtils.mkdirs(jarOutputFile.parentFile)

                    switch (jarInput.status) {
                        case Status.NOTCHANGED:
                            if (transformInvocation.incremental) {
                                break
                            }
                        case Status.ADDED:
                        case Status.CHANGED:
                            if (!mEnable || mInjector == null) {
                                Files.copy(jarInputFile, jarOutputFile)
                            } else {
                                mInjector.doFog2Jar(jarInputFile, jarOutputFile, mKey)
                            }
                            break
                        case Status.REMOVED:
                            if (jarOutputFile.exists()) {
                                jarOutputFile.delete()
                            }
                            break
                    }
                }
            }
        }

    }

    public String getUniqueHashName(File fileInput) {
        final String fileInputName = fileInput.getName()
        if (fileInput.isDirectory()) {
            return fileInputName
        }
        final String parentDirPath = fileInput.getParentFile().getAbsolutePath()
        final String pathMD5 = MD5.getMessageDigest(parentDirPath.getBytes())
        final int extSepPos = fileInputName.lastIndexOf('.')
        final String fileInputNamePrefix =
                (extSepPos >= 0 ? fileInputName.substring(0, extSepPos) : fileInputName)
        return fileInputNamePrefix + '_' + pathMD5
    }
}