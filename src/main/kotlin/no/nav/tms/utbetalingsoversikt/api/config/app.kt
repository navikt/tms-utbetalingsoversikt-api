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
import no.nav.tms.common.metrics.installTmsMicrometerMetrics
import no.nav.tms.common.observability.ApiMdc
import no.nav.tms.common.util.config.StringEnvVar
import no.nav.tms.common.util.config.UrlEnvVar
import no.nav.tms.token.support.idporten.sidecar.IdPortenLogin
import no.nav.tms.token.support.idporten.sidecar.LevelOfAssurance.SUBSTANTIAL
import no.nav.tms.token.support.idporten.sidecar.idPorten
import no.nav.tms.token.support.tokendings.exchange.TokendingsServiceBuilder
import no.nav.tms.token.support.tokenx.validation.LevelOfAssurance
import no.nav.tms.token.support.tokenx.validation.TokenXAuthenticator
import no.nav.tms.token.support.tokenx.validation.tokenX
import no.nav.tms.utbetalingsoversikt.api.utbetaling.*
import no.nav.tms.utbetalingsoversikt.api.ytelse.ApiException
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
                    authConfig = setupAuth(),
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
    val secureLog = KotlinLogging.logger("secureLog")

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
                    log.warn { cause.message }
                    call.respondText(text = cause.message ?: "Ukjent ytelse-id feil", status = BadRequest)
                }

                is UtbetalingNotFoundException -> {
                    log.warn { "Fant ikke utbetaling med id ${cause.ytelseId}: ${cause.details}" }
                    call.respondText(text = "Utbetaling ikke funnnet", status = NotFound)
                }

                is UtbetalingSerializationException -> {
                    log.error(cause) { cause.message }
                    call.respond(HttpStatusCode.ServiceUnavailable)
                }

                is ApiException -> {
                    log.error { cause.errorMessage }
                    call.respond(HttpStatusCode.ServiceUnavailable)
                }

                else -> {
                    secureLog.error(cause) {
                        "Uventet feil ${cause::class.simpleName}"
                    }
                    log.error { "Uventet feil. Svarer med feilkode. ${cause::class.simpleName}" }
                    call.respondText("Feil i baksystem.", status = InternalServerError)
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

    install(ApiMdc)

    routing {
        healthApi()
        authenticate {
            utbetalingRoutes(sokosUtbetalingConsumer)
        }
        authenticate(TokenXAuthenticator.name) {
            utbetalingRoutesTokenX(sokosUtbetalingConsumer)
        }
    }

    configureShutdownHook(httpClient)
}

private fun setupAuth(): Application.() -> Unit = {
    authentication {
        idPorten {
            setAsDefault = true
            levelOfAssurance = SUBSTANTIAL
        }
        tokenX {
            setAsDefault = false
            levelOfAssurance = LevelOfAssurance.SUBSTANTIAL
        }
    }
    install(IdPortenLogin)
}

private fun Application.configureShutdownHook(httpClient: HttpClient) {
    environment.monitor.subscribe(ApplicationStopping) {
        httpClient.close()
    }
}
