package io.nativeblocks.gradleplugin

internal object GlobalState {

    var endpoint: String? = null
    var authToken: String? = null
    var organizationId: String? = null
    var integrationTypes: Array<IntegrationType>? = null
    var basePackageName: String? = null
    var moduleName: String? = null
}