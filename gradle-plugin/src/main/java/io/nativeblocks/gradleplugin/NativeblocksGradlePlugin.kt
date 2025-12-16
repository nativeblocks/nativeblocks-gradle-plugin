package io.nativeblocks.gradleplugin

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.google.devtools.ksp.gradle.KspExtension
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

        project.plugins.withId("com.google.devtools.ksp") {
            project.afterEvaluate {
                configureKsp(project)
            }
        }

        val supportedComponents =
            listOf(project.androidAppComponent(), project.androidLibraryComponent())
        supportedComponents.forEach { component ->
            component?.onVariants { variant ->
                val flavor = "${variant.flavorName?.capitalized()}${variant.buildType?.capitalized()}"
                registerTask(project, extension, flavor)
            }
        }
    }

    private fun configureKsp(project: Project) {
        val basePackageName = project.namespace()
        val moduleName = project.name

        project.extensions.findByType(KspExtension::class.java)?.apply {
            arg("basePackageName", basePackageName ?: "")
            arg("moduleName", moduleName ?: "")
        }
    }

    private fun registerTask(project: Project, extension: NativeblocksExtension, flavor: String) {
        project.tasks.register("nativeblocksSync$flavor") {
            GlobalState.endpoint = extension.endpoint
            GlobalState.authToken = extension.authToken
            GlobalState.organizationId = extension.organizationId
            GlobalState.basePackageName = project.namespace()
            GlobalState.moduleName = project.name

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
            runBlocking {
                integrationRepository.syncIntegration(project, flavor)
            }
        }
        project.tasks.register("nativeblocksPrepareSchema$flavor") {
            GlobalState.basePackageName = project.namespace()
            GlobalState.moduleName = project.name

            if (GlobalState.basePackageName.isNullOrEmpty() ||
                GlobalState.moduleName.isNullOrEmpty()
            ) {
                throw GradleException("Please make sure basePackageName and moduleName has been provided correctly")
            }

            val integrationRepository = IntegrationRepository()
            runBlocking {
                integrationRepository.prepareSchema(project, flavor)
            }
        }
    }
}

private fun Project.androidAppComponent(): ApplicationAndroidComponentsExtension? =
    extensions.findByType(ApplicationAndroidComponentsExtension::class.java)

private fun Project.androidLibraryComponent(): LibraryAndroidComponentsExtension? =
    extensions.findByType(LibraryAndroidComponentsExtension::class.java)

private fun Project.namespace(): String? {
    val androidExtension = project.extensions.findByName("android")
        ?: throw GradleException("Could not retrieve namespace from Android extension")
    return when (androidExtension) {
        is AppExtension -> androidExtension.namespace
        is LibraryExtension -> androidExtension.namespace
        else -> {
            try {
                androidExtension::class.java.getMethod("getNamespace").invoke(androidExtension) as? String
            } catch (e: Exception) {
                throw GradleException("Could not retrieve namespace from Android extension: ${e.message}")
            }
        }
    }
}