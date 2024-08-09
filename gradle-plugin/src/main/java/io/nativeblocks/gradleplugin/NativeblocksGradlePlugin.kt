package io.nativeblocks.gradleplugin

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

open class NativeblocksGradlePlugin : Plugin<Project> {

    private val name = "nativeblocks"

    override fun apply(project: Project) {
        val extension = project.extensions.create(
            name, NativeblocksExtension::class.java
        )
        project.task("nativeblocksSync") { task ->
            task.doFirst {
                GlobalState.endpoint = extension.endpoint
                GlobalState.authToken = extension.authToken
                GlobalState.organizationId = extension.organizationId
                GlobalState.integrationType = extension.integrationType
                GlobalState.basePackageName = extension.basePackageName
                GlobalState.moduleName = extension.moduleName

                if (GlobalState.endpoint.isNullOrEmpty() ||
                    GlobalState.authToken.isNullOrEmpty() ||
                    GlobalState.organizationId.isNullOrEmpty()
                ) {
                    throw GradleException("Please make sure endpoint and authToken and organizationId has been provided correctly")
                }

                if (GlobalState.basePackageName.isNullOrEmpty() ||
                    GlobalState.moduleName.isNullOrEmpty()
                ) {
                    throw GradleException("Please make sure basePackageName and moduleName has been provided correctly")
                }
            }
                .dependsOn("build")
                .doLast {
                    println("All integrations have been synced with Nativeblocks servers")
                }
        }
    }
}
