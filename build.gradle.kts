plugins {
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    alias(libs.plugins.compose).apply(false)
    alias(libs.plugins.compose.hot.reload).apply(false)
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.buildConfig).apply(false)
    alias(libs.plugins.kotlinx.serialization).apply(false)
    alias(libs.plugins.google.services).apply(false)
    alias(libs.plugins.firebase.crashlytics).apply(false)
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
}


buildscript {
    dependencies {
        classpath("commons-codec:commons-codec:1.18.0")
    }
    configurations.all {
        resolutionStrategy {
            force("org.apache.commons:commons-compress:1.26.0")
            force("commons-codec:commons-codec:1.16.1")
        }
    }
}