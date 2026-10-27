import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    kotlin("jvm").version(Kotlin.version)

    kotlin("plugin.serialization").version(Kotlin.version)

    id(TmsJarBundling.plugin)

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
    implementation(managed.kotlinx) //TODO .coroutines
    implementation(managed.kotlin.logging) //TODO: kotlinlogging i ett ord
    implementation(managed.ktor.client.apache5)
    implementation(managed.ktor.client.content) //TODO: contentnegotiation
    implementation(managed.ktor.serialization.kotlinx)
    implementation(managed.ktor.server.content) //TODO: contentnegotiation
    implementation(managed.ktor.server.auth)
    implementation(managed.ktor.server.cors)
    implementation(managed.ktor.server.header) //TODO: defaultheader
    implementation(managed.ktor.server.netty)
    implementation(managed.ktor.server.status) //TODO: statuspages
    implementation(managed.logback.encoder)
    implementation(managed.tms.common.lib.utils) //todo: commonlib i ett ord
    implementation(managed.tms.common.lib.metrics)
    implementation(managed.tms.common.lib.teamLogger)
    implementation(managed.tms.common.lib.observability)
    implementation(managed.tms.ktor.support.entraId.login) //TODO: tms-ktor-support-entraId-login = {group = "no.nav.tms.token.support", name = "user-login-routes", version.ref = "tmsKtorSupport" } ---> user login routes
    implementation(managed.tms.ktor.support.exchange) //TODO: userTokenExchane
    implementation(managed.tms.ktor.support.verification) //TODO: userTokenVerification

    testImplementation(managed.bundles.junit)
    testImplementation(managed.kotest.runner) //TODO: runnnerJunit5
    testImplementation(managed.kotest.assertion)
    testImplementation(managed.ktor.test.server)
    testImplementation(managed.tms.ktor.support.verification.mock)
    testImplementation(managed.bundles.jackson)
    testImplementation(managed.mockk.default)
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
