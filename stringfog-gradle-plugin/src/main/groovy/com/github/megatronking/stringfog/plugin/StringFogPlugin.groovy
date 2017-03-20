package com.github.megatronking.stringfog.plugin

import org.gradle.api.GradleException
import org.gradle.api.Plugin;
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException;

/**
 * The plugin defines some tasks.
 *
 * @author Megatron King
 * @since 2017/3/6 19:43
 */

public class StringFogPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        project.extensions.create('stringfog', StringFogExtension)

        def android = project.extensions.android
        android.registerTransform(new StringFogTransform(project))

        // throw an exception in instant run mode
        android.applicationVariants.all { variant ->
            def variantName = variant.name.capitalize()
            try {
                def instantRunTask = project.tasks.getByName("transformClassesWithInstantRunFor${variantName}")
                if (instantRunTask) {
                    throw new GradleException(
                            "StringFog does not support instant run mode, please trigger build"
                                    + " by assemble${variantName} or disable instant run"
                                    + " in 'File->Settings...'."
                    )
                }
            } catch (UnknownTaskException e) {
                // Not in instant run mode, continue.
            }
        }
    }
}
