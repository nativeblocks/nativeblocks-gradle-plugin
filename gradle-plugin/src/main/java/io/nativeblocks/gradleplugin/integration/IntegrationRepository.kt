package io.nativeblocks.gradleplugin.integration

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.network.okHttpClient
import io.nativeblocks.gradleplugin.GlobalState
import io.nativeblocks.gradleplugin.network.AuthorizationInterceptor
import io.nativeblocks.gradleplugin.network.NetworkRequestExecutor
import io.nativeblocks.gradleplugin.network.execute
import io.nativeblocks.network.SyncIntegrationDataMutation
import io.nativeblocks.network.SyncIntegrationEventsMutation
import io.nativeblocks.network.SyncIntegrationMutation
import io.nativeblocks.network.SyncIntegrationPropertiesMutation
import io.nativeblocks.network.type.IntegrationDataInput
import io.nativeblocks.network.type.IntegrationEventInput
import io.nativeblocks.network.type.IntegrationPropertyInput
import io.nativeblocks.network.type.SyncIntegrationDataInput
import io.nativeblocks.network.type.SyncIntegrationEventsInput
import io.nativeblocks.network.type.SyncIntegrationInput
import io.nativeblocks.network.type.SyncIntegrationPropertiesInput
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import org.gradle.api.GradleException
import org.gradle.api.Project
import java.util.concurrent.TimeUnit

class IntegrationRepository {

    private val authorizationInterceptor = AuthorizationInterceptor(GlobalState.authToken.orEmpty())

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authorizationInterceptor)
        .readTimeout(120, TimeUnit.SECONDS)
        .connectTimeout(120, TimeUnit.SECONDS)
        .build()

    private val apolloClient = ApolloClient.Builder()
        .httpServerUrl(GlobalState.endpoint.orEmpty())
        .okHttpClient(okHttpClient)
        .build()

    private val networkRequestExecutor = NetworkRequestExecutor()

    suspend fun syncIntegration(project: Project, kind: String) {
        val directoryPath =
            "generated/ksp/debug/resources/${GlobalState.basePackageName?.replace(".", "/")}/integration/consumer/$kind"
        val jsonDirectory = project.layout.buildDirectory.files(directoryPath)

        val files = jsonDirectory.first().listFiles()
        if (files.isNullOrEmpty()) {
            throw GradleException("There is no integration under $directoryPath, Please make sure implementation is correct and there is no build issue")
        }

        val integrationJsonList = files.map { f ->
            val integrationFile = f.listFiles()?.find { it.name == "integration.json" }
            val integrationJson = if (integrationFile?.exists() == true) {
                Json.parseToJsonElement(integrationFile.readText())
            } else {
                null
            }

            val propertiesFile = f.listFiles()?.find { it.name == "properties.json" }
            val propertiesJson = if (propertiesFile?.exists() == true) {
                Json.parseToJsonElement(propertiesFile.readText())
            } else {
                null
            }

            val eventsFile = f.listFiles()?.find { it.name == "events.json" }
            val eventsJson = if (eventsFile?.exists() == true) {
                Json.parseToJsonElement(eventsFile.readText())
            } else {
                null
            }

            val dataFile = f.listFiles()?.find { it.name == "data.json" }
            val dataJson = if (dataFile?.exists() == true) {
                Json.parseToJsonElement(dataFile.readText())
            } else {
                null
            }

            val slotsFile = f.listFiles()?.find { it.name == "slots.json" }
            val slotsJson = if (slotsFile?.exists() == true) {
                Json.parseToJsonElement(slotsFile.readText())
            } else {
                null
            }

            IntegrationMeta(integrationJson, propertiesJson, eventsJson, dataJson, slotsJson)
        }

        integrationJsonList.forEach { meta ->
            if (meta.integrationJson == null) return

            val request = apolloClient.mutation(
                SyncIntegrationMutation(
                    input = SyncIntegrationInput(
                        name = meta.integrationJson.jsonObject["name"]?.jsonPrimitive?.content.orEmpty(),
                        keyType = meta.integrationJson.jsonObject["keyType"]?.jsonPrimitive?.content.orEmpty(),
                        imageIcon = Optional.presentIfNotNull(meta.integrationJson.jsonObject["imageIcon"]?.jsonPrimitive?.content.orEmpty()),
                        price = meta.integrationJson.jsonObject["price"]?.jsonPrimitive?.intOrNull ?: 0,
                        description = Optional.presentIfNotNull(meta.integrationJson.jsonObject["description"]?.jsonPrimitive?.content.orEmpty()),
                        documentation = Optional.presentIfNotNull(meta.integrationJson.jsonObject["documentation"]?.jsonPrimitive?.content.orEmpty()),
                        platformSupport = meta.integrationJson.jsonObject["platformSupport"]?.jsonPrimitive?.content.orEmpty(),
                        public = meta.integrationJson.jsonObject["public"]?.jsonPrimitive?.booleanOrNull ?: false,
                        kind = meta.integrationJson.jsonObject["kind"]?.jsonPrimitive?.content.orEmpty(),
                        organizationId = GlobalState.organizationId.orEmpty()
                    )
                )
            )

            networkRequestExecutor.requestExecutor(request).execute({ res ->
                runBlocking {
                    val id = res?.syncIntegration?.id.orEmpty()
                    val name = res?.syncIntegration?.name.orEmpty()

                    meta.propertiesJson?.let { syncProperties(id, name, it) }
                    meta.eventsJson?.let { syncEvents(id, name, it) }
                    meta.dataJson?.let { syncData(id, name, it) }
                }
            }, { error ->
                throw GradleException("The ${meta.integrationJson.jsonObject["name"]?.jsonPrimitive?.content.orEmpty()} failed to upload because: ${error.message}")
            })
        }
    }


    private suspend fun syncProperties(
        integrationId: String,
        integrationName: String,
        propertiesJson: JsonElement,
    ) {
        val request = apolloClient.mutation(
            SyncIntegrationPropertiesMutation(
                input = SyncIntegrationPropertiesInput(
                    integrationId = integrationId,
                    organizationId = GlobalState.organizationId.orEmpty(),
                    properties = propertiesJson.jsonArray.map { property ->
                        IntegrationPropertyInput(
                            key = property.jsonObject["key"]?.jsonPrimitive?.content.orEmpty(),
                            value = Optional.presentIfNotNull(property.jsonObject["value"]?.jsonPrimitive?.content.orEmpty()),
                            type = property.jsonObject["type"]?.jsonPrimitive?.content.orEmpty(),
                            description = property.jsonObject["description"]?.jsonPrimitive?.content.orEmpty(),
                            valuePicker = property.jsonObject["valuePicker"]?.jsonPrimitive?.content.orEmpty(),
                            valuePickerGroup = property.jsonObject["valuePickerGroup"]?.jsonPrimitive?.content.orEmpty(),
                            valuePickerOptions = property.jsonObject["valuePickerOptions"]?.jsonPrimitive?.content.orEmpty()
                        )
                    }
                )
            )
        )
        runBlocking {
            networkRequestExecutor.requestExecutor(request).execute({ _ ->
            }, { error ->
                throw GradleException("The $integrationName failed to upload because: ${error.message}")
            })
        }
    }

    private suspend fun syncEvents(
        integrationId: String,
        integrationName: String,
        eventsJson: JsonElement,
    ) {
        val request = apolloClient.mutation(
            SyncIntegrationEventsMutation(
                input = SyncIntegrationEventsInput(
                    integrationId = integrationId,
                    organizationId = GlobalState.organizationId.orEmpty(),
                    events = eventsJson.jsonArray.map { event ->
                        IntegrationEventInput(
                            event = event.jsonObject["event"]?.jsonPrimitive?.content.orEmpty(),
                            description = event.jsonObject["description"]?.jsonPrimitive?.content.orEmpty(),
                        )
                    }
                )
            )
        )
        runBlocking {
            networkRequestExecutor.requestExecutor(request).execute({ _ ->
            }, { error ->
                throw GradleException("The $integrationName failed to upload because: ${error.message}")
            })
        }
    }

    private suspend fun syncData(
        integrationId: String,
        integrationName: String,
        dataJson: JsonElement,
    ) {
        val request = apolloClient.mutation(
            SyncIntegrationDataMutation(
                input = SyncIntegrationDataInput(
                    integrationId = integrationId,
                    organizationId = GlobalState.organizationId.orEmpty(),
                    data = dataJson.jsonArray.map { dataItem ->
                        IntegrationDataInput(
                            key = dataItem.jsonObject["key"]?.jsonPrimitive?.content.orEmpty(),
                            type = dataItem.jsonObject["type"]?.jsonPrimitive?.content.orEmpty(),
                            description = dataItem.jsonObject["description"]?.jsonPrimitive?.content.orEmpty(),
                        )
                    }
                )
            )
        )
        runBlocking {
            networkRequestExecutor.requestExecutor(request).execute({ _ ->
            }, { error ->
                throw GradleException("The $integrationName failed to upload because: ${error.message}")
            })
        }
    }

}