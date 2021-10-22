package no.nav.tms.utbetalingsoversikt.api.config

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.util.*
import io.prometheus.client.hotspot.DefaultExports
import no.nav.tms.utbetalingsoversikt.api.health.healthApi
import no.nav.tms.token.support.idporten.installIdPortenAuth
import no.nav.tms.utbetalingsoversikt.api.utbetaling.utbetalingApi
import no.nav.tms.utbetalingsoversikt.api.utbetaling.utbetalingApiDebug

@KtorExperimentalAPI
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
        tokenCookieName = "tms-utbetalingsoversikt"
        postLogoutRedirectUri = environment.postLogoutUrl
        secureCookie = true
        setAsDefault = true
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
        utbetalingApiDebug(appContext.utbetalingService)
    }

    configureShutdownHook(appContext.httpClient)
}

private fun Application.configureShutdownHook(httpClient: HttpClient) {
    environment.monitor.subscribe(ApplicationStopping) {
        httpClient.close()
    }
}
