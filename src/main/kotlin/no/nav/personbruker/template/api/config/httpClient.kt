package no.nav.personbruker.template.api.config

import io.ktor.client.HttpClient
import io.ktor.client.features.timeout
import io.ktor.client.request.*
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.personbruker.template.api.common.AuthenticatedUser
import java.net.URL

suspend inline fun <reified T> HttpClient.get(url: URL, user: AuthenticatedUser): T = withContext(Dispatchers.IO) {
    request {
        url(url)
        method = HttpMethod.Get
        header(HttpHeaders.Authorization, user.createAuthenticationHeader())
    }
}

suspend inline fun <reified T> HttpClient.getExtendedTimeout(url: URL, user: AuthenticatedUser): T = withContext(Dispatchers.IO) {
    request {
        url(url)
        method = HttpMethod.Get
        header(HttpHeaders.Authorization, user.createAuthenticationHeader())
        timeout {
            socketTimeoutMillis = 30000
            connectTimeoutMillis = 10000
            requestTimeoutMillis = 40000
        }
    }
}

suspend inline fun <reified T> HttpClient.getWithEssoTokenHeader(url: URL, user: AuthenticatedUser): T = withContext(Dispatchers.IO) {
    require(user.auxiliaryEssoToken != null) {
        "Prøvde å sette esso-token som header, men fant det ikke for innlogget bruker."
    }
    request {
        url(url)
        method = HttpMethod.Get
        header(HttpHeaders.Authorization, user.createAuthenticationHeader())
        header("nav-esso", user.auxiliaryEssoToken)
    }
}