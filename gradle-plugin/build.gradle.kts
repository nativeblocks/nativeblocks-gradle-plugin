import com.vanniktech.maven.publish.GradlePlugin
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("java-gradle-plugin")
    id("maven-publish")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.apollographql.apollo3").version("3.8.2")
    id("com.vanniktech.maven.publish")
}

gradlePlugin {
    plugins {
        create(ModuleInfo.ARTIFACT_ID) {
            id = ModuleInfo.GROUP_ID + "." + ModuleInfo.ARTIFACT_ID
            implementationClass = ModuleInfo.IMPLEMENTATION_CLASS
            displayName = ModuleInfo.ARTIFACT_ID
            description = ModuleInfo.DESCRIPTION
            version = ModuleInfo.VERSION
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

apollo {
    service("service") {
        packageName.set("io.nativeblocks.network")
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    coordinates(ModuleInfo.GROUP_ID, ModuleInfo.ARTIFACT_ID, ModuleInfo.VERSION)
    signAllPublications()
    configure(
        GradlePlugin(
            javadocJar = JavadocJar.Javadoc(),
            sourcesJar = true
        )
    )

    pom {
        name.set(ModuleInfo.ARTIFACT_ID)
        description.set(ModuleInfo.DESCRIPTION)
        url.set(ModuleInfo.URL)
        licenses {
            license {
                name = "NATIVEBLOCKS TERMS OF SERVICE"
                url = "https://nativeblocks.io/terms-of-service"
            }
        }
        developers {
            developer {
                name = "Nativeblocks"
                email = "dev@nativeblocks.io"
            }
        }
        scm {
            connection = "scm:git:github.com/nativeblocks/nativeblocks-gradle-plugin.git"
            developerConnection =
                "scm:git:ssh://github.com/nativeblocks/nativeblocks-gradle-plugin.git"
            url = "https://github.com/nativeblocks/nativeblocks-gradle-plugin"
        }
    }
}

dependencies {
    implementation(gradleApi())
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    //==========================remote===========================
    implementation("com.apollographql.apollo3:apollo-runtime:3.8.2")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.12")
    implementation("com.android.tools.build:gradle:8.4.1")
}

object ModuleInfo {
    const val GROUP_ID = "io.nativeblocks"
    const val ARTIFACT_ID = "nativeblocks-gradle-plugin"
    const val VERSION = "1.0.2"
    const val DESCRIPTION = "Nativeblocks gradle plugin for Android"
    const val URL = "https://nativeblocks.io"
    const val IMPLEMENTATION_CLASS = "io.nativeblocks.gradleplugin.NativeblocksGradlePlugin"
}

