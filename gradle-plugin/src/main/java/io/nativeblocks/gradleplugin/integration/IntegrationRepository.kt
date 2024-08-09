package io.nativeblocks.gradleplugin.integration

import io.nativeblocks.gradleplugin.GlobalState
import kotlinx.serialization.json.Json
import org.gradle.api.GradleException
import org.gradle.api.Project


class IntegrationRepository {

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
    }
}