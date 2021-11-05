package no.nav.tms.utbetalingsoversikt.api.config

import no.nav.tms.token.support.tokendings.exchange.TokendingsServiceBuilder
import no.nav.tms.utbetalingsoversikt.api.health.HealthService
import no.nav.tms.utbetalingsoversikt.api.utbetaling.UtbetalingService
import no.nav.tms.utbetalingsoversikt.api.ytelse.HovedytelseService
import no.nav.tms.utbetalingsoversikt.api.ytelse.SokosUtbetalingConsumer

class ApplicationContext {

    val environment = Environment()

    val httpClient = HttpClientBuilder.build()
    val healthService = HealthService(this)

    private val tokendingsService = TokendingsServiceBuilder.buildTokendingsService()
    private val tokendingsTokenFetcher = TokendingsTokenFetcher(tokendingsService, environment.sokosUtebatlingTokenxClientId)

    private val sokosUtbetalingConsumer = SokosUtbetalingConsumer(httpClient, tokendingsTokenFetcher, environment.sokosUtbetalingUrl)
    private val hovedytelseService = HovedytelseService(sokosUtbetalingConsumer)
    val utbetalingService = UtbetalingService(hovedytelseService)
}
