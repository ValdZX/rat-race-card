plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinx.rpc)
    alias(libs.plugins.ktor)
    application
}

group = "ua.vald_zx.game.rat.race.server"
version = "0.2.11"
application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

dependencies {
    implementation(projects.shared)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.logback)
    implementation(libs.kotlinx.datetime)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.cors.jvm)
    implementation(libs.ktor.server.websockets.jvm)
    implementation(libs.ktor.server.host.common.jvm)
    implementation(libs.kotlinx.rpc.krpc.server)
    implementation(libs.kotlinx.rpc.krpc.serialization.json)
    implementation(libs.kotlinx.rpc.krpc.ktor.server)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlinx.rpc.krpc.client)
    testImplementation(libs.kotlinx.rpc.krpc.ktor.client)
    testImplementation(libs.kotlin.test.junit)
    implementation(libs.google.cloud.firestore)
    implementation(libs.google.cloud.storage)
    implementation(platform(libs.mongodb.bom))
    implementation(libs.mongodb.coroutine)
    implementation(libs.mongodb.bson.kotlinx)
}

ktor {
    docker {
        jreVersion.set(JavaVersion.VERSION_21)
        localImageName.set("kharvin/race-rat")
        imageTag.set(version.toString())
        portMappings.set(listOf(
            io.ktor.plugin.features.DockerPortMapping(
                80,
                8080,
                io.ktor.plugin.features.DockerPortMappingProtocol.TCP
            )
        ))
    }
}

jib {
    dockerClient {
        executable = "/usr/local/bin/docker"
    }
}