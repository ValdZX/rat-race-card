import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    android {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        namespace = "ua.vald_zx.game.rat.race.card.core"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        androidResources {
            enable = true
        }
    }

    jvm()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(project(":shared"))
            api(compose.runtime)
            api(compose.foundation)
            api(compose.material)
            api(compose.material3)
            api(compose.preview)
            api(compose.components.resources)
            api(compose.materialIconsExtended)
            api(libs.voyager.navigator)
            api(libs.voyager.bottom.sheet.navigator)
            api(libs.napier)
            api(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.serialization.json)
            api(libs.kotlinx.datetime)
            api(libs.kstore)
            api(libs.ktor.client.core)
            api(libs.ktor.client.websockets)
            api(libs.kotlinx.rpc.krpc.client)
            api(libs.kotlinx.rpc.krpc.serialization.json)
            api(libs.kotlinx.rpc.krpc.ktor.client)
            api(libs.lexilabs.basic.sound)
            api(libs.charts)
            api(libs.tts)
            api(libs.tts.compose)
            api(libs.compottie)
            api(libs.compottie.resources)
            api(libs.constraintlayout)
            api(libs.composables.core)
            api(project.dependencies.platform(libs.koin.bom))
            api(libs.androidx.lifecycle.viewmodelCompose)
            api(libs.androidx.lifecycle.runtimeCompose)
            api(libs.koin.core)
            api(libs.koin.compose)
            api(libs.koin.compose.viewmodel)
            api(libs.koin.compose.viewmodel.navigation)
            api(libs.localina)
            api(libs.materialKolor)
            api(libs.jsontree)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
        }

        androidMain.dependencies {
            api(compose.uiTooling)
            api(libs.androidx.activityCompose)
            api(libs.kotlinx.coroutines.android)
            api(libs.ktor.client.cio)
            api(libs.kstore.file)
            api(libs.app.update.ktx)
            api(libs.fragment)
            api(libs.lexilabs.basic.haptic)
        }

        jvmMain.dependencies {
            api(compose.desktop.currentOs)
            api(libs.kotlinx.coroutines.swing)
            api(libs.ktor.client.cio)
            api(libs.appdirs)
            api(libs.kstore.file)
        }

        iosMain.dependencies {
            api(libs.ktor.client.darwin)
            api(libs.kstore.file)
            api(libs.lexilabs.basic.haptic)
        }

        wasmJsMain.dependencies {
            api(libs.ktor.client.js)
            api(libs.kstore.storage)
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "ua.vald_zx.game.rat.race.card.resources"
}
