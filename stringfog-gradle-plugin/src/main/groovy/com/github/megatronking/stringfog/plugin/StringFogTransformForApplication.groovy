package com.github.megatronking.stringfog.plugin

import com.android.build.api.transform.QualifiedContent
import com.android.build.gradle.api.BaseVariant
import com.google.common.collect.ImmutableSet
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project

/**
 * StringFog transform used in application.
 *
 * @author Megatron King
 * @since 17/7/28 12:28
 */

public class StringFogTransformForApplication extends StringFogTransform {

    public StringFogTransformForApplication(Project project, DomainObjectSet<BaseVariant> variants) {
        super(project, variants);
    }

    @Override
    public Set<QualifiedContent.Scope> getScopes() {
        return ImmutableSet.of(
                QualifiedContent.Scope.PROJECT,
                QualifiedContent.Scope.SUB_PROJECTS,
                QualifiedContent.Scope.PROJECT_LOCAL_DEPS,
                QualifiedContent.Scope.SUB_PROJECTS_LOCAL_DEPS,
                QualifiedContent.Scope.EXTERNAL_LIBRARIES
        );
    }

}
