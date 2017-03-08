package com.github.megatronking.stringfog.plugin

import org.gradle.api.Plugin;
import org.gradle.api.Project;

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
    }
}
