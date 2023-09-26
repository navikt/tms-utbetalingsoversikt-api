package no.nav.tms.utbetalingsoversikt.api.config

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import nav.no.tms.common.metrics.installTmsMicrometerMetrics
import no.nav.personbruker.dittnav.common.util.config.StringEnvVar
import no.nav.personbruker.dittnav.common.util.config.UrlEnvVar
import no.nav.tms.token.support.idporten.sidecar.LevelOfAssurance.SUBSTANTIAL
import no.nav.tms.token.support.idporten.sidecar.installIdPortenAuth
import no.nav.tms.token.support.idporten.sidecar.user.IdportenUser
import no.nav.tms.token.support.idporten.sidecar.user.IdportenUserFactory
import no.nav.tms.token.support.tokendings.exchange.TokendingsServiceBuilder
import no.nav.tms.utbetalingsoversikt.api.utbetaling.UtbetalingService
import no.nav.tms.utbetalingsoversikt.api.utbetaling.IllegalYtelseIdException
import no.nav.tms.utbetalingsoversikt.api.utbetaling.UtbetalingNotFoundException
import no.nav.tms.utbetalingsoversikt.api.utbetaling.utbetalingApi
import no.nav.tms.utbetalingsoversikt.api.v2.utbetalingRoutesV2
import no.nav.tms.utbetalingsoversikt.api.ytelse.HovedytelseService
import no.nav.tms.utbetalingsoversikt.api.ytelse.SokosUtbetalingConsumer

fun main() {
    val httpClient = HttpClientBuilder.build()
    val tokendingsService = TokendingsServiceBuilder.buildTokendingsService()
    val sokosUtbetalingConsumer = SokosUtbetalingConsumer(
        client = httpClient,
        sokosUtbetaldataClientId = StringEnvVar.getEnvVar("SOKOS_UTBETALING_TOKENX_CLIENT_ID"),
        tokendingsService = tokendingsService,
        baseUrl = UrlEnvVar.getEnvVarAsURL("SOKOS_UTBETALDATA_URL"),

        )

    embeddedServer(
        factory = Netty,
        environment = applicationEngineEnvironment {
            rootPath = "tms-utbetalingsoversikt-api"

            module {
                utbetalingApi(
                    httpClient = httpClient,
                    sokosUtbetalingConsumer = sokosUtbetalingConsumer,
                    authConfig = idPortenAuth(),
                    corsAllowedOrigins = StringEnvVar.getEnvVar("CORS_ALLOWED_ORIGINS"),
                    corsAllowedSchemes = StringEnvVar.getEnvVarAsList("CORS_ALLOWED_SCHEMES"),

                    )
            }
            connector {
                port = 8080
            }
        }
    ).start(wait = true)
}

fun Application.utbetalingApi(
    httpClient: HttpClient,
    sokosUtbetalingConsumer: SokosUtbetalingConsumer,
    authConfig: Application.() -> Unit,
    corsAllowedOrigins: String,
    corsAllowedSchemes: List<String>
) {
    val log = KotlinLogging.logger { }
    install(DefaultHeaders)

    install(CORS) {
        allowHost(corsAllowedOrigins, corsAllowedSchemes)
        allowCredentials = true
        allowHeader(HttpHeaders.ContentType)
    }
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            when (cause) {
                is IllegalYtelseIdException -> {
                    call.respondText(text = cause.message ?: "Ukjent ytelse-input feil", status = BadRequest)
                }

                is UtbetalingNotFoundException -> {
                    log.warn { "Fant ikke utbetaling med id ${cause.ytelseId}: ${cause.details}" }
                    call.respondText(text = "Utbetaling ikke funnnet", status = NotFound)
                }

                else -> {
                    log.error { "${cause.message}" }
                    call.respondText(text = "500: $cause", status = InternalServerError)
                }
            }

        }
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
        healthApi()
        authenticate {
            utbetalingApi(UtbetalingService(HovedytelseService(sokosUtbetalingConsumer)))
            utbetalingRoutesV2(sokosUtbetalingConsumer)
        }
    }

    configureShutdownHook(httpClient)
}

private fun idPortenAuth(): Application.() -> Unit = {
    installIdPortenAuth {
        setAsDefault = true
        levelOfAssurance = SUBSTANTIAL
    }
}

private fun Application.configureShutdownHook(httpClient: HttpClient) {
    environment.monitor.subscribe(ApplicationStopping) {
        httpClient.close()
    }
}

val PipelineContext<Unit, ApplicationCall>.authenticatedUser: IdportenUser
    get() = IdportenUserFactory.createIdportenUser(call)