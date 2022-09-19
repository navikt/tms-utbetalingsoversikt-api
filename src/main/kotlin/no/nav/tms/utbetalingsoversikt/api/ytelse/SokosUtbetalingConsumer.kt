package no.nav.tms.utbetalingsoversikt.api.ytelse

import io.ktor.client.*
import no.nav.tms.token.support.idporten.sidecar.user.IdportenUser
import no.nav.tms.utbetalingsoversikt.api.config.TokendingsTokenFetcher
import no.nav.tms.utbetalingsoversikt.api.config.post
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.*
import java.net.URL
import java.time.LocalDate

class SokosUtbetalingConsumer(
    private val client: HttpClient,
    private val tokendingsTokenFetcher: TokendingsTokenFetcher,
    baseUrl: URL,
) {
    private val utbetalingsinformasjonInternUrl = URL("$baseUrl/hent-utbetalingsinformasjon/ekstern")

    suspend fun fetchUtbetalingsInfo(user: IdportenUser, fom: LocalDate, tom: LocalDate, rolle: RolleEkstern): List<UtbetalingEkstern> {
        val targetToken = tokendingsTokenFetcher.getSokosUtbetaldataToken(user.tokenString)

        val requestBody = createRequest(user.ident, fom, tom, rolle)

        return client.post(utbetalingsinformasjonInternUrl, requestBody, targetToken)
    }

    private fun createRequest(fnr: String, fom: LocalDate, tom: LocalDate, rolle: RolleEkstern): Utbetalingsoppslag {
        val periode = PeriodeEkstern(
            fom = fom.toString(),
            tom = tom.plusDays(1).toString() // Compensate for external service being end-exclusive.
        )

        return Utbetalingsoppslag(
            ident = fnr,
            rolle = rolle,
            periode = periode,
            periodetype = PeriodetypeEkstern.UTBETALINGSPERIODE
        )
    }
}
