@file:Suppress("UnstableApiUsage")
rootProject.name = "Rat-race-card"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(":composeApp")
include(":shared")
include(":server")
include(":core")
include(":card")
include(":board")

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
