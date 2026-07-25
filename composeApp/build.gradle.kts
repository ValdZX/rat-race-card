import com.android.build.gradle.internal.tasks.factory.dependsOn
import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.hot.reload)
    alias(libs.plugins.android.application)
    alias(libs.plugins.buildConfig)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvm()
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core"))
            implementation(project(":card"))
            implementation(project(":board"))
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.androidx.ui.android)
}
version = "3.4"
android {
    namespace = "ua.vald_zx.game.rat.race.card"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()

        applicationId = "ua.vald_zx.game.rat.race.card.androidApp"
        versionCode = version.toString().let {
            val split = it.split(".")
            split[0].toInt() * 100 + split[1].toInt()
        }
        versionName = version.toString()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildOutputs.all {
            val variantOutputImpl =
                this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            variantOutputImpl.outputFileName = "RatRaceCard.apk"
        }
    }
    sourceSets["main"].apply {
        manifest.srcFile("src/androidMain/AndroidManifest.xml")
        res.srcDirs("src/androidMain/res")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    signingConfigs {
        create("release") {
            keyAlias = "ratracecard"
            keyPassword = "pqxp3bSpFvj48rzSdjcNV5jFshdkKDdD"
            storeFile = File(projectDir, "app.keystore")
            storePassword = "fe7mV2B7su4ZPcVqBdxw34KDjJUWuvmu"
        }
    }
    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            signingConfig = signingConfigs.getByName("debug")
            manifestPlaceholders["appIcon"] = "@mipmap/ic_launcher_debug"
            isMinifyEnabled = false
            isShrinkResources = false
        }
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            manifestPlaceholders["appIcon"] = "@mipmap/ic_launcher"
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ua.vald_zx.game.rat.race.card"
            packageVersion = "1.0.0"
        }
    }
}

buildConfig {
    // https://github.com/gmazzo/gradle-buildconfig-plugin#usage-in-kts
    packageName("ua.vald_zx.game.rat.race.card")
    val cardOnly = (project.findProperty("cardOnly") as String?)?.toBoolean() ?: false
    sourceSets.maybeCreate("commonMain").apply {
        className("BuildConfig")
        buildConfigField("Boolean", "CARD_ONLY_MODE", cardOnly.toString())
    }
}

kotlin.sourceSets.commonMain {
    kotlin.srcDir(tasks.named("generateCommonMainBuildConfigClasses"))
}

tasks.register("buildDist") {
    doFirst {
        val file = File("docs")
        file.deleteRecursively()
        File("composeApp/build/dist/wasmJs/productionExecutable").copyRecursively(file)
    }
}.dependsOn("wasmJsBrowserDistribution")