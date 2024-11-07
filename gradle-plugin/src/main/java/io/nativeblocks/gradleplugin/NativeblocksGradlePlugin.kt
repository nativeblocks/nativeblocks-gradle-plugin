package io.nativeblocks.gradleplugin

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import io.nativeblocks.gradleplugin.integration.IntegrationRepository
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.configurationcache.extensions.capitalized

open class NativeblocksGradlePlugin : Plugin<Project> {

    private val name = "nativeblocks"

    override fun apply(project: Project) {
        val extension = project.extensions.create(name, NativeblocksExtension::class.java)

        val supportedComponents =
            listOf(project.androidAppComponent(), project.androidLibraryComponent())
        supportedComponents.forEach { component ->
            component?.onVariants { variant ->
                val flavor = "${variant.flavorName?.capitalized()}${variant.buildType?.capitalized()}"
                registerTask(project, extension, flavor)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun registerTask(project: Project, extension: NativeblocksExtension, flavor: String) {
        project.tasks.register("nativeblocksSync$flavor") {
            GlobalState.endpoint = extension.endpoint
            GlobalState.authToken = extension.authToken
            GlobalState.organizationId = extension.organizationId
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
            runBlocking {
                integrationRepository.syncIntegration(project, flavor)
            }
        }
        project.tasks.register("nativeblocksPrepareSchema$flavor") {
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
        }
    }
}

private fun Project.androidAppComponent(): ApplicationAndroidComponentsExtension? =
    extensions.findByType(ApplicationAndroidComponentsExtension::class.java)

private fun Project.androidLibraryComponent(): LibraryAndroidComponentsExtension? =
    extensions.findByType(LibraryAndroidComponentsExtension::class.java)

private fun Project.androidProject(): AppExtension? =
    extensions.findByType(AppExtension::class.java)

private fun Project.libraryProject(): LibraryExtension? =
    extensions.findByType(LibraryExtension::class.java)