package no.nav.tms.utbetalingsoversikt.api.config

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.prometheus.client.hotspot.DefaultExports
import no.nav.tms.token.support.idporten.sidecar.LoginLevel.LEVEL_3
import no.nav.tms.token.support.idporten.sidecar.installIdPortenAuth
import no.nav.tms.utbetalingsoversikt.api.health.healthApi
import no.nav.tms.utbetalingsoversikt.api.utbetaling.utbetalingApi

fun Application.mainModule(appContext: ApplicationContext = ApplicationContext()) {
    val environment = Environment()

    DefaultExports.initialize()

    install(DefaultHeaders)

    install(CORS) {
        host(environment.corsAllowedOrigins, environment.corsAllowedSchemes)
        allowCredentials = true
        header(HttpHeaders.ContentType)
    }

    installIdPortenAuth {
        setAsDefault = true
        loginLevel = LEVEL_3
    }

    install(ContentNegotiation) {
        json(jsonConfig())
    }

    install(CallLogging)

    routing {
        healthApi(appContext.healthService)
        authenticate {
            utbetalingApi(appContext.utbetalingService)
        }
    }

    configureShutdownHook(appContext.httpClient)
}

private fun Application.configureShutdownHook(httpClient: HttpClient) {
    environment.monitor.subscribe(ApplicationStopping) {
        httpClient.close()
    }
}
