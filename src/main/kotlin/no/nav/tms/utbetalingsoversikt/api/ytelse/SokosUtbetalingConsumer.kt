package no.nav.tms.utbetalingsoversikt.api.ytelse

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.tms.token.support.idporten.sidecar.user.IdportenUser
import no.nav.tms.token.support.tokendings.exchange.TokendingsService
import no.nav.tms.utbetalingsoversikt.api.ytelse.domain.external.*
import java.net.URL
import java.time.LocalDate

class SokosUtbetalingConsumer(
    private val client: HttpClient,
    baseUrl: URL,
    private val sokosUtbetaldataClientId: String,
    private val tokendingsService: TokendingsService,
) {
    private val utbetalingsinformasjonInternUrl = URL("$baseUrl/hent-utbetalingsinformasjon/ekstern")

    suspend fun fetchUtbetalingsInfo(user: IdportenUser, fom: LocalDate, tom: LocalDate): List<UtbetalingEkstern> {
        val targetToken = tokendingsService.exchangeToken(user.tokenString, sokosUtbetaldataClientId)
        val requestBody = createRequest(user.ident, fom, tom, RolleEkstern.UTBETALT_TIL)
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

suspend inline fun <reified T> HttpClient.post(url: URL, requestBody: Any, token: String): T = withContext(Dispatchers.IO) {
    request {
        url("$url")
        method = HttpMethod.Post
        header(HttpHeaders.Authorization, "Bearer $token")
        contentType(ContentType.Application.Json)
        setBody(requestBody)
    }.let { response ->
        if ( response.status != HttpStatusCode.OK ){
            throw ApiException(response.status, url)
        } else response
    }.body()
}

class ApiException (private val statusCode: HttpStatusCode, private  val url: URL) : Exception () {
    val errorMessage = "Kall mot ${url.host} feiler med statuskode $statusCode"
}