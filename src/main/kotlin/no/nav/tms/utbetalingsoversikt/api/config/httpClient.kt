package no.nav.tms.utbetalingsoversikt.api.config

import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.engine.apache.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

object HttpClientBuilder {

    fun build() = HttpClient(Apache) {
        install(ContentNegotiation) {
            json(jsonConfig())
        }
        install(HttpTimeout)
    }
}