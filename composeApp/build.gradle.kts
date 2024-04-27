import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import com.android.build.api.dsl.ManagedVirtualDevice
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree
import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import com.android.build.gradle.internal.tasks.factory.dependsOn

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.application)
    alias(libs.plugins.buildConfig)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        instrumentedTestVariant {
            sourceSetTree.set(KotlinSourceSetTree.test)
            dependencies {
                debugImplementation(libs.androidx.testManifest)
                implementation(libs.androidx.junit4)
            }
        }
    }

    jvm()
//
//    wasmJs {
//        browser()
//        binaries.executable()
//    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        all {
            languageSettings {
                optIn("org.jetbrains.compose.resources.ExperimentalResourceApi")
            }
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.bottom.sheet.navigator)
            implementation(libs.napier)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kstore)
            implementation(libs.kstore.file)
            implementation(libs.ktor.core)
            implementation(libs.multiplatform.settings.no.arg)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
            implementation(libs.kotlinx.coroutines.test)
        }

        androidMain.dependencies {
            implementation(compose.uiTooling)
            implementation(libs.androidx.activityCompose)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.androidx.ui.viewbinding)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.appdirs)
        }

        iosMain.dependencies {
            implementation(libs.ktor.core)
            implementation(libs.ktor.client.darwin)
        }

    }
}

val natives = configurations.create("natives")

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.gdx.controllers.android)
    implementation(libs.gdx)
    implementation(libs.gdx.bullet)
    implementation(libs.gdx.backend.android)
    implementation(libs.ktx.app)
    implementation(libs.ktx.async)
    val gdxVersion = libs.versions.gdx.get()
    natives("com.badlogicgames.gdx:gdx-bullet-platform:$gdxVersion:natives-arm64-v8a")
    natives("com.badlogicgames.gdx:gdx-bullet-platform:$gdxVersion:natives-armeabi-v7a")
    natives("com.badlogicgames.gdx:gdx-bullet-platform:$gdxVersion:natives-x86")
    natives("com.badlogicgames.gdx:gdx-bullet-platform:$gdxVersion:natives-x86_64")
    natives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-armeabi-v7a")
    natives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-arm64-v8a")
    natives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-x86")
    natives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-x86_64")
}
version = "1.3"
android {
    namespace = "ua.vald_zx.game.rat.race.card"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
        targetSdk = 34

        applicationId = "ua.vald_zx.game.rat.race.card.androidApp"
        versionCode = version.toString().let {
            val split = it.split(".")
            split[0].toInt() * 100 + split[1].toInt()
        }
        versionName = version.toString()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildOutputs.all {
            val variantOutputImpl = this as BaseVariantOutputImpl
            variantOutputImpl.outputFileName = "RatRaceCard.apk"
        }
    }
    sourceSets["main"].apply {
        manifest.srcFile("src/androidMain/AndroidManifest.xml")
        res.srcDirs("src/androidMain/res")
    }
    //https://developer.android.com/studio/test/gradle-managed-devices
    @Suppress("UnstableApiUsage")
    testOptions {
        managedDevices.devices {
            maybeCreate<ManagedVirtualDevice>("pixel5").apply {
                device = "Pixel 5"
                apiLevel = 34
                systemImageSource = "aosp"
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        viewBinding = true
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
            isMinifyEnabled = false
            isShrinkResources = false
        }
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.txt")
        }
    }
    sourceSets {
        named("main") {
            jniLibs.srcDir("libs")
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ua.vald_zx.game.rat.race.card.desktopApp"
            packageVersion = "1.0.0"
        }
    }
}

buildConfig {
    // BuildConfig configuration here.
    // https://github.com/gmazzo/gradle-buildconfig-plugin#usage-in-kts
}

tasks.register("copyAndroidNatives") {
    doFirst {
        file("libs/armeabi-v7a/").mkdirs()
        file("libs/arm64-v8a/").mkdirs()
        file("libs/x86_64/").mkdirs()
        file("libs/x86/").mkdirs()

        natives.files.forEach { jar ->
            val outputDir: File? =
                when {
                    jar.name.endsWith("natives-arm64-v8a.jar") -> file("libs/arm64-v8a")
                    jar.name.endsWith("natives-armeabi-v7a.jar") -> file("libs/armeabi-v7a")
                    jar.name.endsWith("natives-x86_64.jar") -> file("libs/x86_64")
                    jar.name.endsWith("natives-x86.jar") -> file("libs/x86")
                    else -> null
                }

            if (outputDir != null) {
                copy {
                    from(zipTree(jar))
                    into(outputDir)
                    include("*.so")
                }
            }
        }
    }
}

project.tasks.preBuild.dependsOn("copyAndroidNatives")