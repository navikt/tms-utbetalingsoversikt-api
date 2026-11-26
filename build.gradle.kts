import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    alias(managed.plugins.kotlin)
    alias(managed.plugins.serialization)
    alias(managed.plugins.tms.jarBundling)

    application
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    }
    mavenLocal()
}

dependencies {
    implementation(managed.kotlinx.coroutines)
    implementation(managed.kotlinLogging)
    implementation(managed.bundles.ktorClient)
    implementation(managed.bundles.ktorServer)
    implementation(managed.ktor.server.contentNegotiation)
    implementation(managed.ktor.serialization.kotlinx)
    implementation(managed.ktor.server.auth)
    implementation(managed.ktor.server.cors)
    implementation(managed.ktor.server.defaultHeaders)
    implementation(managed.ktor.server.netty)
    implementation(managed.ktor.server.statusPages)
    runtimeOnly(managed.logback.encoder)
    implementation(managed.tms.commonLib.utils)
    implementation(managed.tms.commonLib.metrics)
    implementation(managed.tms.commonLib.teamLogger)
    implementation(managed.tms.commonLib.observability)
    implementation(managed.tms.ktorTokenSupport.userToken.loginRoutes)
    implementation(managed.tms.ktorTokenSupport.userToken.exchange)
    implementation(managed.tms.ktorTokenSupport.userToken.verification)

    testImplementation(managed.bundles.junit)
    testImplementation(managed.kotest.assertion)
    testImplementation(managed.ktor.test.server)
    testImplementation(managed.tms.ktorTokenSupport.userToken.verificationMock)
    testImplementation(managed.bundles.jackson)
    testImplementation(managed.mockk)
}

application {
    mainClass.set("no.nav.tms.utbetalingsoversikt.api.AppKt")
}

tasks {
    withType<Test> {
        useJUnitPlatform()
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            events("passed", "skipped", "failed")
        }
    }
}
