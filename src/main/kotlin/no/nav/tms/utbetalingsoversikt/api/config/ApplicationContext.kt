package no.nav.tms.utbetalingsoversikt.api.config

import io.ktor.client.features.json.serializer.*
import no.nav.tms.token.support.azure.exchange.AzureServiceBuilder
import no.nav.tms.utbetalingsoversikt.api.health.HealthService
import no.nav.tms.utbetalingsoversikt.api.utbetaling.UtbetalingService
import no.nav.tms.utbetalingsoversikt.api.ytelse.HovedytelseService
import no.nav.tms.utbetalingsoversikt.api.ytelse.SokosUtbetalingConsumer

class ApplicationContext {

    val environment = Environment()

    val httpClient = HttpClientBuilder.build()
    val healthService = HealthService(this)

    private val azureService = AzureServiceBuilder.buildAzureService()
    private val azureTokenFetcher = AzureTokenFetcher(azureService, environment.sokosUtebatlingAzureClientId)

    private val sokosUtbetalingConsumer = SokosUtbetalingConsumer(httpClient, azureTokenFetcher, environment.sokosUtbetalingUrl)
    private val hovedytelseService = HovedytelseService(sokosUtbetalingConsumer)
    val utbetalingService = UtbetalingService(hovedytelseService)
}
