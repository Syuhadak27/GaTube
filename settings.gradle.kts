import groovy.json.JsonSlurper

// Single source of truth for the project: app-config.json in the project root.
// pluginManagement runs in an isolated scope, so it reads the file itself.
pluginManagement {
    val pluginVersions = (groovy.json.JsonSlurper().parse(file("app-config.json")) as Map<*, *>)["build"] as Map<*, *>
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "com.android.application" -> useVersion(pluginVersions["agp"].toString())
                "org.jetbrains.kotlin.plugin.compose" -> useVersion(pluginVersions["kotlin"].toString())
            }
        }
    }
}

val config = JsonSlurper().parse(file("app-config.json")) as Map<*, *>

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

rootProject.name = config["projectName"].toString()
include(":app")
