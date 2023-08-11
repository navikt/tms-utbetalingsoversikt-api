package no.nav.tms.utbetalingsoversikt.api.config

import io.ktor.client.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.routing.*
import nav.no.tms.common.metrics.installTmsMicrometerMetrics
import no.nav.tms.token.support.idporten.sidecar.LevelOfAssurance
import no.nav.tms.token.support.idporten.sidecar.LevelOfAssurance.SUBSTANTIAL
import no.nav.tms.token.support.idporten.sidecar.LoginLevel.LEVEL_3
import no.nav.tms.token.support.idporten.sidecar.installIdPortenAuth
import no.nav.tms.token.support.tokendings.exchange.TokendingsServiceBuilder
import no.nav.tms.utbetalingsoversikt.api.utbetaling.UtbetalingService
import no.nav.tms.utbetalingsoversikt.api.utbetaling.utbetalingApi
import no.nav.tms.utbetalingsoversikt.api.ytelse.HovedytelseService
import no.nav.tms.utbetalingsoversikt.api.ytelse.SokosUtbetalingConsumer

fun main() {
    val environment = Environment()

    val httpClient = HttpClientBuilder.build()

    val utbetalingService = setupUtbetalingService(httpClient, environment)

    embeddedServer(Netty, port = environment.port) {
        utbetalingApi(
            httpClient = httpClient,
            environment = environment,
            utbetalingService = utbetalingService,
            authConfig = idPortenAuth(environment.rootPath)
        )
    }.start(wait = true)
}

fun Application.utbetalingApi(
    httpClient: HttpClient,
    environment: Environment,
    utbetalingService: UtbetalingService,
    authConfig: Application.() -> Unit
) {
    install(DefaultHeaders)

    install(CORS) {
        allowHost(environment.corsAllowedOrigins, environment.corsAllowedSchemes)
        allowCredentials = true
        allowHeader(HttpHeaders.ContentType)
    }

    authConfig()

    install(ContentNegotiation) {
        json(jsonConfig())
    }

    installTmsMicrometerMetrics {
        setupMetricsRoute = true
        installMicrometerPlugin = true
    }

    routing {
        route(environment.rootPath) {
            healthApi()
            authenticate {
                utbetalingApi(utbetalingService)
            }
        }
    }

    configureShutdownHook(httpClient)
}

private fun setupUtbetalingService(httpClient: HttpClient, environment: Environment): UtbetalingService {

    val tokendingsService = TokendingsServiceBuilder.buildTokendingsService()
    val tokenFetcher = TokendingsTokenFetcher(tokendingsService, environment.sokosUtbetalingClientId)

    val sokosUtbetalingConsumer = SokosUtbetalingConsumer(httpClient, tokenFetcher, environment.sokosUtbetalingUrl)
    val hovedytelseService = HovedytelseService(sokosUtbetalingConsumer)
    return UtbetalingService(hovedytelseService)
}

private fun idPortenAuth(workingRootPath: String): Application.() -> Unit = {
    installIdPortenAuth {
        setAsDefault = true
        levelOfAssurance = SUBSTANTIAL
        inheritProjectRootPath = false
        rootPath = workingRootPath
    }
}

private fun Application.configureShutdownHook(httpClient: HttpClient) {
    environment.monitor.subscribe(ApplicationStopping) {
        httpClient.close()
    }
}
