package no.nav.tms.utbetalingsoversikt.api.config

import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

suspend inline fun <reified T> HttpClient.post(url: URL, requestBody: Any, token: String): T = withContext(Dispatchers.IO) {
    request {
        url(url)
        method = HttpMethod.Post
        body = requestBody
        header(HttpHeaders.Authorization, "Bearer $token")
    }
}
