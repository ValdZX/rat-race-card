import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinx.rpc)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    wasmJs {
        browser {
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        add(projectDirPath)
                    }
                }
            }
        }
    }

    androidLibrary {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        namespace = "ua.vald_zx.game.rat.race.card.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    jvm()

    sourceSets {
        commonMain {
            dependencies {
                api(libs.kotlinx.coroutines.core)
                implementation(libs.kotlin.stdlib)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.rpc.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.mongodb.bson)
            }
        }
    }
}