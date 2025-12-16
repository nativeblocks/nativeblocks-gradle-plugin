package io.nativeblocks.gradleplugin

import kotlinx.serialization.Serializable

@Serializable
internal data class NativeblocksConfig(
    val endpoint: String = "",
    val authToken: String = "",
    val organizationId: String = ""
)