val invoker by configurations.creating

plugins {
    kotlin("jvm")
    id("com.gradleup.shadow") version "8.3.4"
    application
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.logging)
    implementation(libs.functions.framework.api)
    implementation(libs.google.cloud.firestore)
    invoker(libs.java.function.invoker)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.truth)
    testImplementation(libs.guava.testlib)
}

application {
    mainClass = "ua.vald_zx.game.rat.race.server.AppKt"
}

task<JavaExec>("runFunction") {
    mainClass = "com.google.cloud.functions.invoker.runner.Invoker"
    classpath(invoker)
    inputs.files(configurations.runtimeClasspath, sourceSets["main"].output)
    args(
        "--target",
        project.findProperty("runFunction.target") ?: "ua.vald_zx.game.rat.race.server.App",
        "--port",
        project.findProperty("runFunction.port") ?: 8080
    )
    doFirst {
        args(
            "--classpath",
            files(configurations.runtimeClasspath, sourceSets["main"].output).asPath
        )
    }
}

tasks.named("build") {
    dependsOn(":server:shadowJar")
}

tasks.register<Copy>("buildFunction") {
    dependsOn("build")
    from("build/libs/" + project.name + "-all.jar")
    into("build/deploy")
}
