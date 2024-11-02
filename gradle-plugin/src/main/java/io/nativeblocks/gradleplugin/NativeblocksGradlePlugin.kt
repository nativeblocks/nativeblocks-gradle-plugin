package io.nativeblocks.gradleplugin

import com.android.build.gradle.AppExtension
import io.nativeblocks.gradleplugin.integration.IntegrationRepository
import kotlinx.coroutines.runBlocking
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.configurationcache.extensions.capitalized

open class NativeblocksGradlePlugin : Plugin<Project> {

    private val name = "nativeblocks"

    override fun apply(project: Project) {
        val extension = project.extensions.create(name, NativeblocksExtension::class.java)
        project.pluginManager.withPlugin("com.android.application") {
            val androidExtension = project.extensions.findByType(AppExtension::class.java)
            androidExtension?.applicationVariants?.all { variant ->
                val flavor = "${variant.flavorName.capitalized()}${variant.buildType.name.capitalized()}"
                project.tasks.register("nativeblocksSync$flavor") { task ->
                    task.doFirst {
                        GlobalState.endpoint = extension.endpoint
                        GlobalState.authToken = extension.authToken
                        GlobalState.organizationId = extension.organizationId
                        GlobalState.integrationTypes = extension.integrationTypes
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

                        val integrationRepository = IntegrationRepository()
                        if (GlobalState.integrationTypes?.find { it == IntegrationType.BLOCK } != null) {
                            runBlocking {
                                integrationRepository.syncIntegration(project, "block", flavor)
                            }
                        }
                        if (GlobalState.integrationTypes?.find { it == IntegrationType.ACTION } != null) {
                            runBlocking {
                                integrationRepository.syncIntegration(project, "action", flavor)
                            }
                        }
                    }.doLast {
                        println("All integrations have been synced with Nativeblocks servers")
                    }
                }
                project.tasks.register("nativeblocksPrepareSchema$flavor") { task ->
                    task.doFirst {
                        GlobalState.basePackageName = extension.basePackageName
                        GlobalState.moduleName = extension.moduleName

                        if (GlobalState.basePackageName.isNullOrEmpty() ||
                            GlobalState.moduleName.isNullOrEmpty()
                        ) {
                            throw GradleException("Please make sure basePackageName and moduleName has been provided correctly")
                        }

                        val integrationRepository = IntegrationRepository()
                        runBlocking {
                            integrationRepository.prepareSchema(project, flavor)
                        }
                    }.doLast {
                        println("All integrations json copied under ${project.rootDir.path + "/.nativeblocks"}")
                    }
                }
            }
        }
    }
}
