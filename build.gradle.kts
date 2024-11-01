import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    kotlin("jvm").version(Kotlin.version)

    kotlin("plugin.serialization").version(Kotlin.version)

    id(Shadow.pluginId) version (Shadow.version)

    application
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
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
    implementation(Kotlinx.coroutines)
    implementation(KotlinLogging.logging)
    implementation(Ktor.Client.apache)
    implementation(Ktor.Client.contentNegotiation)
    implementation(Ktor.Serialization.kotlinX)
    implementation(Ktor.Server.auth)
    implementation(Ktor.Server.contentNegotiation)
    implementation(Ktor.Server.cors)
    implementation(Ktor.Server.defaultHeaders)
    implementation(Ktor.Server.netty)
    implementation(Ktor.Server.statusPages)
    implementation(TmsCommonLib.utils)
    implementation(TmsCommonLib.metrics)
    implementation(TmsCommonLib.observability)
    implementation(TmsKtorTokenSupport.tokendingsExchange)
    implementation(TmsKtorTokenSupport.tokenXValidation)
    implementation(TmsKtorTokenSupport.idportenSidecar)

    testImplementation(Junit.api)
    testImplementation(Junit.engine)
    testImplementation(Kotest.runnerJunit5)
    testImplementation(Kotest.assertionsCore)
    testImplementation(Ktor.Test.serverTestHost)
    testImplementation(TmsKtorTokenSupport.idportenSidecarMock)
    testImplementation(TmsKtorTokenSupport.tokenXValidationMock)
    testImplementation(JacksonDatatype.datatypeJsr310)
    testImplementation(JacksonDatatype.moduleKotlin)
    testImplementation(Mockk.mockk)
}

application {
    mainClass.set("no.nav.tms.utbetalingsoversikt.api.config.AppKt")
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
