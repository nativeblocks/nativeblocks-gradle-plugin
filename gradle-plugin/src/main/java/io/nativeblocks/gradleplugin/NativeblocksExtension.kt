package io.nativeblocks.gradleplugin

import org.gradle.api.tasks.Input

open class NativeblocksExtension {

    @Input
    var endpoint: String? = null

    @Input
    var authToken: String? = null

    @Input
    var organizationId: String? = null

    @Input
    var integrationType: Array<IntegrationType>? = null

    @Input
    var basePackageName: String? = null

    @Input
    var moduleName: String? = null

}

enum class IntegrationType {
    BLOCK,
    ACTION
}