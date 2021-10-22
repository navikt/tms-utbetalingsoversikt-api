package no.nav.tms.utbetalingsoversikt.api.ytelse

import io.ktor.client.*
import no.nav.tms.utbetalingsoversikt.api.config.AzureTokenFetcher
import no.nav.tms.utbetalingsoversikt.api.config.post
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.*
import java.net.URL
import java.time.LocalDate

class SokosUtbetalingConsumer(
    private val client: HttpClient,
    private val azureTokenFetcher: AzureTokenFetcher,
    baseUrl: URL,
) {
    private val utbetalingsinformasjonInternUrl = URL("$baseUrl/utbetaldata/api/v1/hent-utbetalingsinformasjon/intern")

    suspend fun fetchUtbetalingsInfo(ident: String, fom: LocalDate, tom: LocalDate): List<UtbetalingEkstern> {
        val azureToken = azureTokenFetcher.getSokosUtbetaldataToken()

        val requestBody = createRequest(ident, fom, tom)

        return client.post(utbetalingsinformasjonInternUrl, requestBody, azureToken)
    }

    private fun createRequest(fnr: String, fom: LocalDate, tom: LocalDate): Utbetalingsoppslag {
        val periode = PeriodeEkstern(
            fom = fom.toString(),
            tom = tom.plusDays(1).toString() // Compensate for external service being end-exclusive.
        )

        return Utbetalingsoppslag(
            ident = fnr,
            rolle = RolleEkstern.RETTIGHETSHAVER,
            periode = periode,
            periodetype = PeriodetypeEkstern.UTBETALINGSPERIODE
        )
    }
}
